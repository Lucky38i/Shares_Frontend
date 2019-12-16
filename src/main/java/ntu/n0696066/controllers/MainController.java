package ntu.n0696066.controllers;

import animatefx.animation.FadeIn;
import animatefx.animation.ZoomOut;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.util.Callback;
import javafx.util.Duration;
import ntu.n0696066.model.Shares;
import ntu.n0696066.model.User;
import ntu.n0696066.tools.DialogHandler;
import okhttp3.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Objects;

public class MainController {

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
    }

    public void setUpScene(String token) {
        this.accessToken = token;

        // Delegate Rest Call to a separate thread
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    // Build GET call
                    Request request = new Request.Builder()
                            .url(BASE_URL + "user/getusershares")
                            .addHeader("Authorization", "Bearer " + accessToken)
                            .build();
                    Call call = client.newCall(request);
                    Response response = call.execute();
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


    @FXML
    private void SearchShares(){

    }


}
