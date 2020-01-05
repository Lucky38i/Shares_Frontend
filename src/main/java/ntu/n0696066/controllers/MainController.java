package ntu.n0696066.controllers;

import animatefx.animation.FadeOut;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import okhttp3.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController {

    @FXML
    private JFXSlider slider_Sell_NumOfShares;
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
            lbl_OwnedShares, lbl_Equity, lbl_Buy_CompanySym, lbl_Sell_CompanySym, lbl_Welcome;
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

    private String accessToken, classJSON;
    private final OkHttpClient client = new OkHttpClient();
    private final String BASE_URL = "http://localhost:8080/api/";
    ObjectMapper mapper;
    private Shares tempShare;
    private User tempUser;
    DecimalFormat formatter = new DecimalFormat();
    ExecutorService executor;
    HashMap currencyRates;

    /*
     * Adds all owned shares to the RecursiveTreeObject copy of the Shares model
     * This is done as Jackson cannot de-serialize into ObservableLists while still
     * being able to build the TreeTableView
     */
    private void copyShareToRecursiveTreeShare() {

        for (Shares i : tempUser.getOwnedShares()) {
            SharesRecursive tempRecursiveShare = new SharesRecursive();
            tempRecursiveShare.setCompanyName(i.getCompanyName());
            tempRecursiveShare.setCompanySymbol(i.getCompanySymbol());
            tempRecursiveShare.setOwnedShares(i.getOwnedShares());
            tempUser.getSharesRecursivesList().add(tempRecursiveShare);
        }
    }

    /**
     * Used to updates shares
     * @param modifier The API call
     * @return Returns the task that carries out the API call then updates the UI
     */
    private Task<Integer> updateShares(String modifier) {
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                int responseCode;
                classJSON = mapper.writeValueAsString(tempShare);
                RequestBody body = RequestBody.create(classJSON, MediaType.parse("application/json; charset=utf-8"));

                Request request = new Request.Builder()
                        .url(BASE_URL + "shares/" + modifier)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .post(body)
                        .build();
                Call call = client.newCall(request);
                try (Response response = call.execute()) {
                    responseCode = response.code();
                    updateMessage(Objects.requireNonNull(response.body()).string());
                }
                return responseCode;
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() == 200) {
                try {
                    tempUser = mapper.readValue(task.getMessage(), User.class);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                copyShareToRecursiveTreeShare();
                Platform.runLater(() -> {
                    progressBar_Loading.setVisible(false);
                    final TreeItem<SharesRecursive> root = new RecursiveTreeItem<>(
                            tempUser.getSharesRecursivesList(), RecursiveTreeObject::getChildren);
                    treeTblView_Dashboard.setRoot(root);
                    treeTblView_Dashboard.setShowRoot(false);
                    tabPane_Main.getSelectionModel().select(tab_Dashboard);
                    pane_SelectItem.toFront();
                });
            }
        });
        return task;
    }

    /**
     * Used to retrieve stock item from RESTful WS as-well as Currency Rates
     * @param shareSymbol The share symbol used to find the stock item
     */
    private void retrieveStock(String shareSymbol) {
        progressBar_Loading.setVisible(true);
        Task<Integer> currencyRetrievalTask = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                int code;
                Request request = new Request.Builder()
                        .url(BASE_URL + "currency/currencyrates?base=" + lbl_ShareCurrency.getText())
                        .build();
                Call call = client.newCall(request);
                try (Response response = call.execute()) {
                    updateMessage(Objects.requireNonNull(response.body()).string());
                    code = response.code();
                }
                assert(code != 0);
                return code;
            }
        };
        currencyRetrievalTask.setOnSucceeded(event -> {
            switch (currencyRetrievalTask.getValue()) {
                case 404: //NOT FOUND
                    //TODO
                    System.out.println("404");
                    break;
                case 408: //REQUEST TIMEOUT
                    //TODO
                    break;
                case 200: //OK
                    try {
                        JsonNode apiResults = mapper.readValue(currencyRetrievalTask.getMessage(), JsonNode.class);
                        currencyRates = mapper.convertValue(apiResults.get("rates"), HashMap.class);
                        Platform.runLater(()-> {
                            cmb_Buy_Currency.getItems().clear();
                            cmb_Sell_Currency.getItems().clear();

                            @SuppressWarnings("unchecked") //No need for checking as keyset is string
                            List<String> currencyList = new ArrayList<String>(currencyRates.keySet());
                            Collections.sort(currencyList);

                            cmb_Sell_Currency.getItems().addAll(currencyList);
                            cmb_Buy_Currency.getItems().addAll(currencyList);
                        });
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        });
        Task<Integer> stockRetrievalTask = new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {

                int code;
                Request request = new Request.Builder()
                        .url(BASE_URL + "shares/retrievestock?sharesymbol=" + shareSymbol)
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();
                Call call = client.newCall(request);
                try (Response response = call.execute()) {
                    updateMessage(Objects.requireNonNull(response.body()).string());
                    code = response.code();
                }
                return code;
            }
        };
        stockRetrievalTask.setOnSucceeded(event -> {
            switch (stockRetrievalTask.getValue()) {
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
                    try {
                        tempShare = mapper.readValue(stockRetrievalTask.getMessage(), Shares.class);
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
                            lbl_Equity.setText(String.valueOf(formatter.format(tempShare.getOwnedShares()
                                    * tempShare.getStock().getValue())));
                            //Check if the User owns shares in this stock
                            if (tempShare.getOwnedShares() <= 0) btn_GotoSell.setDisable(true);
                            else btn_GotoSell.setDisable(false);

                            // Switch tabs and pane and combo box
                            tabPane_Main.getSelectionModel().select(tab_Stocks);
                            executor.execute(currencyRetrievalTask);
                            pane_StockDetails.toFront();
                        });
                    }
                    catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        });
        executor.execute(stockRetrievalTask);
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
        Task<Integer> task = new Task<Integer>() {
            @Override
            protected Integer call() {
                int responseCode = 0;
                // Build GET call
                Request request = new Request.Builder()
                        .url(BASE_URL + "user/getusershares")
                        .addHeader("Authorization", "Bearer " + accessToken)
                        .build();
                Call call = client.newCall(request);
                try (Response response = call.execute()){
                    responseCode = response.code();
                    updateMessage(Objects.requireNonNull(response.body()).string());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return responseCode;
            }
        };
        task.setOnSucceeded(event -> {
            if (task.getValue() == 200) {
                try {
                    tempUser = mapper.readValue(task.getMessage(), User.class);
                    copyShareToRecursiveTreeShare();
                    lbl_Welcome.setText("Welcome " + tempUser.getUsername());

                    Platform.runLater(() -> {
                        final TreeItem<SharesRecursive> root = new RecursiveTreeItem<>(
                                tempUser.getSharesRecursivesList(), RecursiveTreeObject::getChildren);
                        treeTblView_Dashboard.setRoot(root);
                        treeTblView_Dashboard.setShowRoot(false);
                    });
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        });
        executor.execute(task);
    }


    @FXML
    public void initialize() {
        Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
        // Setup Share columns
        clm_CompanyName.setCellValueFactory(param -> param.getValue().getValue().companyNameProperty());
        clm_CompanySymbol.setCellValueFactory(param -> param.getValue().getValue().companySymbolProperty());
        clm_OwnedShares.setCellValueFactory(param -> param.getValue().getValue().ownedSharesProperty().asObject());

        // Attribute Instantiation
        formatter.setMaximumFractionDigits(2);
        mapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .build();
        executor = Executors.newCachedThreadPool();

        // Event & Listeners
        treeTblView_Dashboard.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                retrieveStock(treeTblView_Dashboard.getSelectionModel().getSelectedItem().getValue().getCompanySymbol());
            }
        });

        txt_Buy_NumofShare.textProperty().addListener(((observable, oldValue, newValue) -> {
            boolean parseable = false;
            double marketPrice = 0;
            int numOfShares = 0;
            Double tempRate;

            if (cmb_Buy_Currency.getValue() == null) {
                tempRate = (Double) currencyRates.get(tempShare.getStock().getCurrency());
            } else {
                tempRate = (Double) currencyRates.get(cmb_Buy_Currency.getValue());
            }

            try {

                marketPrice = tempRate * tempShare.getStock().getValue();
                numOfShares = Integer.parseInt(newValue);
                parseable = true;
            } catch (NumberFormatException ignored) {}
            if (parseable) {
                txt_Buy_Equity.setText(String.valueOf(formatter.format( marketPrice * numOfShares)));
            }
        }));
    }

    @FXML
    private void closeWindow() {
        FadeOut exit = new FadeOut(stackPane_Root);
        exit.setOnFinished(exitEvent -> System.exit(0));
        exit.play();
    }

    @FXML
    private void minimizeWindow() {
        ((Stage) stackPane_Root.getScene().getWindow()).setIconified(true);
    }

    @FXML
    private void updateBuyPrice() {
        Double tempRate;
        if (cmb_Buy_Currency.getValue() == null ) {
            tempRate = (Double) currencyRates.get(tempShare.getStock().getCurrency());
        } else tempRate = (Double) currencyRates.get(cmb_Buy_Currency.getValue());
        Platform.runLater(()-> {
            txt_Buy_SharePrice.setText(String.valueOf(
                formatter.format(tempRate * tempShare.getStock().getValue())));

            Double tempSharePrice = tempRate * tempShare.getStock().getValue();
            Integer tempNomOfShares = Integer.valueOf(txt_Buy_NumofShare.getText());
            txt_Buy_Equity.setText(String.valueOf(formatter.format(tempSharePrice * tempNomOfShares)));});
    }

    @FXML
    private void updateSellPrice() {
        Double tempRate;
        if (cmb_Sell_Currency.getValue() == null ) {
            tempRate = (Double) currencyRates.get(tempShare.getStock().getCurrency());
        } else tempRate = (Double) currencyRates.get(cmb_Sell_Currency.getValue());

        Platform.runLater(() -> txt_Sell_SharePrice.setText(String.valueOf(
                formatter.format(tempRate * tempShare.getStock().getValue()))));
    }

    /**
     * Returns to the stock details pane
     * //TODO Introduce Animations
     */
    @FXML void returnToStockDetails() {
        pane_StockDetails.toFront();
    }

    /**
     * Switch Pane to Buy Pane
     * //TODO Introduce Animations
     */
    @FXML
    private void goToBuy() {
        pane_PurchaseShares.toFront();
        txt_Buy_SharePrice.setText(formatter.format(tempShare.getStock().getValue()));
        cmb_Buy_Currency.setPromptText(tempShare.getStock().getCurrency());
        lbl_Buy_CompanySym.setText("Buy " + tempShare.getCompanySymbol());
    }

    /**
     * Switch pane to Sell Pane
     * //TODO Introduce Animations
     */
    @FXML
    private void goToSell() {
        pane_SellShares.toFront();
        slider_Sell_NumOfShares.setMax(Double.valueOf(tempShare.getOwnedShares()));
        slider_Sell_NumOfShares.setValue(0);
        lbl_Sell_CompanySym.setText("Sell " + tempShare.getCompanyName());
        txt_Sell_SharePrice.setText(tempShare.getStock().getValue().toString());
        cmb_Sell_Currency.setPromptText(tempShare.getStock().getCurrency());
    }

    /**
     * Sell shares in a selected stock
     */
    @FXML
    private void sellShares() {
        progressBar_Loading.setVisible(true);
        if (slider_Sell_NumOfShares.getValue() > 0) {
            tempShare.setOwnedShares((long) (tempShare.getOwnedShares() - slider_Sell_NumOfShares.getValue()));
            executor.execute(updateShares("sellshare"));
        }
    }

    /**
     * Buy shares in the specified Stock
     */
    @FXML
    private void buyShares() {
        try {
            progressBar_Loading.setVisible(true);
            if (Integer.parseInt(txt_Buy_NumofShare.getText()) > 0) {
                tempShare.setOwnedShares(Long.parseLong(txt_Buy_NumofShare.getText()));
                executor.execute(updateShares("buyshare"));
            }
        } catch (NumberFormatException e) {
            // TODO print this in a dialog
            System.out.println("Malformed Number");
        }

    }

    /**
     * Set the share symbol to retrieve from the RESTFul API
     */
    @FXML
    private void setSearchSymbol() {
        if (cmb_SearchShare.getValue() != null) {
            retrieveStock(cmb_SearchShare.getValue().split("-")[0]);
        }
    }

    /**
     * Used to search share symbol based on on context in the search bar.
     * Eg. "Amazon" Retrieves a list of stocks with amazon in the company name
     */
    @FXML
    private void listStockItems() {
        if (txt_SearchStock.getText().length() >= 3) {
            progressBar_Loading.setVisible(true);

            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() {
                    Request request = new Request.Builder()
                            .url(BASE_URL + "shares/liststock?sharesymbol=" + txt_SearchStock.getText())
                            .addHeader("Authorization", "Bearer " + accessToken)
                            .build();
                    Call call = client.newCall(request);

                    try (Response response = call.execute()){
                        updateMessage(Objects.requireNonNull(response.body()).string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                JsonNode responseNode = null;
                try {
                    responseNode = mapper.readTree(task.getMessage());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
                assert responseNode != null;
                cmb_SearchShare.getItems().clear();
                for (int i = 0; i < responseNode.path("bestMatches").size(); i++) {
                    cmb_SearchShare.getItems().add(
                            responseNode.path("bestMatches").get(i).get("1. symbol").textValue()
                                    + " - "
                                    + responseNode.path("bestMatches").get(i).get("2. name").textValue()
                    );
                }
                Platform.runLater(() -> {
                            progressBar_Loading.setVisible(false);
                            txt_SearchStock.setText("");
                            cmb_SearchShare.show();
                });
            });
            executor.execute(task);
        }
    }
}
