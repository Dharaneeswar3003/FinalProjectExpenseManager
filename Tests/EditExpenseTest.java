import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class EditExpenseTest extends ApplicationTest {

    private EditExpenseController editController;
    private final String fileName = "Food&Drinks.txt"; // Adjust file name based on implementation
    private Expense expense;   

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("EditExpense.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
        editController = loader.getController();
    }

    @Before
    public void setUp() throws Exception {
        // Simulate creating and saving a mock expense
        expense = new Expense("Groceries", 50.0, new Date(), "Food", 123456);
        // Add the expense to the file
        expense.writeToFile(fileName);
        tableController tableController = new tableController(); // Create an instance of TableController
        editController.setTableController(tableController); // Set the tableController instance in EditExpenseController
        editController.setEditedExpense(expense);
        
        // Fill the fields with the expense data
        editController.fillFields(expense);
        
    }
    @Test
    public void testEditTransactionWithValidInput() throws IOException {
        assertTrue(isExpenseInFile(expense));
    
        clickOn("#editpurposeField").eraseText(expense.getpurpose().length()).write("Updated Groceries"); // Rewrite the text
        clickOn("#editSumField").eraseText(String.valueOf(expense.getamount()).length()).write("75.0");
        clickOn("#editdatePicker").eraseText(9).write("5/22/2024");
    
        // Perform the edit action
        clickOn("#editButton");
    
        // Verify the changes in the expense object
        assertEquals("Updated Groceries", expense.getpurpose());
        assertEquals(75.0, expense.getamount(), 0.01); // Delta parameter for double comparison
        assertEquals(Date.from(LocalDate.of(2024, 5, 22).atStartOfDay(ZoneId.systemDefault()).toInstant()), expense.getdate());
    
        // Verify the changes are reflected in the file
        assertTrue(isExpenseInFile(expense));
    }
 
    @Test
    public void testEditTransactionWithInvalidInput() throws IOException {
        assertTrue(isExpenseInFile(expense));
    
        // Provide invalid input (empty purpose)
        clickOn("#editpurposeField").eraseText(expense.getpurpose().length());
        clickOn("#editSumField").eraseText(String.valueOf(expense.getamount()).length()).write("abc");
        clickOn("#editdatePicker").eraseText(9).write("5/30/2025");
    
        // Perform the edit action
        clickOn("#editButton");
    
        // Verify that the edit action is aborted due to invalid input
        assertTrue(isExpenseInFile(expense)); // Expense should not be edited
    }
    
    @Test
    public void testEditTransactionWithInvalidPurpose() throws IOException {
        assertTrue(isExpenseInFile(expense));
    
        // Provide invalid input (special characters in purpose)
        clickOn("#editpurposeField").eraseText(expense.getpurpose().length()).write("Groceries$%");
        clickOn("#editSumField").eraseText(String.valueOf(expense.getamount()).length()).write("75.0");
        clickOn("#editdatePicker").eraseText(9).write("5/22/2024");
    
        // Perform the edit action
        clickOn("#editButton");
    
        // Verify that the edit action is aborted due to invalid input
        assertTrue(isExpenseInFile(expense)); // Expense should not be edited
    }

    @Test
    public void testEditTransactionWithInvalidDate() throws IOException {
        assertTrue(isExpenseInFile(expense));
    
        // Provide invalid input (special characters in purpose)
        clickOn("#editpurposeField").eraseText(expense.getpurpose().length()).write("Updated Groceries");
        clickOn("#editSumField").eraseText(String.valueOf(expense.getamount()).length()).write("75.0");
        clickOn("#editdatePicker").eraseText(9).write("5/30/2025"); //Change date to be after now when running the test
    
        // Perform the edit action
        clickOn("#editButton");
    
        // Verify that the edit action is aborted due to invalid input
        assertTrue(isExpenseInFile(expense)); // Expense should not be edited
    }
    
    
    @Test
    public void testEditTransactionWithInvalidAmount() throws IOException {
        assertTrue(isExpenseInFile(expense));
    
        // Provide invalid input (non-numeric amount)
        clickOn("#editpurposeField").eraseText(expense.getpurpose().length()).write("Updated Groceries");
        clickOn("#editSumField").eraseText(String.valueOf(expense.getamount()).length()).write("abc");
        clickOn("#editdatePicker").eraseText(9).write("5/22/2024");
    
        // Perform the edit action
        clickOn("#editButton");
    
        // Verify that the edit action is aborted due to invalid input
        assertTrue(isExpenseInFile(expense)); // Expense should not be edited
    }
    
    @Test
    public void testEditTransactionWithoutCategory() throws IOException {
        assertTrue(isExpenseInFile(expense));
    
        // Remove category selection
        clickOn("#foodBox");
    
        // Perform the edit action
        clickOn("#editButton");
    
        // Verify that the edit action is aborted due to missing category
        assertTrue(isExpenseInFile(expense)); // Expense should not be edited
    }
    

    private boolean isExpenseInFile(Expense expense) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] transactions = line.split(";");
                for (String transaction : transactions) {
                    String[] parts = transaction.split(",");
                    if (parts.length == 5) {
                        String purpose = parts[0].trim();
                        double amount = Double.parseDouble(parts[1].trim());
                        String category = parts[2].trim();
                        Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(parts[3].trim());
                        int transactionID = Integer.parseInt(parts[4].trim());
                        if (transactionID == expense.gettransactionID()) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @After
    public void tearDown() throws Exception {
        // Clean up the file after the test
       // editController.deleteExpenseFromFile(fileName, expense);
    }
}

