/** Stores the student's personal data and starting allowance (Encapsulation). */
public class Student {
    private String name;
    private String studentId;
    private double allowance;

    public Student(String name, String studentId, double allowance) {
        setName(name);
        setStudentId(studentId);
        setAllowance(allowance);
    }

    public String getName() { return name; }
    public String getStudentId() { return studentId; }
    public double getAllowance() { return allowance; }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Name cannot be empty.");
        this.name = name.trim();
    }

    public void setStudentId(String studentId) {
        if (studentId == null || studentId.trim().isEmpty())
            throw new IllegalArgumentException("Student ID cannot be empty.");
        this.studentId = studentId.trim();
    }

    public void setAllowance(double allowance) {
        if (allowance < 0)
            throw new IllegalArgumentException("Allowance cannot be negative.");
        this.allowance = allowance;
    }
}