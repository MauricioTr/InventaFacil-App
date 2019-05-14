package com.mtrevino.inventafacil;

import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Intent;
import android.content.Loader;
import android.icu.text.UnicodeSetSpanner;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.mtrevino.inventafacil.apiclient.ApiController;
import com.mtrevino.inventafacil.internaldb.InventaFacilDbHelper;
import com.mtrevino.inventafacil.objects.Catalog;
import com.mtrevino.inventafacil.objects.Depot;
import com.mtrevino.inventafacil.utils.InventaFacilPrefManager;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {



    private static final int INITIAL_LOGIN_LOADER = 18;
    private static final int ATTEMPT_LOGIN_SKIP_LOADER = 19;
    private static final int DOWNLOAD_CATALOG_LOADER = 20;
    private static final int DOWNLOAD_USER_PERMISSIONS_LOADER = 21;


    private static final String USERNAME_BUNDLE_KEY = "usernamekey";
    private static final String PASSWORD_BUNDLE_KEY = "passwordkey";

    private EditText mUsernameField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Initialize the member variables
        mUsernameField = (EditText) findViewById(R.id.et_username);
        mPasswordField = (EditText) findViewById(R.id.et_password);


        attemptLoginSkip(getCurrentFocus());
    }


    public void attemptLoginSkip(View view){
        LoaderManager loaderManager = getLoaderManager();

        // ) Get our Loader by calling getLoader and passing the ID we specified
        Loader<String> loginLoader = loaderManager.getLoader(ATTEMPT_LOGIN_SKIP_LOADER);
        Bundle emptyBundle = new Bundle();
        // ) If the Loader was null, initialize it. Else, restart it.
        if (loginLoader == null) {
            loaderManager.initLoader(ATTEMPT_LOGIN_SKIP_LOADER, emptyBundle, loginSkipTaskLoader);
        } else {
            loaderManager.restartLoader(ATTEMPT_LOGIN_SKIP_LOADER, emptyBundle, loginSkipTaskLoader);
        }
    }

    public void logIn(View view){


        String usernameEntered = mUsernameField.getText().toString(); //Gets username entered
        String passwordEntered = mPasswordField.getText().toString();    //Gets password entered

        if(TextUtils.isEmpty(usernameEntered) || TextUtils.isEmpty(passwordEntered)){
            Toast.makeText(this, "Please enter your username and password", Toast.LENGTH_LONG).show();
            return;
        }

        Bundle loginBundle = new Bundle();
        loginBundle.putString(USERNAME_BUNDLE_KEY, usernameEntered);
        loginBundle.putString(PASSWORD_BUNDLE_KEY, passwordEntered);

        LoaderManager loaderManager = getLoaderManager();

        // ) Get our Loader by calling getLoader and passing the ID we specified
        Loader<String> loginLoader = loaderManager.getLoader(INITIAL_LOGIN_LOADER);
        // ) If the Loader was null, initialize it. Else, restart it.
        if (loginLoader == null) {
            loaderManager.initLoader(INITIAL_LOGIN_LOADER, loginBundle, loginTaskLoader);
        } else {
            loaderManager.restartLoader(INITIAL_LOGIN_LOADER, loginBundle, loginTaskLoader);
        }
    }

    public void downloadCatalog(View view){
        LoaderManager loaderManager = getLoaderManager();

        // ) Get our Loader by calling getLoader and passing the ID we specified
        Loader<String> loginLoader = loaderManager.getLoader(DOWNLOAD_CATALOG_LOADER);
        Bundle emptyBundle = new Bundle();
        // ) If the Loader was null, initialize it. Else, restart it.
        if (loginLoader == null) {
            loaderManager.initLoader(DOWNLOAD_CATALOG_LOADER, emptyBundle, downloadCatalogTaskLoader);
        } else {
            loaderManager.restartLoader(DOWNLOAD_CATALOG_LOADER, emptyBundle, downloadCatalogTaskLoader);
        }
    }

    public void downloadUserPermissions(View view){
        LoaderManager loaderManager = getLoaderManager();

        // ) Get our Loader by calling getLoader and passing the ID we specified
        Loader<ArrayList<Depot>> dlUserPermissionLoader = loaderManager.getLoader(DOWNLOAD_USER_PERMISSIONS_LOADER);
        Bundle emptyBundle = new Bundle();
        // ) If the Loader was null, initialize it. Else, restart it.
        if (dlUserPermissionLoader == null) {
            loaderManager.initLoader(DOWNLOAD_USER_PERMISSIONS_LOADER, emptyBundle, downloadUserPermissionsTaskLoader);
        } else {
            loaderManager.restartLoader(DOWNLOAD_USER_PERMISSIONS_LOADER, emptyBundle, downloadUserPermissionsTaskLoader);
        }
    }

    private LoaderManager.LoaderCallbacks<Boolean> loginSkipTaskLoader = new LoaderManager.LoaderCallbacks<Boolean>() {
        @Override
        public Loader<Boolean> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<Boolean>(getApplicationContext()) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();

                    forceLoad();
                }

                @Override
                public Boolean loadInBackground() {
                    ApiController apiController = new ApiController();


                    boolean isUserLoggedInOrOffline = apiController.isUserLoggedInOrOffline(getApplicationContext());


                    return isUserLoggedInOrOffline;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<Boolean> loader, Boolean isUserLoggedInOrOffline) {


            InventaFacilPrefManager prefManager = new InventaFacilPrefManager(getApplicationContext());
            InventaFacilDbHelper internalDb = new InventaFacilDbHelper(getApplicationContext());

            boolean isInternalCatalogEmpty = internalDb.isCatalogEmpty(getApplicationContext());

            if (isUserLoggedInOrOffline && isInternalCatalogEmpty == false) {

                Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);

                startActivity(intent);
                finish();
            }

        }

        @Override
        public void onLoaderReset(Loader<Boolean> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<String> loginTaskLoader = new LoaderManager.LoaderCallbacks<String>() {
        @Override
        public Loader<String> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<String>(getApplicationContext()) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    if (args == null) return; //If no arguments are passed just end the task
                    forceLoad();
                }

                @Override
                public String loadInBackground() {
                    ApiController apiController = new ApiController();
                    String usernameString = args.getString(USERNAME_BUNDLE_KEY);
                    String passwordString = args.getString(PASSWORD_BUNDLE_KEY);

                    String receivedToken = apiController.loginToServer(usernameString, passwordString);


                    return receivedToken;
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {


            InventaFacilPrefManager prefManager = new InventaFacilPrefManager(getApplicationContext());

            //TODO: Add behavior for when login is not successful
            if (prefManager.isTokenValid(data) == false) return;

            //Store the received token and username in a preferences file
            prefManager.saveNewToken(data);
            prefManager.saveNewUsername(mUsernameField.getText().toString());

            InventaFacilDbHelper internalDb = new InventaFacilDbHelper(getApplicationContext());

            //check if the internal catalog is empty, if so start a task to download the catalog
            if (internalDb.isCatalogEmpty(getApplicationContext())){
                downloadCatalog(getCurrentFocus());
                return;
            }

            downloadUserPermissions(getCurrentFocus());

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
            if (downloadedCatalog != null) {

                InventaFacilDbHelper internalDb = new InventaFacilDbHelper(getApplicationContext());

                boolean wasCatalogSaved = internalDb.updateCatalog(getApplicationContext(), downloadedCatalog);

                if (wasCatalogSaved == false) return;

                // Save the version of the catalog received through the preferenceManager helper
                InventaFacilPrefManager prefManager = new InventaFacilPrefManager(getApplicationContext());
                String catalogVersion = String.valueOf(downloadedCatalog.getCatalogVersion());
                prefManager.saveNewCatalogVersion(catalogVersion);

                downloadUserPermissions(getCurrentFocus());

            }
        }
        @Override
        public void onLoaderReset(Loader<Catalog> loader) {

        }
    };

    private LoaderManager.LoaderCallbacks<ArrayList<Depot>> downloadUserPermissionsTaskLoader = new LoaderManager.LoaderCallbacks<ArrayList<Depot>>() {
        @Override
        public Loader<ArrayList<Depot>> onCreateLoader(int id, final Bundle args) {
            return new AsyncTaskLoader<ArrayList<Depot>>(getApplicationContext()) {

                @Override
                protected void onStartLoading() {
                    super.onStartLoading();

                    forceLoad();
                }

                @Override
                public ArrayList<Depot> loadInBackground() {
                    ApiController apiController = new ApiController();
                    try {
                        ArrayList<Depot> userPermissions = apiController.getUserPermissions(getApplicationContext());
                        return userPermissions;
                    }catch (Exception e){
                        e.printStackTrace();
                        return null;
                    }
                }
            };
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<Depot>> loader, ArrayList<Depot> data) {
            if (data == null) {
                Toast.makeText(getApplicationContext(), "Error downloading user permission, please try again later", Toast.LENGTH_LONG);

                InventaFacilPrefManager prefManager = new InventaFacilPrefManager(getApplicationContext());
                prefManager.saveNewToken(null); //Invalidate the result of the login as permissions could not be downloaded

                return;
            }

            InventaFacilDbHelper internalDb = new InventaFacilDbHelper(getApplicationContext());
            internalDb.saveUserPermissions(getApplicationContext(), data);

            Intent intent = new Intent(getApplicationContext(), MainMenuActivity.class);
            startActivity(intent);
            finish();
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<Depot>> loader) {

        }
    };

}
