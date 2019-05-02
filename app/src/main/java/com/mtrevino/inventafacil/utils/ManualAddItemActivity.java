package com.mtrevino.inventafacil.utils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mtrevino.inventafacil.R;
import com.mtrevino.inventafacil.internaldb.InventaFacilDbHelper;
import com.mtrevino.inventafacil.objects.Catalog;
import com.mtrevino.inventafacil.objects.CatalogItem;
import com.mtrevino.inventafacil.objects.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ManualAddItemActivity extends AppCompatActivity {

    private static final String KEY_EXTRA_INVENTORY_ID = "inventoryIdInUse";

    private AutoCompleteTextView mProductNameAutoComplete;
    private TextView mBarcodeTextView;
    private EditText mProductQuantityEditText;
    private Button mAddItemButton;

    private int inventoryIdInUse;
    private ArrayList<CatalogItem> catalogItemsList;
    private CatalogItem selectedItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_add_item);

        if (getIntent().hasExtra(KEY_EXTRA_INVENTORY_ID) == false ||
                getIntent().getIntExtra(KEY_EXTRA_INVENTORY_ID, 0) == 0){

            Toast.makeText(this, "No Inventory selected", Toast.LENGTH_LONG).show();
            finish();
        }

        android.support.v7.widget.Toolbar myToolbar =  findViewById(R.id.tb_toolbar_manual_product);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setTitle("Ingreso manual" );

        inventoryIdInUse = getIntent().getIntExtra(KEY_EXTRA_INVENTORY_ID, 0);

        mProductNameAutoComplete = findViewById(R.id.actv_manual_product_name_autocomplete);
        mBarcodeTextView = findViewById(R.id.tv_manual_product_barcode_display);
        mProductQuantityEditText = findViewById(R.id.et_manual_product_quantity_input);
        mAddItemButton = findViewById(R.id.btn_manual_product_add_product);

        catalogItemsList = loadCatalog();

        setupAutoCompleteView();

        final TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().equals("")){
                    s.append("1");
                }
            }
        };
        mProductQuantityEditText.setText("1");
        mProductQuantityEditText.addTextChangedListener(textWatcher);

        mAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedItem == null){
                    Toast.makeText(getApplicationContext(), "Porfavor elija un producto a agregar", Toast.LENGTH_SHORT).show();
                }else{
                    mAddItemButton.setClickable(false);
                    addItemToInventory();
                }
            }
        });
    }

    private void setupAutoCompleteView(){
        final ArrayAdapter<CatalogItem> adapter =
                new ArrayAdapter<CatalogItem>(this,
                        android.R.layout.simple_dropdown_item_1line, catalogItemsList);

        mProductNameAutoComplete.setAdapter(adapter);
        mProductNameAutoComplete.setThreshold(1);
        mProductNameAutoComplete.setValidator(new ProductNameValidator());

        mProductNameAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CatalogItem itemSelected = adapter.getItem(position);
                populateBarcodeDisplay(itemSelected.getBarcode());
                selectedItem = itemSelected;
            }
        });

        mProductNameAutoComplete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    mProductNameAutoComplete.showDropDown();
                }
            }
        });

        mProductNameAutoComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProductNameAutoComplete.showDropDown();
            }
        });
    }

    private void populateBarcodeDisplay(long barcode){
        mBarcodeTextView.setText(String.valueOf(barcode));
    }

    private void clearBarcodeDisplay(){
        mBarcodeTextView.setText("");
    }

    private ArrayList<CatalogItem> loadCatalog(){
        InventaFacilDbHelper internalDb = new InventaFacilDbHelper(this);

        try {
            return internalDb.getCatalogItems(this);
        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(this, "Error al cargar catalogo", Toast.LENGTH_LONG).show();
            finish();
        }finally {
            internalDb.close();
        }

        return null;
    }

    private class ProductNameValidator implements AutoCompleteTextView.Validator{

        @Override
        public boolean isValid(CharSequence text) {

            Iterator<CatalogItem> it = catalogItemsList.iterator();

            while (it.hasNext()){
                CatalogItem catalogItem = it.next();


                if (catalogItem.getProductName().equals(text.toString())){
                    populateBarcodeDisplay(catalogItem.getBarcode());
                    selectedItem = catalogItem;
                    return true;
                }
            }
            selectedItem = null;
            return false;
        }

        @Override
        public CharSequence fixText(CharSequence invalidText) {
            Toast.makeText(getApplicationContext(), "Please select a product from the list", Toast.LENGTH_SHORT).show();
            clearBarcodeDisplay();
            return "";
        }
    }

    private void addItemToInventory(){
        InventaFacilDbHelper internalDb = new InventaFacilDbHelper(this);

        Inventory inventory = internalDb.getInventoryFromId(this, inventoryIdInUse);

        HashMap<Long, Integer> inventoryItems = inventory.getInventoryItems();

        int quantityToAdd = Integer.valueOf(mProductQuantityEditText.getText().toString());

        //Checks if the inventory already has this product
        if (inventoryItems.get(selectedItem.getBarcode()) == null){
            inventoryItems.put(selectedItem.getBarcode(), quantityToAdd);
            internalDb.updateInventoryItems(this, inventoryIdInUse, inventoryItems);
        }else{
            int existingQuantity = inventoryItems.get(selectedItem.getBarcode());
            inventoryItems.put(selectedItem.getBarcode(), existingQuantity + quantityToAdd);
            internalDb.updateInventoryItems(this, inventoryIdInUse, inventoryItems);
        }

        internalDb.close();
        finish();
    }
}
