package com.mtrevino.inventafacil.objects;

import java.util.ArrayList;

public class Catalog {

    private int catalogVersion;

    private ArrayList<CatalogItem> catalogItems;

    private String catalogDate;

    public Catalog(int catalogVersion, ArrayList<CatalogItem> catalogItems, String catalogDate) {
        super();
        this.catalogVersion = catalogVersion;
        this.catalogItems = catalogItems;
        this.catalogDate = catalogDate;
    }

    public int getCatalogVersion() {
        return catalogVersion;
    }

    public void setCatalogVersion(int catalogVersion) {
        this.catalogVersion = catalogVersion;
    }

    public ArrayList<CatalogItem> getCatalogItems() {
        return catalogItems;
    }

    public void setCatalogItems(ArrayList<CatalogItem> catalogItems) {
        this.catalogItems = catalogItems;
    }

    public String getCatalogDate() {
        return catalogDate;
    }

    public void setCatalogDate(String catalogDate) {
        this.catalogDate = catalogDate;
    }
}
