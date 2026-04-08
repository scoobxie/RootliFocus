package com.onrender.rootlify;

import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class CharacterView extends StackPane {
    private ImageView skin, outfit, hair;
    private double size;

    public CharacterView(double size) {
        this.size = size;
        this.setAlignment(Pos.CENTER);

        skin = new ImageView(); outfit = new ImageView(); hair = new ImageView();
        refresh();
        this.getChildren().addAll(skin, outfit, hair);

        this.setCursor(Cursor.HAND);

        // hover effect
        this.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), this);
            st.setToX(1.05); st.setToY(1.05);
            st.play();
        });

        this.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), this);
            st.setToX(1.0); st.setToY(1.0);
            st.play();
        });

        // click -> settings
        this.setOnMouseClicked(e -> SettingsWindow.open(this));
    }

    public void refresh() {
        try {
            skin.setImage(new Image("file:assets/skins/" + MainApp.current_skin, size, size, true, true));
            outfit.setImage(new Image("file:assets/outfits/" + MainApp.current_outfit, size, size, true, true));
            hair.setImage(new Image("file:assets/hair/" + MainApp.current_hair, size, size, true, true));
        } catch (Exception e) {}
    }

    //animations
    public void playWaveAnimation() {
        RotateTransition rt = new RotateTransition(Duration.millis(150), this);
        rt.setByAngle(12); rt.setCycleCount(4); rt.setAutoReverse(true); rt.play();
    }

    public void playJumpAnimation() {
        TranslateTransition jump = new TranslateTransition(Duration.millis(250), this);
        jump.setByY(-25); jump.setCycleCount(2); jump.setAutoReverse(true); jump.play();
    }
}