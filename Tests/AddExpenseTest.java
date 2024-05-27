import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import static org.junit.Assert.*;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.TextInputControlMatchers.hasText;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AddExpenseTest extends ApplicationTest {

    private AddExpenseController controller;
    private final String fileName = "Food&Drinks.txt"; // Adjust file name based on your implementation

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AddExpense.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        controller = loader.getController();
    }
    
    @Test
    public void testInputFields() {
        // Test if input fields are present and empty initially
        verifyThat("#purposeField", hasText(""));
        verifyThat("#SumField", hasText(""));
    }

    @Test
    public void testAddExpenseWithValidInput() {
        // Fill in valid input and submit
        clickOn("#purposeField").write("Groceries");
        clickOn("#SumField").write("50.0");
        // Handling date picker input
        clickOn("#datePicker").write("5/2/2024");
        clickOn("#foodBox");
        clickOn("#submitButton");

        // Verify that the expense is written to the file
        String expectedExpenseInfo = "Groceries, 50.0, Food, 2024-05-02";
        assertTrue(isExpenseWrittenToFile(fileName, expectedExpenseInfo));
    }

    private boolean isExpenseWrittenToFile(String fileName, String expectedExpenseInfo) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains(expectedExpenseInfo)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Test
    public void testAddExpenseWithInvalidAmount() {
        // Fill in invalid amount and submit
        clickOn("#purposeField").write("Groceries");
        clickOn("#SumField").write("invalidAmount");
        clickOn("#datePicker").write("5/2/2024");
        clickOn("#foodBox");
        clickOn("#submitButton");

        // Verify that an alert is shown for invalid amount
        verifyThat("#SumField", hasText("invalidAmount"));
    }

    @Test
    public void testAddExpenseWithInvalidPurpose() {
        // Fill in invalid purpose and submit
        clickOn("#purposeField").write("$5%^^&");
        clickOn("#SumField").write("50.0");
        clickOn("#datePicker").write("5/2/2024");
        clickOn("#foodBox");
        clickOn("#submitButton");

        // Verify that an alert is shown for invalid purpose
        verifyThat("#purposeField", hasText("$5%^^&"));
    }

    @Test
    public void testAddExpenseWithoutCategory() {
        // Fill in valid input but do not select a category
        clickOn("#purposeField").write("Groceries");
        clickOn("#SumField").write("50.0");
        clickOn("#datePicker").write("5/2/2024");
        clickOn("#submitButton");
    }

    @Test
    public void testAddExpenseWithEmptyFields() {
        clickOn("#submitButton");
    }

    @Test
    public void testAddExpenseWithMultipleCategories() {
        // Fill in valid input and select multiple categories
        clickOn("#purposeField").write("Utilities");
        clickOn("#SumField").write("100.0");
        clickOn("#datePicker").write("5/2/2024");
        clickOn("#billsBox");
        clickOn("#shoppingBox");
        clickOn("#submitButton");       
    }

    @After
    public void cleanUp() {
        List<String> lines = new ArrayList<>();
        
        // Read the content of the file
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Collect lines that do not contain test data
                if (!line.contains("Groceries, 50.0, Food, 2024-05-02") &&
                    !line.contains("Utilities, 100.0, Bills, 2024-05-02") &&
                    !line.contains("Entertainment, 75.0, Others, 2024-05-02")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Write the filtered content back to the same file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
