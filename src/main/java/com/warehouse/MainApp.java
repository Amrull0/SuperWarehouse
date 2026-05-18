package com.warehouse;

import com.warehouse.db.Database;
import com.warehouse.model.User;
import com.warehouse.model.UserRole;
import java.util.Objects;
import com.warehouse.ui.DashboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        Database.init();

        User currentUser = new User(1, "Пользователь", UserRole.USER);
        DashboardView dashboardView = new DashboardView(currentUser);

        Scene scene = new Scene(dashboardView.getRoot(), 1380, 860);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm());
        stage.setTitle("SuperWarehouse");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
