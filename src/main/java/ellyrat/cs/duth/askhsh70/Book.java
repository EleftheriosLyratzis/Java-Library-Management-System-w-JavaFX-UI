
package ellyrat.cs.duth.askhsh70;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Book {
    // Fixed lengths (number of characters) for string fields
    public static final int TITLE_LENGTH  = 50;
    public static final int AUTHOR_LENGTH = 30;
    public static final int ISBN_LENGTH   = 13;
    // Each character is written as 2 bytes.
    public static final int STRING_PART_SIZE = (TITLE_LENGTH + AUTHOR_LENGTH + ISBN_LENGTH) * 2;
    // Price is stored as a double (8 bytes), pages as an int (4 bytes), fileSize as a double (8 bytes)
    public static final int RECORD_SIZE = STRING_PART_SIZE + 8 + 4 + 8; 

    private String title;
    private String author;
    private String isbn;
    private double price;
    // For printed books, pages is non-null; for electronic books, pages is null.
    private Integer pages;   
    // For electronic books, fileSize is non-null (in MB); for printed books, fileSize is null.
    private Double fileSize; 

    public Book(String title, String author, String isbn, double price, Integer pages, Double fileSize) {
        this.title   = title;
        this.author  = author;
        this.isbn    = isbn;
        this.price   = price;
        this.pages   = pages;
        this.fileSize = fileSize;
    }

    // Getters and setters (you can add more if needed)
    public String getTitle() {
        return title;
    }
    public String getAuthor() {
        return author;
    }
    public String getIsbn() {
        return isbn;
    }
    public double getPrice() {
        return price;
    }
    public Integer getPages() {
        return pages;
    }
    public Double getFileSize() {
        return fileSize;
    }

    /**
     * Writes this book record to the given RandomAccessFile at the current pointer.
     */
    public void writeRecord(RandomAccessFile file) throws IOException {
        // Write fixed-length strings for title, author, and ISBN
        writeFixedString(file, title, TITLE_LENGTH);
        writeFixedString(file, author, AUTHOR_LENGTH);
        writeFixedString(file, isbn, ISBN_LENGTH);
        // Write numeric values
        file.writeDouble(price);
        // For pages, if null, we store -1 to indicate “not applicable”
        file.writeInt(pages == null ? -1 : pages);
        // For fileSize, if null, we store -1.0
        file.writeDouble(fileSize == null ? -1.0 : fileSize);
    }

    /**
     * Reads a book record from the given RandomAccessFile at the current pointer.
     */
    public static Book readRecord(RandomAccessFile file) throws IOException {
        String title  = readFixedString(file, TITLE_LENGTH);
        String author = readFixedString(file, AUTHOR_LENGTH);
        String isbn   = readFixedString(file, ISBN_LENGTH);
        double price  = file.readDouble();
        int pagesInt  = file.readInt();
        Integer pages = (pagesInt == -1 ? null : pagesInt);
        double fileSizeDouble = file.readDouble();
        Double fileSize = (fileSizeDouble == -1.0 ? null : fileSizeDouble);
        return new Book(title, author, isbn, price, pages, fileSize);
    }

    // Helper: Writes a fixed-length string padded with spaces.
    public static void writeFixedString(RandomAccessFile file, String s, int length) throws IOException {
        StringBuilder sb = new StringBuilder(s);
        if (sb.length() > length) {
            sb.setLength(length);
        } else {
            while (sb.length() < length) {
                sb.append(' ');
            }
        }
        file.writeChars(sb.toString());
    }

    // Helper: Reads a fixed-length string.
    public static String readFixedString(RandomAccessFile file, int length) throws IOException {
        char[] chars = new char[length];
        for (int i = 0; i < length; i++) {
            chars[i] = file.readChar();
        }
        return new String(chars).trim();
    }

    @Override
    public String toString() {
        String extraInfo = (pages != null) ? ("Pages: " + pages) : ("File Size: " + fileSize + " MB");
        return "Title: " + title + " | Author: " + author + " | ISBN: " + isbn +
               " | Price: $" + price + " | " + extraInfo;
    }
}
