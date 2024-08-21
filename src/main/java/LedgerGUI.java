
import com.formdev.flatlaf.intellijthemes.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;

public class LedgerGUI extends JFrame {

    private static final Map<
        String, Class<? extends LookAndFeel>> INTELLIJ_THEMES = new TreeMap<>();

    static {
        INTELLIJ_THEMES.put("Arc", FlatArcIJTheme.class);
        INTELLIJ_THEMES.put("Arc Orange", FlatArcOrangeIJTheme.class);
        INTELLIJ_THEMES.put("Carbon", FlatCarbonIJTheme.class);
        INTELLIJ_THEMES.put("Cobalt 2", FlatCobalt2IJTheme.class);
        INTELLIJ_THEMES.put("Cyan Light", FlatCyanLightIJTheme.class);
        INTELLIJ_THEMES.put("Dark Purple", FlatDarkPurpleIJTheme.class);
        INTELLIJ_THEMES.put("Dracula", FlatDraculaIJTheme.class);
        INTELLIJ_THEMES.put("Gray", FlatGrayIJTheme.class);
        INTELLIJ_THEMES.put(
                "Gruvbox Dark Hard",
                FlatGruvboxDarkHardIJTheme.class
        );
        INTELLIJ_THEMES.put("Hiberbee Dark", FlatHiberbeeDarkIJTheme.class);
        INTELLIJ_THEMES.put("High Contrast", FlatHighContrastIJTheme.class);
        INTELLIJ_THEMES.put("Light Flat", FlatLightFlatIJTheme.class);
        INTELLIJ_THEMES.put(
                "Material Design Dark",
                FlatMaterialDesignDarkIJTheme.class
        );
        INTELLIJ_THEMES.put("Monocai", FlatMonocaiIJTheme.class);
        INTELLIJ_THEMES.put("Nord", FlatNordIJTheme.class);
        INTELLIJ_THEMES.put("One Dark", FlatOneDarkIJTheme.class);
        INTELLIJ_THEMES.put("Solarized Dark", FlatSolarizedDarkIJTheme.class);
        INTELLIJ_THEMES.put("Solarized Light", FlatSolarizedLightIJTheme.class);
        INTELLIJ_THEMES.put("Spacegray", FlatSpacegrayIJTheme.class);
        INTELLIJ_THEMES.put("Vuesion", FlatVuesionIJTheme.class);
    }

    private final JTextField dateField;
    private final JTextField timeField;
    private final JTextArea notesField;
    private final JComboBox<
            String> projectField;
    private final JComboBox<
            String> schoolField;
    private final JComboBox<
            String> studentField;
    private final JComboBox<
            String> subjectField;
    private final JCheckBox completeCheckBox;
    private final JButton submitButton;
    private final JButton generatePdfButton;
    private final JTable dataTable;
    private final DefaultTableModel tableModel;

    private static final String DB_URL = "jdbc:sqlite:ledger.db";

