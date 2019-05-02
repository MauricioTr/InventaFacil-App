package com.mtrevino.inventafacil;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.mtrevino.inventafacil.apiclient.ApiController;
import com.mtrevino.inventafacil.internaldb.InventaFacilDbHelper;
import com.mtrevino.inventafacil.objects.Catalog;
import com.mtrevino.inventafacil.objects.Depot;
import com.mtrevino.inventafacil.utils.InventaFacilGeneralUtils;
import com.mtrevino.inventafacil.utils.InventaFacilPrefManager;

import java.util.ArrayList;

public class MainMenuActivity extends AppCompatActivity {

    private static final int CHECK_FOR_CATALOG_UPDATES_LOADER = 28;
    private static final int DOWNLOAD_CATALOG_LOADER = 29;

    private Button mCatalogButton;
    private Button mContinueInventoryButton;
    private Button mNewInventoryButton;

    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        android.support.v7.widget.Toolbar myToolbar =  findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        InventaFacilPrefManager prefManager = new InventaFacilPrefManager(this);
        String currentUsername = prefManager.getCurrentUsername();

        getSupportActionBar().setTitle("Signed in as: " + currentUsername);


        mCatalogButton = (Button) findViewById(R.id.btn_main_menu_catalog_display);
        mContinueInventoryButton = (Button) findViewById(R.id.btn_main_menu_continue_inventory);
        mNewInventoryButton = (Button) findViewById(R.id.btn_main_menu_new_inventory);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_main_menu_loading_indicator);

        displayCurrentCatalogVersion();
        checkForCatalogUpdates();



        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

    }

    public void displayCurrentCatalogVersion(){
        InventaFacilPrefManager prefManager = new InventaFacilPrefManager(this);
        String currentCatalogVersion = prefManager.getCurrentCatalogVersion();

        mCatalogButton.setText("Versión actual del catálogo: " + currentCatalogVersion);
        mCatalogButton.setClickable(false);
    }

    public void displayCatalogUpdateAvailable(){

        mCatalogButton.setText("Actualización de catálogo disponible");
        mCatalogButton.setClickable(true);
    }

    public void checkForCatalogUpdates(){
        InventaFacilGeneralUtils generalUtils = new InventaFacilGeneralUtils();
        LoaderManager loaderManager = getLoaderManager();

        if (generalUtils.isDeviceConnectedToNetwork(this) ==  false) return;


        Loader<String> checkForCatalogUpdatesLoader = loaderManager.getLoader(CHECK_FOR_CATALOG_UPDATES_LOADER);
        Bundle emptyBundle = new Bundle();

        // ) If the Loader was null, initialize it. Else, restart it.
        if (checkForCatalogUpdatesLoader == null) {
            loaderManager.initLoader(CHECK_FOR_CATALOG_UPDATES_LOADER, emptyBundle, checkForCatalogUpdateTaskLoader);
        } else {
            loaderManager.restartLoader(CHECK_FOR_CATALOG_UPDATES_LOADER, emptyBundle, checkForCatalogUpdateTaskLoader);
        }

    }

    public void navigateToInventoryCreation(View view){
        Intent intent = new Intent(this, InventoryCreationActivity.class);

        startActivity(intent);
    }

    public void navigateToInventorySelection(View view){
        Intent intent = new Intent(this, InventorySelectionActivity.class);

        startActivity(intent);
    }

    public void downloadCatalog(View view){
        InventaFacilGeneralUtils generalUtils = new InventaFacilGeneralUtils();
        LoaderManager loaderManager = getLoaderManager();

        if (generalUtils.isDeviceConnectedToNetwork(this) ==  false) return;

        //Stop user interaction and show loading icon
        mLoadingIndicator.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        Loader<String> downloadCatalogLoader = loaderManager.getLoader(DOWNLOAD_CATALOG_LOADER);
        Bundle emptyBundle = new Bundle();

        //  If the Loader was null, initialize it. Else, restart it.
        if (downloadCatalogLoader == null) {
            loaderManager.initLoader(DOWNLOAD_CATALOG_LOADER, emptyBundle, downloadCatalogTaskLoader);
        } else {
            loaderManager.restartLoader(DOWNLOAD_CATALOG_LOADER, emptyBundle, downloadCatalogTaskLoader);
        }

    }

    private LoaderManager.LoaderCallbacks<String> checkForCatalogUpdateTaskLoader = new LoaderManager.LoaderCallbacks<String>() {
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
                    try {
                        String currentCatalogVersion = apiController.queryForCurrentCatalogVersion(getApplicationContext());
                        return currentCatalogVersion;
                    }catch (Exception e){
                        e.printStackTrace();
                        return null;
                    }
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            // If the result from the network request is null or not a number exit the method
            if (data == null || data.matches("[0-9]+") == false) return;

            InventaFacilPrefManager prefManager = new InventaFacilPrefManager(getApplicationContext());
            //TODO: Remove this test code
            //int deviceCatalogVersion = Integer.parseInt(prefManager.getCurrentCatalogVersion());
            int deviceCatalogVersion = 0;

            int newestCatalogVersion = Integer.parseInt(data);


            //If a more recent catalog version is available call helper method to change the view to reflect this
            if (deviceCatalogVersion < newestCatalogVersion){
                displayCatalogUpdateAvailable();
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<Catalog> downloadCatalogTaskLoader = new LoaderManager.LoaderCallbacks<Catalog>() {
        @Override
        public Loader<Catalog> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<Catalog>(getApplicationContext()) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();

                    forceLoad();
                }

                @Override
                public Catalog loadInBackground() {
                    ApiController apiController = new ApiController();
                    try {
                        Catalog currentCatalog = apiController.queryForCurrentCatalog(getApplicationContext());
                        return currentCatalog;
                    }catch (Exception e){
                        e.printStackTrace();
                        return null;
                    }
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Catalog> loader, Catalog downloadedCatalog) {
            if (downloadedCatalog == null) {
                // Remove loading icon and restore user interaction
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                return;
            }

            InventaFacilDbHelper internalDb = new InventaFacilDbHelper(getApplicationContext());

            boolean wasCatalogSaved = internalDb.updateCatalog(getApplicationContext(), downloadedCatalog);

            if (wasCatalogSaved == false) {

                Toast.makeText(getApplicationContext(),"Failed to save catalog", Toast.LENGTH_LONG);
            }else {

                // Save the version of the catalog received through the preferenceManager helper
                InventaFacilPrefManager prefManager = new InventaFacilPrefManager(getApplicationContext());
                String catalogVersion = String.valueOf(downloadedCatalog.getCatalogVersion());
                prefManager.saveNewCatalogVersion(catalogVersion);

                displayCurrentCatalogVersion();
            }
            // Remove loading icon and restore user interaction
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        }
        @Override
        public void onLoaderReset(Loader<Catalog> loader) {

        }
    };

}
