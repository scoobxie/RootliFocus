package com.onrender.rootlify;

import javafx.animation.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class MainApp extends Application {

    public static VBox root;
    public static Label stateLabel;
    public static Label brandPrefix;
    public static Label timerLabel;
    public static boolean isFocusMode = true;
    public static int timeSeconds;
    public static int focusDuration;
    public static int breakDuration;

    private static CharacterView mascot;
    private static Label mascotBubble;
    private static VBox taskListScrollContainer;
    public static Map<String, ViewerColumn> activeViewers = new HashMap<>();

    // settings
    public static String twitch_channel;
    public static int focus_time;
    public static int break_time;
    public static String color_focus;
    public static String color_break;
    public static String color_bg;
    public static String color_username;
    public static String current_skin, current_hair, current_outfit;

    // sound
    private static AudioClip doneSound;
    private static AudioClip breakSound;
    private static AudioClip focusSound;

    @Override
    public void start(Stage primaryStage) {
        loadSettings();
        loadSounds();

        primaryStage.initStyle(StageStyle.TRANSPARENT);
        primaryStage.setTitle("RootliFocus");

        root = new VBox(5);
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: " + color_bg + "; " +
                "-fx-padding: 12px; -fx-background-radius: 20px; " +
                "-fx-border-color: " + color_focus + "; -fx-border-width: 4px; -fx-border-radius: 18px;");

        VBox header = new VBox(-12);
        header.setAlignment(Pos.CENTER);


        brandPrefix = new Label("Rootli");
        brandPrefix.setStyle("-fx-font-size: 19px; -fx-text-fill: " + color_focus + "; -fx-font-family: 'Verdana'; -fx-opacity: 0.9; -fx-font-weight: bold;");

        stateLabel = new Label("FOCUS");
        stateLabel.setStyle("-fx-font-size: 26px; -fx-text-fill: " + color_focus + "; -fx-font-family: 'Verdana'; -fx-opacity: 0.8;-fx-font-weight: bold; -fx-letter-spacing: 2px;");

        HBox stateBox = new HBox(2);
        stateBox.setAlignment(Pos.BASELINE_CENTER);
        stateBox.getChildren().addAll(brandPrefix, stateLabel);

        timerLabel = new Label(String.format("%02d:00", focus_time));
        timerLabel.setStyle("-fx-font-size: 50px; -fx-text-fill: " + color_focus + "; -fx-font-family: 'Verdana'; -fx-font-weight: bold;");
        header.getChildren().addAll(stateBox, timerLabel);

        HBox contentArea = new HBox(15);
        contentArea.setAlignment(Pos.BOTTOM_LEFT);

        VBox mascotBox = new VBox(5);
        mascotBox.setAlignment(Pos.BOTTOM_CENTER);

        mascotBubble = new Label("");
        mascotBubble.setOpacity(0);
        mascotBubble.setWrapText(false);
        mascotBox.setMinWidth(200);
        mascotBubble.setMaxWidth(600);
        mascotBubble.setTextAlignment(TextAlignment.CENTER);
        mascotBubble.setAlignment(Pos.CENTER);
        mascotBubble.setStyle("-fx-background-color: white; -fx-text-fill: #d81b60; -fx-padding: 8px 15px; " +
                "-fx-background-radius: 15px; -fx-font-size: 15px; -fx-font-weight: bold; " +
                "-fx-border-color: #ffb3d9; -fx-border-width: 2px; -fx-border-radius: 15px;");

        mascot = new CharacterView(150);
        mascot.setCursor(javafx.scene.Cursor.HAND);
        mascot.setCursor(javafx.scene.Cursor.HAND);

        mascot.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), mascot);
            st.setToX(1.05); st.setToY(1.05);
            st.play();
        });

        mascot.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(100), mascot);
            st.setToX(1.0); st.setToY(1.0);
            st.play();
        });

        mascot.setOnMouseClicked(e -> SettingsWindow.open(mascot));
        mascotBox.getChildren().addAll(mascotBubble, mascot);

        taskListScrollContainer = new VBox(10);
        taskListScrollContainer.setAlignment(Pos.TOP_CENTER);
        Pane scrollPane = new Pane(taskListScrollContainer);
        scrollPane.setPrefSize(280, 200);
        scrollPane.setClip(new Rectangle(280, 200));

        contentArea.getChildren().addAll(mascotBox, scrollPane);

        // window relocating
        final double[] moveOffset = new double[2];

        root.setOnMousePressed(event -> {
            moveOffset[0] = event.getScreenX() - primaryStage.getX();
            moveOffset[1] = event.getScreenY() - primaryStage.getY();
            root.setStyle("-fx-background-color: " + color_bg + "; " +
                    "-fx-padding: 12px; -fx-background-radius: 20px; " +
                    "-fx-border-color: " + (isFocusMode ? color_focus : color_break) + "; -fx-border-width: 4px; -fx-border-radius: 18px; -fx-cursor: closed-hand;");
        });

        root.setOnMouseDragged(event -> {
            primaryStage.setX(event.getScreenX() - moveOffset[0]);
            primaryStage.setY(event.getScreenY() - moveOffset[1]);
        });

        root.setOnMouseReleased(event -> {
            root.setStyle("-fx-background-color: " + color_bg + "; " +
                    "-fx-padding: 12px; -fx-background-radius: 20px; " +
                    "-fx-border-color: " + (isFocusMode ? color_focus : color_break) + "; -fx-border-width: 4px; -fx-border-radius: 18px; -fx-cursor: default;");
        });

        // to do list manual input
        TextField manualTaskInput = new TextField();
        manualTaskInput.setPromptText("Adaugă task manual (Enter)...");
        manualTaskInput.setStyle("-fx-background-radius: 10; -fx-border-color: #ffb3d9; -fx-border-radius: 10; -fx-background-color: white;");
        HBox.setHgrow(manualTaskInput, Priority.ALWAYS);

        manualTaskInput.setOnAction(e -> {
            String text = manualTaskInput.getText();
            if (!text.trim().isEmpty()) {
                addViewerTask(twitch_channel, text);
                manualTaskInput.clear();
                saveTasksToFile();
            }
        });

        // window resizing
        Label resizeHandle = new Label("↘");
        resizeHandle.setStyle("-fx-font-size: 18px; -fx-text-fill: " + color_focus + "; -fx-cursor: se-resize; -fx-font-weight: bold;");

        HBox bottomBar = new HBox(10);
        bottomBar.setAlignment(Pos.BOTTOM_RIGHT);
        bottomBar.getChildren().addAll(manualTaskInput, resizeHandle);
        root.getChildren().addAll(header, contentArea, bottomBar);

        root.setPrefSize(500, 340);
        root.setMinSize(500, 340);
        root.setMaxSize(500, 340);

        javafx.scene.Group scaleGroup = new javafx.scene.Group(root);
        javafx.scene.transform.Scale scaleTransform = new javafx.scene.transform.Scale(1, 1, 0, 0);
        scaleGroup.getTransforms().add(scaleTransform);

        Pane rootWrapper = new Pane(scaleGroup);
        rootWrapper.setStyle("-fx-background-color: transparent;");

        rootWrapper.widthProperty().addListener((obs, oldVal, newVal) -> {
            double scale = newVal.doubleValue() / 500.0;
            scaleTransform.setX(scale);
            scaleTransform.setY(scale);
        });

        final double[] resizeStart = new double[2];
        resizeHandle.setOnMousePressed(event -> {
            resizeStart[0] = event.getScreenX();
            resizeStart[1] = primaryStage.getWidth();
            event.consume();
        });

        resizeHandle.setOnMouseDragged(event -> {
            double dragAmount = event.getScreenX() - resizeStart[0];
            double newWidth = resizeStart[1] + dragAmount;

            if (newWidth < 300) newWidth = 300;
            if (newWidth > 1200) newWidth = 1200;

            primaryStage.setWidth(newWidth);
            primaryStage.setHeight(newWidth / (500.0 / 340.0));
            event.consume();

        });

        Scene scene = new Scene(rootWrapper, 500, 340);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.setAlwaysOnTop(true);
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        } catch (Exception e) {}
        applyNewSettings();
        primaryStage.show();

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        primaryStage.setX(bounds.getMinX() + 25);
        primaryStage.setY(bounds.getMinY() + 25);

        startTimer();
        startAutoScroll();
        loadTasksFromFile();
        new Thread(() -> TwitchBot.startBot(twitch_channel)).start();
    }

    private void loadSounds() {
        try {
            File fDone = new File("assets/sounds/done.wav");
            File fBreak = new File("assets/sounds/break.wav");
            File fFocus = new File("assets/sounds/focus.wav");

            if (fDone.exists()) doneSound = new AudioClip(fDone.toURI().toString());
            if (fBreak.exists()) breakSound = new AudioClip(fBreak.toURI().toString());
            if (fFocus.exists()) focusSound = new AudioClip(fFocus.toURI().toString());
        } catch (Exception e) {
            System.out.println("Error loading sounds.");
        }
    }

    public static void applyNewSettings() {
        Platform.runLater(() -> {
            root.setStyle("-fx-background-color: " + color_bg + "; " +
                    "-fx-padding: 12px; -fx-background-radius: 20px; " +
                    "-fx-border-color: " + (isFocusMode ? color_focus : color_break) + "; -fx-border-width: 4px; -fx-border-radius: 18px;");

            if (activeViewers != null) {
                for (ViewerColumn col : activeViewers.values()) {
                    col.refreshStyle();
                }
            }

            stateLabel.setStyle("-fx-font-size: 26px; -fx-font-family: 'Verdana'; -fx-opacity: 0.8; -fx-font-weight: bold; -fx-letter-spacing: 2px; -fx-text-fill: " + (isFocusMode ? color_focus : color_break) + ";");
            brandPrefix.setStyle("-fx-font-size: 18px; -fx-font-family: 'Verdana'; -fx-opacity: 0.9; -fx-font-weight: bold; -fx-text-fill: " + (isFocusMode ? color_focus : color_break) + ";");
            timerLabel.setStyle("-fx-font-size: 50px; -fx-font-family: 'Verdana'; -fx-font-weight: bold; -fx-text-fill: " + (isFocusMode ? color_focus : color_break) + ";");

            focusDuration = focus_time * 60;
            breakDuration = break_time * 60;
            timeSeconds = isFocusMode ? focusDuration : breakDuration;
            int min = timeSeconds / 60, sec = timeSeconds % 60;
            timerLabel.setText(String.format("%02d:%02d", min, sec));
        });
    }

    private void loadSettings() {
        Properties prop = new Properties();
        try (FileInputStream input = new FileInputStream("config.properties")) {
            prop.load(input);
            twitch_channel = prop.getProperty("twitch_channel", "scoobxie");
            focus_time = Integer.parseInt(prop.getProperty("focus_time", "25"));
            break_time = Integer.parseInt(prop.getProperty("break_time", "5"));
            color_focus = prop.getProperty("color_focus", "#b14565");
            color_break = prop.getProperty("color_break", "#718249");
            color_bg = prop.getProperty("color_bg", "rgba(255, 230, 238, 0.92)");
            color_username = prop.getProperty("color_username", "#ff99cc");
            current_skin = prop.getProperty("current_skin", "girl-skintone-medium.png");
            current_hair = prop.getProperty("current_hair", "girl-hair-brunette.png");
            current_outfit = prop.getProperty("current_outfit", "girl-outfit-protagonist.png");

            focusDuration = focus_time * 60;
            breakDuration = break_time * 60;
            timeSeconds = focusDuration;
        } catch (IOException ex) {
            twitch_channel = "scoobxie"; focus_time = 25; break_time = 5;
            focusDuration = 25 * 60; breakDuration = 5 * 60; timeSeconds = focusDuration;
            color_focus = "#b14565"; color_break = "#718249";
            color_bg = "rgba(255, 230, 238, 0.92)";
            color_username = "#ff99cc";
            current_skin = "girl-skintone-medium.png"; current_hair = "girl-hair-brunette.png"; current_outfit = "girl-outfit-protagonist.png";
        }
    }

    public static void mascotSpeak(String text, boolean jump) {
        Platform.runLater(() -> {
            mascotBubble.setText(text);
            mascotBubble.setOpacity(1);
            if (jump) mascot.playJumpAnimation();
            else mascot.playWaveAnimation();

            PauseTransition p = new PauseTransition(Duration.seconds(3));
            p.setOnFinished(e -> mascotBubble.setOpacity(0));
            p.play();
        });
    }

    private void startTimer() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            timeSeconds--;
            int min = timeSeconds / 60, sec = timeSeconds % 60;
            timerLabel.setText(String.format("%02d:%02d", min, sec));

            if (timeSeconds <= 0) {
                isFocusMode = !isFocusMode;
                timeSeconds = isFocusMode ? focusDuration : breakDuration;
                stateLabel.setText(isFocusMode ? "FOCUS" : "BREAK");

                if (isFocusMode) {
                    if (focusSound != null) focusSound.play();
                    mascotSpeak("Back to work!", true);
                } else {
                    if (breakSound != null) breakSound.play();
                    mascotSpeak("Time for a break!", false);
                }
                applyNewSettings();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void startAutoScroll() {
        Timeline scrollTimeline = new Timeline(new KeyFrame(Duration.millis(30), e -> {
            double h = taskListScrollContainer.getHeight();
            if (h > 0) {
                double y = taskListScrollContainer.getTranslateY() + 0.8;
                if (y > 160) y = -h;
                taskListScrollContainer.setTranslateY(y);
            }
        }));
        scrollTimeline.setCycleCount(Timeline.INDEFINITE);
        scrollTimeline.play();
    }

    public static void addViewerTask(String username, String taskText) {
        Platform.runLater(() -> {
            ViewerColumn column = activeViewers.get(username);
            if (column == null) {
                column = new ViewerColumn(username);
                activeViewers.put(username, column);
                taskListScrollContainer.getChildren().add(column);
            }

            column.addTask(taskText);
            mascotSpeak("Good luck, " + username + "!", false);
            saveTasksToFile();
        });
    }

    public static void completeViewerTask(String username) {
        Platform.runLater(() -> {
            ViewerColumn column = activeViewers.get(username);
            if (column != null && column.markNextTaskDone()) {
                if (doneSound != null) doneSound.play();
                mascotSpeak("Great job, " + username + "!", true);
                saveTasksToFile();
            }
        });
    }

    public static void playManualDoneAction() {
        Platform.runLater(() -> {
            if (doneSound != null) doneSound.play();
            mascotSpeak("Great job!", true);
        });
    }

    public static void saveTasksToFile() {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter("tasks.txt"))) {
            for (ViewerColumn col : activeViewers.values()) {
                if (col.getUsername().equals(twitch_channel) || col.getUsername().equals("me")) {
                    for (javafx.scene.Node node : col.getChildren()) {
                        if (node instanceof VBox) {
                            for (javafx.scene.Node taskNode : ((VBox) node).getChildren()) {
                                if (taskNode instanceof TaskItem) {
                                    TaskItem item = (TaskItem) taskNode;
                                    writer.println(col.getUsername() + "|" + item.getTaskText() + "|" + item.isDone());
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving tasks: " + e.getMessage());
        }
    }

    private void loadTasksFromFile() {
        File file = new File("tasks.txt");
        if (!file.exists()) return;

        try (java.util.Scanner scanner = new java.util.Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String user = parts[0];
                    String text = parts[1];
                    boolean done = Boolean.parseBoolean(parts[2]);

                    Platform.runLater(() -> {
                        String correctUser = (user.equals("me")) ? twitch_channel : user;
                        addViewerTask(correctUser, text);
                        if (done) completeViewerTask(correctUser);
                    });
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading tasks.");
        }
    }

    public static void main(String[] args) { launch(args); }
}

class SettingsWindow {
    private static String tempSkin = MainApp.current_skin;
    private static String tempHair = MainApp.current_hair;
    private static String tempOutfit = MainApp.current_outfit;
    private static boolean isBoyMode = tempSkin.toLowerCase().contains("Male ♂");

    public static void open(CharacterView targetMascot) {
        Stage stage = new Stage();
        stage.setTitle("RootliFocus Settings");

        try {
            stage.getIcons().add(new Image("file:icon.png"));
        } catch (Exception ignored) {}

        isBoyMode = MainApp.current_skin.toLowerCase().contains("boy");
        tempSkin = MainApp.current_skin;
        tempHair = MainApp.current_hair;
        tempOutfit = MainApp.current_outfit;

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: #ffe6f0; -fx-padding: 20px;");

        Label title = new Label("✨ Settings ✨");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #d81b60;");

        ComboBox<String> genderBox = new ComboBox<>();
        genderBox.getItems().addAll("♀ Female", "♂ Male");
        genderBox.setValue(isBoyMode ? "♂ Male" : "♀ Female");

        VBox wardrobeBox = new VBox(10);
        Runnable updateWardrobeUI = () -> {
            wardrobeBox.getChildren().clear();
            wardrobeBox.getChildren().addAll(
                    createSelector("Skin", "skins", tempSkin, targetMascot, val -> tempSkin = val),
                    createSelector("Hair", "hair", tempHair, targetMascot, val -> tempHair = val),
                    createSelector("Outfit", "outfits", tempOutfit, targetMascot, val -> tempOutfit = val)
            );
        };

        genderBox.setOnAction(e -> {
            boolean selectedBoy = genderBox.getValue().equals("♂ Male");
            if (isBoyMode != selectedBoy) {
                isBoyMode = selectedBoy;
                tempSkin = getFirstAsset("skins", isBoyMode, tempSkin);
                tempHair = getFirstAsset("hair", isBoyMode, tempHair);
                tempOutfit = getFirstAsset("outfits", isBoyMode, tempOutfit);

                MainApp.current_skin = tempSkin; MainApp.current_hair = tempHair; MainApp.current_outfit = tempOutfit;
                targetMascot.refresh();
                updateWardrobeUI.run();
            }
        });
        updateWardrobeUI.run();

        GridPane settingsGrid = new GridPane();
        settingsGrid.setHgap(10); settingsGrid.setVgap(10);
        settingsGrid.setAlignment(Pos.CENTER);

        TextField focusTimeField = new TextField(String.valueOf(MainApp.focus_time));
        focusTimeField.setPrefWidth(50);
        TextField breakTimeField = new TextField(String.valueOf(MainApp.break_time));
        breakTimeField.setPrefWidth(50);
        TextField twitchField = new TextField(String.valueOf(MainApp.twitch_channel));

        ColorPicker focusColorPicker = new ColorPicker(Color.web(MainApp.color_focus));
        ColorPicker breakColorPicker = new ColorPicker(Color.web(MainApp.color_break));
        ColorPicker bgColorPicker = new ColorPicker(Color.web(MainApp.color_bg));
        ColorPicker usernameColorPicker = new ColorPicker(Color.web(MainApp.color_username));

        settingsGrid.add(new Label("Username:"), 0, 1); settingsGrid.add(twitchField, 1,1);
        settingsGrid.add(new Label("Focus (min):"), 0, 2); settingsGrid.add(focusTimeField, 1, 2);
        settingsGrid.add(new Label("Break (min):"), 0, 3); settingsGrid.add(breakTimeField, 1, 3);
        settingsGrid.add(new Label("Focus Color:"), 0, 4); settingsGrid.add(focusColorPicker, 1, 4);
        settingsGrid.add(new Label("Break Color:"), 0, 5); settingsGrid.add(breakColorPicker, 1, 5);
        settingsGrid.add(new Label("Background Color:"), 0, 6); settingsGrid.add(bgColorPicker, 1, 6);
        settingsGrid.add(new Label("Username Color:"), 0, 7); settingsGrid.add(usernameColorPicker, 1, 7);

        Button resetBtn = new Button("Reset");
        resetBtn.setStyle("-fx-background-color: #fd98ca; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        resetBtn.setOnAction(e -> {
            focusColorPicker.setValue(Color.web("#b14565"));
            breakColorPicker.setValue(Color.web("#718249"));
            bgColorPicker.setValue(Color.web("rgba(255, 230, 238, 0.92)"));
            usernameColorPicker.setValue(Color.web("#ff99cc"));

            focusTimeField.setText("25");
            breakTimeField.setText("5");

            tempSkin = "girl-skintone-medium.png";
            tempHair = "girl-hair-brunette.png";
            tempOutfit = "girl-outfit-protagonist.png";

            isBoyMode = false;
            genderBox.setValue("♀ Female");

            targetMascot.refresh();
            updateWardrobeUI.run();
        });

        Button saveBtn = new Button("Save & Apply");
        saveBtn.setStyle("-fx-background-color: #d81b60; -fx-text-fill: white; -fx-font-weight: bold;");
        saveBtn.setOnAction(e -> {
            String newChannel = twitchField.getText().trim().toLowerCase();

            if (!newChannel.equals(MainApp.twitch_channel)) {
                MainApp.twitch_channel = newChannel;
            }
            saveConfig();

            MainApp.current_skin = tempSkin; MainApp.current_hair = tempHair; MainApp.current_outfit = tempOutfit;
            try {
                MainApp.focus_time = Integer.parseInt(focusTimeField.getText());
                MainApp.break_time = Integer.parseInt(breakTimeField.getText());
            } catch (Exception ex) { }
            MainApp.color_focus = toHex(focusColorPicker.getValue());
            MainApp.color_break = toHex(breakColorPicker.getValue());
            MainApp.color_bg = toHex(bgColorPicker.getValue());
            MainApp.color_username = toHex(usernameColorPicker.getValue());

            saveConfig();
            targetMascot.refresh();
            MainApp.applyNewSettings();
            stage.close();
        });
        HBox actionButtons = new HBox(15, saveBtn, resetBtn);
        actionButtons.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(title, genderBox, wardrobeBox, new Separator(), settingsGrid, actionButtons);
        stage.setScene(new Scene(layout, 400, 600));
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.show();
    }

    private static String getFirstAsset(String folder, boolean wantBoy, String fallback) {
        File dir = new File("assets/" + folder);
        if (dir.exists() && dir.listFiles() != null) {
            for (File f : dir.listFiles()) {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".png") && (name.contains("boy") == wantBoy)) return f.getName();
            }
        }
        return fallback;
    }

    private static HBox createSelector(String typeName, String folder, String currentVal, CharacterView mascot, java.util.function.Consumer<String> onUpdate) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER);

        File dir = new File("assets/" + folder);
        List<String> files = new ArrayList<>();
        if (dir.exists()) {
            for (File f : dir.listFiles()) {
                String name = f.getName().toLowerCase();
                if (name.endsWith(".png") && (name.contains("boy") == isBoyMode)) files.add(f.getName());
            }
        }

        if (files.isEmpty()) return new HBox(new Label("No " + typeName + " found"));

        int currentIndex = Math.max(0, files.indexOf(currentVal));
        Label displayLabel = new Label(typeName);
        displayLabel.setPrefWidth(60);
        displayLabel.setAlignment(Pos.CENTER);

        Button btnPrev = new Button("<");
        Button btnNext = new Button(">");

        int[] indexTracker = { currentIndex };

        btnPrev.setOnAction(e -> {
            indexTracker[0] = (indexTracker[0] - 1 + files.size()) % files.size();
            updateSelection(files.get(indexTracker[0]), onUpdate, mascot);
        });

        btnNext.setOnAction(e -> {
            indexTracker[0] = (indexTracker[0] + 1) % files.size();
            updateSelection(files.get(indexTracker[0]), onUpdate, mascot);
        });

        row.getChildren().addAll(btnPrev, displayLabel, btnNext);
        return row;
    }

    private static void updateSelection(String newFile, java.util.function.Consumer<String> onUpdate, CharacterView mascot) {
        onUpdate.accept(newFile);
        MainApp.current_skin = tempSkin; MainApp.current_hair = tempHair; MainApp.current_outfit = tempOutfit;
        mascot.refresh();
    }

    private static String toHex(Color color) {
        return String.format("#%02X%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255),
                (int)(color.getOpacity() * 255));
    }

    private static void saveConfig() {
        try {
            java.util.Properties prop = new java.util.Properties();
            java.io.FileInputStream in = new java.io.FileInputStream("config.properties");
            prop.load(in);
            in.close();

            prop.setProperty("current_skin", MainApp.current_skin);
            prop.setProperty("current_hair", MainApp.current_hair);
            prop.setProperty("current_outfit", MainApp.current_outfit);
            prop.setProperty("focus_time", String.valueOf(MainApp.focus_time));
            prop.setProperty("break_time", String.valueOf(MainApp.break_time));
            prop.setProperty("color_focus", MainApp.color_focus);
            prop.setProperty("color_break", MainApp.color_break);
            prop.setProperty("color_bg", MainApp.color_bg);
            prop.setProperty("color_username", MainApp.color_username);
            prop.setProperty("twitch_channel", MainApp.twitch_channel);

            java.io.FileOutputStream out = new java.io.FileOutputStream("config.properties");
            prop.store(out, "Updated via Settings Menu");
            out.close();
        } catch (Exception e) {}
    }
}

