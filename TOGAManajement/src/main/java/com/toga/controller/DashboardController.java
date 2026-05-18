package com.toga.controller;

import com.toga.repository.impl.DashboardRepositoryImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import com.toga.config.DBConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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

    private final DashboardRepositoryImpl repo = new DashboardRepositoryImpl();

    @FXML
    public void initialize() {
        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colSisa.setCellValueFactory(new PropertyValueFactory<>("sisa"));
        loadDashboard();
    }

    private void loadDashboard() {
        try {
            lblTotalTanaman.setText(String.valueOf(repo.getCount("SELECT COUNT(*) FROM tanaman")));
            lblTotalPengguna.setText(String.valueOf(repo.getCount("SELECT COUNT(*) FROM pengguna")));
            lblJadwalHariIni.setText(String.valueOf(repo.getCount("SELECT COUNT(*) FROM jadwal_perawatan WHERE tanggal = CURDATE() AND sudah_dilakukan = FALSE")));
            lblJmlRempah.setText(repo.getCount("SELECT COUNT(*) FROM tanaman WHERE jenis = 'Tanaman Rempah'") + " tanaman");
            lblJmlDaun.setText(repo.getCount("SELECT COUNT(*) FROM tanaman WHERE jenis = 'Tanaman Daun'") + " tanaman");
            lblJmlBuah.setText(repo.getCount("SELECT COUNT(*) FROM tanaman WHERE jenis = 'Tanaman Buah'") + " tanaman");
            loadMendekatiPanen();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Gagal memuat dashboard: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    private void loadMendekatiPanen() throws Exception {
        ObservableList<PanenRow> list = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT nama, tanggal_tanam, estimasi_hari FROM tanaman WHERE status != 'SUDAH_DIPANEN'")) {
            while (rs.next()) {
                String nama = rs.getString("nama");
                LocalDate tanam = rs.getDate("tanggal_tanam").toLocalDate();
                int estimasi = rs.getInt("estimasi_hari");
                LocalDate panen = tanam.plusDays(estimasi);
                long sisa = ChronoUnit.DAYS.between(LocalDate.now(), panen);
                if (sisa >= 0 && sisa <= 30) {
                    list.add(new PanenRow(nama, sisa + " hari"));
                }
            }
        }
        lblSiapPanen.setText(String.valueOf(list.size()));
        tblMendekatiPanen.setItems(list);
    }

    public static class PanenRow {
        private final String nama;
        private final String sisa;
        public PanenRow(String nama, String sisa) { this.nama = nama; this.sisa = sisa; }
        public String getNama() { return nama; }
        public String getSisa() { return sisa; }
    }
}