    public LedgerGUI() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double widthPercentage = 0.67; // 67% of screen width
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;
        int appWidth = (int) (screenSize.width * widthPercentage);
        int appHeight = screenSize.height;
        setTitle("Accessible Document Generation Ledger");
        setSize(appWidth, appHeight);
        setLocation(
                (screenWidth - appWidth) / 2,
                (screenHeight - appHeight) / 2
        );
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create MenuBar
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenu themeMenu = new JMenu("Themes");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        aboutMenuItem.addActionListener((ActionEvent e) -> {
            JOptionPane.showMessageDialog(LedgerGUI.this, """
                                                                      Accessible Document Generation Ledger
                                                                      Version 2024.0.0-beta
                                                                      \u00a9 2024 Michael Ryan Hunsaker, M.Ed., Ph.D.
                                                                      All rights reserved.""",
                    "About",
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener((ActionEvent e) -> {
            System.exit(0);
        });

        for (String themeName : INTELLIJ_THEMES.keySet()) {
            JMenuItem item = new JMenuItem(themeName);
            item.addActionListener(e -> setIntelliJTheme(themeName));
            themeMenu.add(item);
        }
        fileMenu.add(aboutMenuItem);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        menuBar.add(themeMenu);
        setJMenuBar(menuBar); // Use setJMenuBar() to add the menu bar to the JFrame
        // Input Panel

        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addLabelAndField(
                inputPanel,
                "<html>Date: <br><i>(YYYY-MM-DD)</i></html>",
                dateField = new JTextField()
        //dateField.setText(formattedDate)
        );
        String[] studentOptions = {
            "AlPu",
            "AlRo",
            "AmRi",
            "AmOl",
            "AsNe",
            "AvWi",
            "BaAl",
            "BeWi",
            "BoUt",
            "BrTi",
            "CaDa",
            "CaHe",
            "CeNe",
            "ChTr",
            "ChCh",
            "ChGr",
            "ClPe",
            "CoBl",
            "CoCo",
            "CoHa",
            "CoBu",
            "CoHa",
            "CrPe",
            "CrAn",
            "DaCa",
            "DyPe",
            "ElLe",
            "ElWh",
            "ElSt",
            "EmTh",
            "EmTo",
            "EvCo",
            "FrAn",
            "FrLe",
            "GeBr",
            "GrDa",
            "GrCh",
            "HaGa",
            "HaHa",
            "HeUt",
            "HiWh",
            "HuHa",
            "HuTr",
            "InJo",
            "JaKa",
            "JaPe",
            "JaAb",
            "JaSm",
            "JeLe",
            "JuMa",
            "JuBa",
            "KaSt",
            "KaBr",
            "KaVi",
            "KaWa",
            "KeJo",
            "KeBy",
            "KiCh",
            "KiEl",
            "KiAg",
            "KiMi",
            "LaZa",
            "LaUl",
            "LaGr",
            "LaLe",
            "LaAr",
            "LiVa",
            "LiHo",
            "LuKi",
            "LuMo",
            "LyPe",
            "MaMc",
            "MaHa",
            "MaWi",
            "MaMa",
            "MaBl",
            "MaHe",
            "MaHe",
            "MeSc",
            "MiCo",
            "MiWe",
            "MiBe",
            "MoSt",
            "NaBu",
            "OlPa",
            "OlEv",
            "PaSa",
            "PeLa",
            "PrPe",
            "RaSc",
            "RaBa",
            "RoSo",
            "RyWe",
            "SaWi",
            "SaHi",
            "ScUt",
            "TaTi",
            "TaTr",
            "ThLl",
            "TjGu",
            "TrWe",
            "TrHa",
            "TrKe",
            "TyAs",
            "TyGr",
            "WeUt",
            "WeHe",
            "WiHa",
            "YaVa",
            "ZoFe",};
        dateField.setText(LocalDate.now().toString());
        addLabelAndField(
                inputPanel,
                "<html>Student: <br><i>(First Two Letters of First and Last Name)</i></html>",
                studentField = new JComboBox<>(studentOptions)
        );
        subjectField = new JComboBox<>(
                new String[]{
                    "Math",
                    "English",
                    "Chemistry",
                    "Biology",
                    "Health",
                    "Social Studies",
                    "Art",
                    "Music",
                    "Library",
                    "Computer Lab",
                    "Other",}
        );
        addLabelAndField(
                inputPanel,
                "<html>Academic Subject</html>",
                subjectField
        );
        String[] schoolOptions = {
            "Adelaide Elementary",
            "Antelope Elementary",
            "Bluff Ridge Elementary",
            "Boulton Elementary",
            "Bountiful Elementary",
            "Buffalo Point Elementary",
            "Burton Elementary",
            "Canyon Creek Elementary",
            "Centerville Elementary",
            "Clinton Elementary",
            "Columbia Elementary",
            "Cook Elementary",
            "Creekside Elementary",
            "Crestview Elementary",
            "Davis Connect K-6 Online School",
            "Doxey Elementary",
            "Eagle Bay Elementary",
            "East Layton Elementary",
            "Ellison Park Elementary",
            "Endeavour Elementary",
            "Farmington Elementary",
            "Foxboro Elementary",
            "Heritage Elementary",
            "Hill Field Elementary",
            "Holbrook Elementary",
            "Holt Elementary",
            "Island View Elementary",
            "Kays Creek Elementary",
            "Kaysville Elementary",
            "King Elementary",
            "Knowlton Elementary",
            "Lakeside Elementary",
            "Layton Elementary",
            "Lincoln Elementary",
            "Meadowbrook Elementary",
            "Morgan Elementary",
            "Mountain View Elementary",
            "Muir Elementary",
            "Oak Hills Elementary",
            "Odyssey Elementary",
            "Orchard Elementary",
            "Parkside Elementary",
            "Reading Elementary",
            "Sand Springs Elementary",
            "Snow Horse Elementary",
            "South Clearfield Elementary",
            "South Weber Elementary",
            "Stewart Elementary",
            "Sunburst Elementary",
            "Sunset Elementary",
            "Syracuse Elementary",
            "Taylor Elementary",
            "Tolman Elementary",
            "Vae View Elementary",
            "Valley View Elementary",
            "Wasatch Elementary",
            "West Bountiful Elementary",
            "West Clinton Elementary",
            "West Point Elementary",
            "Whitesides Elementary",
            "Windridge Elementary",
            "Woods Cross Elementary ",
            "Bountiful Junior High",
            "Centennial Junior High",
            "Centerville Junior High",
            "Central Davis Junior High",
            "Davis Connect 7-12",
            "Fairfield Junior High",
            "Farmington Junior High",
            "Kaysville Junior High",
            "Legacy Junior High",
            "Millcreek Junior High",
            "Mueller Park Junior High",
            "North Davis Junior High",
            "North Layton Junior High",
            "Shoreline Junior High",
            "South Davis Junior High",
            "Sunset Junior High",
            "Syracuse Junior High",
            "West Point Junior High ",
            "Bountiful High",
            "Clearfield High",
            "Davis Connect 7-12",
            "Davis High",
            "Farmington High",
            "Layton High",
            "Northridge High",
            "Syracuse High",
            "Viewmont High",
            "Woods Cross High",};
        schoolField = new JComboBox<>(schoolOptions);

        addLabelAndField(inputPanel, "School", schoolField);
        String[] projectOptions = {
            "UEB Literary Transcription",
            "UEB Technical Transcription",
            "Tactile Graphics Generation",
            "Large Print Generation",
            "3D Print Rendering",
            "3D Print Production",};
        projectField = new JComboBox<>(projectOptions);
        addLabelAndField(inputPanel, "Project Type", projectField);
        addLabelAndField(
                inputPanel,
                "<html>Time: <br>(Rounded UP to Nearest .25 hr fter 5 min)</html>",
                timeField = new JTextField()
        );
        notesField = new JTextArea();
        notesField.setLineWrap(true);
        notesField.setWrapStyleWord(true);
        JScrollPane notesScrollPane = new JScrollPane(notesField);
        addLabelAndField(
                inputPanel,
                "<html>Process Notes:</html>",
                notesScrollPane
        );
        JLabel completeLabel = new JLabel("Complete:");
        completeCheckBox = new JCheckBox();
        completeLabel.setLabelFor(completeCheckBox);
        inputPanel.add(completeLabel);
        inputPanel.add(completeCheckBox);
        completeCheckBox.setMnemonic(KeyEvent.VK_C);
        completeCheckBox
                .getAccessibleContext()
                .setAccessibleDescription("Check if the task is complete");
        submitButton = new JButton("Submit");
        submitButton.setMnemonic(KeyEvent.VK_S);
        submitButton.addActionListener(e -> submitData());
        submitButton
                .getAccessibleContext()
                .setAccessibleDescription(
                        "Submit the entered data to the database"
                );
        generatePdfButton = new JButton("Generate PDF Report");
        generatePdfButton.setMnemonic(KeyEvent.VK_G);
        generatePdfButton.addActionListener(e -> showDateRangeDialog());
        generatePdfButton
                .getAccessibleContext()
                .setAccessibleDescription(
                        "Generate a PDF report for a specified date range"
                );

        JPanel buttonPanel = new JPanel(
                new FlowLayout(FlowLayout.CENTER, 10, 0)
        );
        buttonPanel.add(submitButton);
        buttonPanel.add(generatePdfButton);
        inputPanel.add(buttonPanel);

        add(inputPanel, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new String[]{
                    "Date",
                    "Student",
                    "Subject",
                    "School",
                    "Project",
                    "Time",},
                0
        );
        dataTable = new JTable(tableModel);
        dataTable
                .getAccessibleContext()
                .setAccessibleDescription("Table showing ledger entries");
        dataTable.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(dataTable);
        add(scrollPane, BorderLayout.CENTER);

        initializeDatabase();
        loadDataFromDatabase();

        // Set up focus traversal
        setFocusTraversalPolicy(new LayoutFocusTraversalPolicy());
        setFocusCycleRoot(true);
    }

    private void addLabelAndField(
            JPanel panel,
            String labelText,
            JComponent field
    ) {
        JLabel label = new JLabel(labelText);
        label.setLabelFor(field);
        panel.add(label);
        panel.add(field);
        field
                .getAccessibleContext()
                .setAccessibleDescription("Enter " + labelText.toLowerCase());
    }

    private String cleanInput(String input) {
        // Remove any characters that might cause issues with SQLite
        // This example removes quotes and escapes backslashes
        return input
                .replace("'", "''")
                .replace("\"", "\"\"")
                .replace("\\", "\\\\");
    }

    private void showDateRangeDialog() {
        JTextField startDateField = new JTextField(getPreviousMonth16th(), 10);
        JTextField endDateField = new JTextField(getCurrentMonth15th(), 10);
        JComboBox studentMaterial = new JComboBox<>(
                new String[]{
                    "AlPu",
                    "AlRo",
                    "AmRi",
                    "AmOl",
                    "AsNe",
                    "AvWi",
                    "BaAl",
                    "BeWi",
                    "BoUt",
                    "BrTi",
                    "CaDa",
                    "CaHe",
                    "CeNe",
                    "ChTr",
                    "ChCh",
                    "ChGr",
                    "ClPe",
                    "CoBl",
                    "CoCo",
                    "CoHa",
                    "CoBu",
                    "CoHa",
                    "CrPe",
                    "CrAn",
                    "DaCa",
                    "DyPe",
                    "ElLe",
                    "ElWh",
                    "ElSt",
                    "EmTh",
                    "EmTo",
                    "EvCo",
                    "FrAn",
                    "FrLe",
                    "GeBr",
                    "GrDa",
                    "GrCh",
                    "HaGa",
                    "HaHa",
                    "HeUt",
                    "HiWh",
                    "HuHa",
                    "HuTr",
                    "InJo",
                    "JaKa",
                    "JaPe",
                    "JaAb",
                    "JaSm",
                    "JeLe",
                    "JuMa",
                    "JuBa",
                    "KaSt",
                    "KaBr",
                    "KaVi",
                    "KaWa",
                    "KeJo",
                    "KeBy",
                    "KiCh",
                    "KiEl",
                    "KiAg",
                    "KiMi",
                    "LaZa",
                    "LaUl",
                    "LaGr",
                    "LaLe",
                    "LaAr",
                    "LiVa",
                    "LiHo",
                    "LuKi",
                    "LuMo",
                    "LyPe",
                    "MaMc",
                    "MaHa",
                    "MaWi",
                    "MaMa",
                    "MaBl",
                    "MaHe",
                    "MaHe",
                    "MeSc",
                    "MiCo",
                    "MiWe",
                    "MiBe",
                    "MoSt",
                    "NaBu",
                    "OlPa",
                    "OlEv",
                    "PaSa",
                    "PeLa",
                    "PrPe",
                    "RaSc",
                    "RaBa",
                    "RoSo",
                    "RyWe",
                    "SaWi",
                    "SaHi",
                    "ScUt",
                    "TaTi",
                    "TaTr",
                    "ThLl",
                    "TjGu",
                    "TrWe",
                    "TrHa",
                    "TrKe",
                    "TyAs",
                    "TyGr",
                    "WeUt",
                    "WeHe",
                    "WiHa",
                    "YaVa",
                    "ZoFe",});
        // Create checkboxes for project options
        String[] projectOptions = {
            "UEB Literary Transcription",
            "UEB Technical Transcription",
            "Tactile Graphics Generation",
            "Large Print Generation",
            "3D Print Rendering",
            "3D Print Production",};
        JCheckBox[] projectCheckboxes = new JCheckBox[projectOptions.length];

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);

        panel.add(new JLabel("Start Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        panel.add(startDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("End Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        panel.add(endDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Student Receiving Materials"), gbc);
        gbc.gridx = 1;
        panel.add(studentMaterial, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Select Projects:"), gbc);

        gbc.gridy++;
        for (int i = 0; i < projectOptions.length; i++) {
            projectCheckboxes[i] = new JCheckBox(projectOptions[i]);
            projectCheckboxes[i].setSelected(true); // Default to selected
            panel.add(projectCheckboxes[i], gbc);
            gbc.gridy++;
        }

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Enter Date Range and Select Projects",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            String startDate = startDateField.getText();
            String endDate = endDateField.getText();
            List<String> selectedProjects = new ArrayList<>();
            for (JCheckBox checkbox : projectCheckboxes) {
                if (checkbox.isSelected()) {
                    selectedProjects.add(checkbox.getText());
                }
            }
            generatePdfReport(startDate, endDate, selectedProjects);
        }
    }

    // Helper methods to get current date and previous month end date
    public String getCurrentMonth15th() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 15); // Set day to 15
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    public String getPreviousMonth16th() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1); // Go back one month
        cal.set(Calendar.DAY_OF_MONTH, 16); // Set day to 15
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(cal.getTime());
    }

    public static void main(String[] args) {
        // Set the look and feel to the system look and feel
        String defaultTheme = "Cobalt 2"; // or any other theme name from the map
        try {
            UIManager.setLookAndFeel(
                    INTELLIJ_THEMES.get(defaultTheme)
                            .getDeclaredConstructor()
                            .newInstance()
            );
            UIManager.put(
                    "defaultFont",
                    new FontUIResource(
                            new java.awt.Font("Helvetica", java.awt.Font.PLAIN, 24)
                    )
            ); // Example font size 16
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | UnsupportedLookAndFeelException ex) {
        }

        SwingUtilities.invokeLater(() -> {
            new LedgerGUI().setVisible(true);
        });
    }

    private void setIntelliJTheme(String themeName) {
        try {
            Class<? extends LookAndFeel> themeClass = INTELLIJ_THEMES.get(
                    themeName
            );
            if (themeClass != null) {
                UIManager.setLookAndFeel(
                        themeClass.getDeclaredConstructor().newInstance()
                );
                UIManager.put(
                        "defaultFont",
                        new FontUIResource(
                                new java.awt.Font("Helvetica", java.awt.Font.PLAIN, 24)
                        )
                ); // Example font size 16
                SwingUtilities.updateComponentTreeUI(this);
            } else {
                System.err.println("Theme not found: " + themeName);
            }
        } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | UnsupportedLookAndFeelException ex) {
        }
    }

