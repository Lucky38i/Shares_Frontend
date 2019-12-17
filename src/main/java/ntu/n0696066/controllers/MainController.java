package ntu.n0696066.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import ntu.n0696066.model.Shares;
import ntu.n0696066.model.User;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    @FXML
    private JFXProgressBar progressBar_Loading;
    @FXML
    private JFXComboBox<String> cmb_SearchShare;
    @FXML
    private TreeTableColumn<Shares, String> clm_CompanyName,  clm_CompanySymbol, clm_ShareCurrency;
    @FXML
    private TreeTableColumn<Shares, Long> clm_OwnedShares, clm_CurrentShare;
    @FXML
    private TreeTableColumn<Shares, Float> clm_ShareValue;
    @FXML
    private TreeTableColumn<Shares, LocalDate> clm_LastUpdate;
    @FXML
    private JFXTreeTableView<Shares> treeTblView_Dashboard;

    private String accessToken;
    private final OkHttpClient client = new OkHttpClient();
    private final String BASE_URL = "http://localhost:8080/api";
    private final String RED_STATUS_CSS = "../css/statusred.css";
    private final String GREEN_STATUS_CSS = "../css/statusgreen.css";


    @FXML
    public void initialize() {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
        // Setup Share columns
        clm_CompanyName.setCellValueFactory(param -> param.getValue().getValue().getCompanyName());
        clm_CompanySymbol.setCellValueFactory(param -> param.getValue().getValue().getCompanySymbol());
        clm_OwnedShares.setCellValueFactory(param -> param.getValue().getValue().getOwnedShares().asObject());

        // Setup Shareprice Column
        clm_ShareCurrency.setCellValueFactory(param -> param.getValue().getValue().getSharePrice().getCurrency());
        clm_CurrentShare.setCellValueFactory(param -> param.getValue().getValue()
                .getSharePrice().getCurrentShares().asObject());
        clm_ShareValue.setCellValueFactory(param -> param.getValue().getValue().getSharePrice().getValue().asObject());
        clm_LastUpdate.setCellValueFactory(param -> param.getValue().getValue().getSharePrice().getLastUpdate());

        JFXAutoCompletePopup<String> autoCompletePopup = new JFXAutoCompletePopup<>();

        autoCompletePopup.setSelectionHandler(event -> cmb_SearchShare.setValue(event.getObject()));

        TextField editor = cmb_SearchShare.getEditor();
        editor.textProperty().addListener(observable -> {

            if (editor.getLength() >= 3) {

            }

            autoCompletePopup.filter(item -> item.toLowerCase().contains(editor.getText()));

            if (autoCompletePopup.getFilteredSuggestions().isEmpty() || cmb_SearchShare.showingProperty().get()) {
                autoCompletePopup.hide();
            }
            else {
                autoCompletePopup.show(editor);
            }
        });
        cmb_SearchShare.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 3) {
                progressBar_Loading.setVisible(true);
                // Build GET call
                Task<Void> task = new Task<Void>() {
                    @Override
                    protected Void call() {
                        Request request = new Request.Builder()
                                .url(BASE_URL + "/shares/liststock?sharesymbol=" + newValue)
                                .addHeader("Authorization", "Bearer " + accessToken)
                                .build();
                        Call call = client.newCall(request);

                        try (Response response = call.execute()){
                            JsonNode responseNode = new ObjectMapper().readTree(
                                    Objects.requireNonNull(response.body()).string());

                            Platform.runLater(() -> {
                                progressBar_Loading.setVisible(false);
                                cmb_SearchShare.getItems().clear();
                                for (int i = 0; i < responseNode.path("bestMatches").size(); i++) {
                                    cmb_SearchShare.getItems().add(
                                            responseNode.path("bestMatches").get(i).get("1. symbol").textValue()
                                                    + "\t\t"
                                                    +  responseNode.path("bestMatches").get(i).get("2. name").textValue()
                                            );
                                }
                                cmb_SearchShare.show();
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        });
    }

    public void setUpScene(String token) {
        this.accessToken = token;

        // Delegate Rest Call to a separate thread
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                // Build GET call
                Request request = new Request.Builder()
                        .url(BASE_URL + "user/getusershares")
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();
                Call call = client.newCall(request);
                try (Response response = call.execute()){
                    User tempUser = new User();
                    if (response.code() == 200) {
                        new ObjectMapper().readValue(Objects.requireNonNull(response.body()).string(),
                                tempUser.getClass());

                        Platform.runLater(() -> {
                            final TreeItem<Shares> root = new RecursiveTreeItem<>(
                                    tempUser.getOwnedShares(), RecursiveTreeObject::getChildren);
                            treeTblView_Dashboard.setRoot(root);
                            treeTblView_Dashboard.setShowRoot(false);
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }
}
