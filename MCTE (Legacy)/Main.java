import shared.AbstractModule;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

public class Main extends AbstractModule {

    // Instance variables for encryption key and file data
    private boolean setEmptyDelete;
    private String dataLoaded = "";
    private String keyStringMaker = "";

    // GUI components
    private JFrame frame;
    private JTextArea textArea;
    private JTextArea editor;
    private JScrollPane scrollPane;
    private JButton decryptButton;
    private JButton encryptButton;
    private JTextField location;
    private JTextField textInput;

    // Variables for encryption key table
    private ArrayList<ArrayList<String[]>> section = new ArrayList<>();
    private ArrayList<String[]> row = new ArrayList<>();
    private String[] keyPoint;

    // Constructor
    public Main() {
        // Avoid asynchronous initialization here.
    }

    // -------------------------------
    // AbstractModule Lifecycle Methods
    // -------------------------------

    @Override
    public void start() {
        // Ensure that the frame is initialized on the EDT.
        if (frame == null) {
            if (SwingUtilities.isEventDispatchThread()) {
                initialize();
                setDefaultKey();
            } else {
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        initialize();
                        setDefaultKey();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        frame.setVisible(true);
    }

    @Override
    public void bringToFront() {
        if (frame != null) {
            frame.toFront();
            frame.repaint();
        }
    }

    @Override
    public void hideModule() {
        if (frame != null) {
            frame.setVisible(false);
        }
    }

    @Override
    public void showModule() {
        if (frame != null) {
            frame.setVisible(true);
        }
    }

    @Override
    public boolean isVisible() {
        return frame != null && frame.isVisible();
    }

    /**
     * Cleanup resources when closing.
     */
    @Override
    protected void onClose() {
        if (frame != null) {
            frame.dispose();
            frame = null;
        }
    }

    // -------------------------------
    // GUI Initialization
    // -------------------------------

    private void initialize() {
        frame = new JFrame("CCCrypter V.1.1.4.7");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setSize(555, 400);
        frame.setLayout(new BorderLayout());
        frame.setAlwaysOnTop(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Instead of calling onClose() directly, call the final close() method.
                close();
            }
        });

        // Create a tabbed pane with two tabs: Main Panel and Editor.
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

        // ============================
        // Main Panel
        // ============================
        JPanel mainPanel = new JPanel(null);
        tabbedPane.addTab("Main Panel", null, mainPanel, null);

        JButton keyLoader = new JButton("Load Key");
        keyLoader.setBounds(10, 11, 98, 23);
        mainPanel.add(keyLoader);

        location = new JTextField();
        location.setBounds(118, 12, 390, 20);
        location.setText("Not loaded yet, Using Default");
        location.setEditable(false);
        mainPanel.add(location);

        JLabel lblInput = new JLabel("Input Portal:");
        lblInput.setBounds(10, 43, 138, 14);
        mainPanel.add(lblInput);

        encryptButton = new JButton("Encrypt");
        encryptButton.setBounds(10, 241, 89, 23);
        mainPanel.add(encryptButton);

        scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 58, 498, 172);
        mainPanel.add(scrollPane);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        scrollPane.setViewportView(textArea);

        decryptButton = new JButton("Decrypt");
        decryptButton.setBounds(109, 241, 89, 23);
        mainPanel.add(decryptButton);

        // ============================
        // Editor Panel
        // ============================
        JPanel editorPanel = new JPanel(null);
        tabbedPane.addTab("Editor", null, editorPanel, null);

        JScrollPane scrollPaneEditor = new JScrollPane();
        scrollPaneEditor.setBounds(10, 11, 351, 304);
        editorPanel.add(scrollPaneEditor);

        editor = new JTextArea();
        editor.setLineWrap(true);
        scrollPaneEditor.setViewportView(editor);

        textInput = new JTextField();
        textInput.setBounds(371, 36, 49, 33);
        editorPanel.add(textInput);

        JButton enterButton = new JButton("Enter");
        enterButton.setBounds(430, 36, 78, 33);
        editorPanel.add(enterButton);

        JLabel lblAddLetter = new JLabel("Add Letter:");
        lblAddLetter.setBounds(371, 11, 102, 14);
        editorPanel.add(lblAddLetter);

        JButton createKeyButton = new JButton("Create Key");
        createKeyButton.setBounds(369, 80, 139, 23);
        editorPanel.add(createKeyButton);

        JButton loadDefault = new JButton("Default");
        loadDefault.setBounds(371, 114, 137, 23);
        editorPanel.add(loadDefault);

