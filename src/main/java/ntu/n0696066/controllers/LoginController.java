package ntu.n0696066.controllers;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import animatefx.animation.ZoomIn;
import animatefx.animation.ZoomOut;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTextField;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import ntu.n0696066.payloads.LoginRequest;
import ntu.n0696066.payloads.SignUpRequest;
import ntu.n0696066.tools.DialogHandler;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginController {

    @FXML
    private TextArea txtLoginInfo, txtRegisterInfo;
    @FXML
    private JFXSpinner spin_Loading;
    @FXML
    private Pane paneLogin, paneRegister;
    @FXML
    private StackPane stackRoot;
    @FXML
    private JFXTextField txtUsername, txtRegisterUsername;
    @FXML
    private JFXPasswordField txtPassword, txtRegisterPassword;

    private ObjectMapper mapper;
    private final OkHttpClient client = new OkHttpClient();
    private final String BASE_URL = "http://localhost:8080/api";
    final String RED_STATUS_CSS = "../css/statusred.css";
    final String GREEN_STATUS_CSS = "../css/statusgreen.css";
    final String MAIN_WINDOW = "../view/MainWindow.fxml";
    private ExecutorService executor;

    @FXML
    public void initialize() {

        mapper = JsonMapper.builder().build();
        executor = Executors.newCachedThreadPool();

        Tooltip passwordTip = new Tooltip("Enter a password");
        txtRegisterPassword.setTooltip(passwordTip);

        txtRegisterPassword.setOnKeyTyped(event -> {
            if (txtRegisterPassword.getText().length() < 6) {
                passwordTip.setText("Your password needs to be more than 6 characters");
                txtRegisterPassword.setStyle("-fx-text-inner-color:red;" +
                        " -fx-prompt-text-fill: red;" +
                        " -jfx-focus-color: red");
            }
            else if (txtRegisterPassword.getText().length() > 6) passwordTip.setText("Valid password");
            else {
                txtRegisterPassword.setStyle("-fx-text-inner-color:#094270;" +
                        " -fx-prompt-text-fill:#094270;" +
                        "-jfx-focus-color: #ed0362");
            }
        });
    }

    @FXML
    private void CloseWindow() {
        FadeOut exit = new FadeOut(stackRoot);
        exit.setOnFinished(exitEvent -> System.exit(0));
        exit.play();
    }

    @FXML
    private void MinimizeWindow() {
        ((Stage) stackRoot.getScene().getWindow()).setIconified(true);
    }


    @FXML
    private void ReturnToLogin() {
        ZoomOut tempAnimation = new ZoomOut(paneRegister);
        tempAnimation.setOnFinished(exitEvent -> paneLogin.toFront());
        tempAnimation.play();
        txtRegisterInfo.setMaxHeight(0);
    }

    @FXML
    private void SwitchToSignUp() {
        new ZoomIn(paneRegister).play();
        paneRegister.toFront();
    }

    /**
     * Used to register a new user.
     * Upon success the user is sent back to the login screen
     * Upon failure the screen
     */
    @FXML
    private void RegisterUser()  {
        // Check password field complies with model rules
        if (txtRegisterPassword.getLength() < 6) {
            txtRegisterPassword.setText("");
            DialogHandler.handleInfo(txtRegisterInfo,
                    RED_STATUS_CSS,
                    "password requires at least 6 characters",
                    3);
        } else if (txtRegisterPassword.getText().isEmpty()) {
            DialogHandler.handleInfo(txtRegisterInfo,
                    RED_STATUS_CSS,
                    "Please enter a username",
                    3);
        } else {
            SignUpRequest tempUser = new SignUpRequest();
            tempUser.setUsername(txtRegisterUsername.getText());
            tempUser.setPassword(txtRegisterPassword.getText());
            spin_Loading.setVisible(true);

            // Delegate Rest Call to a separate thread
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws JsonProcessingException {
                    String node = mapper.writeValueAsString(tempUser);
                    RequestBody body = RequestBody.create(node, MediaType.parse("application/json; charset=utf-8"));

                    // Build POST call
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/auth/register")
                            .post(body)
                            .build();
                    Call call = client.newCall(request);
                    try (Response response = call.execute()) {
                        updateMessage(Objects.requireNonNull(response.body()).string());
                    } catch (IOException e) {
                        updateMessage("Server down, try again later");
                    }
                    return null;
                }
            };
            task.setOnSucceeded(event -> {
                JsonNode responseNode;
                try {
                    responseNode = mapper.readTree(task.getMessage());
                    if (responseNode.get("success").booleanValue()) {
                        txtRegisterInfo.getStylesheets().clear();
                        txtRegisterInfo.getStylesheets().add(String.valueOf(
                                LoginController.class.getResource(GREEN_STATUS_CSS)));
                    }

                    JsonNode finalResponseNode = responseNode;
                    Platform.runLater(() -> {
                        // Inform user successful user registration and return to login screen
                        txtRegisterInfo.setText(finalResponseNode.get("message").textValue());

                        FadeIn tempFade = new FadeIn(txtRegisterInfo);
                        tempFade.setOnFinished(enterEvent -> {
                            txtRegisterUsername.setText("");
                            txtRegisterPassword.setText("");

                            ZoomOut tempAnimation = new ZoomOut(paneRegister);
                            tempAnimation.setOnFinished(exitEvent -> {
                                paneLogin.toFront();
                                txtRegisterInfo.setMaxHeight(0);
                            });
                            tempAnimation.setDelay(Duration.seconds(2));
                            tempAnimation.play();
                        });
                        tempFade.play();
                        txtRegisterInfo.setMaxHeight(txtRegisterInfo.getPrefHeight());
                        });
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
            task.setOnFailed(event -> Platform.runLater(() -> {
                DialogHandler.handleInfo(txtRegisterInfo, RED_STATUS_CSS, task.getMessage(), 3);
                txtRegisterUsername.setText("");
                txtRegisterPassword.setText("");
                spin_Loading.setVisible(false);
            }));
            executor.execute(task);
        }
    }

    /**
     * Takes the user's details and attempts to login.
     * If there's a success then the scene is switched to the main menu
     * If failure then response from server is notified to the user
     * @param actionEvent Used to retrieve the current node.
     */
    @FXML
    private void LoginUser(ActionEvent actionEvent) {
        // Check password field complies with model rules
        if (txtPassword.getLength() < 6) {
            txtPassword.setText("");
            DialogHandler.handleInfo(txtLoginInfo,
                    RED_STATUS_CSS,
                    "password requires at least 6 characters",
                    3);
            // Checks username
        } else if (txtUsername.getText().isEmpty()) {
            DialogHandler.handleInfo(txtLoginInfo,
                    RED_STATUS_CSS,
                    "Please enter a username",
                    3);
        } else {
            LoginRequest tempUser = new LoginRequest();
            tempUser.setUsername(txtUsername.getText());
            tempUser.setPassword(txtPassword.getText());
            spin_Loading.setVisible(true);

            Task<Integer> task = new Task<Integer>() {
                @Override
                protected Integer call() throws JsonProcessingException {
                    Integer responseCode = null;
                    String node = mapper.writeValueAsString(tempUser);
                    RequestBody body = RequestBody.create(node, MediaType.parse("application/json; charset=utf-8"));

                    // Build POST call
                    Request request = new Request.Builder()
                            .url(BASE_URL + "/auth/login")
                            .post(body)
                            .build();
                    Call call = client.newCall(request);
                    try (Response response = call.execute();) {
                        responseCode = response.code();
                        updateMessage(Objects.requireNonNull(response.body()).string());
                    } catch (IOException e ) {
                        updateMessage("Server down, try again later");
                    }
                    return responseCode;
                }
            };
            task.setOnSucceeded(event -> {
                try {
                    JsonNode responseNode = mapper.readTree(task.getMessage());
                    Platform.runLater(() -> {
                        // Bad Credentials
                        switch (task.getValue()) {
                            case 401:
                                spin_Loading.setVisible(false);
                                txtUsername.setText("");
                                txtPassword.setText("");
                                DialogHandler.handleInfo(txtLoginInfo,
                                        RED_STATUS_CSS,
                                        responseNode.get("message").textValue(),
                                        3);
                                break;
                            case 200:
                                try {
                                    // Successful Login
                                    FXMLLoader loader = new FXMLLoader();
                                    loader.setLocation(getClass().getResource(MAIN_WINDOW));

                                    Parent parent = loader.load();
                                    Scene scene = new Scene(parent);

                                    MainController mainController = loader.getController();
                                    mainController.setUpScene(responseNode.get("accessToken").textValue());

                                    //Find the stage information
                                    Stage window = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();

                                    FadeOut tempFade = new FadeOut(stackRoot);
                                    tempFade.setOnFinished(exitEvent -> {
                                        window.setScene(scene);
                                        window.show();
                                    });
                                    tempFade.play();
                                    spin_Loading.setVisible(false);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                        }
                    });
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
            task.setOnFailed(event -> Platform.runLater(() -> {
                DialogHandler.handleInfo(txtLoginInfo,
                        "../css/statusred.css",
                        task.getMessage(),
                        3);
                txtRegisterUsername.setText("");
                txtRegisterPassword.setText("");
                spin_Loading.setVisible(false);
            }));
            executor.execute(task);
        }
    }
}
