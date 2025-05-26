package org.example.Posrednik;

import java.util.ArrayList;
import java.util.List;

public class Posrednik {
    private List<Dostawca> dostawcaList;
    private List<Odbiorca> odbiorcaList;
    // indeks - i - dostawca
    // indeks - j - odbiorca
    private ArrayList<ArrayList<Double>> macierzKosztowTransportu;
    public ArrayList<ArrayList<Double>> macierzWielkosciTransportu;
    public ArrayList<ArrayList<Double>> zyskJednostokowy = new ArrayList<>();
    private List<Double> pozostalyPopyt = new ArrayList<>();
    private List<Double> pozostalaPodaz = new ArrayList<>();
    //persistent data for base solution
    List<int[]> wykluczoneId = new ArrayList<>();

    public Posrednik(List<Dostawca> dostawcas, List<Odbiorca> odbiorcas, ArrayList<ArrayList<Double>> kosztyTransportu) {
        this.dostawcaList = dostawcas;
        this.odbiorcaList = odbiorcas;
        this.macierzKosztowTransportu = kosztyTransportu;
        //inicjalizacja macierzy Wielkosci transportu
        this.macierzWielkosciTransportu = new ArrayList<>();
        for(Dostawca dostawca : dostawcas){
            ArrayList<Double> nrow = new ArrayList<>();
            for(Odbiorca odbiorca : odbiorcas){
                nrow.add(0.0);
            }
            macierzWielkosciTransportu.add(nrow);
        }
    }

    public ArrayList<ArrayList<Double>> getMacierzTransportowa() {
        return macierzWielkosciTransportu;
    }

    public double getPrzychodCalkowity() {
        double suma = 0.0;
        for (int i = 0; i < macierzWielkosciTransportu.size(); i++) {
            int limit = Math.min(macierzWielkosciTransportu.get(i).size(), odbiorcaList.size());
            for (int j = 0; j < limit; j++) {
                double ilosc = macierzWielkosciTransportu.get(i).get(j);
                double cena = odbiorcaList.get(j).cena;
                suma += ilosc * cena;
            }
        }
        return suma;
    }


