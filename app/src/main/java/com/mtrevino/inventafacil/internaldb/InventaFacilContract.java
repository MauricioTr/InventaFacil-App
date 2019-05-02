package com.mtrevino.inventafacil.internaldb;

import android.provider.BaseColumns;

public final class InventaFacilContract {

    private InventaFacilContract(){}

    public static class Catalog implements BaseColumns{
        public  static String TABLE_NAME = "catalog";
        public  static String COLUMN_NAME_BARCODE = "bar_code";
        public  static String COLUMN_NAME_PRODUCT_NAME = "product_name";
        public  static String COLUMN_NAME_QUANTITY = "quantity_per_package";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE "+ Catalog.TABLE_NAME+" ("+ Catalog._ID+" INTEGER PRIMARY KEY," +
                        " "+ Catalog.COLUMN_NAME_BARCODE+" LONG NOT NULL UNIQUE," +
                        " "+ Catalog.COLUMN_NAME_PRODUCT_NAME+" TEXT NOT NULL ," +
                        " "+ Catalog.COLUMN_NAME_QUANTITY+" INTEGER NOT NULL) ";
    }

    public static class Depot implements BaseColumns{
        public  static String TABLE_NAME = "depot";
        public  static String COLUMN_NAME_DEPOT_NAME = "bar_code";


        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE "+ Depot.TABLE_NAME+" ("+ Depot._ID+" INTEGER PRIMARY KEY," +
                        " "+ Depot.COLUMN_NAME_DEPOT_NAME+" TEXT NOT NULL UNIQUE) ";
    }

    public static class Section implements BaseColumns{
        public  static String TABLE_NAME = "section";
        public  static String COLUMN_NAME_CONSTANT_ID= "constant_id";
        public  static String COLUMN_NAME_SECTION_NAME = "section_name";
        public  static String COLUMN_NAME_DEPOT_ID = "depot_id";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE "+ Section.TABLE_NAME+" ("+ Section._ID+" INTEGER PRIMARY KEY," +
                        " "+ Section.COLUMN_NAME_CONSTANT_ID+" INTEGER NOT NULL UNIQUE," +
                        " "+ Section.COLUMN_NAME_SECTION_NAME+" TEXT NOT NULL ," +
                        " "+ Section.COLUMN_NAME_DEPOT_ID+" INTEGER NOT NULL ," +
                        " FOREIGN KEY("+ Section.COLUMN_NAME_DEPOT_ID+") REFERENCES "+Depot.TABLE_NAME+"("+Depot._ID+") " +
                        "ON DELETE CASCADE) ";
    }

    public static class Inventory implements BaseColumns{
        public  static String TABLE_NAME = "inventory";
        public  static String COLUMN_NAME_INVENTORY_NAME = "inventory_name";
        public  static String COLUMN_NAME_SECTION_ID = "section_id";
        public  static String COLUMN_NAME_STATUS = "status"; // 0=disabled 1=In Progress 2=Sent

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE "+ Inventory.TABLE_NAME+" ("+ Inventory._ID+" INTEGER PRIMARY KEY," +
                        " "+ Inventory.COLUMN_NAME_INVENTORY_NAME+" TEXT NOT NULL ," +
                        " "+ Inventory.COLUMN_NAME_SECTION_ID+" INTEGER NOT NULL ," +
                        " "+ Inventory.COLUMN_NAME_STATUS+" INTEGER NOT NULL DEFAULT 1," +
                        " FOREIGN KEY("+ Inventory.COLUMN_NAME_SECTION_ID+") REFERENCES "+Section.TABLE_NAME+"("+Section._ID+")) ";
    }

    public static class InventoryItem implements BaseColumns{
        public  static String TABLE_NAME = "inventory_item";
        public  static String COLUMN_NAME_INVENTORY_ID= "inventory_id";
        public  static String COLUMN_NAME_BAR_CODE = "bar_code";
        public  static String COLUMN_NAME_QUANTITY = "quantity";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE "+ InventoryItem.TABLE_NAME+" ("+ InventoryItem._ID+" INTEGER PRIMARY KEY," +
                        " "+ InventoryItem.COLUMN_NAME_INVENTORY_ID+" INTEGER NOT NULL ," +
                        " "+ InventoryItem.COLUMN_NAME_BAR_CODE+" LONG NOT NULL ," +
                        " "+ InventoryItem.COLUMN_NAME_QUANTITY+" INTEGER NOT NULL ," +
                        " FOREIGN KEY("+ InventoryItem.COLUMN_NAME_INVENTORY_ID+") REFERENCES "+Inventory.TABLE_NAME+"("+Inventory._ID+")" +
                        "ON DELETE CASCADE)";
    }


}
