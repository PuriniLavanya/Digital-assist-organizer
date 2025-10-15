import com.mongodb.MongoTimeoutException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.util.Scanner;

public class DigitalAssistOrganizer {
    private static final String MONGODB_URI = "mongodb://localhost:27017"; // Change if needed
    private static final String DB_NAME = "digital_assist";

    private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> tasksCol;
    private MongoCollection<Document> eventsCol;
    private MongoCollection<Document> notesCol;
    private boolean dbConnected = false;

    // Constructor with MongoDB connection safety
    public DigitalAssistOrganizer() {
        try {
            mongoClient = MongoClients.create(MONGODB_URI);
            db = mongoClient.getDatabase(DB_NAME);
            tasksCol = db.getCollection("tasks");
            eventsCol = db.getCollection("events");
            notesCol = db.getCollection("notes");
            dbConnected = true;
            System.out.println("✅ Connected to MongoDB successfully.");
        } catch (MongoTimeoutException e) {
            System.out.println("⚠️ Unable to connect to MongoDB. Please make sure MongoDB server is running on localhost:27017.");
        } catch (Exception e) {
            System.out.println("⚠️ Unexpected error while connecting to MongoDB: " + e.getMessage());
        }
    }

    public void close() {
        if (mongoClient != null) mongoClient.close();
    }

    private boolean checkConnection() {
        if (!dbConnected) {
            System.out.println("❌ MongoDB is not connected. Please start MongoDB and restart the application.");
            return false;
        }
        return true;
    }

    // ---------------- TASKS ----------------
    public String addTask(String title, String description, LocalDate dueDate) {
        if (!checkConnection()) return null;
        try {
            Document d = new Document("title", title)
                    .append("description", description)
                    .append("dueDate", dueDate != null ? dueDate.toString() : null)
                    .append("completed", false)
                    .append("createdAt", java.time.Instant.now().toString());
            tasksCol.insertOne(d);
            return d.getObjectId("_id").toHexString();
        } catch (Exception e) {
            System.out.println("❌ Failed to add task: " + e.getMessage());
            return null;
        }
    }

    public void listTasks() {
        if (!checkConnection()) return;
        System.out.println("---- Tasks ----");
        for (Document d : tasksCol.find()) {
            printDoc(d);
        }
    }

