package com.toga.controller;

import com.toga.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;

public class PanenController {

    @FXML private ComboBox<String> cmbTanaman;
    @FXML private ComboBox<String> cmbPengguna;
    @FXML private DatePicker dpTanggalPanen;
    @FXML private TextField tfHasilPanen;
    @FXML private TextArea taKeterangan;

    @FXML private TableView<PanenRow> tblPanen;
    @FXML private TableColumn<PanenRow, String> colTanaman;
    @FXML private TableColumn<PanenRow, String> colPengguna;
    @FXML private TableColumn<PanenRow, String> colTanggal;
    @FXML private TableColumn<PanenRow, String> colHasil;
    @FXML private TableColumn<PanenRow, String> colKeterangan;

    private ObservableList<PanenRow> data = FXCollections.observableArrayList();
    private int selectedId = -1;

    private java.util.HashMap<String, Integer> tanamanMap = new java.util.HashMap<>();
    private java.util.HashMap<String, Integer> penggunaMap = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        dpTanggalPanen.setValue(LocalDate.now());

        colTanaman.setCellValueFactory(new PropertyValueFactory<>("namaTanaman"));
        colPengguna.setCellValueFactory(new PropertyValueFactory<>("namaPengguna"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalPanen"));
        colHasil.setCellValueFactory(new PropertyValueFactory<>("hasilPanen"));
        colKeterangan.setCellValueFactory(new PropertyValueFactory<>("keterangan"));

        tblPanen.setOnMouseClicked(e -> {
            PanenRow row = tblPanen.getSelectionModel().getSelectedItem();
            if (row != null) {
                selectedId = row.getId();
                cmbTanaman.setValue(row.getNamaTanaman());
                cmbPengguna.setValue(row.getNamaPengguna());
                tfHasilPanen.setText(row.getHasilPanen());
                taKeterangan.setText(row.getKeterangan());
            }
        });

        loadCombo();
        loadData();
    }

    private void loadCombo() {
        tanamanMap.clear();
        penggunaMap.clear();
        ObservableList<String> tanamanList = FXCollections.observableArrayList();
        ObservableList<String> penggunaList = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs1 = conn.createStatement().executeQuery(
                    "SELECT id, nama FROM tanaman ORDER BY nama");
            while (rs1.next()) {
                tanamanMap.put(rs1.getString("nama"), rs1.getInt("id"));
                tanamanList.add(rs1.getString("nama"));
            }

            ResultSet rs2 = conn.createStatement().executeQuery(
                    "SELECT id, nama FROM pengguna ORDER BY nama");
            while (rs2.next()) {
                penggunaMap.put(rs2.getString("nama"), rs2.getInt("id"));
                penggunaList.add(rs2.getString("nama"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        cmbTanaman.setItems(tanamanList);
        cmbPengguna.setItems(penggunaList);
        if (!tanamanList.isEmpty()) cmbTanaman.setValue(tanamanList.get(0));
        if (!penggunaList.isEmpty()) cmbPengguna.setValue(penggunaList.get(0));
    }

    @FXML
    public void handleCatat() {
        String namaTanaman = cmbTanaman.getValue();
        String namaPengguna = cmbPengguna.getValue();
        LocalDate tanggal = dpTanggalPanen.getValue();
        String hasil = tfHasilPanen.getText().trim();
        String keterangan = taKeterangan.getText().trim();

        if (namaTanaman == null || namaPengguna == null || tanggal == null
                || hasil.isEmpty() || keterangan.isEmpty()) {
            showAlert("Semua field harus diisi!"); return;
        }

        int tanamanId = tanamanMap.getOrDefault(namaTanaman, -1);
        int penggunaId = penggunaMap.getOrDefault(namaPengguna, -1);
        if (tanamanId == -1 || penggunaId == -1) {
            showAlert("Data tidak valid!"); return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO catatan_panen (tanaman_id, pengguna_id, keterangan, tanggal_panen, hasil_panen) VALUES (?,?,?,?,?)");
            ps.setInt(1, tanamanId);
            ps.setInt(2, penggunaId);
            ps.setString(3, keterangan);
            ps.setDate(4, Date.valueOf(tanggal));
            ps.setString(5, hasil);
            ps.executeUpdate();

            // update status tanaman jadi SUDAH_DIPANEN
            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE tanaman SET status='SUDAH_DIPANEN' WHERE id=?");
            ps2.setInt(1, tanamanId);
            ps2.executeUpdate();

            loadData();
            clearForm();
            showInfo("Panen berhasil dicatat!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleHapus() {
        if (selectedId == -1) { showAlert("Pilih catatan terlebih dahulu!"); return; }
        Alert konfirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Yakin ingin menghapus catatan panen ini?", ButtonType.YES, ButtonType.NO);
        konfirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM catatan_panen WHERE id=?");
                    ps.setInt(1, selectedId);
                    ps.executeUpdate();
                    loadData();
                    clearForm();
                    showInfo("Catatan panen berhasil dihapus!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadData() {
        data.clear();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT cp.id, t.nama AS nama_tanaman, p.nama AS nama_pengguna, " +
                            "cp.tanggal_panen, cp.hasil_panen, cp.keterangan " +
                            "FROM catatan_panen cp " +
                            "JOIN tanaman t ON cp.tanaman_id = t.id " +
                            "JOIN pengguna p ON cp.pengguna_id = p.id " +
                            "ORDER BY cp.tanggal_panen DESC");
            while (rs.next()) {
                data.add(new PanenRow(
                        rs.getInt("id"),
                        rs.getString("nama_tanaman"),
                        rs.getString("nama_pengguna"),
                        rs.getDate("tanggal_panen").toLocalDate().toString(),
                        rs.getString("hasil_panen"),
                        rs.getString("keterangan")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tblPanen.setItems(data);
    }

    private void clearForm() {
        tfHasilPanen.clear();
        taKeterangan.clear();
        dpTanggalPanen.setValue(LocalDate.now());
        selectedId = -1;
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    public static class PanenRow {
        private int id;
        private String namaTanaman, namaPengguna, tanggalPanen, hasilPanen, keterangan;

        public PanenRow(int id, String namaTanaman, String namaPengguna, String tanggalPanen, String hasilPanen, String keterangan) {
            this.id = id;
            this.namaTanaman = namaTanaman;
            this.namaPengguna = namaPengguna;
            this.tanggalPanen = tanggalPanen;
            this.hasilPanen = hasilPanen;
            this.keterangan = keterangan;
        }

        public int getId() { return id; }
        public String getNamaTanaman() { return namaTanaman; }
        public String getNamaPengguna() { return namaPengguna; }
        public String getTanggalPanen() { return tanggalPanen; }
        public String getHasilPanen() { return hasilPanen; }
        public String getKeterangan() { return keterangan; }
    }
}
