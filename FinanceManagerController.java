import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.StackPane;

public class FinanceManagerController {

    @FXML
    private Button addCashButton;

    @FXML
    private Button dashboardButton;

    @FXML
    private Button quitButton;

    @FXML
    private StackPane mainStackPane;

    private Alert alert;

    @FXML
    public void initialize() {
        //Initialize to set the main stack pane to the dashboard/home which includes table-view and add expense window...
        loadView("Home.fxml");
    }

    @FXML
    void addCash(ActionEvent event) {
        //Open the add cash window...
        loadView("AddCash.fxml");
    }

    @FXML
    void openDashboard(ActionEvent event) {
        //Reloads/opens the dashboard...
        loadView("Home.fxml");
    }

    @FXML
    public void quit(ActionEvent event) {
        // Opens a confirmation alert box...
        alert = new Alert(Alert.AlertType.CONFIRMATION); // Initialize the alert
        alert.setTitle("Confirmation Dialog");
        alert.setHeaderText("Quit");
        alert.setContentText("Are you sure you want to quit?");
        ButtonType yesButton = new ButtonType("Yes");
        ButtonType noButton = new ButtonType("No");
        alert.getButtonTypes().setAll(yesButton, noButton);
        alert.showAndWait().ifPresent(response -> {
            if (response == yesButton) {
                System.out.println("Exited the program successfully!");
                // If user clicks on yes button, the program will close.
                System.exit(0);
            }
            // If user clicks on no button then the program will pick up where it left off.
        });
    }

    //Method to load FXML files...
    private void loadView(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent createTaskRoot = loader.load();
            mainStackPane.getChildren().clear();
            mainStackPane.getChildren().add(createTaskRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

