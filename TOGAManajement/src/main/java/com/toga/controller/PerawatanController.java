package com.toga.controller;

import com.toga.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;

public class PerawatanController {

    @FXML private ComboBox<String> cmbTanaman;
    @FXML private ComboBox<String> cmbJenisPerawatan;
    @FXML private DatePicker       dpTanggal;
    @FXML private ComboBox<String> cmbPengguna;   // untuk Tandai Selesai
    @FXML private Label            lblBelumHariIni;

    @FXML private TableView<JadwalRow>              tblJadwal;
    @FXML private TableColumn<JadwalRow, String>    colTanaman;
    @FXML private TableColumn<JadwalRow, String>    colJenis;
    @FXML private TableColumn<JadwalRow, String>    colTanggal;
    @FXML private TableColumn<JadwalRow, String>    colStatus;

    private ObservableList<JadwalRow> data = FXCollections.observableArrayList();
    private int selectedId = -1;

    private final HashMap<String, Integer> tanamanMap  = new HashMap<>();
    private final HashMap<String, Integer> penggunaMap = new HashMap<>();

    @FXML
    public void initialize() {
        cmbJenisPerawatan.setItems(FXCollections.observableArrayList(
                "Penyiraman", "Pemupukan", "Penyiangan", "Pemangkasan"));
        cmbJenisPerawatan.setValue("Penyiraman");
        dpTanggal.setValue(LocalDate.now());

        colTanaman.setCellValueFactory(new PropertyValueFactory<>("namaTanaman"));
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenisPerawatan"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggal"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tblJadwal.setOnMouseClicked(e -> {
            JadwalRow row = tblJadwal.getSelectionModel().getSelectedItem();
            if (row != null) selectedId = row.getId();
        });

        loadCombo();
        loadData();
    }

