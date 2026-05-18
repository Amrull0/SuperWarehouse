package com.warehouse.ui;

import com.warehouse.model.User;
import com.warehouse.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RegisterView {

    private final VBox root = new VBox(12);

    public RegisterView(AuthService authService, Stage stage) {
        root.setPadding(new Insets(22));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Регистрация пользователя");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Логин");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");

        PasswordField confirmField = new PasswordField();
        confirmField.setPromptText("Подтверждение пароля");

        Button registerButton = new Button("Создать аккаунт");
        registerButton.setMaxWidth(Double.MAX_VALUE);

        registerButton.setOnAction(e -> {
            try {
                User user = authService.register(usernameField.getText(), passwordField.getText(), confirmField.getText());
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Пользователь " + user.getUsername() + " создан");
                alert.showAndWait();
                stage.close();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        root.getChildren().addAll(title, usernameField, passwordField, confirmField, registerButton);
    }

    public Parent getRoot() {
        return root;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка регистрации");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}