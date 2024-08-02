import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.FileOutputStream;
import java.util.List;

public class LedgerGUI extends JFrame {
    private JTextField dateField, schoolField, projectField, timeField, studentField, subjectField, notesField;
    private JCheckBox completeCheckBox;
    private JButton submitButton, generatePdfButton;
    private JTable dataTable;
    private DefaultTableModel tableModel;

    private static final String DB_URL = "jdbc:sqlite:ledger.db";

    public LedgerGUI() {
        setTitle("Ledger Input");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel(new GridLayout(0,2,10,10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addLabelAndField(inputPanel, "<html>Date: <br><i>(YYYY-MM-DD)</i></html>", dateField = new JTextField());
        addLabelAndField(inputPanel, "<html>Student: <br><i>(First Two Letters of First and Last Name)</i></html>", studentField = new JTextField());
        addLabelAndField(inputPanel, "<html>Academic Subject</html>", subjectField = new JTextField());
        addLabelAndField(inputPanel, "<html>School</html>", schoolField = new JTextField());
        addLabelAndField(inputPanel, "<html>Project: <br>(UEB, UEB Technical, Tactile Graphics, Large Print, 3D Print)</html>", projectField = new JTextField());
        addLabelAndField(inputPanel, "<html>Time: <br>(Rounded UP to Nearest .25 hr fter 5 min)</html>", timeField = new JTextField());
        addLabelAndField(inputPanel, "<html>Process Notes:</html>", notesField = new JTextField());

        JLabel completeLabel = new JLabel("Complete:");
        completeCheckBox = new JCheckBox();
        completeLabel.setLabelFor(completeCheckBox);
        inputPanel.add(completeLabel);
        inputPanel.add(completeCheckBox);
        completeCheckBox.setMnemonic(KeyEvent.VK_C);
        completeCheckBox.getAccessibleContext().setAccessibleDescription("Check if the task is complete");

        submitButton = new JButton("Submit");
        submitButton.setMnemonic(KeyEvent.VK_S);
        submitButton.addActionListener(e -> submitData());
        submitButton.getAccessibleContext().setAccessibleDescription("Submit the entered data to the database");

        generatePdfButton = new JButton("Generate PDF Report");
        generatePdfButton.setMnemonic(KeyEvent.VK_G);
        generatePdfButton.addActionListener(e -> showDateRangeDialog());
        generatePdfButton.getAccessibleContext().setAccessibleDescription("Generate a PDF report for a specified date range");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(submitButton);
        buttonPanel.add(generatePdfButton);
        inputPanel.add(buttonPanel);

        add(inputPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Date", "Student", "Subject", "School", "Project", "Time", "Complete"}, 0);
        dataTable = new JTable(tableModel);
        dataTable.getAccessibleContext().setAccessibleDescription("Table showing ledger entries");
        dataTable.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(dataTable);
        add(scrollPane, BorderLayout.CENTER);

        initializeDatabase();
        loadDataFromDatabase();

        // Set up focus traversal
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        setFocusCycleRoot(true);
    }

    private void addLabelAndField(JPanel panel, String labelText, JTextField field) {
        JLabel label = new JLabel(labelText);
        label.setLabelFor(field);
        panel.add(label);
        panel.add(field);
        field.getAccessibleContext().setAccessibleDescription("Enter " + labelText.toLowerCase());
    }

    // ... (other methods remain largely the same)

    private void showDateRangeDialog() {
        JTextField startDateField = new JTextField(10);
        JTextField endDateField = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addLabelAndField(panel, "Start Date (YYYY-MM-DD):", startDateField);
        addLabelAndField(panel, "End Date (YYYY-MM-DD):", endDateField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Enter Date Range",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String startDate = startDateField.getText();
            String endDate = endDateField.getText();
            generatePdfReport(startDate, endDate);
        }
    }

    public static void main(String[] args) {
        // Set the look and feel to the system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LedgerGUI gui = new LedgerGUI();
            gui.setVisible(true);
        });
    }
    private void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS ledger " +
                    "(id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "date TEXT NOT NULL, " +
                    "student TEXT NOT NULL," +
                    "subject TEXT NOT NULL," +
                    "school TEXT NOT NULL, " +
                    "project TEXT NOT NULL, " +
                    "time TEXT NOT NULL, " +
                    "notes TEXT NOT NULL," +
                    "complete BOOLEAN NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error initializing database: " + e.getMessage());
        }
    }

    private void loadDataFromDatabase() {
        tableModel.setRowCount(0);
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT date, student, subject, school, project, time, complete FROM ledger")) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("date"),
                    rs.getString("student"),
                    rs.getString("subject"),
                    rs.getString("school"),
                    rs.getString("project"),
                    rs.getString("time"),
                    rs.getBoolean("complete") ? "Yes" : "No"
                };
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + e.getMessage());
        }
    }

    private void submitData() {
        String date = dateField.getText();
        String school = schoolField.getText();
        String student = studentField.getText();
        String subject = subjectField.getText();
        String notes = notesField.getText();
        String project = projectField.getText();
        String time = timeField.getText();
        boolean complete = completeCheckBox.isSelected();

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "INSERT INTO ledger (date, student, subject, school, project, time, notes, complete) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, date);
            pstmt.setString(2, student);
            pstmt.setString(3, subject);
            pstmt.setString(4, school);
            pstmt.setString(5, project);
            pstmt.setString(6, time);
            pstmt.setString(7, notes);
            pstmt.setBoolean(8, complete);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Data submitted successfully!");
            clearInputFields();
            loadDataFromDatabase();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error submitting data: " + e.getMessage());
        }
    }

    private void generatePdfReport(String startDate, String endDate) {
        try {
            // Get the user's Downloads directory
            String userHome = System.getProperty("user.home");
            String downloadsDir = userHome + "/Downloads";

            // Create the filename with the current date
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String currentDate = dateFormat.format(new Date());
            String fileName = downloadsDir + "/REPORT" + currentDate + ".pdf";

            // Create the PDF document
            Document document = new Document(PageSize.LETTER.rotate());
            PdfWriter.getInstance(document, new FileOutputStream(fileName));
            document.open();

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2, 2, 2, 2, 2, 2});

            table.addCell("Date");
            table.addCell("Student");
            table.addCell("Subject");
            table.addCell("School");
            table.addCell("Project");
            table.addCell("Time");

            List<String[]> data = fetchDataFromDatabase(startDate, endDate);
            double totalTime = 0; // Variable to hold the sum of the Time column
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    table.addCell(row[i]);
                    if (i == 5) { // Assuming "Time" column is at index 5
                        try {
                            totalTime += Double.parseDouble(row[i]);
                        } catch (NumberFormatException e) {
                            // Handle invalid time format
                            System.err.println("Invalid time format: " + row[i]);
                        }
                    }
                }
            }
            Paragraph headerParagraph = new Paragraph();
            headerParagraph.setAlignment(Element.ALIGN_CENTER);
            headerParagraph.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18));
            headerParagraph.setSpacingAfter(30);
            headerParagraph.add("INVOICE");

            Paragraph addressParagraph1 = new Paragraph();
            addressParagraph1.setAlignment(Element.ALIGN_LEFT);
            addressParagraph1.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            addressParagraph1.add("Michael Ryan Hunsaker, M.Ed., Ph.D.");

            Paragraph addressParagraph2 = new Paragraph();
            addressParagraph2.setAlignment(Element.ALIGN_LEFT);
            addressParagraph2.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            addressParagraph2.add("Davis School District");

            Paragraph addressParagraph3 = new Paragraph();
            addressParagraph3.setAlignment(Element.ALIGN_LEFT);
            addressParagraph3.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            addressParagraph3.add("Farmington, UT 84025");
            addressParagraph3.setSpacingAfter(28);

            Paragraph timespanParagraph = new Paragraph();
            timespanParagraph.setAlignment(Element.ALIGN_LEFT);
            timespanParagraph.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            timespanParagraph.add("For Billing Cycle " );
            timespanParagraph.add(startDate);
            timespanParagraph.add(" To " );
            timespanParagraph.add(endDate);
            timespanParagraph.setSpacingAfter(14);

            Paragraph footParagraph = new Paragraph();
            footParagraph.setAlignment(Element.ALIGN_RIGHT);
            footParagraph.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            footParagraph.add("Created: ");
            footParagraph.add(currentDate);

            Paragraph totalParagraph = new Paragraph();
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);

            // Add "Total" label
            Chunk totalLabel = new Chunk("Total: ");
            totalLabel.setUnderline(1f, -2f); // Underline the label
            totalParagraph.add(totalLabel);

            // Add total time value
            Chunk totalValue = new Chunk(String.format("%.2f", totalTime));
            totalValue.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12));
            totalParagraph.add(totalValue);

            // Create a paragraph to hold the line
            Paragraph lineParagraph = new Paragraph();
            lineParagraph.setSpacingAfter(12);

            // Build PDF FileS
            //document.add(headerParagraph);
            //document.add(addressParagraph1);
            //document.add(addressParagraph2);
            //document.add(addressParagraph3);
            document.add(table);
            document.add(totalParagraph);
            //document.add(footParagraph);

            document.close();

            JOptionPane.showMessageDialog(this, "PDF report generated: " + fileName);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error generating PDF: " + e.getMessage());
        }
    }

    private List<String[]> fetchDataFromDatabase(String startDate, String endDate) {
        List<String[]> data = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                     "SELECT date, student, subject, school, project, time, complete FROM ledger WHERE date BETWEEN ? AND ? ORDER BY date")) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] row = {
                    rs.getString("date"),
                    rs.getString("student"),
                    rs.getString("subject"),
                    rs.getString("school"),
                    rs.getString("project"),
                    rs.getString("time")
                };
                data.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching data: " + e.getMessage());
        }
        return data;
    }

    private void clearInputFields() {
        dateField.setText("");
        studentField.setText("");
        subjectField.setText("");
        schoolField.setText("");
        projectField.setText("");
        timeField.setText("");
        notesField.setText("");
        completeCheckBox.setSelected(false);
    }
    private PdfPTable createTable(List<String[]> data) {
    PdfPTable table = new PdfPTable(5);
    table.setWidthPercentage(100);
    try {
        table.setWidths(new float[]{2, 2, 2, 1, 1});
    } catch (DocumentException e) {
        e.printStackTrace();
    }
    
    // Add table headers
    for (String header : new String[]{"Date", "School", "Project", "Time", "Complete"}) {
        PdfPCell cell = new PdfPCell(new Phrase(header, new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD)));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell);
    }
    
    // Add data
    for (String[] row : data) {
        for (String cell : row) {
            PdfPCell pdfCell = new PdfPCell(new Phrase(cell, new Font(Font.FontFamily.HELVETICA, 10)));
            pdfCell.setBorder(Rectangle.NO_BORDER);
            table.addCell(pdfCell);
        }
    }
    
    return table;
}

