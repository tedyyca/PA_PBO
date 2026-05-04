package com.toga.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML private StackPane contentArea;

    @FXML
    public void initialize() {
        loadPage("DashboardView.fxml");
    }

    @FXML
    public void showDashboard() { loadPage("DashboardView.fxml"); }

    @FXML
    public void showTanaman() { loadPage("TanamanView.fxml"); }

    @FXML
    public void showPengguna() { loadPage("PenggunaView.fxml"); }

    @FXML
    public void showPerawatan() { loadPage("PerawatanView.fxml"); }

    @FXML
    public void showPanen() { loadPage("PanenView.fxml"); }

    @FXML
    public void showLaporan() { loadPage("LaporanView.fxml"); }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/toga/view/" + fxmlFile));
            javafx.scene.Node node = loader.load();
            contentArea.getChildren().setAll(node);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}