// username
class ViewerColumn extends VBox {
    private String username;
    private VBox taskList;
    private Label nameLabel;

    public ViewerColumn(String username) {
        this.username = username;
        this.setAlignment(Pos.TOP_CENTER);
        this.setSpacing(6);

        this.nameLabel = new Label(username);
        refreshStyle();

        this.taskList = new VBox(4);
        this.taskList.setAlignment(Pos.TOP_CENTER);
        this.getChildren().addAll(nameLabel, taskList);
    }

    public void refreshStyle() {
        if (nameLabel != null) {
            nameLabel.setStyle("-fx-background-color: " + MainApp.color_username + "; " +
                    "-fx-text-fill: white; " +
                    "-fx-padding: 3px 12px; " +
                    "-fx-background-radius: 10px; " +
                    "-fx-font-size: 13px; " +
                    "-fx-font-weight: bold;");
        }
    }

    public String getUsername() { return username; }

    public void addTask(String text) {
        taskList.getChildren().add(new TaskItem(text, username));
    }

    public boolean markNextTaskDone() {
        for (javafx.scene.Node n : taskList.getChildren()) {
            if (n instanceof TaskItem) {
                TaskItem t = (TaskItem) n;
                if (!t.isDone()) {
                    t.setDone();
                    return true;
                }
            }
        }
        return false;
    }
}