    public double getKosztCalkowityZakupu() {
        double koszt = 0.0;
        int limit = Math.min(macierzWielkosciTransportu.size(), dostawcaList.size());
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < macierzWielkosciTransportu.get(i).size(); j++) {
                double ilosc = macierzWielkosciTransportu.get(i).get(j);
                double kosztDostawcy = dostawcaList.get(i).koszt;
                koszt += ilosc * kosztDostawcy;
            }
        }
        return koszt;
    }


    public double getKosztCalkowityTransportu() {
        double suma = 0.0;
        int limit = Math.min(macierzWielkosciTransportu.size(), dostawcaList.size());
        for (int i = 0; i < limit; i++) {
            for (int j = 0; j < macierzWielkosciTransportu.get(i).size(); j++) {
                double ilosc = macierzWielkosciTransportu.get(i).get(j);
                double kosztTransportu = macierzKosztowTransportu.get(i).get(j);
                suma += ilosc * kosztTransportu;
            }
        }
        return suma;
    }

    public double getZyskCalkowity() {
        // zysk = przychód - koszt zakupu - koszt transportu
        return getPrzychodCalkowity() - getKosztCalkowityZakupu() - getKosztCalkowityTransportu();
    }



    public void obliczZyskJednostkowy(){
        int iloscDostawcow = macierzWielkosciTransportu.size();
        int iloscOdbiorcow = macierzWielkosciTransportu.get(iloscDostawcow-1).size();
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
    public void powiekszWymiaryTablicy(ArrayList<ArrayList<Double>> tab){
        //zera na kocow rzedow
        for(int i = 0; i < tab.size(); i++){
            tab.get(i).add(0.0);
        }
        //nowy rzad z zerami
        ArrayList<Double> nrow = new ArrayList<>();
        for(int i = 0; i < tab.get(0).size(); i++){
            nrow.add(0.0);
        }
        tab.add(nrow);
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

            powiekszWymiaryTablicy(macierzWielkosciTransportu);
            powiekszWymiaryTablicy(macierzKosztowTransportu);
            powiekszWymiaryTablicy(zyskJednostokowy);
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

        boolean foundid = false;
        while(!foundid){
            double maxvalue = Double.NEGATIVE_INFINITY;
            //iteracja po tablicy
            for(int i = 0; i < zyskJednostokowy.size(); i++){
                for(int j = 0; j < zyskJednostokowy.get(i).size(); j++){
                    //sprawdzenie czy indeksy legalne
                    boolean legal = true;
                    for(int k = 0; k < wykluczoneId.size(); k++){
                        if(i == wykluczoneId.get(k)[0] && j == wykluczoneId.get(k)[1]) legal = false;
                    }
                    //sprdzenie czy warotsc najbardziej zyskowna
                    if(legal && maxvalue <= zyskJednostokowy.get(i).get(j)){
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
    public void bazoweRozwiazanie(){
        double sumaPopytu = 0;
        double sumaPodazy = 0;
        for(Double podaz : pozostalaPodaz) sumaPodazy += podaz;
        for(Double popyt : pozostalyPopyt) sumaPopytu += popyt;

        if (sumaPopytu != sumaPodazy) {
            throw new RuntimeException("Popyt != Podaz, a po dodaniu fikcyjnych ziutków powinno, ergo kod do poprawy");
        }

        int[] idContainer = new int[2];
        while(sumaPopytu > 0 && sumaPodazy > 0){
            idContainer = znajdzIndeksMaxZysku();
            int idDostawca = idContainer[0];
            int idOdbiorca = idContainer[1];

            //znajdz obietosc
            Double transportLowerbound = pozostalaPodaz.get(idDostawca) - pozostalyPopyt.get(idOdbiorca);
            if(transportLowerbound < 0) transportLowerbound = pozostalaPodaz.get(idDostawca);
            else transportLowerbound = pozostalyPopyt.get(idOdbiorca);

            //update data
            macierzWielkosciTransportu.get(idDostawca).set(idOdbiorca, transportLowerbound);
            pozostalaPodaz.set(idDostawca, pozostalaPodaz.get(idDostawca) - transportLowerbound);
            pozostalyPopyt.set(idOdbiorca, pozostalyPopyt.get(idOdbiorca) - transportLowerbound);

            sumaPopytu -= transportLowerbound;
            sumaPodazy -= transportLowerbound;
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
    public void rozwiazUkladRownan(double[] alpha, double[] beta, int m, int n){
        ArrayList<Z> z = new ArrayList<>();
        for(int i = 0; i < m; i++){
            for(int j = 0; j < n; j++){
                if(macierzWielkosciTransportu.get(i).get(j) > 0){
                    z.add(new Z(zyskJednostokowy.get(i).get(j), i, j));
                }
            }
        }

        boolean[] knownAlpha = new boolean[m];
        boolean[] knownBeta = new boolean[n];
        knownAlpha[m-1] = true;
        knownBeta[n-1] = true;

        for(int i = z.size() - 1; i >= 0; i--){
            int alphaid = z.get(i).i;
            int betaid = z.get(i).j;
            double zval = z.get(i).val;
            if (knownAlpha[alphaid]){
                beta[betaid] = zval - alpha[alphaid];
                knownBeta[betaid] = true;
            }
            else if (knownBeta[betaid]){
                alpha[alphaid] = zval - beta[betaid];
                knownAlpha[alphaid] = true;
            }
            else{
                throw new RuntimeException("W trakcie liczenia alpha i beta cos sie zjebalo, trzeba szukac innego sposobu pewie");
            }
        }
    }
    private boolean obliczDelte(ArrayList<Z> delta,double[] alpha, double[] beta, int m, int n) {
        ArrayList<Z> z = new ArrayList<>();
        for(int i = 0; i < m; i++){
            for(int j = 0; j < n; j++){
                if(macierzWielkosciTransportu.get(i).get(j) == 0){
                    z.add(new Z(zyskJednostokowy.get(i).get(j), i, j));
                }
            }
        }
        boolean allneg = true;
        for(int i = 0; i < z.size(); i++){
            int idi = z.get(i).i;
            int idj = z.get(i).j;
            double zval = z.get(i).val;
            double deltaval = zval - alpha[idi] - beta[idj];
            if(deltaval > 0) allneg = false;
            delta.add(new Z(deltaval, idi, idj));
        }
        return allneg;
    }
    public void optymalizacja(){
        int i = zyskJednostokowy.size();
        int j = zyskJednostokowy.get(i-1).size();
        double[] alpha = new double[i];
        double[] beta = new double[j];
        alpha[i-1] = 0;
        beta[j-1] = 0;
        boolean warunekStopu = true;
        while(warunekStopu){
            rozwiazUkladRownan(alpha, beta, i, j);
            ArrayList<Z> deltas = new ArrayList<>();
            warunekStopu = obliczDelte(deltas, alpha, beta, i, j);
            //pętla zmian
            if(!warunekStopu){
                for(int k = 0; k < deltas.size(); k++){
                    if(deltas.get(k).val > 0){
                        // pierwszy plus to delta[k]
                        int plus1i = deltas.get(k).i;
                        int plus1j = deltas.get(k).j;
                        // znajdz minusy
                        ArrayList<Z> minus = new ArrayList<>();
                        //gora check
                        if(plus1i - 1 > 0){
                            if(macierzWielkosciTransportu.get(plus1i - 1).get(plus1j) > 0){
                                minus.add(new Z(macierzWielkosciTransportu.get(plus1i - 1).get(plus1j),plus1i - 1, plus1j));
                            }
                        }
                        //lewo czeck
                        if(plus1j - 1 > 0){
                            if(macierzWielkosciTransportu.get(plus1i).get(plus1j - 1) > 0){
                                minus.add(new Z(macierzWielkosciTransportu.get(plus1i).get(plus1j - 1),plus1i, plus1j-1));
                            }
                        }
                        //doł czek
                        if(plus1i + 1 < i){
                            if(macierzWielkosciTransportu.get(plus1i + 1).get(plus1j) > 0){
                                minus.add(new Z(macierzWielkosciTransportu.get(plus1i + 1).get(plus1j),plus1i + 1, plus1j));
                            }
                        }
                        //prawochek
                        if(plus1j + 1 < j){
                            if(macierzWielkosciTransportu.get(plus1i).get(plus1j + 1) > 0){
                                minus.add(new Z(macierzWielkosciTransportu.get(plus1i).get(plus1j + 1),plus1i, plus1j+1));
                            }
                        }
                        //znajdz min value podmianki
                        double minval = minus.get(0).val;
                        if(minval > minus.get(1).val) minval = minus.get(1).val;
                        //podmien
                        macierzWielkosciTransportu.get(plus1i).set(plus1j, macierzWielkosciTransportu.get(plus1i).get(plus1j) + minval);
                        int min1i = minus.get(0).i;
                        int min1j = minus.get(0).j;
                        macierzWielkosciTransportu.get(min1i).set(min1j, macierzWielkosciTransportu.get(min1i).get(min1j) - minval);
                        int min2i = minus.get(1).i;
                        int min2j = minus.get(1).j;
                        macierzWielkosciTransportu.get(min2i).set(min2j, macierzWielkosciTransportu.get(min2i).get(min2j) - minval);
                        int plus2i, plus2j;
                        if(plus1i == min1i) plus2j = min1j;
                        else plus2j = min2j;
                        if(plus1j == min1j) plus2i = min1i;
                        else plus2i = min2i;
                        macierzWielkosciTransportu.get(plus2i).set(plus2j, macierzWielkosciTransportu.get(plus2i).get(plus2j) + minval);
                        break;
                    }
                }
            }
        }

    }

    public void calculate(){
        obliczZyskJednostkowy();
        incjalizacjaPopytuPodazy();
        dostawcyOdbiorcyFikcyjni();
        bazoweRozwiazanie();
        optymalizacja();
    }

    public void printTransport(){
        System.out.println("]===Objetosc transportu===[");
        for (int i = 0; i < macierzWielkosciTransportu.size(); i++) {
            for (int j = 0; j < macierzWielkosciTransportu.get(i).size(); j++) {
                System.out.print(String.format("%10.2f", macierzWielkosciTransportu.get(i).get(j)));
            }
            System.out.println();
        }
        System.out.println("===========================");
    }
    public void printSummary(){
        System.out.println("++++++SUMMARY++++++");
        System.out.println("----> Lista dostawcow");
        for(Dostawca d : dostawcaList) System.out.println(d);
        System.out.println("----> Lista odbiorcow");
        for(Odbiorca o : odbiorcaList) System.out.println(o);
        System.out.println("+++++++++++++++++++");
    }

}
