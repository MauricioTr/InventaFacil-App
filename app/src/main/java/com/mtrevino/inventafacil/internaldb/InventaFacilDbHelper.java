package com.mtrevino.inventafacil.internaldb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Pair;

import com.mtrevino.inventafacil.objects.CatalogItem;
import com.mtrevino.inventafacil.objects.Depot;
import com.mtrevino.inventafacil.objects.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class InventaFacilDbHelper extends SQLiteOpenHelper {

    //Increase this number if the database schema is changed
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "InventaFacil.db";

    public InventaFacilDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(InventaFacilContract.Catalog.SQL_CREATE_ENTRIES);
        db.execSQL(InventaFacilContract.Depot.SQL_CREATE_ENTRIES);
        db.execSQL(InventaFacilContract.Section.SQL_CREATE_ENTRIES);
        db.execSQL(InventaFacilContract.Inventory.SQL_CREATE_ENTRIES);
        db.execSQL(InventaFacilContract.InventoryItem.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean isCatalogEmpty(Context currentContext){

        ArrayList<CatalogItem> catalogItems = getCatalogItems(currentContext);

        if (catalogItems.size() == 0){
            return true;
        }
        return false;
    }

    public boolean updateCatalog(Context context, com.mtrevino.inventafacil.objects.Catalog newCatalog){

        if (newCatalog == null) return false;

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.execSQL("DELETE FROM " + InventaFacilContract.Catalog.TABLE_NAME);
            ArrayList<CatalogItem> catalogItems = newCatalog.getCatalogItems();
            Iterator<CatalogItem> it = catalogItems.iterator();
            while (it.hasNext()) {
                CatalogItem itemToInsert = it.next();
                ContentValues values = new ContentValues();
                values.put(InventaFacilContract.Catalog.COLUMN_NAME_BARCODE, itemToInsert.getBarcode());
                values.put(InventaFacilContract.Catalog.COLUMN_NAME_PRODUCT_NAME, itemToInsert.getProductName());
                values.put(InventaFacilContract.Catalog.COLUMN_NAME_QUANTITY, itemToInsert.getQuantityPerPackage());
                db.insert(InventaFacilContract.Catalog.TABLE_NAME, null, values);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public ArrayList<CatalogItem> getCatalogItems(Context context){
        ArrayList<CatalogItem> arrayToReturn = new ArrayList<CatalogItem>();
        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                InventaFacilContract.Catalog.COLUMN_NAME_BARCODE,
                InventaFacilContract.Catalog.COLUMN_NAME_PRODUCT_NAME,
                InventaFacilContract.Catalog.COLUMN_NAME_QUANTITY
        };

        // How the results will be sorted
        String sortOrder =
                BaseColumns._ID + " ASC";

        try {
            //Query the database and return results to a cursor
            Cursor cursor = db.query(
                    InventaFacilContract.Catalog.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            while (cursor.moveToNext()){ //Iterate through the cursor to map each row a CatalogItem
                int itemId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                long itemBarcode = cursor.getLong(cursor.getColumnIndex(InventaFacilContract.Catalog.COLUMN_NAME_BARCODE));
                String itemName = cursor.getString(cursor.getColumnIndex(InventaFacilContract.Catalog.COLUMN_NAME_PRODUCT_NAME));
                int itemQuantityPerPackage = cursor.getInt(cursor.getColumnIndex(InventaFacilContract.Catalog.COLUMN_NAME_QUANTITY));

                CatalogItem catalogItem = new CatalogItem(itemId,itemBarcode, itemName, itemQuantityPerPackage);
                arrayToReturn.add(catalogItem);
            }

            return arrayToReturn;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public String getItemNameFromCatalog(Context context, long barcode) {
        String nameToReturn = null;

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //Define which columns to query
        String[] Projection = {
                InventaFacilContract.Catalog.COLUMN_NAME_PRODUCT_NAME
        };

        String selection = InventaFacilContract.Catalog.COLUMN_NAME_BARCODE + " = ?";
        String[] selectionArgs = { String.valueOf(barcode) };

        try {
            Cursor cursor = db.query(       //Query the database and store the results in a cursor
                    InventaFacilContract.Catalog.TABLE_NAME,   // The table to query
                    Projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    null               // The sort order
            );

            cursor.moveToNext();
            nameToReturn = cursor.getString(cursor.getColumnIndex(InventaFacilContract.Catalog.COLUMN_NAME_PRODUCT_NAME));
            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            db.close();
        }


        return nameToReturn;

    }

    public boolean doesInventoryExist(Context context, String inventoryNameToValidate){

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                InventaFacilContract.Inventory.COLUMN_NAME_INVENTORY_NAME
        };


        // How the results will be sorted
        String sortOrder =
                InventaFacilContract.Inventory.COLUMN_NAME_INVENTORY_NAME + " ASC";

        try {
            //Query the database and return results to a cursor
            Cursor cursor = db.query(
                    InventaFacilContract.Inventory.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            while (cursor.moveToNext()){ //Iterate through the cursor to check if the given name matches any existing inventory
                String inventoryName = cursor.getString(cursor.getColumnIndex(InventaFacilContract.Inventory.COLUMN_NAME_INVENTORY_NAME));

                if (inventoryNameToValidate.equals(inventoryName)) return true;

            }

        }catch (Exception e){
            e.printStackTrace();
            return true;
        }
        return false;
    }

    public int saveNewInventory(Context context, Inventory inventoryToSave){

        if (inventoryToSave == null) return -1;

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {


            ContentValues inventoryValues = new ContentValues();
            inventoryValues.put(InventaFacilContract.Inventory.COLUMN_NAME_INVENTORY_NAME, inventoryToSave.getInventoryName());
            inventoryValues.put(InventaFacilContract.Inventory.COLUMN_NAME_SECTION_ID, inventoryToSave.getSectionId());


            //Insert a new row for the inventory and store the id
            long insertedInventoryId = db.insert(InventaFacilContract.Inventory.TABLE_NAME, null, inventoryValues);

            HashMap<Long, Integer> inventoryItems = inventoryToSave.getInventoryItems();

            for(long itemBarcode: inventoryItems.keySet()) {    //Iterate through each inventory items in the inventory's hashmap
                int itemQuantity = inventoryItems.get(itemBarcode);

                ContentValues itemValues = new ContentValues();
                itemValues.put(InventaFacilContract.InventoryItem.COLUMN_NAME_BAR_CODE, itemBarcode);
                itemValues.put(InventaFacilContract.InventoryItem.COLUMN_NAME_QUANTITY, itemQuantity);
                itemValues.put(InventaFacilContract.InventoryItem.COLUMN_NAME_INVENTORY_ID, insertedInventoryId);
                //Insert each inventoryItem referencing the recently created Inventory
                db.insert(InventaFacilContract.InventoryItem.TABLE_NAME, null, itemValues);
            }

            return (int) insertedInventoryId;

        }catch (Exception e){
            e.printStackTrace();
            return -1;
        }

    }

    public ArrayList<Inventory> getAllInventories(Context context){

        ArrayList<Inventory> inventoryList = new ArrayList<>();

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID
        };

        String selection = InventaFacilContract.Inventory.COLUMN_NAME_STATUS + " = ?";
        String[] selectionArgs = { String.valueOf(1) };

        // How the results will be sorted
        String sortOrder =
                BaseColumns._ID + " DESC";

        try {
            //Query the database and return results to a cursor
            Cursor cursor = db.query(
                    InventaFacilContract.Inventory.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            while (cursor.moveToNext()){ //Iterate through the cursor to map each row an inventory
                int inventoryId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));

                Inventory inventory = getInventoryFromId(context, inventoryId);

                inventoryList.add(inventory);
            }

            return inventoryList;


        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            db.close();
        }
    }

    public Inventory getInventoryFromId(Context context, int inventoryId){

        Inventory inventoryToReturn ;

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                InventaFacilContract.Inventory.COLUMN_NAME_INVENTORY_NAME,
                InventaFacilContract.Inventory.COLUMN_NAME_SECTION_ID
        };

        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = { String.valueOf(inventoryId) };

        // How the results will be sorted
        String sortOrder =
                InventaFacilContract.Inventory.COLUMN_NAME_INVENTORY_NAME + " ASC";

        try {
            //Query the database and return results to a cursor
            Cursor cursor = db.query(
                    InventaFacilContract.Inventory.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            while (cursor.moveToNext()){ //Iterate through the cursor to map each row an Inventory
                String inventoryName = cursor.getString(cursor.getColumnIndex(InventaFacilContract.Inventory.COLUMN_NAME_INVENTORY_NAME));
                int inventorySectionId = cursor.getInt(cursor.getColumnIndex(InventaFacilContract.Inventory.COLUMN_NAME_SECTION_ID));

                HashMap<Long, Integer> inventoryItems = getInventoryItemsFromInventoryId(context, inventoryId);

                inventoryToReturn = new Inventory(inventoryName, inventorySectionId, inventoryItems, inventoryId);
                return inventoryToReturn;
            }

        }catch (Exception e){
            e.printStackTrace();

        } finally {
            db.close();
        }
        return null;
    }

    private HashMap<Long, Integer> getInventoryItemsFromInventoryId(Context context, int inventoryId){
        HashMap<Long, Integer> inventoryItems = new HashMap<>();

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //Define which columns to query
        String[] Projection = {
                BaseColumns._ID,
                InventaFacilContract.InventoryItem.COLUMN_NAME_BAR_CODE,
                InventaFacilContract.InventoryItem.COLUMN_NAME_QUANTITY
        };

        String selection = InventaFacilContract.InventoryItem.COLUMN_NAME_INVENTORY_ID + " = ?";
        String[] selectionArgs = { String.valueOf(inventoryId) };

        // How the results will be sorted
        String sortOrder =
                InventaFacilContract.InventoryItem.COLUMN_NAME_BAR_CODE + " ASC";

        Cursor cursor = db.query(       //Query the database and store the results in a cursor
                InventaFacilContract.InventoryItem.TABLE_NAME,   // The table to query
                Projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        while(cursor.moveToNext()){
            long barcode = cursor.getLong(cursor.getColumnIndex(InventaFacilContract.InventoryItem.COLUMN_NAME_BAR_CODE));
            int quantity = cursor.getInt(cursor.getColumnIndex(InventaFacilContract.InventoryItem.COLUMN_NAME_QUANTITY));

            inventoryItems.put(barcode, quantity);
        }

        return inventoryItems;

    }

    public boolean updateInventoryItems(Context context, int idInventoryToUpdate, HashMap<Long, Integer> inventoryItemsMap) {

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();


        String selection = InventaFacilContract.InventoryItem.COLUMN_NAME_INVENTORY_ID + " = ?";
        String[] selectionArgs = { String.valueOf(idInventoryToUpdate) };

        //Previous items associated with this id
        db.delete(       //Query the database and store the results in a cursor
                InventaFacilContract.InventoryItem.TABLE_NAME,   // The table to query
                selection,              // The columns for the WHERE clause
                selectionArgs
        );

        for(long itemBarcode: inventoryItemsMap.keySet()) {    //Iterate through each inventory items in the inventory's hashmap
            int itemQuantity = inventoryItemsMap.get(itemBarcode);

            ContentValues itemValues = new ContentValues();
            itemValues.put(InventaFacilContract.InventoryItem.COLUMN_NAME_BAR_CODE, itemBarcode);
            itemValues.put(InventaFacilContract.InventoryItem.COLUMN_NAME_QUANTITY, itemQuantity);
            itemValues.put(InventaFacilContract.InventoryItem.COLUMN_NAME_INVENTORY_ID, idInventoryToUpdate);
            //Insert each inventoryItem referencing the recently created Inventory
            db.insert(InventaFacilContract.InventoryItem.TABLE_NAME, null, itemValues);
        }

        return true;
    }

    public boolean saveUserPermissions(Context context, ArrayList<Depot> permittedDepots){

        if (permittedDepots == null) return false;

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            db.execSQL("DELETE FROM " + InventaFacilContract.Depot.TABLE_NAME);
            db.execSQL("DELETE FROM " + InventaFacilContract.Section.TABLE_NAME);

            Iterator<Depot> it = permittedDepots.iterator();

            while (it.hasNext()) {  //Iterate through each depot in the list
                Depot depotToInsert = it.next();
                ContentValues depotValues = new ContentValues();
                depotValues.put(InventaFacilContract.Depot.COLUMN_NAME_DEPOT_NAME, depotToInsert.getDepotName());

                //Insert a new row for the depot and store the id
                long insertedDepotId = db.insert(InventaFacilContract.Depot.TABLE_NAME, null, depotValues);

                HashMap<Integer, String> depotSections = depotToInsert.getDepotSections();

                for(int sectionId: depotSections.keySet()) {    //Iterate through each section in the depot's hashmap

                    ContentValues sectionValues = new ContentValues();
                    sectionValues.put(InventaFacilContract.Section.COLUMN_NAME_CONSTANT_ID, sectionId);
                    sectionValues.put(InventaFacilContract.Section.COLUMN_NAME_SECTION_NAME, depotSections.get(sectionId));
                    sectionValues.put(InventaFacilContract.Section.COLUMN_NAME_DEPOT_ID, insertedDepotId);
                    //Insert each section referencing the recently created depot
                    db.insert(InventaFacilContract.Section.TABLE_NAME, null, sectionValues);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public ArrayList<Depot> getUserPermissions(Context context){

        ArrayList<Depot> permissionsToReturn = new ArrayList<>();

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                BaseColumns._ID,
                InventaFacilContract.Depot.COLUMN_NAME_DEPOT_NAME
        };

        // How the results will be sorted
        String sortOrder =
                BaseColumns._ID + " ASC";

        try {
            //Query the database and return results to a cursor
            Cursor cursor = db.query(
                    InventaFacilContract.Depot.TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            );

            while (cursor.moveToNext()){ //Iterate through the cursor to map each row a Depot
                int depotId = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
                String depotName = cursor.getString(cursor.getColumnIndex(InventaFacilContract.Depot.COLUMN_NAME_DEPOT_NAME));

                HashMap<Integer, String> sections = getSectionsFromDepotId(context, depotId);

                Depot depot = new Depot(depotName, sections);
                permissionsToReturn.add(depot);
            }

            cursor.close();
            return permissionsToReturn;


        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            db.close();
        }
    }

    private HashMap<Integer, String> getSectionsFromDepotId(Context context, int depotId){
        HashMap<Integer, String> sectionsToReturn = new HashMap<>();

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //Define which columns to query
        String[] Projection = {
                BaseColumns._ID,
                InventaFacilContract.Section.COLUMN_NAME_CONSTANT_ID,
                InventaFacilContract.Section.COLUMN_NAME_SECTION_NAME
        };

        String selection = InventaFacilContract.Section.COLUMN_NAME_DEPOT_ID + " = ?";
        String[] selectionArgs = { String.valueOf(depotId) };

        // How the results will be sorted
        String sortOrder =
                InventaFacilContract.Section.COLUMN_NAME_SECTION_NAME + " ASC";

        Cursor cursor = db.query(       //Query the database and store the results in a cursor
                InventaFacilContract.Section.TABLE_NAME,   // The table to query
                Projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        while(cursor.moveToNext()){
            int sectionId = cursor.getInt(cursor.getColumnIndex(InventaFacilContract.Section.COLUMN_NAME_CONSTANT_ID));
            String sectionName = cursor.getString(cursor.getColumnIndex(InventaFacilContract.Section.COLUMN_NAME_SECTION_NAME));

            sectionsToReturn.put(sectionId, sectionName);
        }
        cursor.close();
        db.close();

        return sectionsToReturn;
    }

    public  Pair<String, String> getDepotAndSectionNameFromSectionId(Context context, int sectionId){
        String sectionName = getSectionNameFromSectionId(context, sectionId);
        Pair<String, String> pairToReturn;

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursorForDepotId = db.query(
                InventaFacilContract.Section.TABLE_NAME,
                null,
                InventaFacilContract.Section.COLUMN_NAME_CONSTANT_ID + " = ?",
                new String[]{String.valueOf(sectionId)},
                null,
                null,
                null
        );

        cursorForDepotId.moveToNext();
        int depotId = cursorForDepotId.getInt(cursorForDepotId.getColumnIndex(InventaFacilContract.Section.COLUMN_NAME_DEPOT_ID));

        //Define which columns to query
        String[] Projection = {
                BaseColumns._ID,
                InventaFacilContract.Depot.COLUMN_NAME_DEPOT_NAME
        };

        String selection = BaseColumns._ID + " = ?";
        String[] selectionArgs = { String.valueOf(depotId) };

        // How the results will be sorted
        String sortOrder =
                InventaFacilContract.Depot.COLUMN_NAME_DEPOT_NAME + " ASC";

        Cursor cursorForDepotName = db.query(       //Query the database and store the results in a cursor
                InventaFacilContract.Depot.TABLE_NAME,   // The table to query
                Projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );


        cursorForDepotName.moveToNext();

        String depotName = cursorForDepotName.getString(cursorForDepotName.getColumnIndex(InventaFacilContract.Depot.COLUMN_NAME_DEPOT_NAME));

        pairToReturn = new Pair<>(sectionName, depotName);

        cursorForDepotId.close();
        cursorForDepotName.close();
        db.close();

        return pairToReturn;
    }

    private String getSectionNameFromSectionId(Context context, int sectionId){
        String nameToReturn = null;

        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        //Define which columns to query
        String[] Projection = {
                BaseColumns._ID,
                InventaFacilContract.Section.COLUMN_NAME_CONSTANT_ID,
                InventaFacilContract.Section.COLUMN_NAME_SECTION_NAME
        };

        String selection = InventaFacilContract.Section.COLUMN_NAME_CONSTANT_ID + " = ?";
        String[] selectionArgs = { String.valueOf(sectionId) };

        // How the results will be sorted
        String sortOrder =
                InventaFacilContract.Section.COLUMN_NAME_SECTION_NAME + " ASC";

        Cursor cursor = db.query(       //Query the database and store the results in a cursor
                InventaFacilContract.Section.TABLE_NAME,   // The table to query
                Projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );

        cursor.moveToNext();
        nameToReturn = cursor.getString(cursor.getColumnIndex(InventaFacilContract.Section.COLUMN_NAME_SECTION_NAME));

        cursor.close();
        db.close();

        return nameToReturn;
    }

    public void markInventoryAsSent(Context context, int inventoryId) {


        InventaFacilDbHelper dbHelper = new InventaFacilDbHelper(context);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // Define a projection that specifies which columns from the database
            // you will actually use after this query.

            String selection = BaseColumns._ID + " = ?";
            String[] selectionArgs = { String.valueOf(inventoryId) };

            ContentValues cv = new ContentValues();
            cv.put(InventaFacilContract.Inventory.COLUMN_NAME_STATUS,2);


            db.update(InventaFacilContract.Inventory.TABLE_NAME, cv, selection, selectionArgs);

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            db.close();
        }

    }
}
