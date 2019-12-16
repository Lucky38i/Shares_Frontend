package ntu.n0696066.controllers;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import animatefx.animation.ZoomIn;
import animatefx.animation.ZoomOut;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import ntu.n0696066.model.User;
import ntu.n0696066.tools.DialogHandler;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;

public class LoginController {

    @FXML
    private TextArea txtLoginInfo, txtRegisterInfo;
    @FXML
    private JFXSpinner spin_Loading;
    @FXML
    private StackPane stckPane_Login;
    @FXML
    private Pane paneLogin, paneRegister;
    @FXML
    private StackPane stackRoot;
    @FXML
    private ImageView btnMinimize, btnClose;
    @FXML
    private JFXTextField txtUsername, txtRegisterUsername;
    @FXML
    private JFXPasswordField txtPassword, txtRegisterPassword;
    @FXML
    private JFXButton btnLogin, btnSignUp, btnReturn, btnRegister;

    private final OkHttpClient client = new OkHttpClient();
    private final String BASE_URL = "http://localhost:8080/api";

    @FXML
    public void initialize() {

        Tooltip passwordTip = new Tooltip("Enter a password");
        txtRegisterPassword.setTooltip(passwordTip);

        txtRegisterPassword.setOnKeyTyped(event -> {
            if (txtRegisterPassword.getText().length() < 6) {
                passwordTip.setText("Your password needs to be more than 6 characters");
                txtRegisterPassword.setStyle("-fx-text-inner-color:red; -fx-prompt-text-fill: red;" +
                        " -jfx-focus-color: red");
            }
            else if (txtRegisterPassword.getText().length() > 6) passwordTip.setText("Valid password");
            else {
                txtRegisterPassword.setStyle("-fx-text-inner-color:#094270; -fx-prompt-text-fill:#094270;" +
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
        tempAnimation.setOnFinished(exitEvent -> {
            paneLogin.toFront();
        });
        tempAnimation.play();
        txtRegisterInfo.setMaxHeight(0);
    }

    @FXML
    private void SwitchToSignUp() {
        new ZoomIn(paneRegister).play();
        paneRegister.toFront();
    }

    @FXML
    private void RegisterUser()  {
        // Check password field complies with model rules
        if (txtRegisterPassword.getLength() < 6) {
            txtRegisterPassword.setText("");
            DialogHandler.handleInfo(txtRegisterInfo,
                    "../css/statusred.css",
                    "password requires at least 6 characters",
                    3);
        }
        else {
            User tempUser = new User(txtRegisterUsername.getText(), txtRegisterPassword.getText());
            spin_Loading.setVisible(true);

            // Delegate Rest Call to a separate thread
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        String node = new ObjectMapper().writeValueAsString(tempUser);
                        RequestBody body = RequestBody.create(node, MediaType.parse("application/json; charset=utf-8"));

                        // Build POST call
                        Request request = new Request.Builder()
                                .url(BASE_URL + "/auth/register")
                                .post(body)
                                .build();
                        Call call = client.newCall(request);
                        Response response = call.execute();
                        JsonNode responseNode = new ObjectMapper().readTree(
                                Objects.requireNonNull(response.body()).string());

                        Platform.runLater(() -> {
                            // Inform user successful user registration and return to login screen
                            if (responseNode.get("success").booleanValue()) {
                                txtRegisterInfo.getStylesheets().clear();
                                txtRegisterInfo.getStylesheets().add(String.valueOf(
                                        LoginController.class.getResource("../css/statusgreen.css")));
                                txtRegisterInfo.setText(responseNode.get("message").textValue());

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
                            }
                            // Inform failure to register user and print message from RESTful WS
                            else {
                                DialogHandler.handleInfo(txtLoginInfo,
                                        "../css/statusred.css",
                                        responseNode.get("message").textValue(),
                                        3);
                            }
                            spin_Loading.setVisible(false);
                        });
                    } catch (IOException e) {
                        // Inform user that server is down
                        Platform.runLater(() -> {
                            DialogHandler.handleInfo(txtRegisterInfo,
                                    "../css/statusred.css",
                                    "Server down, try again later",
                                    3);
                            txtRegisterUsername.setText("");
                            txtRegisterPassword.setText("");
                            spin_Loading.setVisible(false);
                        });
                    }
                    return null;
                }
            };
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    private void LoginUser() {
        // Check password field complies with model rules
        if (txtPassword.getLength() < 6) {
            txtPassword.setText("");
            DialogHandler.handleInfo(txtLoginInfo,
                    "../css/statusred.css",
                    "password requires at least 6 characters",
                    3);
        } else {
            User tempUser = new User(txtUsername.getText(), txtPassword.getText());
        }

    }
}
