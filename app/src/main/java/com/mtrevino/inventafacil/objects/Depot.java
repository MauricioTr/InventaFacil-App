package com.mtrevino.inventafacil.objects;

import java.util.HashMap;

public class Depot {

    private String depotName;

    private HashMap<Integer, String> depotSections;


    public Depot( String depotName, HashMap<Integer, String> depotSections) {
        super();
        this.depotName = depotName;
        this.depotSections = depotSections;
    }



    public String getDepotName() {
        return depotName;
    }

    public void setDepotName(String depotName) {
        this.depotName = depotName;
    }

    public HashMap<Integer, String> getDepotSections() {
        return depotSections;
    }

    public void setDepotSections(HashMap<Integer, String> depotSections) {
        this.depotSections = depotSections;
    }


}
