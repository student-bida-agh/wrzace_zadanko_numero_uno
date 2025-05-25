package org.example.Posrednik;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class GUIPosrednikAppSwing extends JFrame {
    private JTextArea resultArea;
    private JButton loadButton;
    private JButton calculateButton;
    private File currentFile;

    public GUIPosrednikAppSwing() {
        setTitle("Pośrednik - Optymalizacja Transportu");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        loadButton = new JButton("Wczytaj dane CSV");
        calculateButton = new JButton("Oblicz rozwiązanie");
        calculateButton.setEnabled(false);

        JPanel panel = new JPanel();
        panel.add(loadButton);
        panel.add(calculateButton);

        add(panel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // === Obsługa przycisków ===
        loadButton.addActionListener(this::handleLoad);
        calculateButton.addActionListener(this::handleCalculate);
    }

    private void handleLoad(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            calculateButton.setEnabled(true);
            resultArea.setText("Wczytano plik: " + currentFile.getName() + "\nKliknij 'Oblicz rozwiązanie'.");
        }
    }

    private void handleCalculate(ActionEvent e) {
        try {
            List<Dostawca> dostawcy = new ArrayList<>();
            List<Odbiorca> odbiorcy = new ArrayList<>();
            ArrayList<ArrayList<Double>> transport = new ArrayList<>();

            parseCSV(currentFile, dostawcy, odbiorcy, transport);

            Posrednik posrednik = new Posrednik(dostawcy, odbiorcy, transport);
            posrednik.calculate();

            StringBuilder sb = new StringBuilder();
            sb.append("Obliczenia zakończone.\n");
            sb.append("Dochod: ").append(String.format("%.2f", posrednik.funkcjaCelu())).append("\n\n");

            sb.append("Macierz transportu:\n");

            // Nagłówki kolumn (odbiorcy)
            sb.append(String.format("%15s", ""));
            for (int j = 0; j < posrednik.macierzWielkosciTransportu.get(0).size(); j++) {
                if (j < odbiorcy.size()) {
                    sb.append(String.format("%12s", odbiorcy.get(j).nazwa));
                } else {
                    sb.append(String.format("%12s", "O_FIKCYJNY"));
                }
            }
            sb.append("\n");

            // Wiersze (dostawcy + dane)
            for (int i = 0; i < posrednik.macierzWielkosciTransportu.size(); i++) {
                if (i < dostawcy.size()) {
                    sb.append(String.format("%15s", dostawcy.get(i).nazwa));
                } else {
                    sb.append(String.format("%15s", "D_FIKCYJNY"));
                }

                for (Double val : posrednik.macierzWielkosciTransportu.get(i)) {
                    sb.append(String.format("%12.2f", val));
                }
                sb.append("\n");
            }

            resultArea.setText(sb.toString());

        } catch (Exception ex) {
            resultArea.setText("Błąd: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private void parseCSV(File file, List<Dostawca> dostawcy, List<Odbiorca> odbiorcy, ArrayList<ArrayList<Double>> transport) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        boolean readingSuppliers = false;
        boolean readingConsumers = false;
        boolean readingCosts = false;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#DOSTAWCY")) {
                readingSuppliers = true;
                readingConsumers = false;
                readingCosts = false;
            } else if (line.startsWith("#ODBIORCY")) {
                readingSuppliers = false;
                readingConsumers = true;
                readingCosts = false;
            } else if (line.startsWith("#KOSZTY")) {
                readingSuppliers = false;
                readingConsumers = false;
                readingCosts = true;
            } else if (!line.trim().isEmpty()) {
                String[] parts = line.split(";");
                if (readingSuppliers) {
                    dostawcy.add(new Dostawca(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
                } else if (readingConsumers) {
                    odbiorcy.add(new Odbiorca(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
                } else if (readingCosts) {
                    ArrayList<Double> row = new ArrayList<>();
                    for (String s : parts) {
                        row.add(Double.parseDouble(s));
                    }
                    transport.add(row);
                }
            }
        }

        reader.close();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUIPosrednikAppSwing app = new GUIPosrednikAppSwing();
            app.setVisible(true);
        });
    }
}
