package com.toga.controller;

import com.toga.util.DBConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class DashboardController {

    @FXML private Label lblTotalTanaman;
    @FXML private Label lblSiapPanen;
    @FXML private Label lblTotalPengguna;
    @FXML private Label lblJadwalHariIni;
    @FXML private Label lblJmlRempah;
    @FXML private Label lblJmlDaun;
    @FXML private Label lblJmlBuah;
    @FXML private TableView<PanenRow> tblMendekatiPanen;
    @FXML private TableColumn<PanenRow, String> colNama;
    @FXML private TableColumn<PanenRow, String> colSisa;

    @FXML
    public void initialize() {
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colSisa.setCellValueFactory(new PropertyValueFactory<>("sisa"));
        loadDashboard();
    }

    private void loadDashboard() {
        try (Connection conn = DBConnection.getConnection()) {
            if (conn == null) return;

            ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM tanaman");
            if (rs1.next()) lblTotalTanaman.setText(String.valueOf(rs1.getInt(1)));

            ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM pengguna");
            if (rs2.next()) lblTotalPengguna.setText(String.valueOf(rs2.getInt(1)));

            ResultSet rs3 = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM jadwal_perawatan WHERE tanggal = CURDATE() AND sudah_dilakukan = FALSE");
            if (rs3.next()) lblJadwalHariIni.setText(String.valueOf(rs3.getInt(1)));

            ResultSet rs4 = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM tanaman WHERE jenis = 'Tanaman Rempah'");
            if (rs4.next()) lblJmlRempah.setText(rs4.getInt(1) + " tanaman");

            ResultSet rs5 = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM tanaman WHERE jenis = 'Tanaman Daun'");
            if (rs5.next()) lblJmlDaun.setText(rs5.getInt(1) + " tanaman");

            ResultSet rs6 = conn.createStatement().executeQuery(
                    "SELECT COUNT(*) FROM tanaman WHERE jenis = 'Tanaman Buah'");
            if (rs6.next()) lblJmlBuah.setText(rs6.getInt(1) + " tanaman");

            loadMendekatiPanen(conn);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMendekatiPanen(Connection conn) throws Exception {
        ObservableList<PanenRow> list = FXCollections.observableArrayList();
        ResultSet rs = conn.createStatement().executeQuery(
                "SELECT nama, jenis, tanggal_tanam FROM tanaman WHERE status != 'SUDAH_DIPANEN'");
        while (rs.next()) {
            String nama = rs.getString("nama");
            String jenis = rs.getString("jenis");
            java.time.LocalDate tanam = rs.getDate("tanggal_tanam").toLocalDate();
            int estimasi = jenis.equals("Tanaman Rempah") ? 240 : jenis.equals("Tanaman Daun") ? 60 : 180;
            java.time.LocalDate panen = tanam.plusDays(estimasi);
            long sisa = java.time.temporal.ChronoUnit.DAYS.between(java.time.LocalDate.now(), panen);
            if (sisa >= 0 && sisa <= 30) {
                list.add(new PanenRow(nama, sisa + " hari"));
            }
        }
        lblSiapPanen.setText(String.valueOf(list.size()));
        tblMendekatiPanen.setItems(list);
    }

    public static class PanenRow {
        private String nama;
        private String sisa;
        public PanenRow(String nama, String sisa) { this.nama = nama; this.sisa = sisa; }
        public String getNama() { return nama; }
        public String getSisa() { return sisa; }
    }
}
