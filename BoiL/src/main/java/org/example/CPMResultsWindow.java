package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class CPMResultsWindow extends JFrame {
    private JPanel contentPane;
    private JTable resultsTable;

    public CPMResultsWindow(List<Node> nodes) {
        setTitle("CPM Results");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        contentPane = new JPanel(new BorderLayout());
        setContentPane(contentPane);

        // Table model
        String[] columnNames = {"Node","Previous Nodes", "Next nodes", "Early Start", "Early End", "Late Start", "Late End", "Reserve Time", "Critical"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        // Populate table with node data
        for (Node node : nodes) {
            String previousNodes = String.join(", ", node.previousNodes.stream().map(Node::getName).toArray(String[]::new));
            if(node.previousNodes.size() == 0){
                previousNodes = "None";
            }
            String nextNodes = String.join(", ", node.nextNodes.stream().map(Node::getName).toArray(String[]::new));
            if(node.nextNodes.size() == 0){
                nextNodes = "None";
            }
            Object[] rowData = {
                    node.getName(),
                    previousNodes,
                    nextNodes,
                    node.getEarlyStartTime(),
                    node.getEarlyEndTime(),
                    node.getLateStartTime(),
                    node.getLateEndTime(),
                    node.getReserveTime(),
                    node.isCriticalPath() ? "Yes" : "No"
            };
            tableModel.addRow(rowData);
        }

        // Create table
        resultsTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(resultsTable);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }
}