    public boolean completeTask(String idHex) {
        if (!checkConnection()) return false;
        try {
            ObjectId id = new ObjectId(idHex);
            var res = tasksCol.updateOne(Filters.eq("_id", id), Updates.set("completed", true));
            return res.getModifiedCount() > 0;
        } catch (Exception e) {
            System.out.println("❌ Failed to complete task: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteTask(String idHex) {
        if (!checkConnection()) return false;
        try {
            ObjectId id = new ObjectId(idHex);
            var res = tasksCol.deleteOne(Filters.eq("_id", id));
            return res.getDeletedCount() > 0;
        } catch (Exception e) {
            System.out.println("❌ Failed to delete task: " + e.getMessage());
            return false;
        }
    }

    // ---------------- EVENTS ----------------
    public String addEvent(String title, String description, LocalDate date) {
        if (!checkConnection()) return null;
        try {
            Document d = new Document("title", title)
                    .append("description", description)
                    .append("date", date != null ? date.toString() : null)
                    .append("createdAt", java.time.Instant.now().toString());
            eventsCol.insertOne(d);
            return d.getObjectId("_id").toHexString();
        } catch (Exception e) {
            System.out.println("❌ Failed to add event: " + e.getMessage());
            return null;
        }
    }

    public void listEvents() {
        if (!checkConnection()) return;
        System.out.println("---- Events ----");
        for (Document d : eventsCol.find()) {
            printDoc(d);
        }
    }

    public boolean deleteEvent(String idHex) {
        if (!checkConnection()) return false;
        try {
            ObjectId id = new ObjectId(idHex);
            var res = eventsCol.deleteOne(Filters.eq("_id", id));
            return res.getDeletedCount() > 0;
        } catch (Exception e) {
            System.out.println("❌ Failed to delete event: " + e.getMessage());
            return false;
        }
    }

    // ---------------- NOTES ----------------
    public String addNote(String title, String content) {
        if (!checkConnection()) return null;
        try {
            Document d = new Document("title", title)
                    .append("content", content)
                    .append("createdAt", java.time.Instant.now().toString());
            notesCol.insertOne(d);
            return d.getObjectId("_id").toHexString();
        } catch (Exception e) {
            System.out.println("❌ Failed to add note: " + e.getMessage());
            return null;
        }
    }

    public void listNotes() {
        if (!checkConnection()) return;
        System.out.println("---- Notes ----");
        for (Document d : notesCol.find()) {
            printDoc(d);
        }
    }

    public boolean deleteNote(String idHex) {
        if (!checkConnection()) return false;
        try {
            ObjectId id = new ObjectId(idHex);
            var res = notesCol.deleteOne(Filters.eq("_id", id));
            return res.getDeletedCount() > 0;
        } catch (Exception e) {
            System.out.println("❌ Failed to delete note: " + e.getMessage());
            return false;
        }
    }

    // ---------------- Utility ----------------
    private void printDoc(Document d) {
        String id = d.getObjectId("_id").toHexString();
        System.out.println("ID: " + id);
        for (String key : d.keySet()) {
            if ("_id".equals(key)) continue;
            System.out.println("  " + key + ": " + d.get(key));
        }
        System.out.println();
    }

    // ---------------- CLI ----------------
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            DigitalAssistOrganizer dao = new DigitalAssistOrganizer();
            System.out.println("Welcome to Digital Assist Organizer!");

            boolean running = true;
            while (running) {
                printMenu();
                System.out.print("Choose option: ");
                String opt = scanner.nextLine().trim();
                switch (opt) {
                    case "1":
                        System.out.print("Title: ");
                        String t = scanner.nextLine();
                        System.out.print("Description: ");
                        String td = scanner.nextLine();
                        System.out.print("Due date (YYYY-MM-DD) or blank: ");
                        String due = scanner.nextLine().trim();
                        LocalDate dueDate = due.isBlank() ? null : LocalDate.parse(due);
                        String taskId = dao.addTask(t, td, dueDate);
                        if (taskId != null)
                            System.out.println("✅ Task created with id: " + taskId);
                        break;
                    case "2":
                        dao.listTasks();
                        break;
                    case "3":
                        System.out.print("Task ID to mark complete: ");
                        String cid = scanner.nextLine().trim();
                        System.out.println(dao.completeTask(cid) ? "✅ Marked complete." : "❌ Failed — check ID.");
                        break;
                    case "4":
                        System.out.print("Task ID to delete: ");
                        String del = scanner.nextLine().trim();
                        System.out.println(dao.deleteTask(del) ? "✅ Deleted." : "❌ Failed — check ID.");
                        break;
                    case "5":
                        System.out.print("Event title: ");
                        String et = scanner.nextLine();
                        System.out.print("Description: ");
                        String ed = scanner.nextLine();
                        System.out.print("Date (YYYY-MM-DD) or blank: ");
                        String date = scanner.nextLine().trim();
                        LocalDate evDate = date.isBlank() ? null : LocalDate.parse(date);
                        String evId = dao.addEvent(et, ed, evDate);
                        if (evId != null)
                            System.out.println("✅ Event created with id: " + evId);
                        break;
                    case "6":
                        dao.listEvents();
                        break;
                    case "7":
                        System.out.print("Event ID to delete: ");
                        String edel = scanner.nextLine().trim();
                        System.out.println(dao.deleteEvent(edel) ? "✅ Deleted." : "❌ Failed — check ID.");
                        break;
                    case "8":
                        System.out.print("Note title: ");
                        String nt = scanner.nextLine();
                        System.out.print("Content: ");
                        String nc = scanner.nextLine();
                        String nId = dao.addNote(nt, nc);
                        if (nId != null)
                            System.out.println("✅ Note created with id: " + nId);
                        break;
                    case "9":
                        dao.listNotes();
                        break;
                    case "10":
                        System.out.print("Note ID to delete: ");
                        String ndel = scanner.nextLine().trim();
                        System.out.println(dao.deleteNote(ndel) ? "✅ Deleted." : "❌ Failed — check ID.");
                        break;
                    case "0":
                        running = false;
                        break;
                    default:
                        System.out.println("⚠️ Invalid option");
                }
            }
            dao.close();
            System.out.println("👋 Goodbye!");
        } catch (Exception ex) {
            System.out.println("❌ Unexpected error: " + ex.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("\nMenu:");
        System.out.println(" 1 - Add Task");
        System.out.println(" 2 - List Tasks");
        System.out.println(" 3 - Complete Task");
        System.out.println(" 4 - Delete Task");
        System.out.println(" 5 - Add Event");
        System.out.println(" 6 - List Events");
        System.out.println(" 7 - Delete Event");
        System.out.println(" 8 - Add Note");
        System.out.println(" 9 - List Notes");
        System.out.println("10 - Delete Note");
        System.out.println(" 0 - Exit");
    }
}
