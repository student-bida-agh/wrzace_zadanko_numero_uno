package org.example.Posrednik;

import java.util.ArrayList;
import java.util.List;

public class MainPosrednik {
    public static void main(String[] args) {
        //mock data
        List<Dostawca> dostawcy = new ArrayList<>();
        dostawcy.add(new Dostawca("D1", 10.0, 20.0));
        dostawcy.add(new Dostawca("D2", 12, 30.0));
        List<Odbiorca> odbiorcy = new ArrayList<>();
        odbiorcy.add(new Odbiorca("O1", 30.0, 10.0));
        odbiorcy.add(new Odbiorca("O2", 25.0, 28.0));
        odbiorcy.add(new Odbiorca("O3", 30.0, 27.0));
        double[][] transportArray = {
                {8.0, 14.0, 17.0},
                {12.0, 9.0, 19.0}
        };
        ArrayList<ArrayList<Double>> transportkoszt = new ArrayList<>();
        for (double[] row : transportArray) {
            ArrayList<Double> costRow = new ArrayList<>();
            for (double cost : row) {
                costRow.add(cost);
            }
            transportkoszt.add(costRow);
        }
        Posrednik test = new Posrednik(dostawcy, odbiorcy, transportkoszt);
        test.printSummary();
        test.calculate();
        test.printTransport();
        System.out.println("dochod = " + test.funkcjaCelu());

    }
}