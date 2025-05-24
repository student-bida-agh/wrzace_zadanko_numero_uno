package org.example.Posrednik;

import java.util.ArrayList;
import java.util.List;

public class Posrednik {
    private List<Dostawca> dostawcaList;
    private List<Odbiorca> odbiorcaList;
    // indeks - i - dostawca
    // indeks - j - odbiorca
    private ArrayList<ArrayList<Double>> macierzKosztowTransportu;
    private ArrayList<ArrayList<Double>> macierzWielkosciTransportu;
    private ArrayList<ArrayList<Double>> zyskJednostokowy;

    public Posrednik(List<Dostawca> dostawcas, List<Odbiorca> odbiorcas, ArrayList<ArrayList<Double>> kosztyTransportu) {
        this.dostawcaList = dostawcas;
        this.odbiorcaList = odbiorcas;
        this.macierzKosztowTransportu = kosztyTransportu;
    }
    public void obliczZyskJednostkowy(){
        int iloscDostawcow = macierzWielkosciTransportu.size();
        int iloscOdbiorcow = macierzWielkosciTransportu.get(iloscDostawcow).size();
        double[][] zyskJednostokowyArray = new double[iloscDostawcow][iloscOdbiorcow];
        //wypełnij Array
        for(int i = 0; i < macierzWielkosciTransportu.size(); i++){
            for(int j = 0; j < macierzKosztowTransportu.get(i).size(); j++){
                zyskJednostokowyArray[i][j] = odbiorcaList.get(j).cena -
                        macierzKosztowTransportu.get(i).get(j) -
                        dostawcaList.get(i).koszt;
            }
        }
        //Zrównaj z tablicą dynamiczną
        for (double[] row : zyskJednostokowyArray) {
            ArrayList<Double> nrow = new ArrayList<>();
            for (double element : row) {
                nrow.add(element);
            }
            zyskJednostokowy.add(nrow);
        }
    }
    public double funkcjaCelu(){
        double dochod = 0;
        for(int i = 0; i < macierzWielkosciTransportu.size(); i++){
            for(int j = 0; j < macierzKosztowTransportu.get(i).size(); j++){
                dochod += zyskJednostokowy.get(i).get(j) * macierzWielkosciTransportu.get(i).get(j);
            }
        }
        return dochod;
    }


}
