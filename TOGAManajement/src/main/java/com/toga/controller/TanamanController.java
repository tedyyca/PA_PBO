package com.toga.controller;

import com.toga.model.ITanamanObat;
import com.toga.model.StatusTanaman;
import com.toga.model.Tanaman;
import com.toga.model.TanamanBuah;
import com.toga.model.TanamanDaun;
import com.toga.model.TanamanRempah;
import com.toga.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

public class TanamanController {

    @FXML private ComboBox<String> cmbJenis;
    @FXML private TextField        tfNama;
    @FXML private TextField        tfNamaLatin;
    @FXML private TextField        tfProperti;
    @FXML private TextArea         taManfaat;
    @FXML private DatePicker       dpTanggal;
    @FXML private Label            lblProperti;
    @FXML private Label            lblStatusInfo;

    @FXML private TableView<TanamanRow>              tblTanaman;
    @FXML private TableColumn<TanamanRow, String>    colNama;
    @FXML private TableColumn<TanamanRow, String>    colJenis;
    @FXML private TableColumn<TanamanRow, String>    colNamaLatin;
    @FXML private TableColumn<TanamanRow, String>    colStatus;

    private final ObservableList<TanamanRow> data = FXCollections.observableArrayList();
    private int selectedId = -1;

    private static final String REGEX_HURUF_SPASI = "[a-zA-Z ]+";