        // --------------------------
        // Component Listener for Resizing
        // --------------------------
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (scrollPane != null && encryptButton != null && decryptButton != null) {
                    scrollPane.setSize(frame.getWidth() - 35, frame.getHeight() - 225);
                    encryptButton.setLocation(10, frame.getHeight() - 162);
                    decryptButton.setLocation(109, frame.getHeight() - 162);
                }
            }
        });

        // --------------------------
        // Action Listeners
        // --------------------------

        // Encrypt action
        encryptButton.addActionListener(e -> {
            String encrypted = encrypt(textArea.getText());
            encrypted = "Encrypted :\n" + encrypted;
            textArea.setText(encrypted);
        });

        // Decrypt action
        decryptButton.addActionListener(e -> {
            textArea.setText("Decrypted :\n" + decrypt(textArea.getText()));
        });

        // Key Loader action: load key from file
        keyLoader.addActionListener(e -> {
            String dataLocation = getLocation();
            location.setText(dataLocation);
            dataLoaded = loadAllText(dataLocation);
            editor.setText(dataLoaded);
            keyLoad(dataLoaded);
        });

        // Enter letter to add to the key string
        enterButton.addActionListener(e -> {
            if (!keyStringMaker.isEmpty()) {
                keyStringMaker += ",";
            }
            keyStringMaker += textInput.getText();
            editor.setText(keyStringMaker);
        });

        // Create key file action
        createKeyButton.addActionListener(e -> {
            try {
                if (dataLoaded.isEmpty()) {
                    setDefaultKey();
                    createTxtFile(dataLoaded);
                } else {
                    dataLoaded = editor.getText();
                    createTxtFile(dataLoaded);
                }
            } catch (Exception ex) {
                editor.setText("The file failed to create:\n" + ex);
            }
        });

        // Load default key action
        loadDefault.addActionListener(e -> {
            setDefaultKey();
            dataLoaded = "";
            showTable();
        });
    }

    // -------------------------------
    // Key Loading and Processing Methods
    // -------------------------------

    /**
     * Loads key data from the provided text into the key table.
     */
    private void keyLoad(String data) {
        section = new ArrayList<>();
        row = new ArrayList<>();
        String[] allLine = data.split("\n");
        for (String element : allLine) {
            if (element.contains("newSection")) {
                addCurrentRowToSection();
            } else {
                addKeyPoint(element.split(","));
            }
        }
        addCurrentRowToSection();
    }

    /**
     * Sets up the default key table.
     */
    public void setDefaultKey() {
        section = new ArrayList<>();
        row = new ArrayList<>();
        addKeyPoint(new String[] {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"});
        addKeyPoint(new String[] {"K", "L", "M", "N", "O", "P", "Q", "R", "S"});
        addKeyPoint(new String[] {"T", "U", "V", "W", "X", "Y", "Z"});
        addCurrentRowToSection();
        addKeyPoint(new String[] {" ", "\t"});
        addCurrentRowToSection();
        addKeyPoint(new String[] {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j"});
        addKeyPoint(new String[] {"k", "l", "m", "n", "o", "p", "q", "r", "s"});
        addKeyPoint(new String[] {"t", "u", "v", "w", "x", "y", "z"});
        addCurrentRowToSection();
        addKeyPoint(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});
        addCurrentRowToSection();
        addKeyPoint(new String[] {".", ",", "&", "\'", "\"", "_", "|", "\\"});
        addKeyPoint(new String[] {"(", ")", "[", "]", "{", "}", "<", ">", ";", ":"});
        addKeyPoint(new String[] {"!", "?", "~", "`"});
        addCurrentRowToSection();
        addKeyPoint(new String[] {"+", "-", "*", "/", "%", "="});
        addCurrentRowToSection();
        addKeyPoint(new String[] {"@", "#", "$", "^"});
    }

    /**
     * Displays the key table in the editor.
     */
    public void showTable() {
        StringBuilder tableData = new StringBuilder();
        for (int i = 0; i < section.size(); i++) {
            if (i != 0 && i != section.size()) {
                tableData.append("newSection\n");
            }
            for (String[] element : section.get(i)) {
                for (int k = 0; k < element.length; k++) {
                    if (k != 0) {
                        tableData.append(",");
                    }
                    tableData.append(element[k]);
                }
                tableData.append("\n");
            }
        }
        editor.setText(tableData.toString());
    }

    /**
     * Adds a key point (a set of characters) to the current row.
     */
    public void addKeyPoint(String[] key) {
        keyPoint = new String[key.length];
        for (int i = 0; i < key.length; i++) {
            keyPoint[i] = key[i];
        }
        row.add(keyPoint);
    }

    /**
     * Adds the current row of key points to the key table section.
     */
    public void addCurrentRowToSection() {
        section.add(row);
        row = new ArrayList<>();
    }

    // -------------------------------
    // File and Directory Utilities
    // -------------------------------

    public String getLocation() {
        File file = fileSelector.DefaultLoadFile();
        return (file != null ? file.getAbsolutePath() : "");
    }

    public void createTxtFile(String txt) {
        File newTxtFile = new File(directorySelector.DirectorySelector()
                + File.separator
                + askStringBox("File Name", "Please Enter File name and Format."));
        try (FileWriter fw = new FileWriter(newTxtFile)) {
            fw.write(txt);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String askStringBox(String question, String title) {
        return ANPQuestionHolder.questionInput(question, title);
    }

    public static class fileSelector {
        public static File DefaultLoadFile() {
            JFileChooser chooser = new JFileChooser();
            int selected = chooser.showOpenDialog(null);
            if (selected == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile();
            }
            return null;
        }
    }

    public static class directorySelector {
        public static String DirectorySelector() {
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new java.io.File("."));
            chooser.setDialogTitle("Directory Selector");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File directory = chooser.getSelectedFile();
                return directory.getAbsolutePath();
            }
            return "";
        }
    }

    public final static class ANPQuestionHolder {
        public static String questionInput(String question, String title) {
            return JOptionPane.showInputDialog(new JFrame(), question, title, JOptionPane.QUESTION_MESSAGE);
        }
    }

    // -------------------------------
    // Missing Method: loadAllText
    // -------------------------------

    /**
     * Loads all text from the file located at the given path.
     */
    public String loadAllText(String location) {
        File file = new File(location);
        StringBuilder allText = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String st;
            while ((st = br.readLine()) != null) {
                if (setEmptyDelete) {
                    if (!st.equals("")) {
                        allText.append(st).append("\n");
                    }
                } else {
                    allText.append(st).append("\n");
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allText.toString();
    }

    // -------------------------------
    // Encryption / Decryption Logic
    // -------------------------------

    /**
     * Encrypts the given text using the current key table.
     */
    public String encrypt(String text) {
        StringBuilder encText = new StringBuilder();
        for (int c = 0; c < text.length(); c++) {
            String currentChar = text.substring(c, c + 1);
            if (currentChar.equals("\n")) {
                encText.append("\n");
                continue;
            }
            // Loop through the key table to find a match.
            for (int i = 0; i < section.size(); i++) { // section
                for (int j = 0; j < section.get(i).size(); j++) { // row
                    for (int k = 0; k < section.get(i).get(j).length; k++) {
                        if (section.get(i).get(j)[k].equals(currentChar)) {
                            if (currentChar.toLowerCase().matches(".*[a-z].*")) {
                                encText.append("T");
                            }
                            encText.append(i + 1); // section (1-indexed)
                            encText.append(j);     // row
                            int starter = k + j;
                            encText.append(starter); // point
                            int maximum = 10 - j;
                            if (maximum == 10) {
                                maximum--;
                            }
                            encText.append(maximum);
                            int subtract = maximum - k - j;
                            encText.append(subtract);
                        }
                    }
                }
            }
        }
        return encText.toString();
    }

    /**
     * Decrypts the given text using the current key table.
     */
    public String decrypt(String text) {
        StringBuilder totalLetter = new StringBuilder();
        try {
            ArrayList<String> collector = new ArrayList<>();
            String current = "";
            String[] newLineSep = text.split("\n");

            // Collect encoded segments
            for (String element : newLineSep) {
                for (int c = 0; c < element.length(); c++) {
                    String ch = element.substring(c, c + 1);
                    if (Character.isLetter(ch.charAt(0))) {
                        current += ch;
                    } else if (Character.isDigit(ch.charAt(0))) {
                        current += ch;
                        if (c + 1 < element.length()) {
                            if (Character.isLetter(element.substring(c + 1, c + 2).charAt(0))) {
                                collector.add(current);
                                current = "";
                            }
                        } else {
                            collector.add(current);
                            current = "";
                        }
                    }
                }
                collector.add("\n");
            }

            // Process each encoded segment
            for (String element : collector) {
                String Letter = "";
                if (element.contains("\n")) {
                    Letter = "\n";
                } else if (element.length() > 4) {
                    int i = Integer.valueOf(element.substring(1, 2)) - 1;
                    int j = Integer.valueOf(element.substring(2, 3));
                    int k = Integer.valueOf(element.substring(3, 4));
                    if (Integer.valueOf(element.substring(4, 5)) - Integer.valueOf(element.substring(5, 6)) == k) {
                        k = k - j;
                    }
                    Letter = section.get(i).get(j)[k];
                }
                totalLetter.append(Letter);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return totalLetter.toString();
    }

    // -------------------------------
    // Main Method (for testing or standalone use)
    // -------------------------------
    public static void main(String[] args) {
        Main module = new Main();
        module.start();
    }
}

