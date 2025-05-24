package org.example.Posrednik;

public class Odbiorca {
    String nazwa;
    double cena;
    double maxPopyt;

    public Odbiorca(String nazwa, double cena, double maxPopyt) {
        this.nazwa = nazwa;
        this.cena = cena;
        this.maxPopyt = maxPopyt;
    }

    @Override
    public String toString() {
        return "Odbiorca{" +
                "nazwa='" + nazwa + '\'' +
                ", cena=" + cena +
                ", maxPopyt=" + maxPopyt +
                '}';
    }
}
