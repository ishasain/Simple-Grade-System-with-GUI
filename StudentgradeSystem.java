import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

abstract class StudentName {
    String name;

    public StudentName(String a) {
        this.name = a;
    }

    abstract void DisplayResult();
}

class Student extends StudentName {
    int[] marks;
    int total;
    double average;
    double attendance;
    boolean isAssignmentCompleted;

    public Student(String name, double attendance, boolean isAssignmentCompleted) {
        super(name);
        this.attendance = attendance;
        this.isAssignmentCompleted = isAssignmentCompleted;
    }

    public void Calculate(Scanner sc) {
        marks = new int[5];
        total = 0;
        for (int i = 0; i < 5; i++) {
            marks[i] = sc.nextInt();
            total += marks[i];
        }
        average = total / 5.0;
    }

    public String getGrade() {
        if (average >= 90 && attendance >= 85 && isAssignmentCompleted) return "A";
        else if (average >= 75 && attendance >= 75) return "B";
        else if (average >= 60) return "C";
        else return "D";
    }

    public String Remark() {
        switch (getGrade()) {
            case "A": return "Excellent, Keep it up";
            case "B": return "Very Good";
            case "C": return "Good Job";
            default: return "Needs improvement";
        }
    }

    public void DisplayResult() {
        System.out.println("Student: " + name);
        System.out.println("Average Marks: " + average);
        System.out.println("Grade: " + getGrade());
        System.out.println("Remark: " + Remark());
    }
}

public class StudentgradeSystem extends JFrame {
    private DefaultTableModel tableModel;
    private JTable resultTable;
    private JButton submitButton, addStudentButton, topperButton, chartButton;

    private JPanel studentPanelContainer;
    private int studentCount = 0;
    private final ArrayList<StudentInputPanel> studentInputPanels = new ArrayList<>();
    private final ArrayList<Student> processedStudents = new ArrayList<>();

