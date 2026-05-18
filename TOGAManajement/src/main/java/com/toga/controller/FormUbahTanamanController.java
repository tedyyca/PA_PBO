package com.toga.controller;

import com.toga.dto.TanamanDTO;
import com.toga.repository.impl.TanamanRepositoryImpl;
import com.toga.service.TanamanService;
import com.toga.service.impl.TanamanServiceImpl;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FormUbahTanamanController {
    @FXML private ComboBox<String> cmbJenis;
    @FXML private TextField tfNama;
    @FXML private TextField tfNamaLatin;
    @FXML private TextField tfProperti;
    @FXML private TextField tfEstimasiHari;
    @FXML private TextArea taManfaat;
    @FXML private DatePicker dpTanggal;
    @FXML private Label lblProperti;

    private TanamanDTO currentDto;
    private TanamanController parentController;
    private final TanamanService service = new TanamanServiceImpl(new TanamanRepositoryImpl());

    @FXML
    public void initialize() {
        cmbJenis.setItems(FXCollections.observableArrayList("Tanaman Rempah", "Tanaman Daun", "Tanaman Buah"));
        cmbJenis.setOnAction(e -> updateLabelProperti());

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
    }

    public void setInitData(TanamanDTO dto, TanamanController parent) {
        this.currentDto = dto;
        this.parentController = parent;
        tfNama.setText(dto.getNama());
        tfNamaLatin.setText(dto.getNamaLatin());
        taManfaat.setText(dto.getManfaat());
        tfProperti.setText(dto.getPropertiTambahan());
        cmbJenis.setValue(dto.getJenis());
        dpTanggal.setValue(dto.getTanggalTanam());
        tfEstimasiHari.setText(String.valueOf(dto.getEstimasiHari()));
        updateLabelProperti();
    }

    @FXML
    public void simpanUbah() {
        try {
            int estimasi = 0;
            try {
                estimasi = Integer.parseInt(tfEstimasiHari.getText().trim());
            } catch (NumberFormatException ignored) {}

            TanamanDTO dtoBaru = new TanamanDTO();
            dtoBaru.setId(currentDto.getId());
            dtoBaru.setNama(tfNama.getText().trim());
            dtoBaru.setNamaLatin(tfNamaLatin.getText().trim());
            dtoBaru.setManfaat(taManfaat.getText().trim());
            dtoBaru.setPropertiTambahan(tfProperti.getText().trim());
            dtoBaru.setJenis(cmbJenis.getValue());
            dtoBaru.setTanggalTanam(dpTanggal.getValue());
            dtoBaru.setEstimasiHari(estimasi);

            service.ubahTanaman(dtoBaru);
            new Alert(Alert.AlertType.INFORMATION, "Data berhasil diubah!", ButtonType.OK).showAndWait();
            parentController.refreshTable();
            tutup();
        } catch (IllegalArgumentException e) {
            new Alert(Alert.AlertType.WARNING, e.getMessage(), ButtonType.OK).showAndWait();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Terjadi kesalahan: " + e.getMessage(), ButtonType.OK).showAndWait();
        }
    }

    @FXML
    public void tutup() {
        Stage stage = (Stage) tfNama.getScene().getWindow();
        stage.close();
    }

    private void updateLabelProperti() {
        String jenis = cmbJenis.getValue();
        if ("Tanaman Rempah".equals(jenis)) lblProperti.setText("Aroma");
        else if ("Tanaman Daun".equals(jenis)) lblProperti.setText("Bentuk Daun");
        else lblProperti.setText("Musim Berbuah");
    }
}