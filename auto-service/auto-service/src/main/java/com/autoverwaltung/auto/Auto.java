package com.autoverwaltung.auto;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;

@Entity
public class Auto extends PanacheEntity {
    public String marke;
    public String modell;
    public int baujahr;
    public String kennzeichen;
    public String farbe;
    public boolean validated = false;
    
    // Default-Konstruktor für JPA
    public Auto() {}
    
    // Konstruktor für einfacheres Testen
    public Auto(String marke, String modell, int baujahr, String kennzeichen, String farbe) {
        this.marke = marke;
        this.modell = modell;
        this.baujahr = baujahr;
        this.kennzeichen = kennzeichen;
        this.farbe = farbe;
    }
}