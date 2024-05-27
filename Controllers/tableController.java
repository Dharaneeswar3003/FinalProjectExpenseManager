import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;

public class tableController {

    @FXML
    private DatePicker fromDateFilter, toDateFilter;
    @FXML
    private CheckBox incomeFilter, expenseFilter;
    @FXML
    private ComboBox<String> categoryFilter;
    @FXML
    private TableView<Expense> expenseTable;
    @FXML
    private TableColumn<Expense, String> purposeColumn, categoryColumn;
    @FXML
    private TableColumn<Expense, Number> sumColumn, idColumn;
    @FXML
    private TableColumn<Expense, Date> dateColumn;

    public void initialize() {
        categoryFilter.getItems().addAll("Food", "Shopping", "Bills", "Other", "All");
        categoryFilter.setValue("All");

        // Set cell value factories for each column
        purposeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getpurpose()));
        categoryColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getcategory()));
        sumColumn.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getamount()));
        idColumn.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().gettransactionID()));
        dateColumn.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getdate()));

        dateColumn.setCellFactory(column -> new TableCell<Expense, Date>() {
            private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : format.format(item));
            }
        });

        sumColumn.setCellFactory(column -> new TableCell<Expense, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    String text = item.toString();
                    Expense expense = getTableRow().getItem();
                    if (expense != null) {
                        text = expense.getcategory().equals("Revenue") ? "+" + text : "-" + text;
                        setTextFill(expense.getcategory().equals("Revenue") ? Color.GREEN : Color.RED);
                    }
                    setText(text);
                }
            }
        });

        ObservableList<Expense> allExpenses = FXCollections.observableArrayList();
        loadExpensesFromFiles(allExpenses, "Food&Drinks.txt", "Income.txt", "Shopping.txt", "Bills&Utilities.txt", "OtherExpenses.txt");

        FilteredList<Expense> filteredExpenses = new FilteredList<>(allExpenses);
        expenseTable.setItems(filteredExpenses);

        allExpenses.sort(Comparator.comparing(Expense::getdate).reversed());

        fromDateFilter.setOnAction(event -> applyFilters(filteredExpenses));
        toDateFilter.setOnAction(event -> applyFilters(filteredExpenses));
        incomeFilter.setOnAction(event -> applyFilters(filteredExpenses));
        expenseFilter.setOnAction(event -> applyFilters(filteredExpenses));
        categoryFilter.setOnAction(event -> applyFilters(filteredExpenses));

        expenseTable.setRowFactory(tv -> {
            TableRow<Expense> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && !row.isEmpty()) {
                    openEditWindow(row.getItem());
                }
            });
            return row;
        });
    }

    private void applyFilters(FilteredList<Expense> filteredExpenses) {
        filteredExpenses.setPredicate(expense -> {
            Date fromDate = fromDateFilter.getValue() != null ? Date.from(fromDateFilter.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
            Date toDate = toDateFilter.getValue() != null ? Date.from(toDateFilter.getValue().atStartOfDay(ZoneId.systemDefault()).toInstant()) : null;
            boolean dateInRange = (fromDate == null || !expense.getdate().before(fromDate)) && (toDate == null || !expense.getdate().after(toDate));

            boolean isIncome = incomeFilter.isSelected();
            boolean isExpense = expenseFilter.isSelected();
            boolean incomeCondition = isIncome && expense.getcategory().equals("Revenue");
            boolean expenseCondition = isExpense && !expense.getcategory().equals("Revenue");

            String selectedCategory = categoryFilter.getValue();
            boolean categoryMatch = selectedCategory.equals("All") || expense.getcategory().equals(selectedCategory);

            return dateInRange && (incomeCondition || expenseCondition || (!isIncome && !isExpense)) && categoryMatch;
        });
    }

    public void loadExpensesFromFiles(ObservableList<Expense> expenses, String... fileNames) {
        for (String fileName : fileNames) {
            try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] transactions = line.split(";");
                    for (String transaction : transactions) {
                        String[] parts = transaction.split(",");
                        if (parts.length == 5) {
                            String purpose = parts[0].trim();
                            double amount = Double.parseDouble(parts[1].trim());
                            String category = parts[2].trim();
                            Date date = new SimpleDateFormat("yyyy-MM-dd").parse(parts[3].trim());
                            int transactionID = Integer.parseInt(parts[4].trim());
                            expenses.add(new Expense(purpose, amount, date, category, transactionID));
                        }
                    }
                }
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                System.err.println("Error loading expenses from file: " + fileName);
            }
        }
    }

    private void openEditWindow(Expense expense) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditExpense.fxml"));
            Parent root = loader.load();
            EditExpenseController editController = loader.getController();
            editController.setTableController(this);
            editController.setEditedExpense(expense);
            editController.fillFields(expense);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to open edit window.");
        }
    }

    public void refreshTable() {
        ObservableList<Expense> allExpenses = FXCollections.observableArrayList();
        loadExpensesFromFiles(allExpenses, "Food&Drinks.txt", "Income.txt", "Shopping.txt", "Bills&Utilities.txt", "OtherExpenses.txt");
        if(expenseTable != null){
        expenseTable.setItems(allExpenses);
        expenseTable.refresh();
        }
        allExpenses.sort(Comparator.comparing(Expense::getdate).reversed());
    }

    public void removeExpense(Expense expense) {
        ObservableList<Expense> newItems = FXCollections.observableArrayList(expenseTable.getItems());
        if(expenseTable != null){
        newItems.remove(expense);
        expenseTable.setItems(newItems);
        }
    }
    

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