class TaskItem extends HBox {
    private boolean done = false;
    private ImageView icon;
    private Text txt;
    private Label deleteBtn;

    public TaskItem(String text, String username) {
        this.setAlignment(Pos.CENTER_LEFT);
        this.setSpacing(6);
        this.setStyle("-fx-background-color: white; -fx-padding: 5px 10px; -fx-background-radius: 10px; -fx-cursor: hand;");

        // heart checkbox
        icon = new ImageView(new Image(new File("assets/checkboxes/heart1.png").toURI().toString(), 18, 18, true, true));

        // to do list text
        txt = new Text(text);
        txt.setStyle("-fx-font-size: 15px; -fx-fill: #4a2c3a; -fx-font-weight: bold;");

        // x button
        deleteBtn = new Label("❌");
        deleteBtn.setStyle("-fx-text-fill: red; -fx-font-size: 11px; -fx-cursor: hand;");
        deleteBtn.setOpacity(0);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        this.getChildren().addAll(icon, txt, spacer, deleteBtn);

        this.setOnMouseEntered(e -> deleteBtn.setOpacity(1));
        this.setOnMouseExited(e -> deleteBtn.setOpacity(0));

        deleteBtn.setOnMouseClicked(e -> {
            e.consume();
            Pane parentBox = (Pane) this.getParent();
            if (parentBox != null) {
                parentBox.getChildren().remove(this);
                MainApp.saveTasksToFile();
            }
        });

        // checkbox logic
        this.setOnMouseClicked(e -> {
            if (!this.done) {
                this.setDone();
                MainApp.playManualDoneAction();
                MainApp.saveTasksToFile();
            }
        });
    }

    public boolean isDone() { return done; }
    public String getTaskText() { return txt.getText(); }

    public void setDone() {
        this.done = true;
        icon.setImage(new Image(new File("assets/checkboxes/heart2.png").toURI().toString(), 18, 18, true, true));
        txt.setStyle("-fx-font-size: 15px; -fx-fill: #8a6a77; -fx-font-weight: bold;");
    }
}