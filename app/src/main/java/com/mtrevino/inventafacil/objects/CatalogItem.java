package com.mtrevino.inventafacil.objects;

public class CatalogItem {


    private int itemId;
    private long barcode;
    private String productName;
    private int quantityPerPackage;


    public CatalogItem(int itemId, long barcode, String productName, int quantityPerPackage) {
        this.itemId = itemId;
        this.barcode = barcode;
        this.productName = productName;
        this.quantityPerPackage = quantityPerPackage;
    }

    public int getItemId() {
        return itemId;
    }
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
    public long getBarcode() {
        return barcode;
    }
    public void setBarcode(long barcode) {
        this.barcode = barcode;
    }
    public String getProductName() {
        return productName;
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }
    public int getQuantityPerPackage() {
        return quantityPerPackage;
    }
    public void setQuantityPerPackage(int quantityPerPackage) {
        this.quantityPerPackage = quantityPerPackage;
    }


    @Override
    public String toString() {
        return productName;
    }
}
