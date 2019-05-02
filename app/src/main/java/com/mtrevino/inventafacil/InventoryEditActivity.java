package com.mtrevino.inventafacil;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mtrevino.inventafacil.apiclient.ApiController;
import com.mtrevino.inventafacil.internaldb.InventaFacilDbHelper;
import com.mtrevino.inventafacil.objects.Inventory;
import com.mtrevino.inventafacil.utils.InventaFacilGeneralUtils;
import com.mtrevino.inventafacil.utils.InventaFacilPrefManager;
import com.mtrevino.inventafacil.utils.ManualAddItemActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class InventoryEditActivity extends AppCompatActivity {
    private static final String KEY_EXTRA_INVENTORY_ID = "inventoryIdInUse";
    private static final String KEY_EXTRA_CAPTURE_MODE = "captureMode";

    private static final int UPLOAD_INVENTORY_LOADER = 33;

    private Button mAddItemButton;
    private RecyclerView mRecyclerView;
    private Button mScanSingleItemButton;
    private Button mScanMultipleItemButton;


    private LinearLayoutManager mLinearLayoutManager;
    InventoryItemAdapter mInventoryItemAdapter;
    private int inventoryIdInUse;
    private Inventory inventoryInUse;

    private HashMap<Long, Integer> inventoryItemsInUse = new HashMap<>();
    private HashMap<Long, Pair<String, Integer>> inventoryItemsInUseWithNames = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_edit);

        android.support.v7.widget.Toolbar myToolbar =  findViewById(R.id.tb_inventory_edit_toolbar);
        setSupportActionBar(myToolbar);


        if (getIntent().hasExtra(KEY_EXTRA_INVENTORY_ID) == false || getIntent().getIntExtra(KEY_EXTRA_INVENTORY_ID, 0) == 0){
            Toast.makeText(this, "No Inventory selected", Toast.LENGTH_LONG).show();
            finish();
        }

        inventoryIdInUse = getIntent().getIntExtra(KEY_EXTRA_INVENTORY_ID, 0);


        mRecyclerView = findViewById(R.id.rv_inventory_edit_recycler);
        mAddItemButton = findViewById(R.id.btn_inventory_edit_add_item);
        mScanSingleItemButton = findViewById(R.id.btn_inventory_edit_scan_single_item);
        mScanMultipleItemButton = findViewById(R.id.btn_inventory_edit_scan_multiple_item);

        mScanSingleItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScannerActivity(0);
            }
        });

        mScanMultipleItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScannerActivity(1);
            }
        });


        populateActivityView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateActivityView();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mInventoryItemAdapter.myInventoryItemsMap == null) return;

        InventaFacilDbHelper internalDb = new InventaFacilDbHelper(this);
        internalDb.updateInventoryItems(this, inventoryIdInUse, mInventoryItemAdapter.myInventoryItemsMap);

        internalDb.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id != R.id.action_send_inventory) return super.onOptionsItemSelected(item);
        //Call onPause to save any changes done to the inventory
        onPause();


        Toast.makeText(this, "Attempting upload", Toast.LENGTH_SHORT).show();
        uploadInventory();
        return true;

    }

    private void populateActivityView(){
        loadInventoryData();

        getSupportActionBar().setTitle(inventoryInUse.getInventoryName());

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);


        mInventoryItemAdapter = new InventoryItemAdapter(inventoryItemsInUse, inventoryItemsInUseWithNames);
        mRecyclerView.setAdapter(mInventoryItemAdapter);
    }

    private void uploadInventory(){
        //Disable user interaction
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        InventaFacilGeneralUtils generalUtils = new InventaFacilGeneralUtils();
        LoaderManager loaderManager = getLoaderManager();

        if (generalUtils.isDeviceConnectedToNetwork(this) ==  false) {
            Toast.makeText(this, "Sin conexión a la red", Toast.LENGTH_LONG).show();
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            return;
        }

        Loader<String> checkForCatalogUpdatesLoader = loaderManager.getLoader(UPLOAD_INVENTORY_LOADER);
        Bundle emptyBundle = new Bundle();

        // ) If the Loader was null, initialize it. Else, restart it.
        if (checkForCatalogUpdatesLoader == null) {
            loaderManager.initLoader(UPLOAD_INVENTORY_LOADER, emptyBundle, uploadInventoryTaskLoader);
        } else {
            loaderManager.restartLoader(UPLOAD_INVENTORY_LOADER, emptyBundle, uploadInventoryTaskLoader);
        }
    }

    private LoaderManager.LoaderCallbacks<String> uploadInventoryTaskLoader = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader<String> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<String>(getApplicationContext()) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();

                    forceLoad();
                }

                @Override
                public String loadInBackground() {
                    ApiController apiController = new ApiController();
                    InventaFacilDbHelper internalDb = new InventaFacilDbHelper(getApplicationContext());
                    Inventory inventoryToSend = internalDb.getInventoryFromId(getApplicationContext(), inventoryIdInUse);

                    try {
                        String result = apiController.uploadInventory(getApplicationContext(), inventoryToSend);
                        internalDb.markInventoryAsSent(getApplicationContext(), inventoryIdInUse);
                        return result;
                    }catch (Exception e){
                        e.printStackTrace();
                        return null;
                    }finally {
                        internalDb.close();
                    }
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            //Resume user interaction
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

            // If the result from the network request is null or not a number exit the method
            if (data == null ) {
                Toast.makeText(getApplicationContext(), "Error al enviar inventario", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getApplicationContext(), "Inventario enviado exitosamente", Toast.LENGTH_LONG).show();
            finishThisActivity();
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    };

    private void finishThisActivity() {
        finish();
    }

    private class InventoryItemAdapter extends RecyclerView.Adapter<InventoryItemAdapter.InventoryItemViewHolder>{


        public HashMap<Long, Integer> myInventoryItemsMap;
        public HashMap<Long, Pair<String, Integer>> myNamedInventoryItemsMap;
        public ArrayList<Map.Entry<Long, Pair<String, Integer>>> myNamedInventoryItemsList;


        public  InventoryItemAdapter(HashMap<Long, Integer> inventoryItemsInUse, HashMap<Long, Pair<String, Integer>> inventoryItemsInUseWithNames){

            this.myInventoryItemsMap = inventoryItemsInUse;
            this.myNamedInventoryItemsMap = inventoryItemsInUseWithNames;

            updateInventoryItemList();
        }

        private void updateInventoryItemList(){

            Set<Map.Entry<Long, Pair<String, Integer>>> inventoryItemEntrySet = myNamedInventoryItemsMap.entrySet();
            //Creating an ArrayList using Set data
            myNamedInventoryItemsList = new ArrayList<Map.Entry<Long,  Pair<String, Integer>>>(inventoryItemEntrySet);
        }

        public void deleteItemInInventory(long barcode){

            myInventoryItemsMap.remove(barcode);
            myNamedInventoryItemsMap.remove(barcode);
            updateInventoryItemList();
            notifyDataSetChanged();
        }

        public void updateItemQuantityInInventory(long barcode, int quantity){

            myInventoryItemsMap.remove(barcode);
            myInventoryItemsMap.put(barcode, quantity);

            String nameOfItemToChange = myNamedInventoryItemsMap.get(barcode).first;
            myNamedInventoryItemsMap.remove(barcode);
            myNamedInventoryItemsMap.put(barcode, Pair.create(nameOfItemToChange, quantity));
            //As the information in the Hashmaps has changed, the ArrayList needs to be updated
            updateInventoryItemList();
        }

        @NonNull
        @Override
        public InventoryItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            final View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inventory_edit_item_view, viewGroup, false);

            return new InventoryItemViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull final InventoryItemViewHolder inventoryItemViewHolder, int i) {
            final Map.Entry<Long, Pair<String, Integer>> inventoryItem = myNamedInventoryItemsList.get(i);


            inventoryItemViewHolder.vInventoryItemDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = inventoryItemViewHolder.getLayoutPosition();

                    //Whe delete button is presses the item associated with this item's barcode will be deleted
                    deleteItemInInventory(inventoryItem.getKey());
                    notifyItemRemoved(position);
                }
            });


            final TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                    // TODO Auto-generated method stub
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    // TODO Auto-generated method stub
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().equals("")){
                        s.append("1");
                        updateItemQuantityInInventory(inventoryItem.getKey(), 1);
                    }else{
                        updateItemQuantityInInventory(inventoryItem.getKey(), Integer.valueOf(s.toString()));
                    }

                    // TODO Auto-generated method stub
                }
            };

            inventoryItemViewHolder.vInventoryItemQuantityEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {

                    inventoryItemViewHolder.vInventoryItemQuantityEditText.addTextChangedListener(textWatcher);

                }
            });

            inventoryItemViewHolder.vInventoryItemNameTextView.setText(String.valueOf(inventoryItem.getValue().first));
            inventoryItemViewHolder.vInventoryItemQuantityEditText.setText(String.valueOf(inventoryItem.getValue().second));
        }

        @Override
        public int getItemCount() {
            return myNamedInventoryItemsList.size();
        }

        public class InventoryItemViewHolder extends RecyclerView.ViewHolder{
            protected Button vInventoryItemDeleteButton;
            protected TextView vInventoryItemNameTextView;
            protected EditText vInventoryItemQuantityEditText;

            public InventoryItemViewHolder(@NonNull View itemView) {
                super(itemView);
                vInventoryItemDeleteButton = (Button) itemView.findViewById(R.id.btn_inventory_item_delete_item);
                vInventoryItemNameTextView = (TextView) itemView.findViewById(R.id.tv_inventory_item_description);
                vInventoryItemQuantityEditText = (EditText) itemView.findViewById(R.id.et_inventory_item_quantity_field);
            }


        }

    }


    public void startAddItemActivity(View view){
        Intent intent = new Intent(this, ManualAddItemActivity.class);

        intent.putExtra(KEY_EXTRA_INVENTORY_ID, inventoryIdInUse);

        startActivity(intent);
    }

    private void startScannerActivity(int captureMode){
        Intent intent = new Intent(this, ScannerActivity.class);

        intent.putExtra(KEY_EXTRA_INVENTORY_ID, inventoryIdInUse);
        intent.putExtra(KEY_EXTRA_CAPTURE_MODE, captureMode);

        startActivity(intent);
    }

    public void loadInventoryData(){

        InventaFacilDbHelper internalDb = new InventaFacilDbHelper(this);

        /*HashMap<Long, Integer> testInventoryItems = new HashMap<>();
        testInventoryItems.put(Long.valueOf("5554454871"), 2 );
        testInventoryItems.put(Long.valueOf("5554454872"), 3 );
        testInventoryItems.put(Long.valueOf("5554454873"), 4 );
        testInventoryItems.put(Long.valueOf("5554454874"), 5 );
        testInventoryItems.put(Long.valueOf("5554454875"), 6 );


        internalDb.updateInventoryItems(this, inventoryIdInUse, testInventoryItems);*/

        inventoryInUse = internalDb.getInventoryFromId(this, inventoryIdInUse);
        inventoryItemsInUse = inventoryInUse.getInventoryItems();


        for (long barcode : inventoryItemsInUse.keySet()){
            String itemName = internalDb.getItemNameFromCatalog(this, barcode);
            if (itemName == null || itemName.equals("") ) continue;
            int itemQuantity = inventoryItemsInUse.get(barcode);
            inventoryItemsInUseWithNames.put(barcode, new Pair<String, Integer>(itemName,itemQuantity));
        }

        internalDb.close();
    }


}
