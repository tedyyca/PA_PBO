package com.toga.controller;

import com.toga.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;

public class PenggunaController {

    @FXML private TextField tfNama;
    @FXML private TextField tfAlamat;

    @FXML private TableView<PenggunaRow> tblPengguna;
    @FXML private TableColumn<PenggunaRow, String> colNama;
    @FXML private TableColumn<PenggunaRow, String> colAlamat;

    private ObservableList<PenggunaRow> data = FXCollections.observableArrayList();
    private int selectedId = -1;

    @FXML
    public void initialize() {
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colAlamat.setCellValueFactory(new PropertyValueFactory<>("alamat"));

        tblPengguna.setOnMouseClicked(e -> {
            PenggunaRow row = tblPengguna.getSelectionModel().getSelectedItem();
            if (row != null) {
                selectedId = row.getId();
                tfNama.setText(row.getNama());
                tfAlamat.setText(row.getAlamat());
            }
        });

        loadData();
    }

    @FXML
    public void handleTambah() {
        String nama = tfNama.getText().trim();
        String alamat = tfAlamat.getText().trim();

        if (nama.isEmpty() || alamat.isEmpty()) {
            showAlert("Nama dan Alamat harus diisi!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO pengguna (nama, alamat) VALUES (?, ?)");
            ps.setString(1, nama);
            ps.setString(2, alamat);
            ps.executeUpdate();
            loadData();
            clearForm();
            showInfo("Pengguna berhasil ditambahkan!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleUbah() {
        if (selectedId == -1) { showAlert("Pilih pengguna terlebih dahulu!"); return; }
        String nama = tfNama.getText().trim();
        String alamat = tfAlamat.getText().trim();

        if (nama.isEmpty() || alamat.isEmpty()) {
            showAlert("Nama dan Alamat harus diisi!"); return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE pengguna SET nama=?, alamat=? WHERE id=?");
            ps.setString(1, nama);
            ps.setString(2, alamat);
            ps.setInt(3, selectedId);
            ps.executeUpdate();
            loadData();
            clearForm();
            showInfo("Pengguna berhasil diubah!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleHapus() {
        if (selectedId == -1) { showAlert("Pilih pengguna terlebih dahulu!"); return; }
        Alert konfirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Yakin ingin menghapus pengguna ini? Semua catatan terkait akan ikut terhapus.",
                ButtonType.YES, ButtonType.NO);
        konfirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM pengguna WHERE id=?");
                    ps.setInt(1, selectedId);
                    ps.executeUpdate();
                    loadData();
                    clearForm();
                    showInfo("Pengguna berhasil dihapus!");
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
                    "SELECT * FROM pengguna ORDER BY nama");
            while (rs.next()) {
                data.add(new PenggunaRow(
                        rs.getInt("id"),
                        rs.getString("nama"),
                        rs.getString("alamat")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tblPengguna.setItems(data);
    }

    private void clearForm() {
        tfNama.clear();
        tfAlamat.clear();
        selectedId = -1;
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    public static class PenggunaRow {
        private int id;
        private String nama, alamat;

        public PenggunaRow(int id, String nama, String alamat) {
            this.id = id;
            this.nama = nama;
            this.alamat = alamat;
        }

        public int getId() { return id; }
        public String getNama() { return nama; }
        public String getAlamat() { return alamat; }
    }
}
