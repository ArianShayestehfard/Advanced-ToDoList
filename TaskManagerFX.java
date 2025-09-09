package com.example.todolist;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;

public class TaskManagerFX extends Application {
    private static final String TASK_FILE = "tasks.csv";
    private static final String USER_FILE = "user.cred";

    private Stage primaryStage;
    private Scene loginScene;
    private Scene mainScene;
    private ObservableList<TaskList> taskData = FXCollections.observableArrayList();
    private TableView<TaskList> taskTable;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Task Manager");

        createLoginScene();
        createMainScene();

        primaryStage.setScene(loginScene);
        primaryStage.show();
    }

    private void createLoginScene() {
        Label titleLabel = new Label("Task Manager Login");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label userLabel = new Label("Username:");
        TextField userField = new TextField();
        userField.setPromptText("Enter username");

        Label passLabel = new Label("Password:");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter password");

        Button loginButton = new Button("Login");
        Label statusLabel = new Label();

        loginButton.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();

            if (login(username, password)) {
                statusLabel.setText("Login successful!");
                statusLabel.setStyle("-fx-text-fill: green;");
                loadTasks();
                primaryStage.setScene(mainScene);
            } else {
                statusLabel.setText("Login failed. Please try again.");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
        });

        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(20));
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.getChildren().addAll(
                titleLabel, userLabel, userField, passLabel,
                passField, loginButton, statusLabel
        );

        loginScene = new Scene(loginLayout, 450, 400);
    }

    private void createMainScene() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem saveItem = new MenuItem("Save and Exit");
        saveItem.setOnAction(e -> {
            saveTasks();
            primaryStage.close();
        });
        fileMenu.getItems().add(saveItem);
        menuBar.getMenus().add(fileMenu);

        taskTable = new TableView<>();

        TableColumn<TaskList, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getDescription()));

        TableColumn<TaskList, String> deadlineCol = new TableColumn<>("Deadline");
        deadlineCol.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().getDeadline()));

        TableColumn<TaskList, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> javafx.beans.binding.Bindings.createStringBinding(() -> cellData.getValue().isDone() ? "DONE" : "TODO"));

        taskTable.getColumns().addAll(descCol, deadlineCol, statusCol);
        taskTable.setItems(taskData);

        Button addButton = new Button("Add New Task");
        Button markDoneButton = new Button("Mark as Done");
        Button refreshButton = new Button("Refresh");

        addButton.setOnAction(e -> showAddTaskDialog());
        markDoneButton.setOnAction(e -> markTaskDone());
        refreshButton.setOnAction(e -> refreshTaskList());

        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(10));
        buttonBox.getChildren().addAll(addButton, markDoneButton, refreshButton);

        BorderPane mainLayout = new BorderPane();
        mainLayout.setTop(menuBar);
        mainLayout.setCenter(taskTable);
        mainLayout.setBottom(buttonBox);

        mainScene = new Scene(mainLayout, 600, 400);
    }

    private void showAddTaskDialog() {
        Dialog<TaskList> dialog = new Dialog<>();
        dialog.setTitle("Add New Task");
        dialog.setHeaderText("Enter task details:");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField descField = new TextField();
        descField.setPromptText("Task description");
        TextField deadlineField = new TextField();
        deadlineField.setPromptText("YYYY-MM-DD");

        grid.add(new Label("Description:"), 0, 0);
        grid.add(descField, 1, 0);
        grid.add(new Label("Deadline:"), 0, 1);
        grid.add(deadlineField, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new TaskList(descField.getText(), deadlineField.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(task -> {
            taskData.add(task);
            saveTasks();
        });
    }

    private void markTaskDone() {
        TaskList selectedTask = taskTable.getSelectionModel().getSelectedItem();
        if (selectedTask != null) {
            selectedTask.markDone();
            taskTable.refresh();
            saveTasks();
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText("No Task Selected");
            alert.setContentText("Please select a task to mark as done.");
            alert.showAndWait();
        }
    }

    private void refreshTaskList() {
        taskTable.refresh();
    }

    private boolean login(String username, String password) {
        File userFile = new File(USER_FILE);
        if (userFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
                String savedUsername = reader.readLine();
                String savedPassword = reader.readLine();
                return username.equals(savedUsername) && password.equals(savedPassword);
            } catch (IOException e) {
                return false;
            }
        } else {
            try (PrintWriter writer = new PrintWriter(new FileWriter(USER_FILE))) {
                writer.println(username);
                writer.println(password);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    private void loadTasks() {
        taskData.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(TASK_FILE))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    TaskList task = new TaskList(parts[0], parts[1]);
                    if (Boolean.parseBoolean(parts[2])) task.markDone();
                    taskData.add(task);
                }
            }
        } catch (IOException e) {
        }
    }

    private void saveTasks() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(TASK_FILE))) {
            for (TaskList t : taskData) {
                writer.printf("%s,%s,%b%n", t.getDescription(), t.getDeadline(), t.isDone());
            }
        } catch (IOException e) {
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}