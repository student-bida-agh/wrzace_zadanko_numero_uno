package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//built with ai, but checked line by line and fixed so it doesn't poop itself (if you just went "waaaah but ai bad" then write it yourself. its not rocket science, its just boring

public class NodeManager {
    // List of all nodes in the graph (unsorted)
    List<Node> nodes;
    private Map<String, Node> nodeMap; // For quick lookup during connection

    public NodeManager() {
        nodes = new ArrayList<>();
        nodeMap = new HashMap<>();
    }

    public void loadNodes(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Split the line by semicolon
                String[] parts = line.split(";");

                if (parts.length >= 3) {
                    String name = parts[0].trim();
                    double workTime = Double.parseDouble(parts[1].trim());

                    // Create new node
                    Node node = new Node();
                    node.setName(name);
                    node.setWorkTime(workTime);

                    // Store the node
                    nodes.add(node);
                    nodeMap.put(name, node);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number: " + e.getMessage());
        }
    }

    public void connectNodes(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");

                if (parts.length >= 3) {
                    String nodeName = parts[0].trim();
                    String[] neighborNames = parts[2].split(",");

                    Node currentNode = nodeMap.get(nodeName);
                    if (currentNode != null) {
                        // Initialize lists if null
                        if (currentNode.nextNodes == null) {
                            currentNode.nextNodes = new ArrayList<>();
                        }
                        if (currentNode.previousNodes == null) {
                            currentNode.previousNodes = new ArrayList<>();
                        }

                        // Connect nodes
                        for (String neighborName : neighborNames) {
                            neighborName = neighborName.trim();
                            Node neighborNode = nodeMap.get(neighborName);
                            if (neighborNode != null) {
                                currentNode.nextNodes.add(neighborNode);
                                // Also set this node as previous for the neighbor
                                if (neighborNode.previousNodes == null) {
                                    neighborNode.previousNodes = new ArrayList<>();
                                }
                                neighborNode.previousNodes.add(currentNode);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public void calculate(){
        Node startNode = getNodeByName("Start");
        //Przydałoby się zrobić pole start node w managarze i przypisywać je w trakcie łączenia
        for(Node node : startNode.nextNodes){
            node.frontPropagate(startNode);
        }
    }



    // Helper method to get a node by name
    public Node getNodeByName(String name) {
        return nodeMap.get(name);
    }

    // Get all nodes
    public List<Node> getNodes() {
        return nodes;
    }
}