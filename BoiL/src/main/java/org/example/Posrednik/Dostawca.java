package org.example.Posrednik;

public class Dostawca {
    String nazwa;
    double koszt;
    double maxProdukcja;

    public Dostawca(String nazwa, double koszt, double maxProdukcja) {
        this.nazwa = nazwa;
        this.koszt = koszt;
        this.maxProdukcja = maxProdukcja;
    }

    @Override
    public String toString() {
        return "Dostawca{" +
                "nazwa='" + nazwa + '\'' +
                ", koszt=" + koszt +
                ", maxProdukcja=" + maxProdukcja +
                '}';
    }
}
