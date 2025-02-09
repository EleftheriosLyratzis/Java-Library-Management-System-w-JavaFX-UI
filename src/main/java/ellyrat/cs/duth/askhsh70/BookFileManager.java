
package ellyrat.cs.duth.askhsh70;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

public class BookFileManager {
    private RandomAccessFile file;
    // Maps ISBN to file position (byte offset)
    private Map<String, Long> isbnIndex;
    // Maps file position to title (for search by title)
    private Map<Long, String> recordTitles;

    public BookFileManager(String filename) throws IOException {
        file = new RandomAccessFile(filename, "rw");
        isbnIndex = new HashMap<>();
        recordTitles = new HashMap<>();
        loadIndex();
    }

    // Reads the entire file and builds the in-memory indexes.
    public void loadIndex() throws IOException {
        long pos = 0;
        file.seek(0);
        while (pos < file.length()) {
            file.seek(pos);
            Book book = Book.readRecord(file);
            // A nonempty ISBN indicates an active record.
            if (!book.getIsbn().trim().isEmpty()) {
                isbnIndex.put(book.getIsbn(), pos);
                recordTitles.put(pos, book.getTitle());
            }
            pos += Book.RECORD_SIZE;
        }
    }

    /**
     * Checks if the file is empty.
     */
    public boolean isEmpty() throws IOException {
        return file.length() == 0;
    }

    /**
     * Adds a new book record if the ISBN is unique.
     */
    public void addBook(Book book) throws IOException {
        if (isbnIndex.containsKey(book.getIsbn())) {
            throw new IOException("A book with ISBN " + book.getIsbn() + " already exists.");
        }
        long pos = file.length();
        file.seek(pos);
        book.writeRecord(file);
        isbnIndex.put(book.getIsbn(), pos);
        recordTitles.put(pos, book.getTitle());
    }

    /**
     * Updates the record at the given position, ensuring ISBN uniqueness.
     */
    public void updateBook(long pos, Book book) throws IOException {
        Book currentBook = getBookAt(pos);
        if (!currentBook.getIsbn().equals(book.getIsbn()) && isbnIndex.containsKey(book.getIsbn())) {
            throw new IOException("A book with ISBN " + book.getIsbn() + " already exists.");
        }
        file.seek(pos);
        book.writeRecord(file);
        // Update indexes
        isbnIndex.entrySet().removeIf(e -> e.getValue() == pos);
        isbnIndex.put(book.getIsbn(), pos);
        recordTitles.put(pos, book.getTitle());
    }

    /**
     * "Deletes" a record by writing an empty ISBN.
     */
    public void deleteBook(long pos) throws IOException {
        file.seek(pos);
        // Write an empty ISBN (padded) to mark the record as deleted.
        Book.writeFixedString(file, "", Book.ISBN_LENGTH);
        isbnIndex.entrySet().removeIf(e -> e.getValue() == pos);
        recordTitles.remove(pos);
    }

    /**
     * Returns a list of all active book records.
     */
    public List<Book> getAllBooks() throws IOException {
        List<Book> books = new ArrayList<>();
        for (Long pos : recordTitles.keySet()) {
            file.seek(pos);
            Book book = Book.readRecord(file);
            books.add(book);
        }
        return books;
    }

    /**
     * Searches by (part of) the title.
     */
    public List<Long> searchByTitle(String partial) {
        List<Long> positions = new ArrayList<>();
        for (Map.Entry<Long, String> entry : recordTitles.entrySet()) {
            if (entry.getValue().toLowerCase().contains(partial.toLowerCase())) {
                positions.add(entry.getKey());
            }
        }
        return positions;
    }

    /**
     * Retrieves a book at a given file position.
     */
    public Book getBookAt(long pos) throws IOException {
        file.seek(pos);
        return Book.readRecord(file);
    }

    public void close() throws IOException {
        file.close();
    }
    
    public List<Long> searchBooks(String query, String criteria) {
    List<Long> positions = new ArrayList<>();
    query = query.toLowerCase();
    
    if (criteria.equalsIgnoreCase("Title")) {
        // Use the in‑memory title index
        for (Map.Entry<Long, String> entry : recordTitles.entrySet()) {
            if (entry.getValue().toLowerCase().contains(query)) {
                positions.add(entry.getKey());
            }
        }
    } else if (criteria.equalsIgnoreCase("ISBN")) {
        // Iterate over the ISBN index keys
        for (Map.Entry<String, Long> entry : isbnIndex.entrySet()) {
            if (entry.getKey().toLowerCase().contains(query)) {
                positions.add(entry.getValue());
            }
        }
    } else if (criteria.equalsIgnoreCase("Author")) {
        // We do not have an author index, so iterate over all records.
        for (Long pos : recordTitles.keySet()) {
            try {
                Book book = getBookAt(pos);
                if (book.getAuthor().toLowerCase().contains(query)) {
                    positions.add(pos);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    return positions;
}

}