private class HeaderFooterPageEvent extends PdfPageEventHelper {
    private PdfTemplate t;
    //private Image total;

    public void onOpenDocument(PdfWriter writer, Document document) {
        t = writer.getDirectContent().createTemplate(30, 16);
        try {
            total = Image.getInstance(t);
            total.setRole(PdfName.ARTIFACT);
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        addHeader(writer, document);
        addFooter(writer, document);
    }

    private void addHeader(PdfWriter writer, Document document) {
        PdfPTable header = new PdfPTable(2);
        try {
            // set defaults
            header.setWidths(new int[]{2, 24});
            header.setTotalWidth(527);
            header.setLockedWidth(true);
            header.getDefaultCell().setFixedHeight(40);
            header.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            header.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

            // add image
            Image logo = Image.getInstance(getClass().getResource("/Resources/dsd-mark-white.png"));
            header.addCell(logo);

            // add text
            PdfPCell text = new PdfPCell();
            text.setPaddingBottom(15);
            text.setPaddingLeft(10);
            text.setBorder(Rectangle.NO_BORDER);
            text.addElement(new Phrase("Michael Ryan Hunsajer, M.Ed., Ph.D.", new Font(Font.FontFamily.HELVETICA, 12)));
            text.addElement(new Phrase("Davis School District Farmington, UT, 84025", new Font(Font.FontFamily.HELVETICA, 8)));
            header.addCell(text);

            // write content
            header.writeSelectedRows(0, -1, 34, 803, writer.getDirectContent());
        } catch(DocumentException de) {
            throw new ExceptionConverter(de);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addFooter(PdfWriter writer, Document document) {
        PdfPTable footer = new PdfPTable(3);
        try {
            // set defaults
            footer.setWidths(new int[]{24, 2, 1});
            footer.setTotalWidth(527);
            footer.setLockedWidth(true);
            footer.getDefaultCell().setFixedHeight(40);
            footer.getDefaultCell().setBorder(Rectangle.TOP);
            footer.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

            // add copyright
            footer.addCell(new Phrase("© 2024 Michael Ryan Hunsaker, M.Ed., Ph.D. All Rights Reserved.", new Font(Font.FontFamily.HELVETICA, 8)));

            // add current page count
            footer.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
            footer.addCell(new Phrase(String.format("Page %d of", writer.getPageNumber()), new Font(Font.FontFamily.HELVETICA, 8)));

            // add placeholder for total page count
            PdfPCell totalPageCount = new PdfPCell(total);
            totalPageCount.setBorder(Rectangle.TOP);
            totalPageCount.setBorderColor(BaseColor.LIGHT_GRAY);
            footer.addCell(totalPageCount);

            // write page
            PdfContentByte canvas = writer.getDirectContent();
            canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
            footer.writeSelectedRows(0, -1, 34, 50, canvas);
            canvas.endMarkedContentSequence();
        } catch(DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    public void onCloseDocument(PdfWriter writer, Document document) {
        int totalLength = String.valueOf(writer.getPageNumber()).length();
        int totalWidth = totalLength * 5;
        ColumnText.showTextAligned(t, Element.ALIGN_RIGHT,
                new Phrase(String.valueOf(writer.getPageNumber()), new Font(Font.FontFamily.HELVETICA, 8)),
                totalWidth, 6, 0);
    }
}
}