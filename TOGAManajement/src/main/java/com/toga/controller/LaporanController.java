package com.toga.controller;

import com.toga.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;

public class LaporanController {

    @FXML private DatePicker dpDari;
    @FXML private DatePicker dpSampai;
    @FXML private Label lblTotalPanen;
    @FXML private Label lblTotalPerawatan;
    @FXML private Label lblPenggunaAktif;

    @FXML private TableView<PanenRow> tblPanen;
    @FXML private TableColumn<PanenRow, String> colTanaman;
    @FXML private TableColumn<PanenRow, String> colPengguna;
    @FXML private TableColumn<PanenRow, String> colTanggal;
    @FXML private TableColumn<PanenRow, String> colHasil;

    @FXML private TableView<PerawatanRow> tblPerawatan;
    @FXML private TableColumn<PerawatanRow, String> colTanamanP;
    @FXML private TableColumn<PerawatanRow, String> colJenis;
    @FXML private TableColumn<PerawatanRow, String> colTanggalP;
    @FXML private TableColumn<PerawatanRow, String> colStatusP;

    private ObservableList<PanenRow> dataPanen = FXCollections.observableArrayList();
    private ObservableList<PerawatanRow> dataPerawatan = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dpDari.setValue(LocalDate.now().withDayOfMonth(1));
        dpSampai.setValue(LocalDate.now());

        colTanaman.setCellValueFactory(new PropertyValueFactory<>("namaTanaman"));
        colPengguna.setCellValueFactory(new PropertyValueFactory<>("namaPengguna"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colHasil.setCellValueFactory(new PropertyValueFactory<>("hasil"));

        colTanamanP.setCellValueFactory(new PropertyValueFactory<>("namaTanaman"));
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenisPerawatan"));
        colTanggalP.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colStatusP.setCellValueFactory(new PropertyValueFactory<>("status"));

        handleTampilkan();
    }

    @FXML
    public void handleTampilkan() {
        LocalDate dari = dpDari.getValue();
        LocalDate sampai = dpSampai.getValue();

        if (dari == null || sampai == null) {
            new Alert(Alert.AlertType.WARNING, "Pilih rentang tanggal!", ButtonType.OK).showAndWait();
            return;
        }

        if (dari.isAfter(sampai)) {
            new Alert(Alert.AlertType.WARNING, "Tanggal awal tidak boleh setelah tanggal akhir!", ButtonType.OK).showAndWait();
            return;
        }

        loadPanen(dari, sampai);
        loadPerawatan(dari, sampai);
        loadSummary(dari, sampai);
    }

    private void loadPanen(LocalDate dari, LocalDate sampai) {
        dataPanen.clear();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT t.nama AS nama_tanaman, p.nama AS nama_pengguna, " +
                            "cp.tanggal_panen, cp.hasil_panen " +
                            "FROM catatan_panen cp " +
                            "JOIN tanaman t ON cp.tanaman_id = t.id " +
                            "JOIN pengguna p ON cp.pengguna_id = p.id " +
                            "WHERE cp.tanggal_panen BETWEEN ? AND ? ORDER BY cp.tanggal_panen DESC");
            ps.setDate(1, Date.valueOf(dari));
            ps.setDate(2, Date.valueOf(sampai));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dataPanen.add(new PanenRow(
                        rs.getString("nama_tanaman"),
                        rs.getString("nama_pengguna"),
                        rs.getDate("tanggal_panen").toLocalDate().toString(),
                        rs.getString("hasil_panen")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tblPanen.setItems(dataPanen);
    }

    private void loadPerawatan(LocalDate dari, LocalDate sampai) {
        dataPerawatan.clear();
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT t.nama AS nama_tanaman, j.jenis_perawatan, j.tanggal, j.sudah_dilakukan " +
                            "FROM jadwal_perawatan j " +
                            "JOIN tanaman t ON j.tanaman_id = t.id " +
                            "WHERE j.tanggal BETWEEN ? AND ? ORDER BY j.tanggal DESC");
            ps.setDate(1, Date.valueOf(dari));
            ps.setDate(2, Date.valueOf(sampai));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dataPerawatan.add(new PerawatanRow(
                        rs.getString("nama_tanaman"),
                        rs.getString("jenis_perawatan"),
                        rs.getDate("tanggal").toLocalDate().toString(),
                        rs.getBoolean("sudah_dilakukan") ? "Selesai" : "Belum"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tblPerawatan.setItems(dataPerawatan);
    }

    private void loadSummary(LocalDate dari, LocalDate sampai) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps1 = conn.prepareStatement(
                    "SELECT COUNT(*) FROM catatan_panen WHERE tanggal_panen BETWEEN ? AND ?");
            ps1.setDate(1, Date.valueOf(dari));
            ps1.setDate(2, Date.valueOf(sampai));
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) lblTotalPanen.setText(String.valueOf(rs1.getInt(1)));

            PreparedStatement ps2 = conn.prepareStatement(
                    "SELECT COUNT(*) FROM jadwal_perawatan WHERE tanggal BETWEEN ? AND ?");
            ps2.setDate(1, Date.valueOf(dari));
            ps2.setDate(2, Date.valueOf(sampai));
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) lblTotalPerawatan.setText(String.valueOf(rs2.getInt(1)));

            PreparedStatement ps3 = conn.prepareStatement(
                    "SELECT COUNT(DISTINCT pengguna_id) FROM catatan_panen WHERE tanggal_panen BETWEEN ? AND ?");
            ps3.setDate(1, Date.valueOf(dari));
            ps3.setDate(2, Date.valueOf(sampai));
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) lblPenggunaAktif.setText(String.valueOf(rs3.getInt(1)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class PanenRow {
        private String namaTanaman, namaPengguna, tanggal, hasil;

        public PanenRow(String namaTanaman, String namaPengguna, String tanggal, String hasil) {
            this.namaTanaman = namaTanaman;
            this.namaPengguna = namaPengguna;
            this.tanggal = tanggal;
            this.hasil = hasil;
        }

        public String getNamaTanaman() { return namaTanaman; }
        public String getNamaPengguna() { return namaPengguna; }
        public String getTanggal() { return tanggal; }
        public String getHasil() { return hasil; }
    }

    public static class PerawatanRow {
        private String namaTanaman, jenisPerawatan, tanggal, status;

        public PerawatanRow(String namaTanaman, String jenisPerawatan, String tanggal, String status) {
            this.namaTanaman = namaTanaman;
            this.jenisPerawatan = jenisPerawatan;
            this.tanggal = tanggal;
            this.status = status;
        }

        public String getNamaTanaman() { return namaTanaman; }
        public String getJenisPerawatan() { return jenisPerawatan; }
        public String getTanggal() { return tanggal; }
        public String getStatus() { return status; }
    }
}
