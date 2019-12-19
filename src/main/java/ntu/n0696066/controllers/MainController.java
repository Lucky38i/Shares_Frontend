package ntu.n0696066.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jfoenix.controls.*;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import ntu.n0696066.model.Shares;
import ntu.n0696066.model.SharesRecursive;
import ntu.n0696066.model.User;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    @FXML
    private StackPane stackPane_Stocks, stackPane_Root;
    @FXML
    private JFXTabPane tabPane_Main;
    @FXML
    private Tab tab_Dashboard, tab_Stocks;
    @FXML
    private Pane pane_PurchaseShares, pane_SelectItem, pane_StockDetails, pane_SellShares;
    @FXML
    private Text lbl_CompanySymbol, lbl_CompanyName, lbl_ShareCurrency, lbl_ShareValue, lbl_ShareUpdate,
            lbl_OwnedShares, lbl_Equity, lbl_Buy_CompanySym, lbl_Sell_CompanySym;
    @FXML
    private JFXButton btn_GotoSell, btn_GotoBuy, btn_Buy_Confirm, btn_Sell_Confirm;
    @FXML
    private JFXComboBox<String> cmb_Sell_Currency, cmb_Buy_Currency;
    @FXML
    private JFXTextField txt_Sell_SharePrice, txt_Sell_SharesLeft, txt_Sell_NumofShares, txt_Buy_SharePrice,
            txt_Buy_Equity, txt_Buy_NumofShare, txt_SearchStock;
    @FXML
    private JFXProgressBar progressBar_Loading;
    @FXML
    private JFXComboBox<String> cmb_SearchShare;
    @FXML
    private TreeTableColumn<SharesRecursive, String> clm_CompanyName,  clm_CompanySymbol;
    @FXML
    private TreeTableColumn<SharesRecursive, Long> clm_OwnedShares;
    @FXML
    private JFXTreeTableView<SharesRecursive> treeTblView_Dashboard;
    private JFXAlert<String> alertDialog;
    private JFXDialogLayout dialogContent;

    private String accessToken;
    private final OkHttpClient client = new OkHttpClient();
    private final String BASE_URL = "http://localhost:8080/api";
    private final String RED_STATUS_CSS = "../css/statusred.css";
    private final String GREEN_STATUS_CSS = "../css/statusgreen.css";
    ObjectMapper mapper;


    @FXML
    public void initialize() {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
        // Setup Share columns
        clm_CompanyName.setCellValueFactory(param -> param.getValue().getValue().companyNameProperty());
        clm_CompanySymbol.setCellValueFactory(param -> param.getValue().getValue().companySymbolProperty());
        clm_OwnedShares.setCellValueFactory(param -> param.getValue().getValue().ownedSharesProperty().asObject());

        mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
    }

    /**
     * Called by the login controller, to retrieve the User's details and present it on the tree table
     * @param token The JWT token provided by the RESTFul Service
     */
    public void setUpScene(String token) {
        this.accessToken = token;

        alertDialog = new JFXAlert<>((Stage) stackPane_Root.getScene().getWindow());
        alertDialog.initModality(Modality.APPLICATION_MODAL);
        alertDialog.setOverlayClose(false);

        dialogContent = new JFXDialogLayout();
        JFXButton dialogButton = new JFXButton("Okay");
        dialogButton.setOnAction(event -> alertDialog.hideWithAnimation());
        dialogContent.setActions(dialogButton);
        alertDialog.setContent(dialogContent);

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
                        mapper.readValue(Objects.requireNonNull(response.body()).string(),
                                tempUser.getClass());

                        /*
                         * Adds all owned shares to the RecursiveTreeObject copy of the Shares model
                         * This is done as Jackson cannot de-serialize into ObservableLists while still
                         * being able to build the TreeTableView
                         */
                        for (Shares i : tempUser.getOwnedShares()) {
                            SharesRecursive tempRecursiveShare = new SharesRecursive();
                            tempRecursiveShare.setCompanyName(i.getCompanyName());
                            tempRecursiveShare.setCompanySymbol(i.getCompanySymbol());
                            tempRecursiveShare.setOwnedShares(i.getOwnedShares());
                            tempUser.getSharesRecursivesList().add(tempRecursiveShare);
                        }

                        Platform.runLater(() -> {
                            final TreeItem<SharesRecursive> root = new RecursiveTreeItem<>(
                                    tempUser.getSharesRecursivesList(), RecursiveTreeObject::getChildren);
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

    /**
     * Used to retrieve stock item from RESTFul WS
     */
    @FXML
    private void retrieveStock() {
        if (cmb_SearchShare.getValue() != null) {
            String shareSymbol = cmb_SearchShare.getValue().split("-")[0];
            progressBar_Loading.setVisible(true);
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/shares/retrievestock?sharesymbol=" + shareSymbol)
                            .addHeader("Authorization", "Bearer " + accessToken)
                            .build();
                    Call call = client.newCall(request);
                    try (Response response = call.execute()) {
                        switch (response.code()) {
                            case 408:   // REQUEST_TIMEOUT
                                progressBar_Loading.setVisible(false);
                                dialogContent.setBody(new Text("Request Timed Out"));
                                Platform.runLater(() -> alertDialog.showAndWait());
                                break;
                            case 406:   //NOT_ACCEPTABLE
                                progressBar_Loading.setVisible(false);
                                dialogContent.setBody(new Text("Malformed Share Symbol"));
                                Platform.runLater(() -> alertDialog.showAndWait());
                                break;
                            case 429: //TOO_MANY_REQUESTS
                                progressBar_Loading.setVisible(false);
                                dialogContent.setBody(new Text("API Limit Reached"));
                                Platform.runLater(() -> alertDialog.showAndWait());
                                break;
                            case 200:   //OK
                                Shares tempShare = mapper.readValue(
                                        Objects.requireNonNull(response.body()).string(),
                                        Shares.class);
                                Platform.runLater(() -> {
                                    // Populate Stock Details Fields
                                    progressBar_Loading.setVisible(false);
                                    lbl_CompanySymbol.setText(tempShare.getCompanySymbol());
                                    lbl_CompanyName.setText(tempShare.getCompanyName());
                                    lbl_ShareCurrency.setText(tempShare.getStock().getCurrency());
                                    lbl_ShareValue.setText(tempShare.getStock().getValue().toString());
                                    lbl_ShareUpdate.setText("Updated: "
                                            + tempShare.getStock().getLastUpdate().toString());
                                    lbl_OwnedShares.setText(tempShare.getOwnedShares().toString());
                                    lbl_Equity.setText(String.valueOf(tempShare.getOwnedShares()
                                            * tempShare.getStock().getValue()));
                                    //Check if the User owns shares in this stock
                                    if (tempShare.getOwnedShares() <= 0) btn_GotoSell.setDisable(true);

                                    // Switch tabs and pane and combo box
                                    tabPane_Main.getSelectionModel().select(tab_Stocks);
                                    pane_StockDetails.toFront();
                                });
                                break;
                        }
                    } catch (JsonProcessingException e) {
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

    /**
     * Used to search share symbol based on on context in the search bar.
     * Eg. "Amazon" Retrieves a list of stocks with amazon in the company name
     */
    @FXML
    private void searchStock() {
        if (txt_SearchStock.getText().length() >= 3) {
            progressBar_Loading.setVisible(true);
            // Build GET call
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() {
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/shares/liststock?sharesymbol=" + txt_SearchStock.getText())
                            .addHeader("Authorization", "Bearer " + accessToken)
                            .build();
                    Call call = client.newCall(request);

                    try (Response response = call.execute()){
                        JsonNode responseNode = mapper.readTree(
                                Objects.requireNonNull(response.body()).string());

                        Platform.runLater(() -> {
                            progressBar_Loading.setVisible(false);
                            cmb_SearchShare.getItems().clear();
                            for (int i = 0; i < responseNode.path("bestMatches").size(); i++) {
                                cmb_SearchShare.getItems().add(
                                        responseNode.path("bestMatches").get(i).get("1. symbol").textValue()
                                                + " - "
                                                +  responseNode.path("bestMatches").get(i).get("2. name").textValue()
                                );
                            }
                            txt_SearchStock.setText("");
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
    }
}
