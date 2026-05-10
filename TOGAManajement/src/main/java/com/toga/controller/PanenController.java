package com.toga.controller;

import com.toga.model.StatusTanaman;
import com.toga.model.Tanaman;
import com.toga.util.DBConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
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
import java.util.HashMap;

public class PanenController {

    @FXML private ComboBox<String> cmbTanaman;
    @FXML private ComboBox<String> cmbPengguna;
    @FXML private DatePicker       dpTanggalPanen;
    @FXML private TextField        tfHasilPanen;
    @FXML private TextArea         taKeterangan;

    @FXML private TableView<PanenRow>              tblPanen;
    @FXML private TableColumn<PanenRow, String>    colTanaman;
    @FXML private TableColumn<PanenRow, String>    colPengguna;
    @FXML private TableColumn<PanenRow, String>    colTanggal;
    @FXML private TableColumn<PanenRow, String>    colHasil;
    @FXML private TableColumn<PanenRow, String>    colKeterangan;

    private final ObservableList<PanenRow> data = FXCollections.observableArrayList();
    private int selectedId = -1;

    private final HashMap<String, Integer> tanamanMap  = new HashMap<>();
    private final HashMap<String, Integer> penggunaMap = new HashMap<>();

    // Menyimpan data tanaman untuk keperluan recalc status saat panen dihapus
    private final HashMap<Integer, TanamanInfo> tanamanInfoMap = new HashMap<>();

    private static final String REGEX_HASIL = "[a-zA-Z0-9, ]+";

