package org.example;

import java.util.List;

public class Node {
    //Pola Realizujące strukture danych
    private List<Node> nextNodes;
    private List<Node> previousNodes;
    private int frontPropagateCounter;
    private int backPropagateCounter;

    private String name;
    private double workTime;

    private double earlyStartTime;
    private double earlyEndTime;
    private double lateStartTime;
    private double lateEndTime;
    private double reserveTime;
    private boolean criticalPath;

    Node(){
        earlyStartTime = earlyEndTime = lateStartTime = lateEndTime = 0;
        reserveTime = 0;
    }

    public double getEarlyEndTime() {
        return earlyEndTime;
    }

    public double getEarlyStartTime() {
        return earlyStartTime;
    }

    public double getLateStartTime() {
        return lateStartTime;
    }

    public double getLateEndTime() {
        return lateEndTime;
    }

    public void frontPropagate(Node previousNode) {
        //obliczenia zadawane przez poprzedników

        //znajdz early start
        if(previousNode.getEarlyEndTime() > earlyEndTime) {
            earlyStartTime = previousNode.getEarlyEndTime();
        }

        //kiedy otrzyma wszystkie informacje od wszystkich poprzedników
        frontPropagateCounter++;
        if(frontPropagateCounter == nextNodes.size()) {
            frontPropagateCounter = 0;
            //ustal czas wczesnego końca
            earlyEndTime = earlyStartTime + workTime;

            //wyslij zapytanie w przód
            for(Node n : nextNodes) {
                n.frontPropagate(this);
            }
        }

    }
    public void backPropagate(Node nextNode) {
        //obliczenia zadawane przez następników

        //znajdz late finish
        if(nextNode.getLateStartTime() < lateEndTime) {
            lateEndTime = nextNode.getLateStartTime();
        }

        //kiedy otrzyma wszystkie informacje od wszystkich następników
        backPropagateCounter++;
        if(backPropagateCounter == previousNodes.size()) {
            backPropagateCounter = 0;
            //ustal czas pozniego poczaktu
            lateStartTime = lateEndTime - workTime;
            //policz zapas
            reserveTime = lateEndTime - earlyStartTime;
            //scieszka krytyczna??
            if(reserveTime == 0) {
                criticalPath = true;
            }
            //wyślij sygnał w tył
            for(Node n : previousNodes) {
                n.backPropagate(this);
            }
        }
    }
}
