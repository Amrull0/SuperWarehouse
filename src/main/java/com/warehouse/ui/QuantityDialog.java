package com.warehouse.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.function.Consumer;

public class QuantityDialog {
    public QuantityDialog(String title, String buttonText, Consumer<Integer> onSave) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);

        Label header = new Label(title);
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Количество");
        quantityField.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e1; -fx-background-color: white; -fx-padding: 10 12 10 12;");
        quantityField.getStyleClass().add("modern-text-field");

        Button save = new Button(buttonText);
        save.setDefaultButton(true);
        save.setStyle("-fx-background-color: linear-gradient(to bottom, #3b82f6, #1d4ed8); -fx-text-fill: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-font-weight: bold; -fx-padding: 10 16 10 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.12), 10, 0.1, 0, 3);");
        save.getStyleClass().add("gradient-button");
        save.setOnAction(e -> {
            try {
                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (quantity <= 0) {
                    throw new IllegalArgumentException("Количество должно быть больше нуля");
                }
                onSave.accept(quantity);
                stage.close();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText(null);
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            }
        });

        Button cancel = new Button("Отмена");
        cancel.setStyle("-fx-background-color: linear-gradient(to bottom, #3b82f6, #1d4ed8); -fx-text-fill: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-font-weight: bold; -fx-padding: 10 16 10 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.12), 10, 0.1, 0, 3);");
        cancel.getStyleClass().add("gradient-button");
        cancel.setOnAction(e -> stage.close());

        HBox buttons = new HBox(10, cancel, save);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        VBox root = new VBox(18, header, quantityField, buttons);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #eef6ff); -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #cbd5e1;");
        Scene scene = new Scene(root, 460, 240);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }
}
