package org.example.Posrednik;

import java.io.*;
import java.util.*;

public class CSVLoader {

    public static class InputData {
        public List<Dostawca> dostawcy;
        public List<Odbiorca> odbiorcy;
        public ArrayList<ArrayList<Double>> kosztyTransportu;
    }

    public static InputData loadFromFile(File file) throws IOException {
        List<Dostawca> dostawcy = new ArrayList<>();
        List<Odbiorca> odbiorcy = new ArrayList<>();
        ArrayList<ArrayList<Double>> kosztyTransportu = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        int section = 0;

        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#")) continue;

            if (line.startsWith("D")) {
                section = 1;
            } else if (line.startsWith("O")) {
                section = 2;
            } else if (Character.isDigit(line.charAt(0))) {
                section = 3;
            }

            switch (section) {
                case 1 -> {
                    String[] parts = line.split(",");
                    dostawcy.add(new Dostawca(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
                }
                case 2 -> {
                    String[] parts = line.split(",");
                    odbiorcy.add(new Odbiorca(parts[0], Double.parseDouble(parts[1]), Double.parseDouble(parts[2])));
                }
                case 3 -> {
                    String[] parts = line.split(",");
                    ArrayList<Double> row = new ArrayList<>();
                    for (String p : parts) row.add(Double.parseDouble(p));
                    kosztyTransportu.add(row);
                }
            }
        }

        InputData data = new InputData();
        data.dostawcy = dostawcy;
        data.odbiorcy = odbiorcy;
        data.kosztyTransportu = kosztyTransportu;
        return data;
    }
}
