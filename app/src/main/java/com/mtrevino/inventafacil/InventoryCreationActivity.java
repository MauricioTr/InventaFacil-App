package com.mtrevino.inventafacil;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.mtrevino.inventafacil.internaldb.InventaFacilDbHelper;
import com.mtrevino.inventafacil.objects.Depot;
import com.mtrevino.inventafacil.objects.Inventory;
import com.mtrevino.inventafacil.objects.SectionForSpinner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class InventoryCreationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String KEY_EXTRA_INVENTORY_ID = "inventoryIdInUse";

    private EditText mInventoryNameField;
    private Spinner mDepotDropwdown;
    private Spinner mSectionDropdown;
    private Button mCreateInventoryButton;

    private ArrayList<Depot> userPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_creation);
        android.support.v7.widget.Toolbar myToolbar =  findViewById(R.id.tb_inventory_creation_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Creación de inventarios");

        mInventoryNameField = findViewById(R.id.et_inventory_creation_inventory_name);
        mDepotDropwdown = findViewById(R.id.sp_inventory_creation_depot_dropdown);
        mDepotDropwdown.setOnItemSelectedListener(this);
        mSectionDropdown = findViewById(R.id.sp_inventory_creation_section_dropdown);
        mCreateInventoryButton = findViewById(R.id.btn_inventory_creation_create_inventory);

        InventaFacilDbHelper internalDb = new InventaFacilDbHelper(this);
        userPermissions = internalDb.getUserPermissions(this);


        populateDepotSpinner();
        internalDb.close();
    }


    public void populateDepotSpinner(){
        int numDepots = userPermissions.size();

        //Create a temporary array to store the names of the depots
        String[] depotNames = new String[numDepots];

        //Iterate through user permission to populate the temporary array
        for (int i = 0; i < numDepots; i++){
            depotNames[i] = userPermissions.get(i).getDepotName();
        }

        //Turn the temporary array into an adapter and tell the spinner to use the adapter
        ArrayAdapter<String> depotAdapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, depotNames
        );

        mDepotDropwdown.setAdapter(depotAdapter);

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        HashMap<Integer, String> sectionsOfChosenDepot = userPermissions.get(position).getDepotSections();

        ArrayList<SectionForSpinner> sectionArray = new ArrayList<>();

        for ( int key : sectionsOfChosenDepot.keySet()){

            sectionArray.add(new SectionForSpinner(key, sectionsOfChosenDepot.get(key)));
        }

        //Collections.sort(sectionArray.toString());

        ArrayAdapter<SectionForSpinner> sectionAdapter = new ArrayAdapter<SectionForSpinner>(
                this, android.R.layout.simple_spinner_dropdown_item, sectionArray
        );

        mSectionDropdown.setAdapter(sectionAdapter);

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void generateNewInventory(View view){

        if(
                mDepotDropwdown.getSelectedItemPosition() == AdapterView.INVALID_POSITION ||
                mSectionDropdown.getSelectedItemPosition() == AdapterView.INVALID_POSITION
        ){
            Toast.makeText(this, "Por favor elija una sucursal y una sección", Toast.LENGTH_SHORT).show();
            return;
        }else if(mInventoryNameField.getText().toString().isEmpty() == true){
            Toast.makeText(this, "Por favor ingrese un nombre para el inventario", Toast.LENGTH_SHORT).show();
            return;
        }

        String givenInventoryName = mInventoryNameField.getText().toString();
        InventaFacilDbHelper internalDb = new InventaFacilDbHelper(this);

        if (internalDb.doesInventoryExist(this, givenInventoryName)){
            Toast.makeText(this,
                    "Actualmente existe un inventario con el nombre ingresado, por favor elija otro nombre",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        int selectedDepotPosition = mDepotDropwdown.getSelectedItemPosition();
        int selectedSectionPosition = mSectionDropdown.getSelectedItemPosition();
        HashMap<Long, Integer> emptyInventoryItemsMap = new HashMap<>();
        SectionForSpinner selectedSection = (SectionForSpinner) mSectionDropdown.getSelectedItem();
        int sectionId = selectedSection.getSectionId();

        Inventory inventoryToCreate = new Inventory(givenInventoryName, sectionId, emptyInventoryItemsMap);

        int createdInventoryId = internalDb.saveNewInventory(this, inventoryToCreate);

        if (createdInventoryId == -1){
            Toast.makeText(this, "Ocurrió un error al crear el inventario", Toast.LENGTH_LONG).show();
            return;
        }


        internalDb.close();

        Intent intent = new Intent(this, InventoryEditActivity.class);

        intent.putExtra(KEY_EXTRA_INVENTORY_ID, createdInventoryId);

        startActivity(intent);

        finish();
    }
}
