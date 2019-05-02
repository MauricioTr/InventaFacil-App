package com.mtrevino.inventafacil;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.mtrevino.inventafacil.internaldb.InventaFacilDbHelper;
import com.mtrevino.inventafacil.objects.CatalogItem;
import com.mtrevino.inventafacil.objects.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScannerActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private static final String KEY_EXTRA_INVENTORY_ID = "inventoryIdInUse";
    private static final String KEY_EXTRA_CAPTURE_MODE = "captureMode";

    private ZXingScannerView mScannerView;

    private int inventoryIdInUse;
    private int captureMode;
    private HashMap<Long, Integer> inventoryItemsMap;
    private ArrayList<CatalogItem> catalogItemsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view

        if (getIntent().hasExtra(KEY_EXTRA_INVENTORY_ID) == false ||
                getIntent().getIntExtra(KEY_EXTRA_INVENTORY_ID, 0) == 0){

            Toast.makeText(this, "No Inventory selected", Toast.LENGTH_LONG).show();
            finish();
        }

        inventoryIdInUse = getIntent().getIntExtra(KEY_EXTRA_INVENTORY_ID, 0);
        captureMode = getIntent().getIntExtra(KEY_EXTRA_CAPTURE_MODE, 0 );

        loadInventoryAndCatalog();
    }

    private void loadInventoryAndCatalog() {
        InventaFacilDbHelper internalDb = new InventaFacilDbHelper(this);

        Inventory inventory = internalDb.getInventoryFromId(this, inventoryIdInUse);
        inventoryItemsMap = inventory.getInventoryItems();

        catalogItemsList = internalDb.getCatalogItems(this);

        internalDb.close();
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume

        System.out.println("Camera started");
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause

        InventaFacilDbHelper internalDb = new InventaFacilDbHelper(this);
        internalDb.updateInventoryItems(this, inventoryIdInUse, inventoryItemsMap);

    }

    @Override
    public void handleResult(Result rawResult) {

        System.out.println(rawResult.getText());
        System.out.println(rawResult.getBarcodeFormat());

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(this, notification);
        r.play();

        mScannerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mScannerView.startCamera();
            }
        }, 500);

        if (rawResult.getBarcodeFormat() != BarcodeFormat.EAN_13 && rawResult.getBarcodeFormat() != BarcodeFormat.UPC_A ){
            Toast.makeText(this, "Código no se pudo recononer, sólo codigos de barra son aceptados", Toast.LENGTH_SHORT).show();
            mScannerView.resumeCameraPreview(this);
            return;
        }

        Iterator<CatalogItem> it = catalogItemsList.iterator();

        while (it.hasNext()){
            CatalogItem catalogItem = it.next();

            if (Long.valueOf(rawResult.getText()) == catalogItem.getBarcode()){
                addProductToInventory(catalogItem);
                mScannerView.resumeCameraPreview(this);
                return;
            }
        }

        Toast.makeText(this,
                "No se pudo encontrar un producto asociado a este código, porfavor intente un par de veces más",
        Toast.LENGTH_SHORT).show();


        // If you would like to resume scanning, call this method below:
        mScannerView.resumeCameraPreview(this);
    }

    private void addProductToInventory(CatalogItem catalogItemToAdd){
        long itemBarcode = catalogItemToAdd.getBarcode();
        int itemQuantityPerPackage = catalogItemToAdd.getQuantityPerPackage();

        if (inventoryItemsMap.get(itemBarcode) == null){
            //if the inventory didn't contain this product before add either one or many depending on the capture mode
            if (captureMode == 0){
                inventoryItemsMap.put(itemBarcode, 1);
            }else if(captureMode == 1){
                inventoryItemsMap.put(itemBarcode, itemQuantityPerPackage);
            }
        }else{
            int previousQuantity = inventoryItemsMap.get(itemBarcode);

            //if the inventory already contained this product add to the previous quantity
            if (captureMode == 0){
                inventoryItemsMap.put(itemBarcode, ++previousQuantity );
            }else if(captureMode == 1){
                inventoryItemsMap.put(itemBarcode, previousQuantity+itemQuantityPerPackage);
            }
        }

        Toast.makeText(this, "Producto reconocido: " + catalogItemToAdd.getProductName()+
                " | # en inventario: " + inventoryItemsMap.get(itemBarcode),
                Toast.LENGTH_SHORT).show();

    }
}
