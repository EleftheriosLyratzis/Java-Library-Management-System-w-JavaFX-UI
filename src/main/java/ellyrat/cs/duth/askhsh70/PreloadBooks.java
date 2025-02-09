/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ellyrat.cs.duth.askhsh70;

import java.util.List;

public class PreloadBooks {
    public static void preloadBooks(List<Book> inventory) {
        // 5 Printed Books (Modern Greek Literature)
        inventory.add(new Book("Ο Χριστός Ξανασταυρώνεται", "Νίκος Καζαντζάκης", "ISBN-001", 25.99, 300, null));
        inventory.add(new Book("Η Ζωή εν Τάφω", "Στράτης Μυριβήλης", "ISBN-002", 45.50, 416, null));
        inventory.add(new Book("Ματωμένα Χώματα", "Διδώ Σωτηρίου", "ISBN-003", 40.00, 464, null));
        inventory.add(new Book("Νούμερο 31328", "Ηλίας Βενέζης", "ISBN-004", 35.00, 688, null));
        inventory.add(new Book("Βίος και Πολιτεία του Αλέξη Ζορμπά", "Νίκος Καζαντζάκης", "ISBN-005", 55.00, 700, null));

        // 3 Electronic Books (Modern Greek Literature)
        inventory.add(new Book("Το Μόνον της Ζωής του Ταξίδιον", "Γεώργιος Βιζυηνός", "ISBN-006", 39.99, null, 5.5));
        inventory.add(new Book("Ένα παιδί μετράει τ' άστρα", "Μενέλαος Λουντέμης", "ISBN-007", 49.99, null, 8.0));
        inventory.add(new Book("Το Καπλάνι της Βιτρίνας", "Άλκη Ζέη", "ISBN-008", 59.99, null, 12.0));
    }
}

