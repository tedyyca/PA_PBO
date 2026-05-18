package com.toga.controller;

import com.toga.dto.TanamanDTO;
import com.toga.repository.impl.TanamanRepositoryImpl;
import com.toga.service.TanamanService;
import com.toga.service.impl.TanamanServiceImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.time.LocalDate;

public class TanamanController {
    @FXML private ComboBox<String> cmbJenis;
    @FXML private TextField tfNama;
    @FXML private TextField tfNamaLatin;
    @FXML private TextField tfProperti;
    @FXML private TextField tfEstimasiHari;
    @FXML private TextArea taManfaat;
    @FXML private DatePicker dpTanggal;
    @FXML private Label lblProperti;
    @FXML private Label lblStatusInfo;
    @FXML private TableView<TanamanDTO> tblTanaman;
    @FXML private TableColumn<TanamanDTO, String> colNama;
    @FXML private TableColumn<TanamanDTO, String> colJenis;
    @FXML private TableColumn<TanamanDTO, String> colNamaLatin;
    @FXML private TableColumn<TanamanDTO, Integer> colEstimasi;
    @FXML private TableColumn<TanamanDTO, String> colStatus;

    private final TanamanService tanamanService = new TanamanServiceImpl(new TanamanRepositoryImpl());
    private final ObservableList<TanamanDTO> data = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cmbJenis.setItems(FXCollections.observableArrayList("Tanaman Rempah", "Tanaman Daun", "Tanaman Buah"));
        cmbJenis.setValue("Tanaman Rempah");
        dpTanggal.setValue(LocalDate.now());
        cmbJenis.setOnAction(e -> updateLabelProperti());
        dpTanggal.valueProperty().addListener((obs, o, n) -> updateStatusPreview());

        tfNama.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && !newVal.matches("[a-zA-Z ]*")) tfNama.setText(oldVal);
        });
        tfNamaLatin.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && !newVal.matches("[a-zA-Z ]*")) tfNamaLatin.setText(oldVal);
        });
        tfProperti.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isEmpty() && !newVal.matches("[a-zA-Z ]*")) tfProperti.setText(oldVal);
        });
        tfEstimasiHari.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) tfEstimasiHari.setText(oldVal);
        });

        colNama.setCellValueFactory(new PropertyValueFactory<>("nama"));
        colJenis.setCellValueFactory(new PropertyValueFactory<>("jenis"));
        colNamaLatin.setCellValueFactory(new PropertyValueFactory<>("namaLatin"));
        colEstimasi.setCellValueFactory(new PropertyValueFactory<>("estimasiHari"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        updateStatusPreview();
        refreshTable();
    }

    @FXML
    public void handleTambah() {
        try {
            int estimasi = 0;
            try {
                estimasi = Integer.parseInt(tfEstimasiHari.getText().trim());
            } catch (NumberFormatException ignored) {}

            TanamanDTO dto = new TanamanDTO();
            dto.setId(-1);
            dto.setNama(tfNama.getText().trim());
            dto.setNamaLatin(tfNamaLatin.getText().trim());
            dto.setManfaat(taManfaat.getText().trim());
            dto.setPropertiTambahan(tfProperti.getText().trim());
            dto.setJenis(cmbJenis.getValue());
            dto.setTanggalTanam(dpTanggal.getValue());
            dto.setEstimasiHari(estimasi);

            tanamanService.tambahTanaman(dto);
            refreshTable();
            tfNama.clear(); tfNamaLatin.clear(); taManfaat.clear(); tfProperti.clear(); tfEstimasiHari.clear();
            cmbJenis.setValue("Tanaman Rempah"); dpTanggal.setValue(LocalDate.now());
            new Alert(Alert.AlertType.INFORMATION, "Tanaman berhasil ditambahkan!", ButtonType.OK).showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void handleUbah() {
        TanamanDTO selected = tblTanaman.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Pilih tanaman terlebih dahulu!", ButtonType.OK).showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/toga/view/FormUbahTanaman.fxml"));
            Parent root = loader.load();
            FormUbahTanamanController formController = loader.getController();
            formController.setInitData(selected, this);
            Stage stage = new Stage();
            stage.setTitle("Ubah Data Tanaman");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Gagal membuka form ubah: " + ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void handleHapus() {
        TanamanDTO selected = tblTanaman.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Pilih tanaman terlebih dahulu!", ButtonType.OK).showAndWait();
            return;
        }
        try {
            Alert konfirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin ingin menghapus tanaman ini?", ButtonType.YES, ButtonType.NO);
            konfirm.showAndWait().ifPresent(btn -> {
                if (btn == ButtonType.YES) {
                    tanamanService.hapusTanaman(selected.getId());
                    refreshTable();
                    new Alert(Alert.AlertType.INFORMATION, "Tanaman berhasil dihapus!", ButtonType.OK).showAndWait();
                }
            });
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void handleInfoObat() {
        TanamanDTO row = tblTanaman.getSelectionModel().getSelectedItem();
        if (row == null) {
            new Alert(Alert.AlertType.WARNING, "Pilih tanaman terlebih dahulu!", ButtonType.OK).showAndWait();
            return;
        }
        try {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Info Penggunaan Obat");
            alert.setHeaderText(row.getNama() + " (" + row.getJenis() + ")");
            alert.setContentText(tanamanService.getInfoObat(row));
            alert.showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void handleEstimasiPanen() {
        TanamanDTO row = tblTanaman.getSelectionModel().getSelectedItem();
        if (row == null) {
            new Alert(Alert.AlertType.WARNING, "Pilih tanaman terlebih dahulu!", ButtonType.OK).showAndWait();
            return;
        }
        try {
            new Alert(Alert.AlertType.INFORMATION, tanamanService.getEstimasiPanen(row), ButtonType.OK).showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.WARNING, ex.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    public void refreshTable() {
        try {
            data.clear();
            data.addAll(tanamanService.getAllTanaman());
            tblTanaman.setItems(data);
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Gagal memuat data tabel", ButtonType.OK).showAndWait();
        }
    }

    private void updateLabelProperti() {
        String jenis = cmbJenis.getValue();
        if ("Tanaman Rempah".equals(jenis)) lblProperti.setText("Aroma");
        else if ("Tanaman Daun".equals(jenis)) lblProperti.setText("Bentuk Daun");
        else lblProperti.setText("Musim Berbuah");
    }

    private void updateStatusPreview() {
        LocalDate tgl = dpTanggal.getValue();
        String estimasiStr = tfEstimasiHari != null ? tfEstimasiHari.getText() : "";
        if (tgl == null) return;
        int estimasi = 0;
        try { estimasi = Integer.parseInt(estimasiStr); } catch (NumberFormatException ignored) {}
        if (estimasi <= 0) { lblStatusInfo.setText("Isi estimasi hari untuk preview status"); return; }
        lblStatusInfo.setText("Status otomatis: " + com.toga.model.Tanaman.hitungStatus(tgl, estimasi).name());
    }
}