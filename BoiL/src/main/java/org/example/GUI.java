package org.example;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GUI extends JFrame{
    private JPanel contentPane;
    private JButton chooseFile;
    private JTextField displaySelected;
    private JButton runbutton;
    NodeManager nodeManager;
    String selectedFilePath;

    GUI() {
        setContentPane(contentPane);
        setTitle("Project Management Tool");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
        runbutton.setEnabled(false);
        nodeManager = new NodeManager();
        chooseFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showOpenDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    selectedFilePath = fileChooser.getSelectedFile().getAbsolutePath();
                    displaySelected.setText(selectedFilePath);
                    runbutton.setEnabled(true);
                }
            }
        });
        runbutton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                nodeManager.loadNodes(selectedFilePath);
                nodeManager.connectNodes(selectedFilePath);

                List<Node> allNodes = nodeManager.getNodes();
                Node specificNode = nodeManager.getNodeByName("A");
                specificNode.frontPropagate(null);
                for(Node node : allNodes) {
                    System.out.println("Early start time: " + node.getEarlyStartTime());
                    System.out.println("Early end time: " + node.getEarlyEndTime());
                }
            }
        });
    }
}
