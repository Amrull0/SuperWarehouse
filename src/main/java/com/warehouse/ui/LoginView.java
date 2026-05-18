package com.warehouse.ui;

import com.warehouse.model.User;
import com.warehouse.service.AuthService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class LoginView {

    private final VBox root = new VBox(12);

    public LoginView(AuthService authService, Consumer<User> onSuccess) {
        root.setPadding(new Insets(24));
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #f5f7fa, #c3cfe2);");

        Label title = new Label("SuperWarehouse");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Логин");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Пароль");

        Button loginButton = new Button("Войти");
        loginButton.setMaxWidth(Double.MAX_VALUE);
        Button registerButton = new Button("Регистрация");
        registerButton.setMaxWidth(Double.MAX_VALUE);

        Label hint = new Label("Первый вход администратора: admin / Admin123!");
        hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #555;");

        loginButton.setOnAction(e -> {
            try {
                User user = authService.login(usernameField.getText(), passwordField.getText());
                onSuccess.accept(user);
            } catch (Exception ex) {
                showError("Вход", ex.getMessage());
            }
        });

        registerButton.setOnAction(e -> openRegister(authService));

        VBox card = new VBox(10, title, usernameField, passwordField, loginButton, registerButton, hint);
        card.setPadding(new Insets(20));
        card.setMaxWidth(340);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; -fx-border-radius: 18; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 20, 0.15, 0, 6);");

        root.getChildren().add(card);
    }

    public Parent getRoot() {
        return root;
    }

    private void openRegister(AuthService authService) {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Регистрация");
        RegisterView view = new RegisterView(authService, stage);
        stage.setScene(new javafx.scene.Scene(view.getRoot(), 420, 320));
        stage.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}