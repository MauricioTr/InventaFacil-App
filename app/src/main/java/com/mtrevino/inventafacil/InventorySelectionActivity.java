package com.mtrevino.inventafacil;

import android.content.Intent;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.mtrevino.inventafacil.internaldb.InventaFacilDbHelper;
import com.mtrevino.inventafacil.objects.Inventory;

import java.util.ArrayList;

public class InventorySelectionActivity extends AppCompatActivity {

    private static final String KEY_EXTRA_INVENTORY_ID = "inventoryIdInUse";

    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    InventoryAdapter mInventoryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_selection);

        android.support.v7.widget.Toolbar myToolbar =  findViewById(R.id.tb_inventory_selection_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setTitle("Inventarios en curso:");

        mRecyclerView = findViewById(R.id.rv_inventory_selection_recycler);

        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(mLinearLayoutManager);


    }

    @Override
    protected void onResume() {
        super.onResume();

        mInventoryAdapter = new InventoryAdapter(getInventoryList());

        mRecyclerView.setAdapter(mInventoryAdapter);
    }

    private class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>{

        private ArrayList<Inventory> myInventoryList;

        public  InventoryAdapter(ArrayList<Inventory> inventoryList){
            this.myInventoryList = inventoryList;
        }

        @NonNull
        @Override
        public InventoryAdapter.InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            final View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.inventory_selection_item_view, viewGroup, false);

            return new InventoryViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull InventoryAdapter.InventoryViewHolder inventoryViewHolderViewHolder, int i) {
            final Inventory inventoryForView = myInventoryList.get(i);

            inventoryViewHolderViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = new Intent(getApplicationContext(), InventoryEditActivity.class);
                    intent.putExtra(KEY_EXTRA_INVENTORY_ID, inventoryForView.getInventoryId());
                    getApplicationContext().startActivity(intent);
                }
            });

            InventaFacilDbHelper internalDb = new InventaFacilDbHelper(getApplicationContext());
            Pair<String, String> sectionAndDepotNamePair = internalDb.getDepotAndSectionNameFromSectionId(getApplicationContext(), inventoryForView.getSectionId());

            inventoryViewHolderViewHolder.vInventoryNameText.setText(inventoryForView.getInventoryName());
            inventoryViewHolderViewHolder.vSectionNameText.setText(sectionAndDepotNamePair.first);
            inventoryViewHolderViewHolder.vDepotNameText.setText(sectionAndDepotNamePair.second);
        }

        @Override
        public int getItemCount() {
            return myInventoryList.size();
        }

        public class InventoryViewHolder extends RecyclerView.ViewHolder{
            protected TextView vInventoryNameText;
            protected TextView vDepotNameText;
            protected TextView vSectionNameText;

            public InventoryViewHolder(@NonNull View itemView) {
                super(itemView);
                vInventoryNameText = (TextView) itemView.findViewById(R.id.tv_inventory_selection_inventory_name);
                vDepotNameText = (TextView) itemView.findViewById(R.id.tv_inventory_selection_depot_name);
                vSectionNameText = (TextView) itemView.findViewById(R.id.tv_inventory_selection_section_name);
            }
        }

    }



    private ArrayList<Inventory> getInventoryList(){
        InventaFacilDbHelper internalDb = new InventaFacilDbHelper(this);

        return internalDb.getAllInventories(this);
    }

}
