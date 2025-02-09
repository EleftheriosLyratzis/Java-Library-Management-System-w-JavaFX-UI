//Eleftherios Lyratzis

package ellyrat.cs.duth.askhsh70;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class InventoryManagementApp extends Application {
    
    private BookFileManager manager;
    // For the search tab: a ListView to display search results.
    private ListView<String> searchResultsList;
    // List holding file positions for the search results.
    private List<Long> searchResultPositions;  
    // Fields for the "Add Book" pane.
    private TextField addIsbnField, addTitleField, addAuthorField, addPriceField, addPagesField, addFileSizeField;
    private TextField searchField;
    
@Override
public void start(Stage primaryStage) {
    try {
        // Open (or create) the random access file "books.dat"
        manager = new BookFileManager("books.dat");
        // If the file is empty, preload books.
        if (manager.isEmpty()) {
            List<Book> preloaded = new ArrayList<>();
            PreloadBooks.preloadBooks(preloaded);
            for (Book b : preloaded) {
                try {
                    manager.addBook(b);
                } catch (IOException ex) {
                    System.err.println("Error preloading book: " + ex.getMessage());
                }
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
        return;
    }
    
    TabPane tabPane = new TabPane();
    tabPane.getStyleClass().add("tab-pane-custom");
    
    Tab tabAdd = new Tab("Add Book", createAddBookPane());
    Tab tabList = new Tab("List Books", createListBooksPane());
    Tab tabSearch = new Tab("Search Book", createSearchBookPane());
    
    tabPane.getTabs().addAll(tabAdd, tabList, tabSearch);
    // Prevent the tabs from being closed by the user.
    tabPane.getTabs().forEach(tab -> tab.setClosable(false));
    
    // Create a scene with larger dimensions, e.g., 1000x800.
    Scene scene = new Scene(tabPane, 1000, 800);
    // Load external CSS file.
    scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
    
    primaryStage.setTitle("Inventory Management System");
    primaryStage.setScene(scene);
    primaryStage.show();
}

    
    // Pane to add a new book.
    private Pane createAddBookPane() {
        VBox vbox = new VBox(10);
        // Assign a style class for this pane.
        vbox.getStyleClass().add("add-book-pane");
        vbox.setPadding(new Insets(10));
        
        addIsbnField = new TextField();
        addTitleField = new TextField();
        addAuthorField = new TextField();
        addPriceField = new TextField();
        addPagesField = new TextField();
        addFileSizeField = new TextField();
        
        Button addButton = new Button("Add Book");
        addButton.getStyleClass().add("button-add");
        
        Label statusLabel = new Label();
        statusLabel.getStyleClass().add("error-label");
        
        addButton.setOnAction(e -> {
            String isbn = addIsbnField.getText().trim();
            String title = addTitleField.getText().trim();
            String author = addAuthorField.getText().trim();
            String priceStr = addPriceField.getText().trim();
            String pagesStr = addPagesField.getText().trim();
            String fileSizeStr = addFileSizeField.getText().trim();
            
            if (isbn.isEmpty() || title.isEmpty() || author.isEmpty() || priceStr.isEmpty()) {
                statusLabel.setText("ISBN, Title, Author, and Price cannot be empty.");
                return;
            }
            
            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException ex) {
                statusLabel.setText("Invalid price.");
                return;
            }
            
            Integer pages = null;
            if (!pagesStr.isEmpty()) {
                try {
                    pages = Integer.parseInt(pagesStr);
                } catch (NumberFormatException ex) {
                    statusLabel.setText("Invalid pages number.");
                    return;
                }
            }
            
            Double fileSize = null;
            if (!fileSizeStr.isEmpty()) {
                try {
                    fileSize = Double.parseDouble(fileSizeStr);
                } catch (NumberFormatException ex) {
                    statusLabel.setText("Invalid file size.");
                    return;
                }
            }
            
            // Enforce that only one of pages or fileSize is provided.
            if (pages != null && fileSize != null) {
                statusLabel.setText("Only one of Pages or File Size should be provided.");
                return;
            }
            if (pages == null && fileSize == null) {
                statusLabel.setText("Either Pages (printed book) or File Size (electronic book) must be provided.");
                return;
            }
            
            // Create a new book using the updated constructor.
            Book book = new Book(title, author, isbn, price, pages, fileSize);
            try {
                manager.addBook(book);
                statusLabel.setText("Book added successfully.");
                addIsbnField.clear();
                addTitleField.clear();
                addAuthorField.clear();
                addPriceField.clear();
                addPagesField.clear();
                addFileSizeField.clear();
            } catch (IOException ex) {
                statusLabel.setText(ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        vbox.getChildren().addAll(
                new Label("ISBN:"), addIsbnField, 
                new Label("Title:"), addTitleField, 
                new Label("Author:"), addAuthorField,
                new Label("Price:"), addPriceField,
                new Label("Pages (leave empty for electronic books):"), addPagesField,
                new Label("File Size in MB (leave empty for printed books):"), addFileSizeField,
                addButton, statusLabel);
        return vbox;
    }
    
    // Pane to list all books.
    private Pane createListBooksPane() {
        VBox vbox = new VBox(10);
        vbox.getStyleClass().add("list-books-pane");
        vbox.setPadding(new Insets(10));
        ListView<String> listView = new ListView<>();
        Button refreshButton = new Button("Refresh List");
        refreshButton.getStyleClass().add("button-refresh");
        
        refreshButton.setOnAction(e -> {
            listView.getItems().clear();
            try {
                List<Book> books = manager.getAllBooks();
                for (Book book : books) {
                    listView.getItems().add(book.toString());
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        
        vbox.getChildren().addAll(refreshButton, listView);
        return vbox;
    }
    
    // Pane for searching books and then updating or deleting the selected one.
    private Pane createSearchBookPane() {
        VBox vbox = new VBox(10);
        vbox.getStyleClass().add("search-books-pane");
        vbox.setPadding(new Insets(10));
        
        // --- New: Add a ComboBox for selecting search criteria ---
        HBox criteriaBox = new HBox(10);
        Label criteriaLabel = new Label("Search By:");
        ComboBox<String> criteriaCombo = new ComboBox<>();
        criteriaCombo.getItems().addAll("Title", "ISBN", "Author");
        criteriaCombo.setValue("Title");
        criteriaBox.getChildren().addAll(criteriaLabel, criteriaCombo);
        
        // Top part: search input.
        HBox searchBox = new HBox(10);
        searchField = new TextField();
        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("button-search");
        searchBox.getChildren().addAll(new Label("Search:"), searchField, searchButton);
        
        // List to display the search results.
        searchResultsList = new ListView<>();
        
        // Pane for editing the selected book.
        VBox editPane = new VBox(10);
        TextField editIsbnField = new TextField();
        TextField editTitleField = new TextField();
        TextField editAuthorField = new TextField();
        TextField editPriceField = new TextField();
        TextField editPagesField = new TextField();
        TextField editFileSizeField = new TextField();
        Button updateButton = new Button("Update");
        updateButton.getStyleClass().add("button-update");
        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("button-delete");
        Label editStatusLabel = new Label();
        editStatusLabel.getStyleClass().add("error-label");
        
        editPane.getChildren().addAll(
                new Label("ISBN:"), editIsbnField, 
                new Label("Title:"), editTitleField, 
                new Label("Author:"), editAuthorField,
                new Label("Price:"), editPriceField,
                new Label("Pages (leave empty for electronic books):"), editPagesField,
                new Label("File Size in MB (leave empty for printed books):"), editFileSizeField,
                updateButton, deleteButton, editStatusLabel);
        
        // When a search result is selected, load its details for editing.
        searchResultsList.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int index = newVal.intValue();
            if (index >= 0 && searchResultPositions != null && index < searchResultPositions.size()) {
                long pos = searchResultPositions.get(index);
                try {
                    Book book = manager.getBookAt(pos);
                    editIsbnField.setText(book.getIsbn());
                    editTitleField.setText(book.getTitle());
                    editAuthorField.setText(book.getAuthor());
                    editPriceField.setText(String.valueOf(book.getPrice()));
                    editPagesField.setText(book.getPages() == null ? "" : String.valueOf(book.getPages()));
                    editFileSizeField.setText(book.getFileSize() == null ? "" : String.valueOf(book.getFileSize()));
                    // Save the record's file position for update/delete operations.
                    editPane.setUserData(pos);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        // Perform search based on the chosen criteria.
        searchButton.setOnAction(e -> {
            String query = searchField.getText().trim();
            String criteria = criteriaCombo.getValue(); // "Title", "ISBN", or "Author"
            searchResultsList.getItems().clear();
            searchResultPositions = manager.searchBooks(query, criteria);
            for (long pos : searchResultPositions) {
                try {
                    Book book = manager.getBookAt(pos);
                    searchResultsList.getItems().add(book.toString());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        
        // Update the selected book.
        updateButton.setOnAction(e -> {
            Object data = editPane.getUserData();
            if (data == null) {
                editStatusLabel.setText("No book selected.");
                return;
            }
            long pos = (long) data;
            String newIsbn = editIsbnField.getText().trim();
            String newTitle = editTitleField.getText().trim();
            String newAuthor = editAuthorField.getText().trim();
            String priceStr = editPriceField.getText().trim();
            String pagesStr = editPagesField.getText().trim();
            String fileSizeStr = editFileSizeField.getText().trim();
            
            if (newIsbn.isEmpty() || newTitle.isEmpty() || newAuthor.isEmpty() || priceStr.isEmpty()) {
                editStatusLabel.setText("ISBN, Title, Author, and Price cannot be empty.");
                return;
            }
            
            double newPrice;
            try {
                newPrice = Double.parseDouble(priceStr);
            } catch (NumberFormatException ex) {
                editStatusLabel.setText("Invalid price.");
                return;
            }
            
            Integer newPages = null;
            if (!pagesStr.isEmpty()) {
                try {
                    newPages = Integer.parseInt(pagesStr);
                } catch (NumberFormatException ex) {
                    editStatusLabel.setText("Invalid pages number.");
                    return;
                }
            }
            
            Double newFileSize = null;
            if (!fileSizeStr.isEmpty()) {
                try {
                    newFileSize = Double.parseDouble(fileSizeStr);
                } catch (NumberFormatException ex) {
                    editStatusLabel.setText("Invalid file size.");
                    return;
                }
            }
            
            // Check that only one of pages or fileSize is provided.
            if (newPages != null && newFileSize != null) {
                editStatusLabel.setText("Only one of Pages or File Size should be provided.");
                return;
            }
            if (newPages == null && newFileSize == null) {
                editStatusLabel.setText("Either Pages or File Size must be provided.");
                return;
            }
            
            try {
                Book updatedBook = new Book(newTitle, newAuthor, newIsbn, newPrice, newPages, newFileSize);
                manager.updateBook(pos, updatedBook);
                editStatusLabel.setText("Book updated.");
            } catch (IOException ex) {
                editStatusLabel.setText(ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        // Delete the selected book.
        deleteButton.setOnAction(e -> {
            Object data = editPane.getUserData();
            if (data == null) {
                editStatusLabel.setText("No book selected.");
                return;
            }
            long pos = (long) data;
            try {
                manager.deleteBook(pos);
                editStatusLabel.setText("Book deleted.");
                // Optionally, refresh the search results.
                searchButton.fire();
            } catch (IOException ex) {
                ex.printStackTrace();
                editStatusLabel.setText("Error deleting book.");
            }
        });
        
        vbox.getChildren().addAll(criteriaBox, searchBox, searchResultsList, new Separator(), editPane);
        return vbox;
    }
    
    // When the application is closing, ensure the file is properly closed.
    @Override
    public void stop() throws Exception {
        super.stop();
        if (manager != null) {
            manager.close();
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
