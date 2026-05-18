package com.warehouse.ui;

import com.warehouse.model.Product;
import javafx.collections.FXCollections;
import javafx.scene.input.KeyCode;
import java.util.Objects;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Consumer;

public class ProductDialog {

    private static final List<String> CATEGORIES = List.of(
            "Молочные продукты",
            "Мясо и рыба",
            "Овощи",
            "Фрукты",
            "Напитки",
            "Крупы",
            "Хлебобулочные изделия",
            "Бакалея",
            "Другое"
    );

    private static final List<String> UNITS = List.of(
            "шт.",
            "кг",
            "г",
            "л",
            "мл",
            "уп."
    );

    private final Stage stage = new Stage();
    private final TextField nameField = new TextField();
    private final ComboBox<String> categoryBox = new ComboBox<>();
    private final TextField quantityField = new TextField();
    private final ComboBox<String> unitBox = new ComboBox<>();
    private final TextField priceField = new TextField();
    private final DatePicker arrivalPicker = new DatePicker(LocalDate.now());
    private final DatePicker expirationPicker = new DatePicker(LocalDate.now().plusDays(1));
    private final TextField locationField = new TextField();

    private Product result;

    public ProductDialog(String title, Product existing, Consumer<Product> onSave) {
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(title);

        categoryBox.setItems(FXCollections.observableArrayList(CATEGORIES));
        unitBox.setItems(FXCollections.observableArrayList(UNITS));
        categoryBox.setStyle(controlStyle());
        unitBox.setStyle(controlStyle());
        quantityField.setStyle(controlStyle());
        priceField.setStyle(controlStyle());
        nameField.setStyle(controlStyle());
        locationField.setStyle(controlStyle());
        arrivalPicker.setStyle(controlStyle());
        expirationPicker.setStyle(controlStyle());
        arrivalPicker.setEditable(false);
        expirationPicker.setEditable(false);
        categoryBox.getStyleClass().add("modern-combo-box");
        unitBox.getStyleClass().add("modern-combo-box");
        quantityField.getStyleClass().add("modern-text-field");
        priceField.getStyleClass().add("modern-text-field");
        nameField.getStyleClass().add("modern-text-field");
        locationField.getStyleClass().add("modern-text-field");
        arrivalPicker.getStyleClass().add("modern-date-picker");
        expirationPicker.getStyleClass().add("modern-date-picker");
        expirationPicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setDisable(empty || date.isBefore(LocalDate.now()));
            }
        });

        if (existing != null) {
            nameField.setText(existing.getName());
            ensureChoice(categoryBox, existing.getCategory());
            quantityField.setText(String.valueOf(existing.getQuantity()));
            ensureChoice(unitBox, existing.getUnit());
            priceField.setText(String.valueOf(existing.getPrice()));
            arrivalPicker.setValue(LocalDate.parse(existing.getArrivalDate()));
            expirationPicker.setValue(LocalDate.parse(existing.getExpirationDate()));
            locationField.setText(existing.getLocation());
        } else {
            categoryBox.getSelectionModel().selectFirst();
            unitBox.getSelectionModel().selectFirst();
        }

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(14);
        grid.addRow(0, label("Название"), nameField);
        grid.addRow(1, label("Категория"), categoryBox);
        grid.addRow(2, label("Количество"), quantityField);
        grid.addRow(3, label("Единица измерения"), unitBox);
        grid.addRow(4, label("Цена"), priceField);
        grid.addRow(5, label("Дата поступления"), arrivalPicker);
        grid.addRow(6, label("Срок годности"), expirationPicker);
        grid.addRow(7, label("Место хранения"), locationField);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(categoryBox, Priority.ALWAYS);
        GridPane.setHgrow(quantityField, Priority.ALWAYS);
        GridPane.setHgrow(unitBox, Priority.ALWAYS);
        GridPane.setHgrow(priceField, Priority.ALWAYS);
        GridPane.setHgrow(arrivalPicker, Priority.ALWAYS);
        GridPane.setHgrow(expirationPicker, Priority.ALWAYS);
        GridPane.setHgrow(locationField, Priority.ALWAYS);

        Button save = new Button("Сохранить");
        save.setDefaultButton(true);
        save.setStyle(buttonStyle());
        save.getStyleClass().add("gradient-button");
        save.setOnAction(e -> {
            try {
                String category = requireSelection(categoryBox, "Выберите категорию товара");
                String unit = requireSelection(unitBox, "Выберите единицу измерения").toLowerCase();
                Product p = new Product(
                        existing == null ? 0 : existing.getId(),
                        nameField.getText().trim(),
                        category,
                        Integer.parseInt(quantityField.getText().trim()),
                        unit,
                        Double.parseDouble(priceField.getText().trim()),
                        arrivalPicker.getValue().toString(),
                        expirationPicker.getValue().toString(),
                        locationField.getText().trim()
                );
                onSave.accept(p);
                result = p;
                stage.close();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        Button cancel = new Button("Отмена");
        cancel.setOnAction(e -> stage.close());
        cancel.setStyle(buttonStyle());
        cancel.getStyleClass().add("gradient-button");

        HBox buttons = new HBox(10, cancel, save);
        buttons.setAlignment(Pos.CENTER_RIGHT);

        Label header = new Label(title);
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        VBox root = new VBox(16, header, grid, buttons);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #ffffff, #eef6ff); -fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: #cbd5e1;");
        Scene scene = new Scene(root, 740, 560);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        stage.setScene(scene);
    }

    public Product showAndWait() {
        stage.showAndWait();
        return result;
    }

    private Label label(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        return label;
    }

    private String requireSelection(ComboBox<String> comboBox, String message) {
        String value = comboBox.getValue();
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private void ensureChoice(ComboBox<String> comboBox, String value) {
        if (value == null) {
            comboBox.getSelectionModel().selectFirst();
            return;
        }
        String normalized = value.toLowerCase();
        for (String item : comboBox.getItems()) {
            if (item != null && item.toLowerCase().equals(normalized)) {
                comboBox.getSelectionModel().select(item);
                return;
            }
        }
        comboBox.getItems().add(0, normalized);
        comboBox.getSelectionModel().select(normalized);
    }

    private String buttonStyle() {
        return "-fx-background-color: linear-gradient(to bottom, #3b82f6, #1d4ed8); -fx-text-fill: white; -fx-background-radius: 12; -fx-border-radius: 12; -fx-font-weight: bold; -fx-padding: 10 16 10 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.12), 10, 0.1, 0, 3);";
    }

    private String controlStyle() {
        return "-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e1; -fx-background-color: white; -fx-padding: 9 12 9 12;";
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
