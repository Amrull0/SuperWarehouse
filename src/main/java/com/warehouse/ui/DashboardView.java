package com.warehouse.ui;

import com.warehouse.model.Product;
import com.warehouse.model.StockOperation;
import com.warehouse.model.User;
import com.warehouse.service.ProductService;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DashboardView {

    private static final String[] CATEGORY_OPTIONS = {
            "Все категории",
            "Молочные продукты",
            "Мясо и рыба",
            "Овощи",
            "Фрукты",
            "Напитки",
            "Крупы",
            "Хлебобулочные изделия",
            "Бакалея",
            "Другое"
    };

    private static final String[] UNIT_OPTIONS = {
            "Все единицы",
            "шт.",
            "кг",
            "г",
            "л",
            "мл",
            "уп."
    };

    private static final String[] OPERATION_OPTIONS = {
            "Все операции",
            "Поступление",
            "Продажа",
            "Списание"
    };

    private final BorderPane root = new BorderPane();
    private final ProductService productService = new ProductService();
    private final User currentUser;

    private final TableView<Product> productsTable = new TableView<>();
    private final TableView<StockOperation> operationsTable = new TableView<>();
    private final TextField searchField = new TextField();
    private final ComboBox<String> categoryFilter = new ComboBox<>();
    private final ComboBox<String> unitFilter = new ComboBox<>();
    private final ComboBox<SortOption> sortFilter = new ComboBox<>();
    private final ComboBox<String> operationFilter = new ComboBox<>();
    private final Set<Integer> expiringSoonProductIds = new HashSet<>();

    public DashboardView(User currentUser) {
        this.currentUser = currentUser;

        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #eef4ff, #fff6ef);");
        root.getStyleClass().add("dashboard-root");
        root.setTop(buildTopBar());
        root.setCenter(buildTabs());
        root.setPadding(new Insets(10));
        refreshAll();
    }

    public Parent getRoot() {
        return root;
    }

    private Parent buildTopBar() {
        Label title = new Label("SuperWarehouse");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: white;");

        VBox titleBox = new VBox(2, title);

        Region spacer = new Region();
        HBox bar = new HBox(16, titleBox, spacer);
        HBox.setHgrow(spacer, Priority.ALWAYS);
        bar.setPadding(new Insets(16));
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setStyle("-fx-background-color: linear-gradient(to right, #1d4ed8, #7c3aed); -fx-background-radius: 18; -fx-border-radius: 18; -fx-effect: dropshadow(gaussian, rgba(37,99,235,0.35), 18, 0.18, 0, 6);");
        return bar;
    }

    private Parent buildTabs() {
        TabPane tabs = new TabPane();
        tabs.getStyleClass().add("modern-tab-pane");
        tabs.setStyle("-fx-background-color: transparent; -fx-tab-min-height: 42px; -fx-tab-max-height: 42px;");

        Tab productsTab = new Tab("Товары", buildProductsTab());
        Tab operationsTab = new Tab("Операции", buildOperationsTab());
        tabs.getTabs().addAll(productsTab, operationsTab);
        tabs.getTabs().forEach(tab -> tab.setClosable(false));
        return tabs;
    }

    private Parent buildProductsTab() {
        searchField.setPromptText("Поиск по названию");
        searchField.setPrefWidth(260);
        searchField.setStyle(controlStyle());
        searchField.getStyleClass().add("modern-text-field");

        categoryFilter.getItems().addAll(CATEGORY_OPTIONS);
        categoryFilter.getSelectionModel().selectFirst();
        categoryFilter.setStyle(controlStyle());
        categoryFilter.getStyleClass().add("modern-combo-box");

        unitFilter.getItems().addAll(UNIT_OPTIONS);
        unitFilter.getSelectionModel().selectFirst();
        unitFilter.setStyle(controlStyle());
        unitFilter.getStyleClass().add("modern-combo-box");

        sortFilter.getItems().addAll(
                new SortOption("По умолчанию", "id_desc"),
                new SortOption("Название: А-Я", "name_asc"),
                new SortOption("Количество: по возрастанию", "quantity_asc"),
                new SortOption("Количество: по убыванию", "quantity_desc"),
                new SortOption("Цена: по возрастанию", "price_asc"),
                new SortOption("Цена: по убыванию", "price_desc"),
                new SortOption("Срок годности: ближайшие", "expiration_asc")
        );
        sortFilter.getSelectionModel().selectLast();
        sortFilter.setStyle(controlStyle());
        sortFilter.getStyleClass().add("modern-combo-box");
        sortFilter.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(SortOption item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });
        sortFilter.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SortOption item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.label());
            }
        });

        Button refresh = coloredButton("Обновить");
        refresh.setOnAction(e -> refreshProducts());

        Button add = coloredButton("Добавить");
        add.setOnAction(e -> openProductDialog(null));

        Button edit = coloredButton("Редактировать");
        edit.setOnAction(e -> {
            Product selected = productsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Выберите товар");
                return;
            }
            openProductDialog(selected);
        });

        Button delete = coloredButton("Удалить");
        delete.setOnAction(e -> {
            Product selected = productsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Выберите товар");
                return;
            }
            if (confirm("Удалить товар «" + selected.getName() + "»?")) {
                try {
                    productService.deleteProduct(selected.getId(), currentUser);
                    refreshAll();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            }
        });

        Button supply = coloredButton("Поступление");
        supply.setOnAction(e -> {
            Product selected = productsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Выберите товар");
                return;
            }
            new QuantityDialog("Поступление товара", "Добавить", qty -> {
                try {
                    productService.supply(selected.getId(), qty, currentUser);
                    refreshAll();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        });

        Button sell = coloredButton("Продать");
        sell.setOnAction(e -> {
            Product selected = productsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Выберите товар");
                return;
            }
            new QuantityDialog("Продажа товара", "Продать", qty -> {
                try {
                    productService.sell(selected.getId(), qty, currentUser);
                    refreshAll();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        });

        Button writeOff = coloredButton("Списать");
        writeOff.setOnAction(e -> {
            Product selected = productsTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                showInfo("Выберите товар");
                return;
            }
            new QuantityDialog("Списание товара", "Списать", qty -> {
                try {
                    productService.writeOff(selected.getId(), qty, currentUser);
                    refreshAll();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
        });

        HBox filters = new HBox(10,
                new VBox(fieldLabel("Поиск"), searchField),
                new VBox(fieldLabel("Категория"), categoryFilter),
                new VBox(fieldLabel("Единица Измерения"), unitFilter),
                new VBox(fieldLabel("Сортировка"), sortFilter),
                new VBox(new Label(" "), refresh)
        );
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(12, 0, 12, 0));

        HBox actions = new HBox(10, add, edit, delete, supply, sell, writeOff);
        actions.setPadding(new Insets(4, 0, 12, 0));
        actions.setAlignment(Pos.CENTER_LEFT);

        configureProductsTable();

        Label expiringNote = new Label("Красным отмечены товары, у которых срок годности истекает в течение 3 дней");
        expiringNote.setStyle("-fx-text-fill: #b91c1c; -fx-font-weight: bold;");
        VBox panel = sectionPanel("Товары", filters, actions, expiringNote, productsTable);
        panel.setFillWidth(true);

        searchField.textProperty().addListener((obs, o, n) -> refreshProducts());
        categoryFilter.valueProperty().addListener((obs, o, n) -> refreshProducts());
        unitFilter.valueProperty().addListener((obs, o, n) -> refreshProducts());
        sortFilter.valueProperty().addListener((obs, o, n) -> refreshProducts());

        return panel;
    }

    private Parent buildOperationsTab() {
        operationFilter.getItems().addAll(OPERATION_OPTIONS);
        operationFilter.getSelectionModel().selectFirst();
        operationFilter.setStyle(controlStyle());
        operationFilter.getStyleClass().add("modern-combo-box");

        Button refresh = coloredButton("Обновить Историю");
        refresh.setOnAction(e -> refreshOperations());

        operationFilter.valueProperty().addListener((obs, o, n) -> refreshOperations());

        configureOperationsTable();

        HBox filters = new HBox(10,
                new VBox(fieldLabel("Фильтр Операций"), operationFilter),
                new VBox(new Label(" "), refresh)
        );
        filters.setAlignment(Pos.CENTER_LEFT);
        filters.setPadding(new Insets(12, 0, 12, 0));

        return sectionPanel("История операций", filters, operationsTable);
    }

    private VBox sectionPanel(String title, Node... content) {
        Label header = new Label(title);
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        VBox panel = new VBox(12);
        panel.getChildren().add(header);
        panel.getChildren().addAll(content);
        panel.setPadding(new Insets(16));
        panel.setStyle("-fx-background-color: rgba(255,255,255,0.78); -fx-background-radius: 18; -fx-border-radius: 18; -fx-border-color: rgba(148,163,184,0.35); -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.10), 18, 0.12, 0, 6);");
        return panel;
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        return label;
    }

    private Button coloredButton(String text) {
        Button button = new Button(text);
        button.setStyle("-fx-background-color: linear-gradient(to bottom, #3b82f6, #1d4ed8); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 14; -fx-border-radius: 14; -fx-padding: 10 16 10 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(15,23,42,0.12), 10, 0.1, 0, 3);");
        button.getStyleClass().add("gradient-button");
        return button;
    }

    private void configureProductsTable() {
        TableColumn<Product, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Product, String> nameCol = new TableColumn<>("Название");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Product, String> categoryCol = new TableColumn<>("Категория");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Product, Integer> qtyCol = new TableColumn<>("Количество");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<Product, String> unitCol = new TableColumn<>("Единица Измерения");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toLowerCase());
            }
        });
        TableColumn<Product, Double> priceCol = new TableColumn<>("Цена");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<Product, String> arrivalCol = new TableColumn<>("Поступление");
        arrivalCol.setCellValueFactory(new PropertyValueFactory<>("arrivalDate"));

        TableColumn<Product, String> expCol = new TableColumn<>("Срок годности");
        expCol.setCellValueFactory(new PropertyValueFactory<>("expirationDate"));

        TableColumn<Product, String> locCol = new TableColumn<>("Место хранения");
        locCol.setCellValueFactory(new PropertyValueFactory<>("location"));

        productsTable.getColumns().setAll(idCol, nameCol, categoryCol, qtyCol, unitCol, priceCol, arrivalCol, expCol, locCol);
        productsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productsTable.setPrefHeight(620);
        productsTable.setStyle(tableStyle());
        productsTable.getStyleClass().add("modern-table");
        productsTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Product item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setStyle("");
                } else if (expiringSoonProductIds.contains(item.getId())) {
                    setStyle("-fx-background-color: linear-gradient(to right, rgba(254,226,226,0.92), rgba(255,241,242,0.92)); -fx-text-fill: #7f1d1d; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    private void configureOperationsTable() {
        TableColumn<StockOperation, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<StockOperation, String> typeCol = new TableColumn<>("Тип");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("operationType"));
        typeCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(switch (item) {
                        case "SUPPLY" -> "Поступление";
                        case "SALE" -> "Продажа";
                        case "WRITE_OFF" -> "Списание";
                        default -> item;
                    });
                }
            }
        });

        TableColumn<StockOperation, String> productCol = new TableColumn<>("Товар");
        productCol.setCellValueFactory(new PropertyValueFactory<>("productName"));

        TableColumn<StockOperation, Integer> qtyCol = new TableColumn<>("Количество");
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        TableColumn<StockOperation, String> unitCol = new TableColumn<>("Единица Измерения");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.toLowerCase());
            }
        });

        TableColumn<StockOperation, Double> unitPriceCol = new TableColumn<>("Цена За Ед.");
        unitPriceCol.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));

        TableColumn<StockOperation, Double> totalCol = new TableColumn<>("Сумма");
        totalCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        TableColumn<StockOperation, String> dateCol = new TableColumn<>("Дата");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("operationDate"));

        TableColumn<StockOperation, String> noteCol = new TableColumn<>("Примечание");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));

        operationsTable.getColumns().setAll(idCol, typeCol, productCol, qtyCol, unitCol, unitPriceCol, totalCol, dateCol, noteCol);
        operationsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        operationsTable.setPrefHeight(620);
        operationsTable.setStyle(tableStyle());
        operationsTable.getStyleClass().add("modern-table");
    }

    private String tableStyle() {
        return "-fx-background-color: white; -fx-background-radius: 14; -fx-border-color: #dbe4f0; -fx-border-radius: 14; -fx-padding: 2;";
    }

    private void openProductDialog(Product existing) {
        try {
            ProductDialog dialog = new ProductDialog(existing == null ? "Добавить товар" : "Редактировать товар", existing, p -> {
                try {
                    if (existing == null) {
                        productService.addProduct(p, currentUser);
                    } else {
                        productService.updateProduct(p, currentUser);
                    }
                    refreshAll();
                } catch (Exception ex) {
                    showError(ex.getMessage());
                }
            });
            dialog.showAndWait();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    private void refreshAll() {
        refreshProducts();
        refreshOperations();
    }

    private void refreshProducts() {
        SortOption sortOption = sortFilter.getValue();
        List<Product> products = productService.findProducts(
                searchField.getText(),
                normalizeFilter(categoryFilter.getValue()),
                normalizeFilter(unitFilter.getValue()),
                sortOption == null ? null : sortOption.key()
        );
        expiringSoonProductIds.clear();
        for (Product product : productService.expiringWithinDays(3)) {
            expiringSoonProductIds.add(product.getId());
        }
        productsTable.setItems(FXCollections.observableArrayList(products));
    }

    private void refreshOperations() {
        operationsTable.setItems(FXCollections.observableArrayList(
                productService.findOperations(normalizeOperationFilter(operationFilter.getValue()))
        ));
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank() || value.startsWith("Все")) {
            return null;
        }
        return value;
    }

    private String normalizeOperationFilter(String value) {
        if (value == null || value.isBlank() || value.startsWith("Все")) {
            return null;
        }
        return switch (value) {
            case "Поступление" -> "SUPPLY";
            case "Продажа" -> "SALE";
            case "Списание" -> "WRITE_OFF";
            default -> value;
        };
    }

    private boolean confirm(String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        return alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Информация");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String controlStyle() {
        return "-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #cbd5e1; -fx-background-color: white; -fx-padding: 9 12 9 12;";
    }

    private record SortOption(String label, String key) {
        @Override
        public String toString() {
            return label;
        }
    }
}
