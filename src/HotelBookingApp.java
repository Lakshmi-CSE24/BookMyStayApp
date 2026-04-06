import java.io.*;
import java.util.*;

public class HotelBookingApp {

    public static void main(String[] args) {

        System.out.println("System Recovery");

        RoomInventory inventory = new RoomInventory();
        FilePersistenceService persistence = new FilePersistenceService();

        String filePath = "inventory.txt";

        // LOAD previous state
        persistence.loadInventory(inventory, filePath);

        // Show current inventory
        System.out.println("\nCurrent Inventory:");
        System.out.println("Single: " + inventory.getAvailable("Single"));
        System.out.println("Double: " + inventory.getAvailable("Double"));
        System.out.println("Suite: " + inventory.getAvailable("Suite"));

        // SAVE state
        persistence.saveInventory(inventory, filePath);

        System.out.println("Inventory saved successfully.");
    }
}

// ---------------- INVENTORY ----------------
class RoomInventory {

    private Map<String, Integer> rooms;

    public RoomInventory() {
        rooms = new HashMap<>();
        rooms.put("Single", 5);
        rooms.put("Double", 3);
        rooms.put("Suite", 2);
    }

    public Map<String, Integer> getRooms() {
        return rooms;
    }

    public void setRoom(String type, int count) {
        rooms.put(type, count);
    }

    public int getAvailable(String type) {
        return rooms.getOrDefault(type, 0);
    }
}

// ---------------- PERSISTENCE SERVICE ----------------
class FilePersistenceService {

    public void saveInventory(RoomInventory inventory, String filePath) {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {

            for (Map.Entry<String, Integer> entry : inventory.getRooms().entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }

        } catch (IOException e) {
            System.out.println("Error saving inventory.");
        }
    }

    public void loadInventory(RoomInventory inventory, String filePath) {

        File file = new File(filePath);

        if (!file.exists()) {
            System.out.println("No valid inventory data found. Starting fresh.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("=");

                if (parts.length == 2) {
                    String type = parts[0];
                    int count = Integer.parseInt(parts[1]);
                    inventory.setRoom(type, count);
                }
            }

        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading inventory. Starting fresh.");
        }
    }
}