    private void loadCombo() {
        tanamanMap.clear();
        penggunaMap.clear();
        ObservableList<String> tanamanList  = FXCollections.observableArrayList();
        ObservableList<String> penggunaList = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs1 = conn.createStatement()
                    .executeQuery("SELECT id, nama FROM tanaman ORDER BY nama");
            while (rs1.next()) {
                tanamanMap.put(rs1.getString("nama"), rs1.getInt("id"));
                tanamanList.add(rs1.getString("nama"));
            }

            ResultSet rs2 = conn.createStatement()
                    .executeQuery("SELECT id, nama FROM pengguna ORDER BY nama");
            while (rs2.next()) {
                penggunaMap.put(rs2.getString("nama"), rs2.getInt("id"));
                penggunaList.add(rs2.getString("nama"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cmbTanaman.setItems(tanamanList);
        if (!tanamanList.isEmpty()) cmbTanaman.setValue(tanamanList.get(0));

        cmbPengguna.setItems(penggunaList);
        if (!penggunaList.isEmpty()) cmbPengguna.setValue(penggunaList.get(0));
    }

    @FXML
    public void handleTambah() {
        String    namaTanaman = cmbTanaman.getValue();
        String    jenis       = cmbJenisPerawatan.getValue();
        LocalDate tanggal     = dpTanggal.getValue();

        if (namaTanaman == null || jenis == null || tanggal == null) {
            showAlert("Semua field harus diisi!"); return;
        }

        int tanamanId = tanamanMap.getOrDefault(namaTanaman, -1);
        if (tanamanId == -1) { showAlert("Tanaman tidak ditemukan!"); return; }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO jadwal_perawatan "
                            + "(tanaman_id, jenis_perawatan, tanggal, sudah_dilakukan) "
                            + "VALUES (?,?,?,?)");
            ps.setInt(1, tanamanId);
            ps.setString(2, jenis);
            ps.setDate(3, Date.valueOf(tanggal));
            ps.setBoolean(4, false);
            ps.executeUpdate();
            loadData();
            showInfo("Jadwal berhasil ditambahkan!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Tandai jadwal selesai DAN catat ke catatan_perawatan (siapa yang merawat).
     * Pengguna dipilih lewat cmbPengguna.
     */
    @FXML
    public void handleTandaiSelesai() {
        if (selectedId == -1) { showAlert("Pilih jadwal terlebih dahulu!"); return; }

        String namaPengguna = cmbPengguna.getValue();
        if (namaPengguna == null) {
            showAlert("Pilih pengguna yang melakukan perawatan!"); return;
        }

        int penggunaId = penggunaMap.getOrDefault(namaPengguna, -1);
        if (penggunaId == -1) { showAlert("Pengguna tidak ditemukan!"); return; }

        try (Connection conn = DBConnection.getConnection()) {
            // Ambil detail jadwal yang dipilih
            PreparedStatement psGet = conn.prepareStatement(
                    "SELECT j.tanaman_id, j.jenis_perawatan, j.tanggal, j.sudah_dilakukan "
                            + "FROM jadwal_perawatan j WHERE j.id=?");
            psGet.setInt(1, selectedId);
            ResultSet rs = psGet.executeQuery();
            if (!rs.next()) { showAlert("Jadwal tidak ditemukan!"); return; }

            if (rs.getBoolean("sudah_dilakukan")) {
                showAlert("Jadwal ini sudah ditandai selesai sebelumnya!"); return;
            }

            int       tanamanId    = rs.getInt("tanaman_id");
            String    jenisPerawatan = rs.getString("jenis_perawatan");
            LocalDate tanggal      = rs.getDate("tanggal").toLocalDate();

            // Update jadwal jadi selesai
            PreparedStatement psUpdate = conn.prepareStatement(
                    "UPDATE jadwal_perawatan SET sudah_dilakukan=TRUE WHERE id=?");
            psUpdate.setInt(1, selectedId);
            psUpdate.executeUpdate();

            // Insert ke catatan_perawatan — mencatat siapa yang melakukan perawatan
            PreparedStatement psInsert = conn.prepareStatement(
                    "INSERT INTO catatan_perawatan "
                            + "(tanaman_id, pengguna_id, keterangan, tanggal) "
                            + "VALUES (?,?,?,?)");
            psInsert.setInt(1, tanamanId);
            psInsert.setInt(2, penggunaId);
            psInsert.setString(3, jenisPerawatan + " oleh " + namaPengguna);
            psInsert.setDate(4, Date.valueOf(tanggal));
            psInsert.executeUpdate();

            loadData();
            showInfo("Jadwal ditandai selesai dan dicatat oleh " + namaPengguna + "!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleHapus() {
        if (selectedId == -1) { showAlert("Pilih jadwal terlebih dahulu!"); return; }

        Alert konfirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Yakin ingin menghapus jadwal ini?", ButtonType.YES, ButtonType.NO);
        konfirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM jadwal_perawatan WHERE id=?");
                    ps.setInt(1, selectedId);
                    ps.executeUpdate();
                    loadData();
                    selectedId = -1;
                    showInfo("Jadwal berhasil dihapus!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadData() {
        data.clear();
        int belum = 0;
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT j.id, t.nama, j.jenis_perawatan, j.tanggal, j.sudah_dilakukan "
                            + "FROM jadwal_perawatan j "
                            + "JOIN tanaman t ON j.tanaman_id = t.id "
                            + "ORDER BY j.tanggal DESC");
            while (rs.next()) {
                boolean sudah = rs.getBoolean("sudah_dilakukan");
                data.add(new JadwalRow(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("jenis_perawatan"),
                        rs.getDate("tanggal").toLocalDate().toString(),
                        sudah ? "Selesai" : "Belum"));
                LocalDate tgl = rs.getDate("tanggal").toLocalDate();
                if (!sudah && tgl.equals(LocalDate.now())) belum++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tblJadwal.setItems(data);
        lblBelumHariIni.setText(belum + " jadwal belum dilakukan hari ini");
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    public static class JadwalRow {
        private int    id;
        private String namaTanaman, jenisPerawatan, tanggal, status;

        public JadwalRow(int id, String namaTanaman, String jenisPerawatan,
                         String tanggal, String status) {
            this.id             = id;
            this.namaTanaman    = namaTanaman;
            this.jenisPerawatan = jenisPerawatan;
            this.tanggal        = tanggal;
            this.status         = status;
        }

        public int    getId()               { return id; }
        public String getNamaTanaman()      { return namaTanaman; }
        public String getJenisPerawatan()   { return jenisPerawatan; }
        public String getTanggal()          { return tanggal; }
        public String getStatus()           { return status; }
    }
}