    public StudentgradeSystem() {
        setTitle("Student Grading System");
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        studentPanelContainer = new JPanel();
        studentPanelContainer.setLayout(new BoxLayout(studentPanelContainer, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(studentPanelContainer);

        String[] columnNames = {
            "Name", "Attendance", "Assignment",
            "ENGLISH", "OOPS", "JAVA", "MATHEMATICS", "DAA",
            "Average", "Grade", "Remark"
        };
        tableModel = new DefaultTableModel(columnNames, 0);
        resultTable = new JTable(tableModel);
        JScrollPane resultScroll = new JScrollPane(resultTable);

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.add(new JLabel("Result:", SwingConstants.CENTER), BorderLayout.NORTH);
        resultPanel.add(resultScroll, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        addStudentButton = new JButton("Add Student");
        submitButton = new JButton("Calculate All");
        topperButton = new JButton("Show Toppers");
        chartButton = new JButton("Show Chart");

        buttonPanel.add(addStudentButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(topperButton);
        buttonPanel.add(chartButton);

        add(scrollPane, BorderLayout.CENTER);
        add(resultPanel, BorderLayout.SOUTH);
        add(buttonPanel, BorderLayout.NORTH);

        addStudentButton.addActionListener(e -> addStudentPanel());
        submitButton.addActionListener(e -> calculateAllResults());
        topperButton.addActionListener(e -> showToppersPopup());
        chartButton.addActionListener(e -> showBarChart());

        addStudentPanel();
        setVisible(true);
    }

    private void addStudentPanel() {
        StudentInputPanel sip = new StudentInputPanel(++studentCount);
        studentInputPanels.add(sip);
        studentPanelContainer.add(sip);
        studentPanelContainer.revalidate();
        studentPanelContainer.repaint();
    }

    private void calculateAllResults() {
        processedStudents.clear();
        tableModel.setRowCount(0);

        for (StudentInputPanel panel : studentInputPanels) {
            String name = panel.nameField.getText();
            double attendance;
            try {
                attendance = Double.parseDouble(panel.attendanceField.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid attendance for " + name);
                return;
            }

            boolean assignmentCompleted = panel.assignmentCheckBox.isSelected();
            int[] inputMarks = new int[5];
            try {
                for (int i = 0; i < 5; i++) {
                    inputMarks[i] = Integer.parseInt(panel.marksFields[i].getText());
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid marks for student " + name);
                return;
            }

            Student student = new Student(name, attendance, assignmentCompleted);
            Scanner mockScanner = new Scanner(new java.io.ByteArrayInputStream(
                Arrays.stream(inputMarks).mapToObj(String::valueOf)
                      .reduce((a, b) -> a + "\n" + b).get().getBytes()));
            student.Calculate(mockScanner);
            processedStudents.add(student);

            tableModel.addRow(new Object[] {
                student.name, student.attendance, assignmentCompleted ? "Yes" : "No",
                inputMarks[0], inputMarks[1], inputMarks[2], inputMarks[3], inputMarks[4],
                student.average, student.getGrade(), student.Remark()
            });
        }
    }

    private void showToppersPopup() {
        if (processedStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please calculate results first.");
            return;
        }

        String[] columns = {"Subject", "Topper(s)", "Highest Marks"};
        DefaultTableModel popupModel = new DefaultTableModel(columns, 0);
        String[] subjects = {"ENGLISH", "OOPS", "JAVA", "MATHEMATICS", "DAA"};

        for (int i = 0; i < 5; i++) {
            int maxMark = -1;
            ArrayList<String> toppers = new ArrayList<>();

            for (Student student : processedStudents) {
                int mark = student.marks[i];
                if (mark > maxMark) {
                    maxMark = mark;
                    toppers.clear();
                    toppers.add(student.name);
                } else if (mark == maxMark) {
                    toppers.add(student.name);
                }
            }

            popupModel.addRow(new Object[] {
                subjects[i],
                String.join(", ", toppers),
                maxMark
            });
        }

        JTable popupTable = new JTable(popupModel);
        JScrollPane scrollPane = new JScrollPane(popupTable);
        scrollPane.setPreferredSize(new Dimension(500, 200));

        JDialog popup = new JDialog(this, "Subject-wise Toppers", false);
        popup.setLayout(new BorderLayout());
        popup.add(new JLabel("Topper List", SwingConstants.CENTER), BorderLayout.NORTH);
        popup.add(scrollPane, BorderLayout.CENTER);
        popup.setSize(600, 300);
        popup.setLocationRelativeTo(this);
        popup.setVisible(true);
    }

    private void showBarChart() {
        if (processedStudents.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please calculate results first.");
            return;
        }

        String[] subjects = {"ENGLISH", "OOPS", "JAVA", "MATHEMATICS", "DAA"};
        double[] totals = new double[5];

        for (Student student : processedStudents) {
            for (int i = 0; i < 5; i++) {
                totals[i] += student.marks[i];
            }
        }

        double[] averages = new double[5];
        for (int i = 0; i < 5; i++) {
            averages[i] = totals[i] / processedStudents.size();
        }

        JFrame chartFrame = new JFrame("Average Marks per Subject");
        chartFrame.setSize(600, 400);
        chartFrame.setLocationRelativeTo(null);
        chartFrame.add(new ChartPanel(subjects, averages));
        chartFrame.setVisible(true);
    }

    class ChartPanel extends JPanel {
        String[] subjects;
        double[] values;

        ChartPanel(String[] subjects, double[] values) {
            this.subjects = subjects;
            this.values = values;
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int width = getWidth() - 100;
            int height = getHeight() - 100;
            int barWidth = width / values.length;
            int maxVal = (int) Arrays.stream(values).max().getAsDouble();

            g.drawString("Subject-wise Average Marks", getWidth() / 2 - 80, 20);

            for (int i = 0; i < values.length; i++) {
                int barHeight = (int) ((values[i] / maxVal) * (height - 50));
                int x = 50 + i * barWidth;
                int y = height - barHeight;

                g.setColor(Color.BLUE);
                g.fillRect(x, y, barWidth - 10, barHeight);
                g.setColor(Color.BLACK);
                g.drawRect(x, y, barWidth - 10, barHeight);
                g.drawString(subjects[i], x + 5, height + 20);
                g.drawString(String.format("%.1f", values[i]), x + 5, y - 5);
            }
        }
    }

    class StudentInputPanel extends JPanel {
        JTextField nameField, attendanceField;
        JCheckBox assignmentCheckBox;
        JTextField[] marksFields;

        StudentInputPanel(int index) {
            setBorder(BorderFactory.createTitledBorder("Student " + index));
            setLayout(new GridLayout(0, 2));

            nameField = new JTextField();
            attendanceField = new JTextField();
            assignmentCheckBox = new JCheckBox("Assignment Completed");
            marksFields = new JTextField[5];

            add(new JLabel("Name:")); add(nameField);

            add(new JLabel("Attendance (%):")); add(attendanceField);
            add(new JLabel("Assignment Completed:")); add(assignmentCheckBox);
            
            String[] subjectNames = {"ENGLISH", "OOPS", "JAVA", "MATHEMATICS", "DAA"};
            for (int i = 0; i < 5; i++) {
                marksFields[i] = new JTextField();
                add(new JLabel(subjectNames[i] + " Marks:"));
                add(marksFields[i]);
            }

            addEnterKeyNavigation(nameField);
            addEnterKeyNavigation(attendanceField);
            for (JTextField tf : marksFields) {
                addEnterKeyNavigation(tf);
            }
        }

        private void addEnterKeyNavigation(JTextField field) {
            field.addActionListener(e -> field.transferFocus());
        }
    }

    public static void main(String[] args) {
        new StudentgradeSystem();
    }
}

