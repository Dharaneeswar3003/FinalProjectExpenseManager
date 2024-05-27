import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

import java.io.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EditExpenseController {
    private tableController tableController;
    private Expense editedExpense;

    @FXML
    private TableView<Expense> expenseTable;

    @FXML private CheckBox billsBox, expenseCheckBox, foodBox, incomeCheckBox, othersBox, shoppingBox;
    @FXML private HBox categoryBox;
    @FXML private Button deleteButton, editButton;
    @FXML private TextField editSumField, editpurposeField;
    @FXML private DatePicker editdatePicker;

    public void setEditedExpense(Expense expense) {
        this.editedExpense = expense;
    }

    public void setTableController(tableController controller) {
      this.tableController = controller;
    }

    @FXML
    void deleteTransaction(ActionEvent event) {
        String category = editedExpense.getcategory();
        String fileName = getFileNameForCategory(category);
        if (fileName != null) {
            try {
                deleteExpenseFromFile(fileName, editedExpense);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        tableController.removeExpense(editedExpense);
        closeStage();
    }

    @FXML
    void editTransaction(ActionEvent event) {
        String purpose = editpurposeField.getText();
        String sumText = editSumField.getText();
        LocalDate selectedDate = editdatePicker.getValue();
    
        // Validate purpose
        if (purpose.isEmpty() || !purpose.matches("[a-zA-Z0-9\\s]+")) {
            showAlert("Invalid Purpose", "Purpose must contain only alphanumeric characters.");
            return;
        }
    
        // Validate sum
        double amount;
        try {
            amount = Double.parseDouble(sumText);
        } catch (NumberFormatException e) {
            showAlert("Invalid Amount", "Please enter a valid amount.");
            return;
        }

        if (selectedDate == null || selectedDate.isAfter(LocalDate.now())) {
            showAlert("Invalid Date", "Please select a valid date.");
            return;
        }
    
        // Validate if only one category is selected
        if (getSelectedCheckboxCount() != 1) {
            showAlert("Invalid Category", "Please select only one category.");
            return;
        }
    
        // Update expense
        editedExpense.setpurpose(purpose);
        editedExpense.setamount(amount);
        editedExpense.setdate(Date.from(selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        editedExpense.setcategory(determineCategory());
    
        // Save expense to file and refresh table
        saveExpenseToFile(editedExpense);
        tableController.refreshTable();
        closeStage();
    }
    
    private int getSelectedCheckboxCount() {
        int count = 0;
        if (foodBox.isSelected()) count++;
        if (shoppingBox.isSelected()) count++;
        if (billsBox.isSelected()) count++;
        if (othersBox.isSelected()) count++;
        if (incomeCheckBox.isSelected()) count++;
        return count;
    }
    

private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}


    private void saveExpenseToFile(Expense expense) {
        String fileName = getFileNameForCategory(expense.getcategory());
        if (fileName != null) {
            try {
                deleteExpenseFromFile(fileName, expense);
                expense.writeToFile(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void deleteExpenseFromFile(String fileName, Expense expense) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 5 && !parts[4].trim().replace(";", "").equals(String.valueOf(expense.gettransactionID()))) {
                    lines.add(line);
                }
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            for (String newLine : lines) {
                writer.write(newLine);
                writer.newLine();
            }
        }
    }

    private String determineCategory() {
        if (foodBox.isSelected()) return "Food";
        if (shoppingBox.isSelected()) return "Shopping";
        if (billsBox.isSelected()) return "Bills";
        if (othersBox.isSelected()) return "Other";
        if (incomeCheckBox.isSelected()) return "Revenue";
        return "";
    }

    private String getFileNameForCategory(String category) {
        switch (category) {
            case "Food": return "Food&Drinks.txt";
            case "Shopping": return "Shopping.txt";
            case "Bills": return "Bills&Utilities.txt";
            case "Other": return "OtherExpenses.txt";
            case "Revenue": return "Income.txt";
            default: return null;
        }
    }

    public void fillFields(Expense expense) {
        editpurposeField.setText(expense.getpurpose());
        editSumField.setText(String.valueOf(expense.getamount()));
        editdatePicker.setValue(expense.getdate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

        switch (expense.getcategory()) {
            case "Food": setCategoryFields(foodBox, true); break;
            case "Shopping": setCategoryFields(shoppingBox, true); break;
            case "Bills": setCategoryFields(billsBox, true); break;
            case "Other": setCategoryFields(othersBox, true); break;
            case "Revenue": setIncomeFields(true); break;
        }
    }

    private void setCategoryFields(CheckBox categoryBox, boolean isExpense) {
        categoryBox.setSelected(true);
        expenseCheckBox.setSelected(isExpense);
        incomeCheckBox.setDisable(isExpense);
    }

    private void setIncomeFields(boolean isIncome) {
        incomeCheckBox.setSelected(isIncome);
        expenseCheckBox.setDisable(isIncome);
        foodBox.setDisable(isIncome);
        shoppingBox.setDisable(isIncome);
        billsBox.setDisable(isIncome);
        othersBox.setDisable(isIncome);
    }

    public tableController getTableController() {
        return tableController;
    }

    private void closeStage() {
        Stage stage = (Stage) deleteButton.getScene().getWindow();
        stage.close();
    }
}
