package org.example;

import java.util.List;

public class Node {
    //Pola Realizujące strukture danych
    private List<Node> nextNodes;
    private List<Node> previousNodes;
    private int frontPropagateCounter;
    private int backPropagateCounter;


    void frontPropagate() {
        //obliczenia zadawane przez poprzedników

        //TODO

        //kiedy otrzyma wszystkie informacje od poprzedników wyślij polecenie w przód
        frontPropagateCounter++;
        for(Node n : nextNodes) {
            n.frontPropagate();
        }
    }
    void backPropagate() {
        //obliczenia zadawane przez następników

        //TODO

        //kiedy otrzyma wszystkie informacje od następników wyślij polecenie w tył
        backPropagateCounter++;
        for(Node n : previousNodes) {
            n.backPropagate();
        }
    }
}