    @FXML
    public void initialize() {
        cmbJenis.setItems(FXCollections.observableArrayList(
                "Tanaman Rempah", "Tanaman Daun", "Tanaman Buah"));
        cmbJenis.setValue("Tanaman Rempah");
        dpTanggal.setValue(LocalDate.now());

        cmbJenis.setOnAction(e -> updateLabelProperti());

        dpTanggal.valueProperty().addListener((obs, o, n) -> updateStatusPreview());
        cmbJenis.valueProperty().addListener((obs, o, n) -> updateStatusPreview());

        pasangFilterHurufSpasi(tfNama);
        pasangFilterHurufSpasi(tfNamaLatin);
        pasangFilterHurufSpasi(tfProperti);

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
                dpTanggal.setValue(row.getTanggalTanam());
                lblStatusInfo.setText("Status saat ini: " + row.getStatus());
            }
        });

        updateStatusPreview();
        loadData();
    }

    private void pasangFilterHurufSpasi(TextField tf) {
        tf.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && !newVal.matches("[a-zA-Z ]*")) {
                tf.setText(oldVal);
            }
        });
    }

    private void updateLabelProperti() {
        String jenis = cmbJenis.getValue();
        if ("Tanaman Rempah".equals(jenis))    lblProperti.setText("Aroma");
        else if ("Tanaman Daun".equals(jenis)) lblProperti.setText("Bentuk Daun");
        else                                   lblProperti.setText("Musim Berbuah");
        updateStatusPreview();
    }

    private void updateStatusPreview() {
        LocalDate tgl   = dpTanggal.getValue();
        String    jenis = cmbJenis.getValue();
        if (tgl == null || jenis == null) return;

        int estimasi = getEstimasiByJenis(jenis);
        StatusTanaman status = Tanaman.hitungStatus(tgl, estimasi);
        lblStatusInfo.setText("Status otomatis: " + status.name());
    }

    private int getEstimasiByJenis(String jenis) {
        switch (jenis) {
            case "Tanaman Rempah": return 240;
            case "Tanaman Daun":   return 60;
            default:               return 180;
        }
    }

    @FXML
    public void handleTambah() {
        String    nama     = tfNama.getText().trim();
        String    latin    = tfNamaLatin.getText().trim();
        String    manfaat  = taManfaat.getText().trim();
        String    properti = tfProperti.getText().trim();
        String    jenis    = cmbJenis.getValue();
        LocalDate tanggal  = dpTanggal.getValue();

        if (nama.isEmpty() || latin.isEmpty() || manfaat.isEmpty()
                || properti.isEmpty() || tanggal == null) {
            showAlert("Semua field harus diisi!"); return;
        }
        if (!nama.matches(REGEX_HURUF_SPASI)) {
            showAlert("Nama tanaman hanya boleh berisi huruf dan spasi!"); return;
        }
        if (!latin.matches(REGEX_HURUF_SPASI)) {
            showAlert("Nama Latin hanya boleh berisi huruf dan spasi!"); return;
        }
        if (!properti.matches(REGEX_HURUF_SPASI)) {
            showAlert("Field " + lblProperti.getText() + " hanya boleh berisi huruf dan spasi!"); return;
        }
        if (isDuplikatTanaman(-1, nama, latin, manfaat, jenis, properti, tanggal)) {
            showAlert("Data tanaman sudah ada! Tidak dapat menambahkan duplikat."); return;
        }

        int estimasi = getEstimasiByJenis(jenis);
        StatusTanaman status = Tanaman.hitungStatus(tanggal, estimasi);

        // Bangun objek model Tanaman untuk validasi (Modul 4 & 5 — Inheritance & Polymorphism)
        Tanaman tanaman = buildTanamanBaru(nama, latin, manfaat, properti, jenis, status, tanggal);
        if (!tanaman.setNama(nama) || !tanaman.setNamaLatin(latin) || !tanaman.setManfaat(manfaat)) {
            showAlert("Data tanaman tidak valid!"); return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO tanaman "
                            + "(nama, nama_latin, manfaat, jenis, properti_tambahan, status, tanggal_tanam) "
                            + "VALUES (?,?,?,?,?,?,?)");
            // Ambil data dari objek model
            ps.setString(1, tanaman.getNama());
            ps.setString(2, tanaman.getNamaLatin());
            ps.setString(3, tanaman.getManfaat());
            ps.setString(4, tanaman.getJenis());
            ps.setString(5, tanaman.getPropertiTambahan());
            ps.setString(6, tanaman.getStatus().name());
            ps.setDate(7, Date.valueOf(tanaman.getTanggalTanam()));
            ps.executeUpdate();
            loadData();
            clearForm();
            showInfo("Tanaman berhasil ditambahkan! Status: " + status.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleUbah() {
        if (selectedId == -1) { showAlert("Pilih tanaman terlebih dahulu!"); return; }

        String    nama     = tfNama.getText().trim();
        String    latin    = tfNamaLatin.getText().trim();
        String    manfaat  = taManfaat.getText().trim();
        String    properti = tfProperti.getText().trim();
        String    jenis    = cmbJenis.getValue();
        LocalDate tanggal  = dpTanggal.getValue();

        if (nama.isEmpty() || latin.isEmpty() || manfaat.isEmpty()
                || properti.isEmpty() || tanggal == null) {
            showAlert("Semua field harus diisi!"); return;
        }
        if (!nama.matches(REGEX_HURUF_SPASI)) {
            showAlert("Nama tanaman hanya boleh berisi huruf dan spasi!"); return;
        }
        if (!latin.matches(REGEX_HURUF_SPASI)) {
            showAlert("Nama Latin hanya boleh berisi huruf dan spasi!"); return;
        }
        if (!properti.matches(REGEX_HURUF_SPASI)) {
            showAlert("Field " + lblProperti.getText() + " hanya boleh berisi huruf dan spasi!"); return;
        }
        if (isDuplikatTanaman(selectedId, nama, latin, manfaat, jenis, properti, tanggal)) {
            showAlert("Data tanaman sudah ada! Tidak dapat menyimpan duplikat."); return;
        }

        String statusSaatIni = getStatusById(selectedId);
        StatusTanaman statusBaru;
        if ("SUDAH_DIPANEN".equals(statusSaatIni)) {
            statusBaru = StatusTanaman.SUDAH_DIPANEN;
        } else {
            statusBaru = Tanaman.hitungStatus(tanggal, getEstimasiByJenis(jenis));
        }

        Tanaman tanaman = buildTanamanBaru(nama, latin, manfaat, properti, jenis, statusBaru, tanggal);
        if (!tanaman.setNama(nama) || !tanaman.setNamaLatin(latin) || !tanaman.setManfaat(manfaat)) {
            showAlert("Data tanaman tidak valid!"); return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE tanaman "
                            + "SET nama=?, nama_latin=?, manfaat=?, jenis=?, "
                            + "    properti_tambahan=?, status=?, tanggal_tanam=? "
                            + "WHERE id=?");
            ps.setString(1, tanaman.getNama());
            ps.setString(2, tanaman.getNamaLatin());
            ps.setString(3, tanaman.getManfaat());
            ps.setString(4, tanaman.getJenis());
            ps.setString(5, tanaman.getPropertiTambahan());
            ps.setString(6, tanaman.getStatus().name());
            ps.setDate(7, Date.valueOf(tanaman.getTanggalTanam()));
            ps.setInt(8, selectedId);
            ps.executeUpdate();
            loadData();
            clearForm();
            showInfo("Tanaman berhasil diubah! Status: " + statusBaru.name());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleHapus() {
        if (selectedId == -1) { showAlert("Pilih tanaman terlebih dahulu!"); return; }

        Alert konfirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Yakin ingin menghapus tanaman ini?", ButtonType.YES, ButtonType.NO);
        konfirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DBConnection.getConnection()) {
                    PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM tanaman WHERE id=?");
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
        if (t instanceof ITanamanObat) {
            ITanamanObat obat = (ITanamanObat) t;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info Penggunaan Obat");
            alert.setHeaderText(row.getNama() + " (" + row.getJenis() + ")");
            alert.setContentText(
                    "Deskripsi  : " + obat.getDeskripsiObat()
                            + "\n\nCara Pakai : " + obat.getCaraPenggunaan());
            alert.showAndWait();
        }
    }

    @FXML
    public void handleEstimasiPanen() {
        if (selectedId == -1) { showAlert("Pilih tanaman terlebih dahulu!"); return; }
        TanamanRow row = tblTanaman.getSelectionModel().getSelectedItem();
        if (row == null) return;

        Tanaman t = buildTanamanFromRow(row);
        ChoiceDialog<String> dialog = new ChoiceDialog<>(
                "Konsumsi", "Konsumsi", "Obat", "Bibit");
        dialog.setTitle("Estimasi Masa Panen");
        dialog.setHeaderText("Pilih tujuan panen untuk " + row.getNama());
        dialog.setContentText("Tujuan:");
        dialog.showAndWait().ifPresent(tujuan -> {
            int hari  = t.estimasiHariPanen(tujuan.toLowerCase());
            int bulan = hari / 30;
            int sisa  = hari % 30;
            showInfo("Estimasi panen " + row.getNama() + " untuk tujuan " + tujuan
                    + ":\n" + hari + " hari (" + bulan + " bulan " + sisa + " hari)");
        });
    }

    private String getStatusById(int id) {
        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT status FROM tanaman WHERE id=?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("status");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean isDuplikatTanaman(int excludeId,
                                      String nama, String namaLatin, String manfaat,
                                      String jenis, String properti, LocalDate tanggal) {
        String sql =
                "SELECT COUNT(*) FROM tanaman "
                        + "WHERE LOWER(TRIM(nama))              = LOWER(TRIM(?)) "
                        + "  AND LOWER(TRIM(nama_latin))        = LOWER(TRIM(?)) "
                        + "  AND LOWER(TRIM(manfaat))           = LOWER(TRIM(?)) "
                        + "  AND jenis                          = ? "
                        + "  AND LOWER(TRIM(properti_tambahan)) = LOWER(TRIM(?)) "
                        + "  AND tanggal_tanam                  = ? "
                        + "  AND id                            != ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nama);
            ps.setString(2, namaLatin);
            ps.setString(3, manfaat);
            ps.setString(4, jenis);
            ps.setString(5, properti);
            ps.setDate(6, Date.valueOf(tanggal));
            ps.setInt(7, excludeId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void loadData() {
        ArrayList<Tanaman> listTanaman = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement()
                    .executeQuery("SELECT * FROM tanaman ORDER BY nama");
            while (rs.next()) {
                LocalDate tgl = rs.getDate("tanggal_tanam") != null
                        ? rs.getDate("tanggal_tanam").toLocalDate() : LocalDate.now();
                StatusTanaman st = StatusTanaman.valueOf(rs.getString("status"));
                String jenis     = rs.getString("jenis");
                String nama      = rs.getString("nama");
                String latin     = rs.getString("nama_latin");
                String manfaat   = rs.getString("manfaat");
                String properti  = rs.getString("properti_tambahan");

                // Polymorphism — variabel tipe Tanaman menampung subclass yang sesuai (Modul 5)
                Tanaman t;
                switch (jenis) {
                    case "Tanaman Rempah":
                        t = new TanamanRempah(nama, latin, manfaat, properti, st, tgl); break;
                    case "Tanaman Daun":
                        t = new TanamanDaun(nama, latin, manfaat, properti, st, tgl); break;
                    default:
                        t = new TanamanBuah(nama, latin, manfaat, properti, st, tgl); break;
                }
                t.setId(rs.getInt("id"));
                listTanaman.add(t);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        data.clear();
        for (Tanaman t : listTanaman) {
            data.add(new TanamanRow(
                    t.getId(),
                    t.getNama(),
                    t.getJenis(),
                    t.getNamaLatin(),
                    t.getManfaat(),
                    t.getPropertiTambahan(),
                    t.getStatus().name(),
                    t.getTanggalTanam()));
        }
        tblTanaman.setItems(data);
    }

    private Tanaman buildTanamanBaru(String nama, String latin, String manfaat,
                                     String properti, String jenis,
                                     StatusTanaman status, LocalDate tanggal) {
        switch (jenis) {
            case "Tanaman Rempah":
                return new TanamanRempah(nama, latin, manfaat, properti, status, tanggal);
            case "Tanaman Daun":
                return new TanamanDaun(nama, latin, manfaat, properti, status, tanggal);
            default:
                return new TanamanBuah(nama, latin, manfaat, properti, status, tanggal);
        }
    }

    private Tanaman buildTanamanFromRow(TanamanRow row) {
        LocalDate     tgl = row.getTanggalTanam() != null ? row.getTanggalTanam() : LocalDate.now();
        StatusTanaman st  = StatusTanaman.valueOf(row.getStatus());
        return buildTanamanBaru(row.getNama(), row.getNamaLatin(), row.getManfaat(),
                row.getProperti(), row.getJenis(), st, tgl);
    }

    private void clearForm() {
        tfNama.clear();
        tfNamaLatin.clear();
        taManfaat.clear();
        tfProperti.clear();
        cmbJenis.setValue("Tanaman Rempah");
        dpTanggal.setValue(LocalDate.now());
        selectedId = -1;
        updateStatusPreview();
    }

    private void showAlert(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }

    private void showInfo(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    public static class TanamanRow {
        private final int       id;
        private final String    nama;
        private final String    jenis;
        private final String    namaLatin;
        private final String    manfaat;
        private final String    properti;
        private final String    status;
        private final LocalDate tanggalTanam;

        public TanamanRow(int id, String nama, String jenis, String namaLatin,
                          String manfaat, String properti, String status,
                          LocalDate tanggalTanam) {
            this.id           = id;
            this.nama         = nama;
            this.jenis        = jenis;
            this.namaLatin    = namaLatin;
            this.manfaat      = manfaat;
            this.properti     = properti;
            this.status       = status;
            this.tanggalTanam = tanggalTanam;
        }

        public int       getId()           { return id; }
        public String    getNama()         { return nama; }
        public String    getJenis()        { return jenis; }
        public String    getNamaLatin()    { return namaLatin; }
        public String    getManfaat()      { return manfaat; }
        public String    getProperti()     { return properti; }
        public String    getStatus()       { return status; }
        public LocalDate getTanggalTanam() { return tanggalTanam; }
    }
}