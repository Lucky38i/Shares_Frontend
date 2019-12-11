package ntu.n0696066.controllers;

import animatefx.animation.FadeOut;
import animatefx.animation.ZoomIn;
import animatefx.animation.ZoomOut;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jfoenix.controls.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ntu.n0696066.model.User;
import okhttp3.*;

import java.io.IOException;
import java.util.Objects;

public class LoginController {

    @FXML
    private JFXSpinner spin_Loading;
    @FXML
    private StackPane stckPane_Login;
    @FXML
    private Pane paneLogin, paneRegister;
    @FXML
    private VBox vboxLoginScreenRoot;
    @FXML
    private ImageView btnMinimize, btnClose;
    @FXML
    private JFXTextField txtUsername, txtRegisterUsername;
    @FXML
    private JFXPasswordField txtPassword, txtRegisterPassword;
    @FXML
    private JFXButton btnLogin, btnSignUp, btnReturn, btnRegister, btnDialogOk;

    private JFXDialog infoDialog;

    private final OkHttpClient client = new OkHttpClient();
    private final String BASE_URL = "http://localhost:8080/api";

    @FXML
    public void initialize() {


        txtRegisterPassword.setOnKeyTyped(event -> {
            if (txtRegisterPassword.getText().length() <= 6) {
                txtRegisterPassword.setStyle("-fx-text-inner-color:red; -fx-prompt-text-fill: red;");
            }
            else {
                txtRegisterPassword.setStyle("-fx-text-inner-color:#094270; -fx-prompt-text-fill:#094270;");
            }
        });

        txtPassword.setOnKeyTyped(event -> {
            if (txtPassword.getText().length() < 6) {
                txtPassword.setStyle("-fx-text-inner-color:red; -fx-prompt-text-fill: red;");
            }
            else {
                txtPassword.setStyle("-fx-text-inner-color:#094270; -fx-prompt-text-fill:#094270;");
            }
        });
    }

    @FXML
    private void CloseWindow() {
        FadeOut exit = new FadeOut(vboxLoginScreenRoot);
        exit.setOnFinished(exitEvent -> System.exit(0));
        exit.play();
    }

    @FXML
    private void MinimizeWindow() {
        ((Stage) vboxLoginScreenRoot.getScene().getWindow()).setIconified(true);
    }


    @FXML
    private void ReturnToLogin() {
        ZoomOut tempAnimation = new ZoomOut(paneRegister);
        tempAnimation.setOnFinished(exitEvent -> {
            paneLogin.toFront();
        });
        tempAnimation.play();
    }

    @FXML
    private void SwitchToSignUp() {
        new ZoomIn(paneRegister).play();
        paneRegister.toFront();
    }

    @FXML
    private void RegisterUser()  {
        User tempUser = new User(txtRegisterUsername.getText(), txtRegisterPassword.getText());
        spin_Loading.setVisible(true);
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() {
                try {
                    String node = new ObjectMapper().writeValueAsString(tempUser);
                    RequestBody body = RequestBody.create(node, MediaType.parse("application/json; charset=utf-8"));

                    Request request = new Request.Builder()
                            .url(BASE_URL + "/auth/register")
                            .post(body)
                            .build();
                    Call call = client.newCall(request);
                    Response response = call.execute();
                    JsonNode responseNode = new ObjectMapper().readTree(
                            Objects.requireNonNull(response.body()).string());

                    Platform.runLater(() -> {
                        System.out.println("Success : " + responseNode.get("success").toString() + " Message : " +
                                responseNode.get("message"));
                        spin_Loading.setVisible(false);
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

    @FXML
    private void LoginUser() {
        JFXDialogLayout content = new JFXDialogLayout();
    }
}