    private void initializeDatabase() {
        try (
                Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement()) {
            String sql
                    = "CREATE TABLE IF NOT EXISTS ledger "
                    + "(id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "date TEXT NOT NULL, "
                    + "student TEXT NOT NULL,"
                    + "subject TEXT NOT NULL,"
                    + "school TEXT NOT NULL, "
                    + "project TEXT NOT NULL, "
                    + "time TEXT NOT NULL, "
                    + "notes TEXT NOT NULL,"
                    + "complete BOOLEAN NOT NULL)";
            stmt.execute(sql);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error initializing database: " + e.getMessage()
            );
        }
    }

    private void loadDataFromDatabase() {
        tableModel.setRowCount(0);
        try (
                Connection conn = DriverManager.getConnection(DB_URL); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(
                "SELECT date, student, subject, school, project, time FROM ledger"
        )) {
            while (rs.next()) {
                Object[] row = {
                    rs.getString("date"),
                    rs.getString("student"),
                    rs.getString("subject"),
                    rs.getString("school"),
                    rs.getString("project"),
                    rs.getString("time"),};
                tableModel.addRow(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error loading data: " + e.getMessage()
            );
        }
    }

    private void submitData() {
        String date = dateField.getText();
        String school = schoolField.getSelectedItem().toString();
        String student = studentField.getSelectedItem().toString();
        if (!student.matches("[A-Z][a-z][A-Z][a-z]")) {
            JOptionPane.showMessageDialog(
                    this,
                    "Student field must be in the format XxXx (e.g., AbCd)"
            );
            return;
        }
        String subject = subjectField.getSelectedItem().toString();
        String notes = cleanInput(notesField.getText());
        String project = projectField.getSelectedItem().toString();
        String time = timeField.getText();
        boolean complete = completeCheckBox.isSelected();

        try (
                Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO ledger (date, student, subject, school, project, time, notes, complete) VALUES (?, ?, ?, ?, ?, ?, ?, ?)"
        )) {
            pstmt.setString(1, date);
            pstmt.setString(2, student);
            pstmt.setString(3, subject);
            pstmt.setString(4, school);
            pstmt.setString(5, project);
            pstmt.setString(6, time);
            String sqlNotes = notes.replace("\n", "\\n");
            pstmt.setString(7, sqlNotes);
            pstmt.setBoolean(8, complete);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error saving data: " + e.getMessage()
            );
            return;
        }

        loadDataFromDatabase();
        JOptionPane.showMessageDialog(this, "Data submitted successfully.");
        clearInputFields();
    }

    private void clearInputFields() {
        LocalDate currentDate = LocalDate.now();
        // Format the date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = currentDate.format(formatter);
        dateField.setText(formattedDate);
        notesField.setText("");
        timeField.setText("");
        completeCheckBox.setSelected(false);
    }

    private void generatePdfReport(
            String startDate,
            String endDate,
            List<String> selectedProjects
    ) {
        String filePath = "";
        try {
            // Get the user's Downloads directory
            String userHome = System.getProperty("user.home");
            String downloadsDir = userHome + "/Downloads/";

            String fileName
                    = "LedgerReport_" + startDate + "_to_" + endDate + ".pdf";

            filePath = downloadsDir + fileName;

            Document document = new Document(PageSize.LETTER, 36, 36, 108, 72); // top margin increased to 1.5 inches (108 points)
            PdfWriter writer = PdfWriter.getInstance(
                    document,
                    new FileOutputStream(filePath)
            );

            HeaderFooterPageEvent event = new HeaderFooterPageEvent();
            writer.setPageEvent(event);

            document.open();

            // Add title "INVOICE"
            Paragraph title = new Paragraph(
                    "INVOICE: " + startDate + " to " + endDate,
                    new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            16,
                            com.itextpdf.text.Font.BOLD
                    )
            );
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(28.35f); // 1 cm spacing after header
            title.setSpacingAfter(5.67f); // 0.2 cm spacing before table
            document.add(title);

            // Add table
            List<String[]> data = fetchDataFromDatabase(
                    startDate,
                    endDate,
                    selectedProjects
            );
            Object[] tableAndTotal = createTable(data, document, writer, event);
            PdfPTable table = (PdfPTable) tableAndTotal[0];
            double totalTime = (Double) tableAndTotal[1];

            document.add(table);

            // Add total
            DecimalFormat df = new DecimalFormat("#,##0.00");
            Paragraph totalParagraph = new Paragraph(
                    "Total Billed Hours: " + df.format(totalTime),
                    new com.itextpdf.text.Font(
                            com.itextpdf.text.Font.FontFamily.HELVETICA,
                            12,
                            com.itextpdf.text.Font.BOLD
                    )
            );
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalParagraph);

            // Add signature line
            /* Paragraph signatureLine = new Paragraph(
                "Signature: _______________________",
                new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA,
                    12
                )
            );
            signatureLine.setAlignment(Element.ALIGN_RIGHT);
            signatureLine.setSpacingBefore(50f); // 1 cm spacing after table
            document.add(signatureLine); */
            document.close();
        } catch (DocumentException | FileNotFoundException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error generating PDF: " + e.getMessage()
            );
        }
        try {
            File pdfFile = new File(filePath);
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Desktop not supported. Unable to open PDF automatically."
                    );
                }
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "PDF file not found: " + filePath
                );
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "File written to  " + filePath
            );
        }
    }

    private Object[] createTable(
            List<String[]> data,
            Document document,
            PdfWriter writer,
            HeaderFooterPageEvent event
    ) throws DocumentException {
        double totalTime = 0.0;
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 2, 2, 2, 2, 1});

        // Add table headers
        for (String header : new String[]{
            "Date",
            "Student",
            "Subject",
            "School",
            "Project",
            "Time",}) {
            PdfPCell cell = new PdfPCell(
                    new Phrase(
                            header,
                            new com.itextpdf.text.Font(
                                    com.itextpdf.text.Font.FontFamily.HELVETICA,
                                    12,
                                    com.itextpdf.text.Font.BOLD
                            )
                    )
            );
            cell.setBorder(com.itextpdf.text.Rectangle.BOTTOM);
            cell.setBorderColor(BaseColor.LIGHT_GRAY);
            cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
            cell.setPadding(5);
            table.addCell(cell);
        }

        // Calculate available height for table
        float totalHeight
                = document.getPageSize().getHeight()
                - document.topMargin()
                - document.bottomMargin();
        float headerHeight = 108; // 1.5 inches
        float titleHeight = 16 + 28.35f + 5.67f; // Title font size + spacing before and after
        float footerHeight = 50; // Adjust based on your footer height
        float signatureHeight = 20; // Height for signature line
        float availableHeight
                = totalHeight
                - headerHeight
                - titleHeight
                - footerHeight
                - signatureHeight;

        float rowHeight = 20; // Adjust based on your row height
        int maxRows = (int) (availableHeight / rowHeight);

        // Add data
        for (int i = 0; i < Math.min(maxRows, data.size()); i++) {
            String[] row = data.get(i);
            for (int j = 0; j < row.length; j++) {
                PdfPCell pdfCell = new PdfPCell(
                        new Phrase(
                                row[j],
                                new com.itextpdf.text.Font(
                                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                                        10
                                )
                        )
                );
                pdfCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                pdfCell.setPadding(5);
                table.addCell(pdfCell);

                // Sum up the time (assuming it's in the last column)
                if (j == row.length - 1) {
                    try {
                        totalTime += Double.parseDouble(row[j]);
                    } catch (NumberFormatException e) {
                        // Handle parsing error if necessary
                    }
                }
            }
        }

        // Fill remaining rows with empty cells if necessary
        for (int i = data.size(); i < maxRows; i++) {
            for (int j = 0; j < 6; j++) {
                PdfPCell pdfCell = new PdfPCell(new Phrase(" "));
                pdfCell.setBorder(com.itextpdf.text.Rectangle.NO_BORDER);
                pdfCell.setPadding(5);
                table.addCell(pdfCell);
            }
        }

        return new Object[]{table, totalTime};
    }

    private class HeaderFooterPageEvent extends PdfPageEventHelper {

        private PdfTemplate t;
        private com.itextpdf.text.Image total;

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            t = writer.getDirectContent().createTemplate(30, 16);
            try {
                total = com.itextpdf.text.Image.getInstance(t);
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
            PdfPTable header = new PdfPTable(1);
            try {
                // set defaults
                header.setWidths(new int[]{24});
                header.setTotalWidth(527);
                header.setLockedWidth(true);
                header.getDefaultCell().setFixedHeight(108); // 1.5 inches
                header
                        .getDefaultCell()
                        .setBorder(com.itextpdf.text.Rectangle.BOTTOM);
                header.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

                // add text
                PdfPCell text = new PdfPCell();
                text.setPaddingBottom(15);
                text.setPaddingLeft(10);
                text.setBorder(com.itextpdf.text.Rectangle.BOTTOM);
                text.setBorderColor(BaseColor.LIGHT_GRAY);
                text.addElement(
                        new Phrase(
                                " ",
                                new com.itextpdf.text.Font(
                                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                                        12
                                )
                        )
                );
                text.addElement(
                        new Phrase(
                                "Michael Ryan Hunsaker, M.Ed., Ph.D.",
                                new com.itextpdf.text.Font(
                                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                                        12
                                )
                        )
                );
                text.addElement(
                        new Phrase(
                                "Davis School District",
                                new com.itextpdf.text.Font(
                                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                                        12
                                )
                        )
                );
                text.addElement(
                        new Phrase(
                                "Farmington, UT 84025",
                                new com.itextpdf.text.Font(
                                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                                        12
                                )
                        )
                );
                header.addCell(text);

                // write content
                header.writeSelectedRows(
                        0,
                        -1,
                        34,
                        803,
                        writer.getDirectContent()
                );
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
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
                footer
                        .getDefaultCell()
                        .setBorder(com.itextpdf.text.Rectangle.TOP);
                footer.getDefaultCell().setBorderColor(BaseColor.LIGHT_GRAY);

                // add copyright
                footer.addCell(
                        new Phrase(
                                "© 2024 Michael Ryan Hunsaker, M.Ed., Ph.D.. All Rights Reserved.",
                                new com.itextpdf.text.Font(
                                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                                        8
                                )
                        )
                );

                // add current page count
                footer
                        .getDefaultCell()
                        .setHorizontalAlignment(Element.ALIGN_RIGHT);
                footer.addCell(
                        new Phrase(
                                String.format("Page %d of", writer.getPageNumber()),
                                new com.itextpdf.text.Font(
                                        com.itextpdf.text.Font.FontFamily.HELVETICA,
                                        8
                                )
                        )
                );

                // add placeholder for total page count
                PdfPCell totalPageCount = new PdfPCell(total);
                totalPageCount.setBorder(com.itextpdf.text.Rectangle.TOP);
                totalPageCount.setBorderColor(BaseColor.LIGHT_GRAY);
                footer.addCell(totalPageCount);

                // write page
                PdfContentByte canvas = writer.getDirectContent();
                canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
                footer.writeSelectedRows(0, -1, 34, 50, canvas);
                canvas.endMarkedContentSequence();
            } catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            int totalLength = String.valueOf(writer.getPageNumber()).length();
            int totalWidth = totalLength * 5;
            ColumnText.showTextAligned(
                    t,
                    Element.ALIGN_RIGHT,
                    new Phrase(
                            String.valueOf(writer.getPageNumber()),
                            new com.itextpdf.text.Font(
                                    com.itextpdf.text.Font.FontFamily.HELVETICA,
                                    8
                            )
                    ),
                    totalWidth,
                    6,
                    0
            );
        }
    }

    private List<String[]> fetchDataFromDatabase(
            String startDate,
            String endDate,
            List<String> selectedProjects
    ) {
        List<String[]> data = new ArrayList<>();
        try (
                Connection conn = DriverManager.getConnection(DB_URL); PreparedStatement pstmt = conn.prepareStatement(
                "SELECT date, student, subject, school, project, time FROM ledger WHERE date BETWEEN ? AND ? "
                + "AND project IN ("
                + String.join(
                        ",",
                        Collections.nCopies(selectedProjects.size(), "?")
                )
                + ") "
                + "AND student = ? "
                + // Added student filter
                "ORDER BY date"
        )) {
            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);

            // Set project parameters
            for (int i = 0; i < selectedProjects.size(); i++) {
                pstmt.setString(i + 3, selectedProjects.get(i));
            }
            String selectedStudent = null;

            // Set student parameter
            pstmt.setString(selectedProjects.size() + 3, selectedStudent);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String[] row = {
                    rs.getString("date"),
                    rs.getString("student"),
                    rs.getString("subject"),
                    rs.getString("school"),
                    rs.getString("project"),
                    rs.getString("time"),};
                data.add(row);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error fetching data: " + e.getMessage()
            );
        }
        return data;
    }
}
