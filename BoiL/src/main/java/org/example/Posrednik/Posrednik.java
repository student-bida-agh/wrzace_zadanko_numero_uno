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
    private List<Double> pozostalyPopyt = new ArrayList<>();
    private List<Double> pozostalaPodaz = new ArrayList<>();

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
    public void dostawcyOdbiorcyFikcyjni(){
        double sumaPopytu = 0;
        double sumaPodazy = 0;
        for(Dostawca d : dostawcaList){
            sumaPodazy += d.maxProdukcja;
        }
        for(Odbiorca o : odbiorcaList){
            sumaPopytu += o.maxPopyt;
        }
        if(sumaPopytu != sumaPodazy){
            //dodaj fikcyjny podaz/popyt
            pozostalaPodaz.add(sumaPopytu);
            pozostalyPopyt.add(sumaPodazy);
            //macierz zyskow jednostkowych
                //zera na kocow rzedow
            for(int i = 0; i < zyskJednostokowy.size(); i++){
                zyskJednostokowy.get(i).add(0.0);
            }
                //nowy rzad z zerami
            ArrayList<Double> nrow = new ArrayList<>();
            for(int i = 0; i < zyskJednostokowy.size() + 1; i++){
                nrow.add(0.0);
            }
            zyskJednostokowy.add(nrow);
        }
    }
    public void incjalizacjaPopytuPodazy(){
        for(Odbiorca o : odbiorcaList){
            pozostalyPopyt.add(o.maxPopyt);
        }
        for(Dostawca d : dostawcaList){
            pozostalaPodaz.add(d.maxProdukcja);
        }
    }
    //[0] - index i, [1] - index j
    public int[] znajdzIndeksMaxZysku(){
        int[] ids = new int[2];
        List<int[]> wykluczoneId = new ArrayList<>();

        boolean foundid = false;
        while(!foundid){
            double maxvalue = 0;
            //iteracja po tablicy
            for(int i = 0; i < zyskJednostokowy.size(); i++){
                for(int j = 0; j < zyskJednostokowy.get(i).size(); j++){
                    //sprawdzenie czy indeksy legalne
                    boolean legal = true;
                    for(int k = 0; k < wykluczoneId.size(); k++){
                        if(i == wykluczoneId.get(k)[0] && j == wykluczoneId.get(k)[1]) legal = false;
                    }
                    //sprdzenie czy warotsc najbardziej zyskowna
                    if(legal && maxvalue < zyskJednostokowy.get(i).get(j)){
                        maxvalue = zyskJednostokowy.get(i).get(j);
                        ids[0] = i;
                        ids[1] = j;
                    }
                }
            }
            //spradzenie czy maksymalna wartosc ma wolna popyt i podaz
            if(pozostalaPodaz.get(ids[0]) > 0 && pozostalyPopyt.get(ids[1]) > 0) foundid = true;
            else {
                wykluczoneId.add(new int[]{ids[0], ids[1]});
            }
        }


        return ids;
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
    public void calculate(){
        obliczZyskJednostkowy();
        incjalizacjaPopytuPodazy();
        dostawcyOdbiorcyFikcyjni();
    }

}
