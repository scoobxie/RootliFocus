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
import java.io.File;

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
            skin.setImage(new Image(new File("assets/skins/" + MainApp.current_skin).toURI().toString(), size, size, true, true));
            outfit.setImage(new Image(new File("assets/outfits/" + MainApp.current_outfit).toURI().toString(), size, size, true, true));
            hair.setImage(new Image(new File("assets/hair/" + MainApp.current_hair).toURI().toString(), size, size, true, true));
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