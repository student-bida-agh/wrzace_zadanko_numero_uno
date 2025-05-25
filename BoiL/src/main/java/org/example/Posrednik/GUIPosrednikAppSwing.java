package org.example.Posrednik;
import org.example.Posrednik.Dostawca;
import org.example.Posrednik.Odbiorca;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GUIPosrednikAppSwing extends JFrame {
    private JTabbedPane tabbedPane;

    // Tabele na dane
    private JTable dostawcyTable;
    private JTable odbiorcyTable;
    private JTable kosztyTable;

    // Tabele na wyniki
    private JTable zyskJednostkowyTable;
    private JTable transportTable;

    // Podsumowanie
    private JTextArea summaryArea;

    private JButton loadButton;
    private JButton calculateButton;
    private File currentFile;

    // Dane załadowane
    private List<Dostawca> dostawcy = new ArrayList<>();
    private List<Odbiorca> odbiorcy = new ArrayList<>();
    private ArrayList<ArrayList<Double>> kosztyTransportu = new ArrayList<>();

    // Obiekt obliczeniowy
    private Posrednik posrednik;

    public GUIPosrednikAppSwing() {
        setTitle("Pośrednik - Optymalizacja Transportu");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // Górny panel z przyciskami
        JPanel topPanel = new JPanel();
        loadButton = new JButton("Wczytaj dane CSV");
        calculateButton = new JButton("Oblicz rozwiązanie");
        calculateButton.setEnabled(false);
        topPanel.add(loadButton);
        topPanel.add(calculateButton);

        add(topPanel, BorderLayout.NORTH);

        // Zakładki
        tabbedPane = new JTabbedPane();

        // Zakładka 1: Dane wejściowe
        JPanel inputPanel = new JPanel(new BorderLayout());

        // Tabele dane - na jednym panelu połączone pionowo
        JPanel tablesPanel = new JPanel(new GridLayout(3,1,5,5));
        dostawcyTable = new JTable();
        odbiorcyTable = new JTable();
        kosztyTable = new JTable();

        tablesPanel.add(wrapInScrollPaneWithTitle(dostawcyTable, "Dostawcy (nazwa, koszt, max produkcja)"));
        tablesPanel.add(wrapInScrollPaneWithTitle(odbiorcyTable, "Odbiorcy (nazwa, cena, max popyt)"));
        tablesPanel.add(wrapInScrollPaneWithTitle(kosztyTable, "Macierz kosztów transportu"));

        inputPanel.add(tablesPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Dane wejściowe", inputPanel);

        // Zakładka 2: Wyniki
        JPanel resultsPanel = new JPanel(new GridLayout(2,1,5,5));
        zyskJednostkowyTable = new JTable();
        transportTable = new JTable();

        resultsPanel.add(wrapInScrollPaneWithTitle(zyskJednostkowyTable, "Zyski jednostkowe (cena - koszt zakupu - koszt transportu)"));
        resultsPanel.add(wrapInScrollPaneWithTitle(transportTable, "Macierz transportowa (ilość towaru przesłanego)"));

        tabbedPane.addTab("Wyniki obliczeń", resultsPanel);

        // Zakładka 3: Podsumowanie finansowe
        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        tabbedPane.addTab("Podsumowanie finansowe", new JScrollPane(summaryArea));

        add(tabbedPane, BorderLayout.CENTER);

        // Obsługa przycisków
        loadButton.addActionListener(this::handleLoad);
        calculateButton.addActionListener(this::handleCalculate);
    }

    private JScrollPane wrapInScrollPaneWithTitle(JTable table, String title) {
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(title));
        return scrollPane;
    }

    private void handleLoad(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try {
                loadDataFromFile(currentFile);
                calculateButton.setEnabled(true);
                updateInputTables();
                summaryArea.setText("");
                clearResultTables();
                JOptionPane.showMessageDialog(this, "Dane wczytane poprawnie.\nKliknij 'Oblicz rozwiązanie'.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Błąd wczytywania pliku:\n" + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
                calculateButton.setEnabled(false);
            }
        }
    }

    private void loadDataFromFile(File file) throws IOException {
        dostawcy.clear();
        odbiorcy.clear();
        kosztyTransportu.clear();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        boolean readingSuppliers = false;
        boolean readingConsumers = false;
        boolean readingCosts = false;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#") && !line.startsWith("#DOSTAWCY") && !line.startsWith("#ODBIORCY") && !line.startsWith("#KOSZTY"))
                continue;

            if (line.startsWith("#DOSTAWCY")) {
                readingSuppliers = true;
                readingConsumers = false;
                readingCosts = false;
                continue;
            } else if (line.startsWith("#ODBIORCY")) {
                readingSuppliers = false;
                readingConsumers = true;
                readingCosts = false;
                continue;
            } else if (line.startsWith("#KOSZTY")) {
                readingSuppliers = false;
                readingConsumers = false;
                readingCosts = true;
                continue;
            }

            if (!line.isEmpty()) {
                String[] parts = line.split(";");
                if (readingSuppliers) {
                    if(parts.length != 3) throw new IOException("Nieprawidłowy format dostawców");
                    dostawcy.add(new Dostawca(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
                } else if (readingConsumers) {
                    if(parts.length != 3) throw new IOException("Nieprawidłowy format odbiorców");
                    odbiorcy.add(new Odbiorca(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
                } else if (readingCosts) {
                    ArrayList<Double> row = new ArrayList<>();
                    for (String s : parts) {
                        row.add(Double.parseDouble(s));
                    }
                    kosztyTransportu.add(row);
                }
            }
        }
        reader.close();
    }

    private void updateInputTables() {
        // Dostawcy
        DefaultTableModel dostawcyModel = new DefaultTableModel(new Object[]{"Nazwa", "Koszt zakupu", "Max produkcja"}, 0);
        for (Dostawca d : dostawcy) {
            dostawcyModel.addRow(new Object[]{d.nazwa, d.koszt, d.maxProdukcja});
        }
        dostawcyTable.setModel(dostawcyModel);

        // Odbiorcy
        DefaultTableModel odbiorcyModel = new DefaultTableModel(new Object[]{"Nazwa", "Cena sprzedaży", "Max popyt"}, 0);
        for (Odbiorca o : odbiorcy) {
            odbiorcyModel.addRow(new Object[]{o.nazwa, o.cena, o.maxPopyt});
        }
        odbiorcyTable.setModel(odbiorcyModel);

        // Koszty transportu
        if (kosztyTransportu.size() > 0) {
            int rows = kosztyTransportu.size();
            int cols = kosztyTransportu.get(0).size();

            // Nagłówki kolumn - Odbiorcy lub O1, O2, ...
            String[] colNames = new String[cols + 1];
            colNames[0] = "Dostawca / Odbiorca";
            for (int i = 0; i < cols; i++) {
                if (i < odbiorcy.size()) {
                    colNames[i + 1] = odbiorcy.get(i).nazwa;
                } else {
                    colNames[i + 1] = "O_FIKCYJNY_" + (i + 1);
                }
            }

            DefaultTableModel kosztyModel = new DefaultTableModel(colNames, 0);
            for (int i = 0; i < rows; i++) {
                Object[] row = new Object[cols + 1];
                if (i < dostawcy.size()) {
                    row[0] = dostawcy.get(i).nazwa;
                } else {
                    row[0] = "D_FIKCYJNY_" + (i + 1);
                }
                for (int j = 0; j < cols; j++) {
                    row[j + 1] = kosztyTransportu.get(i).get(j);
                }
                kosztyModel.addRow(row);
            }
            kosztyTable.setModel(kosztyModel);
        }
    }

    private void clearResultTables() {
        zyskJednostkowyTable.setModel(new DefaultTableModel());
        transportTable.setModel(new DefaultTableModel());
        summaryArea.setText("");
    }

    private void handleCalculate(ActionEvent e) {
        try {
            posrednik = new Posrednik(dostawcy, odbiorcy, kosztyTransportu);
            posrednik.calculate();

            updateResultTables();
            updateSummary();

            JOptionPane.showMessageDialog(this, "Obliczenia zakończone pomyślnie.");
            tabbedPane.setSelectedIndex(1);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Błąd podczas obliczeń:\n" + ex.getMessage(), "Błąd", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void updateResultTables() {
        // Zysk jednostkowy
        ArrayList<ArrayList<Double>> zysk = posrednik.zyskJednostokowy;
        int rows = zysk.size();
        int cols = zysk.get(0).size();

        // Nagłówki kolumn - odbiorcy
        String[] colNames = new String[cols + 1];
        colNames[0] = "Dostawca / Odbiorca";
        for (int j = 0; j < cols; j++) {
            if (j < odbiorcy.size()) {
                colNames[j + 1] = odbiorcy.get(j).nazwa;
            } else {
                colNames[j + 1] = "O_FIKCYJNY_" + (j + 1);
            }
        }

        DefaultTableModel zyskModel = new DefaultTableModel(colNames, 0);
        for (int i = 0; i < rows; i++) {
            Object[] row = new Object[cols + 1];
            if (i < dostawcy.size()) {
                row[0] = dostawcy.get(i).nazwa;
            } else {
                row[0] = "D_FIKCYJNY_" + (i + 1);
            }
            for (int j = 0; j < cols; j++) {
                row[j + 1] = String.format("%.2f", zysk.get(i).get(j));
            }
            zyskModel.addRow(row);
        }
        zyskJednostkowyTable.setModel(zyskModel);

        // Macierz transportowa - ilosci towaru przesłanego
        ArrayList<ArrayList<Double>> transport = posrednik.getMacierzTransportowa();
        int tRows = transport.size();
        int tCols = transport.get(0).size();

        String[] tColNames = new String[tCols + 1];
        tColNames[0] = "Dostawca / Odbiorca";
        for (int j = 0; j < tCols; j++) {
            if (j < odbiorcy.size()) {
                tColNames[j + 1] = odbiorcy.get(j).nazwa;
            } else {
                tColNames[j + 1] = "O_FIKCYJNY_" + (j + 1);
            }
        }

        DefaultTableModel transportModel = new DefaultTableModel(tColNames, 0);
        for (int i = 0; i < tRows; i++) {
            Object[] row = new Object[tCols + 1];
            if (i < dostawcy.size()) {
                row[0] = dostawcy.get(i).nazwa;
            } else {
                row[0] = "D_FIKCYJNY_" + (i + 1);
            }
            for (int j = 0; j < tCols; j++) {
                row[j + 1] = String.format("%.2f", transport.get(i).get(j));
            }
            transportModel.addRow(row);
        }
        transportTable.setModel(transportModel);
    }

    private void updateSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("Podsumowanie finansowe:\n");
        sb.append("----------------------\n");
        sb.append(String.format("Przychód całkowity: %.2f\n", posrednik.getPrzychodCalkowity()));
        sb.append(String.format("Koszt całkowity zakupu: %.2f\n", posrednik.getKosztCalkowityZakupu()));
        sb.append(String.format("Koszt całkowity transportu: %.2f\n", posrednik.getKosztCalkowityTransportu()));
        sb.append(String.format("Zysk całkowity: %.2f\n", posrednik.getZyskCalkowity()));

        summaryArea.setText(sb.toString());
    }

    // Main do uruchomienia
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUIPosrednikAppSwing app = new GUIPosrednikAppSwing();
            app.setVisible(true);
        });
    }

}
