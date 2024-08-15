package app;

public class Student {
    private int id;
    private String name;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public static String getIdFromDB() {
        return "student_id";
    }

    public static String getNameFromDB() {
        return "student_name";
    }
}