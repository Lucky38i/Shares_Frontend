package ntu.n0696066.tools;

import animatefx.animation.FadeIn;
import animatefx.animation.FadeOut;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

public class DialogHandler {

    /**
     * //TODO
     * @param infoDialog The TextArea to act as a dialog
     * @param stylesheet The designated .css file to style the TextArea
     * @param message The message for the TextArea to contain
     * @param fadeOutDelayInSecs The delay for which to play the Fade Out animation
     */
    public static void handleInfo(TextArea infoDialog, String stylesheet, String message, Integer fadeOutDelayInSecs) {
        infoDialog.getStylesheets().clear();
        infoDialog.getStylesheets().add(String.valueOf(
                DialogHandler.class.getResource(stylesheet)));
        infoDialog.setText(message);
        new FadeIn(infoDialog).play();
        infoDialog.setMaxHeight(infoDialog.getPrefHeight());

        FadeOut tempFade =  new FadeOut(infoDialog);
        tempFade.setOnFinished(exitEvent -> infoDialog.setMaxHeight(0));
        tempFade.setDelay(Duration.seconds(fadeOutDelayInSecs));
        tempFade.play();
    }
}
