import java.util.*;

public class HotelBookingApp {

    public static void main(String[] args) {

        System.out.println("Concurrent Booking Simulation\n");

        BookingRequestQueue bookingQueue = new BookingRequestQueue();
        RoomInventory inventory = new RoomInventory();
        RoomAllocationService allocationService = new RoomAllocationService();

        // Add booking requests
        bookingQueue.add(new Reservation("Abhi", "Single"));
        bookingQueue.add(new Reservation("Vanmathi", "Double"));
        bookingQueue.add(new Reservation("Kural", "Suite"));
        bookingQueue.add(new Reservation("Subha", "Single"));

        // Create threads
        Thread t1 = new Thread(
                new ConcurrentBookingProcessor(bookingQueue, inventory, allocationService)
        );

        Thread t2 = new Thread(
                new ConcurrentBookingProcessor(bookingQueue, inventory, allocationService)
        );

        // Start threads
        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            System.out.println("Thread execution interrupted.");
        }

        // Print remaining inventory
        System.out.println("\nRemaining Inventory:");
        System.out.println("Single: " + inventory.getAvailable("Single"));
        System.out.println("Double: " + inventory.getAvailable("Double"));
        System.out.println("Suite: " + inventory.getAvailable("Suite"));
    }
}

// ---------------- RESERVATION ----------------
class Reservation {
    private String guestName;
    private String roomType;

    public Reservation(String guestName, String roomType) {
        this.guestName = guestName;
        this.roomType = roomType;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getRoomType() {
        return roomType;
    }
}

// ---------------- QUEUE ----------------
class BookingRequestQueue {

    private Queue<Reservation> queue = new LinkedList<>();

    public void add(Reservation r) {
        queue.add(r);
    }

    public Reservation poll() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }
}

// ---------------- INVENTORY ----------------
class RoomInventory {

    private Map<String, Integer> rooms;

    public RoomInventory() {
        rooms = new HashMap<>();
        rooms.put("Single", 4);
        rooms.put("Double", 3);
        rooms.put("Suite", 2);
    }

    public boolean isAvailable(String type) {
        return rooms.getOrDefault(type, 0) > 0;
    }

    public void decrement(String type) {
        rooms.put(type, rooms.get(type) - 1);
    }

    public int getAvailable(String type) {
        return rooms.getOrDefault(type, 0);
    }
}

// ---------------- ALLOCATION ----------------
class RoomAllocationService {

    private Map<String, Integer> countMap = new HashMap<>();

    public void allocateRoom(Reservation r, RoomInventory inventory) {

        String type = r.getRoomType();

        if (!inventory.isAvailable(type)) {
            return;
        }

        int count = countMap.getOrDefault(type, 0) + 1;
        countMap.put(type, count);

        String roomId = type + "-" + count;

        inventory.decrement(type);

        System.out.println("Booking confirmed for Guest: "
                + r.getGuestName()
                + ", Room ID: " + roomId);
    }
}

// ---------------- THREAD PROCESSOR ----------------
class ConcurrentBookingProcessor implements Runnable {

    private BookingRequestQueue bookingQueue;
    private RoomInventory inventory;
    private RoomAllocationService allocationService;

    public ConcurrentBookingProcessor(
            BookingRequestQueue bookingQueue,
            RoomInventory inventory,
            RoomAllocationService allocationService
    ) {
        this.bookingQueue = bookingQueue;
        this.inventory = inventory;
        this.allocationService = allocationService;
    }

    @Override
    public void run() {

        while (true) {

            Reservation reservation;

            // 🔒 LOCK QUEUE
            synchronized (bookingQueue) {
                if (bookingQueue.isEmpty()) break;
                reservation = bookingQueue.poll();
            }

            // 🔒 LOCK INVENTORY + ALLOCATION
            synchronized (inventory) {
                allocationService.allocateRoom(reservation, inventory);
            }
        }
    }
}
