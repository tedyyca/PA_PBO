package com.toga.controller;

import com.toga.model.*;
import com.toga.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.*;
import java.time.LocalDate;

public class TanamanController {

    @FXML private ComboBox<String> cmbJenis;
    @FXML private TextField tfNama;
    @FXML private TextField tfNamaLatin;
    @FXML private TextField tfProperti;
    @FXML private TextArea taManfaat;
    @FXML private DatePicker dpTanggal;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private Label lblProperti;

    @FXML private TableView<TanamanRow> tblTanaman;
    @FXML private TableColumn<TanamanRow, String> colNama;
    @FXML private TableColumn<TanamanRow, String> colJenis;
    @FXML private TableColumn<TanamanRow, String> colNamaLatin;
    @FXML private TableColumn<TanamanRow, String> colStatus;

    private ObservableList<TanamanRow> data = FXCollections.observableArrayList();
    private int selectedId = -1;

    @FXML
    public void initialize() {
        cmbJenis.setItems(FXCollections.observableArrayList("Tanaman Rempah", "Tanaman Daun", "Tanaman Buah"));
        cmbStatus.setItems(FXCollections.observableArrayList("BIBIT", "TUMBUH", "SIAP_PANEN", "SUDAH_DIPANEN"));
        cmbJenis.setValue("Tanaman Rempah");
        cmbStatus.setValue("BIBIT");
        dpTanggal.setValue(LocalDate.now());

        cmbJenis.setOnAction(e -> updateLabelProperti());

        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        colNamaLatin.setCellValueFactory(new PropertyValueFactory<>("namaLatin"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        tblTanaman.setOnMouseClicked(e -> {
            TanamanRow row = tblTanaman.getSelectionModel().getSelectedItem();
            if (row != null) {
                selectedId = row.getId();
                tfNama.setText(row.getNama());
                tfNamaLatin.setText(row.getNamaLatin());
                taManfaat.setText(row.getManfaat());
                tfProperti.setText(row.getProperti());
                cmbJenis.setValue(row.getJenis());
                cmbStatus.setValue(row.getStatus());
                dpTanggal.setValue(row.getTanggalTanam());
            }
        });

        loadData();
    }

    private void updateLabelProperti() {
        String jenis = cmbJenis.getValue();
        if ("Tanaman Rempah".equals(jenis)) lblProperti.setText("Aroma");
        else if ("Tanaman Daun".equals(jenis)) lblProperti.setText("Bentuk Daun");
        else lblProperti.setText("Musim Berbuah");
    }

    @FXML
    public void handleTambah() {
        String nama = tfNama.getText().trim();
        String namaLatin = tfNamaLatin.getText().trim();
        String manfaat = taManfaat.getText().trim();
        String properti = tfProperti.getText().trim();
        String jenis = cmbJenis.getValue();
        String status = cmbStatus.getValue();
        LocalDate tanggal = dpTanggal.getValue();

        if (nama.isEmpty() || namaLatin.isEmpty() || manfaat.isEmpty() || properti.isEmpty() || tanggal == null) {
            showAlert("Semua field harus diisi!");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO tanaman (nama, nama_latin, manfaat, jenis, properti_tambahan, status, tanggal_tanam) VALUES (?,?,?,?,?,?,?)");
            ps.setString(1, nama);
            ps.setString(2, namaLatin);
            ps.setString(3, manfaat);
            ps.setString(4, jenis);
            ps.setString(5, properti);
            ps.setString(6, status);
            ps.setDate(7, Date.valueOf(tanggal));
            ps.executeUpdate();
            loadData();
            clearForm();
            showInfo("Tanaman berhasil ditambahkan!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleUbah() {
        if (selectedId == -1) { showAlert("Pilih tanaman terlebih dahulu!"); return; }
        String nama = tfNama.getText().trim();
        String namaLatin = tfNamaLatin.getText().trim();
        String manfaat = taManfaat.getText().trim();
        String properti = tfProperti.getText().trim();
        String jenis = cmbJenis.getValue();
        String status = cmbStatus.getValue();
        LocalDate tanggal = dpTanggal.getValue();

        if (nama.isEmpty() || namaLatin.isEmpty() || manfaat.isEmpty() || properti.isEmpty()) {
            showAlert("Semua field harus diisi!"); return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE tanaman SET nama=?, nama_latin=?, manfaat=?, jenis=?, properti_tambahan=?, status=?, tanggal_tanam=? WHERE id=?");
            ps.setString(1, nama);
            ps.setString(2, namaLatin);
            ps.setString(3, manfaat);
            ps.setString(4, jenis);
            ps.setString(5, properti);
            ps.setString(6, status);
            ps.setDate(7, Date.valueOf(tanggal));
            ps.setInt(8, selectedId);
            ps.executeUpdate();
            loadData();
            clearForm();
            showInfo("Tanaman berhasil diubah!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleHapus() {
        if (selectedId == -1) { showAlert("Pilih tanaman terlebih dahulu!"); return; }
        Alert konfirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus tanaman ini?", ButtonType.YES, ButtonType.NO);
        konfirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("DELETE FROM tanaman WHERE id=?");
                    ps.setInt(1, selectedId);
                    ps.executeUpdate();
                    loadData();
                    clearForm();
                    showInfo("Tanaman berhasil dihapus!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @FXML
    public void handleInfoObat() {
        if (selectedId == -1) { showAlert("Pilih tanaman terlebih dahulu!"); return; }
        TanamanRow row = tblTanaman.getSelectionModel().getSelectedItem();
        if (row == null) return;

        Tanaman t = buildTanamanFromRow(row);
        if (t instanceof ITanamanObat obat) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info Penggunaan Obat");
            alert.setHeaderText(row.getNama() + " (" + row.getJenis() + ")");
            alert.setContentText("Deskripsi  : " + obat.getDeskripsiObat() +
                    "\n\nCara Pakai : " + obat.getCaraPenggunaan());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleEstimasiPanen() {
        if (selectedId == -1) { showAlert("Pilih tanaman terlebih dahulu!"); return; }
        TanamanRow row = tblTanaman.getSelectionModel().getSelectedItem();
        if (row == null) return;

        Tanaman t = buildTanamanFromRow(row);

        ChoiceDialog<String> dialog = new ChoiceDialog<>("Konsumsi", "Konsumsi", "Obat", "Bibit");
        dialog.setTitle("Estimasi Masa Panen");
        dialog.setHeaderText("Pilih tujuan panen untuk " + row.getNama());
        dialog.setContentText("Tujuan:");
        dialog.showAndWait().ifPresent(tujuan -> {
            int hari = t.estimasiHariPanen(tujuan.toLowerCase());
            int bulan = hari / 30;
            int sisa = hari % 30;
            showInfo("Estimasi panen " + row.getNama() + " untuk tujuan " + tujuan +
                    ":\n" + hari + " hari (" + bulan + " bulan " + sisa + " hari)");
        });
    }

    private Tanaman buildTanamanFromRow(TanamanRow row) {
        LocalDate tgl = row.getTanggalTanam() != null ? row.getTanggalTanam() : LocalDate.now();
        StatusTanaman st = StatusTanaman.valueOf(row.getStatus());
        return switch (row.getJenis()) {
            case "Tanaman Rempah" -> new TanamanRempah(row.getNama(), row.getNamaLatin(), row.getManfaat(), row.getProperti(), st, tgl);
            case "Tanaman Daun" -> new TanamanDaun(row.getNama(), row.getNamaLatin(), row.getManfaat(), row.getProperti(), st, tgl);
            default -> new TanamanBuah(row.getNama(), row.getNamaLatin(), row.getManfaat(), row.getProperti(), st, tgl);
        };
    }

    private void loadData() {
        data.clear();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM tanaman ORDER BY nama");
            while (rs.next()) {
                TanamanRow row = new TanamanRow(
                        rs.getInt("id"), rs.getString("nama"), rs.getString("jenis"),
                        rs.getString("nama_latin"), rs.getString("manfaat"),
                        rs.getString("properti_tambahan"), rs.getString("status"),
                        rs.getDate("tanggal_tanam") != null ? rs.getDate("tanggal_tanam").toLocalDate() : null);
                data.add(row);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tblTanaman.setItems(data);
    }

    private void clearForm() {
        tfNama.clear(); tfNamaLatin.clear(); taManfaat.clear(); tfProperti.clear();
        cmbJenis.setValue("Tanaman Rempah"); cmbStatus.setValue("BIBIT");
        dpTanggal.setValue(LocalDate.now()); selectedId = -1;
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    public static class TanamanRow {
        private int id;
        private String nama, jenis, namaLatin, manfaat, properti, status;
        private LocalDate tanggalTanam;

        public TanamanRow(int id, String nama, String jenis, String namaLatin, String manfaat, String properti, String status, LocalDate tanggalTanam) {
            this.id = id; this.nama = nama; this.jenis = jenis; this.namaLatin = namaLatin;
            this.manfaat = manfaat; this.properti = properti; this.status = status; this.tanggalTanam = tanggalTanam;
        }

        public int getId() { return id; }
        public String getNama() { return nama; }
        public String getJenis() { return jenis; }
        public String getNamaLatin() { return namaLatin; }
        public String getManfaat() { return manfaat; }
        public String getProperti() { return properti; }
        public String getStatus() { return status; }
        public LocalDate getTanggalTanam() { return tanggalTanam; }
    }
}
