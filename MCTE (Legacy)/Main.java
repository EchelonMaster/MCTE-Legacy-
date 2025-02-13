import shared.AbstractModule;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Main extends AbstractModule {

    // Instance variables for encryption key and file data
    private boolean setEmptyDelete;
    private String dataLoaded = "";
    private String keyStringMaker = "";
    
    // GUI components
    private JFrame frame;
    private JPanel editorPanel;
    private JTextArea editor;
    private Dimension d;
    private JScrollPane scrollPane;
    private JButton decryptButton;
    private JButton encryptButton;
    private JTextField location;
    private JTextField textInput;
    
    // Variables for encryption key table
    private ArrayList<ArrayList<String[]>> section = new ArrayList<>();
    private ArrayList<String[]> row = new ArrayList<>();
    private String[] keyPoint;
    
    // -------------------------------
    // Module Lifecycle Methods
    // -------------------------------
    
    @Override
    public void start() {
        // Ensure GUI creation on the EDT.
        SwingUtilities.invokeLater(() -> {
            if (frame == null) {
                initialize();
                setDefault();
            }
            frame.setVisible(true);
        });
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
    public void close() {
        if (frame != null) {
            frame.dispose();
            notifyClose();
        }
    }

    @Override
    public boolean isVisible() {
        return frame != null && frame.isVisible();
    }
    
    // -------------------------------
    // GUI Initialization
    // -------------------------------
    
    private void initialize() {
        frame = new JFrame("CCCrypter V.1.1.4.7");
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setBounds(100, 100, 555, 400);
        frame.getContentPane().setLayout(new BorderLayout());
        
        
        // Adjust layout on resize.
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
        
        // Create a tabbed pane with two tabs: Main Panel and Editor.
        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        
        d = Toolkit.getDefaultToolkit().getScreenSize();
        
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
        
        JTextArea textArea = new JTextArea();
        textArea.setLineWrap(true);
        scrollPane.setViewportView(textArea);
        
        decryptButton = new JButton("Decrypt");
        decryptButton.setBounds(109, 241, 89, 23);
        mainPanel.add(decryptButton);
        
        // ============================
        // Editor Panel
        // ============================
        editorPanel = new JPanel(null);
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
        // Action Listeners
        // --------------------------
        
        // Encrypt action
        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String encrypted = encrypt(textArea.getText());
                encrypted = "Encrypted :\n" + encrypted;
                textArea.setText(encrypted);
            }
        });
        
        // Decrypt action
        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("Decrypted :\n" + decrypt(textArea.getText()));
            }
        });
        
        // Key Loader action: load key from file
        keyLoader.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String dataLocation = getLocation();
                location.setText(dataLocation);
                dataLoaded = loadAllText(dataLocation);
                editor.setText(dataLoaded);
                keyLoad(dataLoaded);
            }
            
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
        });
        
        // Enter letter to add to the key string
        enterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!keyStringMaker.equals("")) {
                    keyStringMaker += ",";
                }
                keyStringMaker += textInput.getText();
                editor.setText(keyStringMaker);
            }
        });
        
        // Create key file action
        createKeyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (dataLoaded.equals("")) {
                        setDefault();
                        createTxtFile(dataLoaded);
                    } else {
                        dataLoaded = editor.getText();
                        createTxtFile(dataLoaded);
                    }
                } catch (Exception ex) {
                    editor.setText("The file failed to create:\n" + ex);
                }
            }
        });
        
        // Load default key
        loadDefault.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDefault();
                dataLoaded = "";
                showTable();
            }
        });
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
    // Encryption Key Methods
    // -------------------------------
    
    public void setDefault() {
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
    
    public void addKeyPoint(String[] key) {
        keyPoint = new String[key.length];
        for (int i = 0; i < key.length; i++) {
            keyPoint[i] = key[i];
        }
        row.add(keyPoint);
    }
    
    public void addCurrentRowToSection() {
        section.add(row);
        row = new ArrayList<>();
    }
    
    public static void writeText(String target, String string) {
        try (FileWriter writer = new FileWriter(target, true)) {
            writer.write(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
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
    
    public String decrypt(String text) { // Decrypt version 1.1
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
    
    // (Optional) Additional helper methods can be added here.
}
