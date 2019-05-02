package com.mtrevino.inventafacil.objects;

import java.util.HashMap;

public class Inventory {

    private String inventoryName;
    private int sectionId;
    private HashMap<Long,Integer> inventoryItems;

    //variable is transient to exclude it from GSON serialization
    private transient int inventoryId;

    public Inventory( String inventoryName,  int sectionId,  HashMap<Long,Integer> inventoryItems) {
        super();
        this.inventoryName = inventoryName;
        this.sectionId = sectionId;
        this.inventoryItems = inventoryItems;
    }

    public Inventory( String inventoryName,  int sectionId,  HashMap<Long,Integer> inventoryItems, int inventoryId) {
        super();
        this.inventoryName = inventoryName;
        this.sectionId = sectionId;
        this.inventoryItems = inventoryItems;
        this.inventoryId = inventoryId;
    }

    public String getInventoryName() {
        return inventoryName;
    }
    public void setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
    }
    public int getSectionId() {
        return sectionId;
    }
    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }
    public HashMap<Long,Integer> getInventoryItems() {
        return inventoryItems;
    }
    public void setItems(HashMap<Long,Integer> inventoryItems) {
        this.inventoryItems = inventoryItems;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }
}
