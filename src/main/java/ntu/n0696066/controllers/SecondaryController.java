package ntu.n0696066.controllers;

import java.io.IOException;
import javafx.fxml.FXML;
import ntu.n0696066.App;

public class SecondaryController {

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}