    @FXML
    public void initialize() {
        dpTanggalPanen.setValue(LocalDate.now());

        colTanaman.setCellValueFactory(new PropertyValueFactory<>("namaTanaman"));
        colPengguna.setCellValueFactory(new PropertyValueFactory<>("namaPengguna"));
        colTanggal.setCellValueFactory(new PropertyValueFactory<>("tanggalPanen"));
        colHasil.setCellValueFactory(new PropertyValueFactory<>("hasilPanen"));
        colKeterangan.setCellValueFactory(new PropertyValueFactory<>("keterangan"));

        tfHasilPanen.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && !newVal.matches("[a-zA-Z0-9, ]*")) {
                tfHasilPanen.setText(oldVal);
            }
        });

        tblPanen.setOnMouseClicked(e -> {
            PanenRow row = tblPanen.getSelectionModel().getSelectedItem();
            if (row != null) {
                selectedId = row.getId();
                cmbTanaman.setValue(row.getNamaTanaman());
                cmbPengguna.setValue(row.getNamaPengguna());
                // Set tanggal dari data yang dipilih, bukan default hari ini
                dpTanggalPanen.setValue(LocalDate.parse(row.getTanggalPanen()));
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
        tanamanInfoMap.clear();
        ObservableList<String> tanamanList  = FXCollections.observableArrayList();
        ObservableList<String> penggunaList = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs1 = conn.createStatement()
                    .executeQuery("SELECT id, nama, jenis, tanggal_tanam FROM tanaman ORDER BY nama");
            while (rs1.next()) {
                String nama = rs1.getString("nama");
                int    id   = rs1.getInt("id");
                tanamanMap.put(nama, id);
                tanamanList.add(nama);

                // Simpan info untuk keperluan recalc status
                LocalDate tgl = rs1.getDate("tanggal_tanam") != null
                        ? rs1.getDate("tanggal_tanam").toLocalDate() : null;
                tanamanInfoMap.put(id, new TanamanInfo(rs1.getString("jenis"), tgl));
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
        cmbPengguna.setItems(penggunaList);
        if (!tanamanList.isEmpty())  cmbTanaman.setValue(tanamanList.get(0));
        if (!penggunaList.isEmpty()) cmbPengguna.setValue(penggunaList.get(0));
    }

    @FXML
    public void handleCatat() {
        String    namaTanaman  = cmbTanaman.getValue();
        String    namaPengguna = cmbPengguna.getValue();
        LocalDate tanggal      = dpTanggalPanen.getValue();
        String    hasil        = tfHasilPanen.getText().trim();
        String    keterangan   = taKeterangan.getText().trim();

        if (namaTanaman == null || namaPengguna == null || tanggal == null
                || hasil.isEmpty() || keterangan.isEmpty()) {
            showAlert("Semua field harus diisi!"); return;
        }
        if (!hasil.matches(REGEX_HASIL)) {
            showAlert("Hasil panen hanya boleh berisi huruf, angka, koma, dan spasi!"); return;
        }

        int tanamanId  = tanamanMap.getOrDefault(namaTanaman, -1);
        int penggunaId = penggunaMap.getOrDefault(namaPengguna, -1);
        if (tanamanId == -1 || penggunaId == -1) {
            showAlert("Data tidak valid!"); return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO catatan_panen "
                            + "(tanaman_id, pengguna_id, keterangan, tanggal_panen, hasil_panen) "
                            + "VALUES (?,?,?,?,?)");
            ps.setInt(1, tanamanId);
            ps.setInt(2, penggunaId);
            ps.setString(3, keterangan);
            ps.setDate(4, Date.valueOf(tanggal));
            ps.setString(5, hasil);
            ps.executeUpdate();

            // Set status SUDAH_DIPANEN — satu-satunya tempat status ini diset
            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE tanaman SET status='SUDAH_DIPANEN' WHERE id=?");
            ps2.setInt(1, tanamanId);
            ps2.executeUpdate();

            loadData();
            loadCombo();
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
                "Yakin ingin menghapus catatan panen ini?\n" +
                        "Status tanaman akan dikembalikan ke kalkulasi otomatis.",
                ButtonType.YES, ButtonType.NO);
        konfirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                try (Connection conn = DBConnection.getConnection()) {
                    // Ambil tanaman_id dari catatan yang akan dihapus
                    PreparedStatement psGet = conn.prepareStatement(
                            "SELECT tanaman_id FROM catatan_panen WHERE id=?");
                    psGet.setInt(1, selectedId);
                    ResultSet rs = psGet.executeQuery();
                    int tanamanId = -1;
                    if (rs.next()) tanamanId = rs.getInt("tanaman_id");

                    // Hapus catatan panen
                    PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM catatan_panen WHERE id=?");
                    ps.setInt(1, selectedId);
                    ps.executeUpdate();

                    // Recalculate status tanaman setelah panen dihapus
                    if (tanamanId != -1) {
                        recalcStatusTanaman(conn, tanamanId);
                    }

                    loadData();
                    loadCombo();
                    clearForm();
                    showInfo("Catatan panen berhasil dihapus!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Recalculate dan update status tanaman setelah catatan panen dihapus.
     * Menggunakan Tanaman.hitungStatus() agar konsisten dengan TanamanController.
     */
    private void recalcStatusTanaman(Connection conn, int tanamanId) throws Exception {
        PreparedStatement psInfo = conn.prepareStatement(
                "SELECT jenis, tanggal_tanam FROM tanaman WHERE id=?");
        psInfo.setInt(1, tanamanId);
        ResultSet rsInfo = psInfo.executeQuery();
        if (!rsInfo.next()) return;

        String    jenis    = rsInfo.getString("jenis");
        LocalDate tglTanam = rsInfo.getDate("tanggal_tanam") != null
                ? rsInfo.getDate("tanggal_tanam").toLocalDate() : null;

        int estimasi;
        switch (jenis) {
            case "Tanaman Rempah": estimasi = 240; break;
            case "Tanaman Daun":   estimasi = 60;  break;
            default:               estimasi = 180; break;
        }

        StatusTanaman statusBaru = Tanaman.hitungStatus(tglTanam, estimasi);

        PreparedStatement psUpdate = conn.prepareStatement(
                "UPDATE tanaman SET status=? WHERE id=?");
        psUpdate.setString(1, statusBaru.name());
        psUpdate.setInt(2, tanamanId);
        psUpdate.executeUpdate();
    }

    private void loadData() {
        data.clear();
        try (Connection conn = DBConnection.getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT cp.id, t.nama AS nama_tanaman, p.nama AS nama_pengguna, "
                            + "       cp.tanggal_panen, cp.hasil_panen, cp.keterangan "
                            + "FROM catatan_panen cp "
                            + "JOIN tanaman  t ON cp.tanaman_id  = t.id "
                            + "JOIN pengguna p ON cp.pengguna_id = p.id "
                            + "ORDER BY cp.tanggal_panen DESC");
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

    /** Helper class untuk menyimpan info tanaman saat recalc status */
    private static class TanamanInfo {
        final String    jenis;
        final LocalDate tanggalTanam;
        TanamanInfo(String jenis, LocalDate tanggalTanam) {
            this.jenis       = jenis;
            this.tanggalTanam = tanggalTanam;
        }
    }

    public static class PanenRow {
        private final int    id;
        private final String namaTanaman;
        private final String namaPengguna;
        private final String tanggalPanen;
        private final String hasilPanen;
        private final String keterangan;

        public PanenRow(int id, String namaTanaman, String namaPengguna,
                        String tanggalPanen, String hasilPanen, String keterangan) {
            this.id           = id;
            this.namaTanaman  = namaTanaman;
            this.namaPengguna = namaPengguna;
            this.tanggalPanen = tanggalPanen;
            this.hasilPanen   = hasilPanen;
            this.keterangan   = keterangan;
        }

        public int    getId()            { return id; }
        public String getNamaTanaman()   { return namaTanaman; }
        public String getNamaPengguna()  { return namaPengguna; }
        public String getTanggalPanen()  { return tanggalPanen; }
        public String getHasilPanen()    { return hasilPanen; }
        public String getKeterangan()    { return keterangan; }
    }
}