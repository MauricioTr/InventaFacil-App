package com.mtrevino.inventafacil.apiclient;

import android.accounts.NetworkErrorException;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.mtrevino.inventafacil.objects.Catalog;

import com.mtrevino.inventafacil.objects.Depot;
import com.mtrevino.inventafacil.objects.Inventory;
import com.mtrevino.inventafacil.utils.InventaFacilPrefManager;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import retrofit2.Call;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiController {
    //TODO: Remove hardcoded url
    static  private final String BASE_URL = "http://10.0.0.23:8088/";
    private final  Gson gson = new GsonBuilder().setLenient().create();
    private final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();



    public String loginToServer(String username, String password) {

        InventaFacilAPI inventaFacilAPI = retrofit.create(InventaFacilAPI.class);

        RequestBody usernameRequestBody = RequestBody.create(MediaType.parse("text/plain"), username);
        RequestBody passwordRequestBody = RequestBody.create(MediaType.parse("text/plain"), password);
        Call<String> call = inventaFacilAPI.login(usernameRequestBody, passwordRequestBody);

        try {
            Response<String> response = call.execute();

            if(response.isSuccessful()){
                return response.body();
            }else if (response.code() == 500){

            }
        }catch (IOException e){
            e.printStackTrace();

        }
        return null;
    }



    public ArrayList<Depot> getUserPermissions(Context currentContext) throws Exception{


        InventaFacilAPI inventaFacilAPI = retrofit.create(InventaFacilAPI.class);
        InventaFacilPrefManager prefManager = new InventaFacilPrefManager(currentContext);
        Call<ArrayList<Depot>> call = inventaFacilAPI.userPermissions(prefManager.getCraftedToken());

        Response<ArrayList<Depot>> response = call.execute();

        if (response.isSuccessful()){
            return response.body();
        }else {
            System.out.println(response.errorBody());
            throw statusExceptionCreator(response.code());
        }

    }



    public String queryForCurrentCatalogVersion(Context currentContext) throws Exception {

        InventaFacilAPI inventaFacilAPI = retrofit.create(InventaFacilAPI.class);
        InventaFacilPrefManager prefManager = new InventaFacilPrefManager(currentContext);

        Call<String> call = inventaFacilAPI.catalogVersion(prefManager.getCraftedToken());
        Response<String> response = call.execute();

        if (response.isSuccessful()){
            return response.body();
        }else {
            System.out.println(response.errorBody());
            throw statusExceptionCreator(response.code());
        }

    }


    public Catalog queryForCurrentCatalog(Context currentContext) throws Exception{

        InventaFacilAPI inventaFacilAPI = retrofit.create(InventaFacilAPI.class);
        InventaFacilPrefManager prefManager = new InventaFacilPrefManager(currentContext);

        Call<Catalog> call = inventaFacilAPI.downloadNewestCatalog(prefManager.getCraftedToken());

        Response<Catalog> response = call.execute();

        if (response.isSuccessful()){
            return response.body();
        }else {
            System.out.println(response.errorBody());
            throw statusExceptionCreator(response.code());
        }
    }




    public String uploadInventory(Context currentContext, Inventory inventoryToUpload) throws  Exception{


        InventaFacilAPI inventaFacilAPI = retrofit.create(InventaFacilAPI.class);
        InventaFacilPrefManager prefManager = new InventaFacilPrefManager(currentContext);

        Call<String> call = inventaFacilAPI.uploadInventory(prefManager.getCraftedToken(), inventoryToUpload);



        Response<String> response = call.execute();

        if(response.isSuccessful()){
            return response.body();
        }else {
            System.out.println(response.errorBody());
            throw statusExceptionCreator(response.code());
        }
    }

    public boolean isUserLoggedInOrOffline(Context currentContext){

        InventaFacilAPI inventaFacilAPI = retrofit.create(InventaFacilAPI.class);
        InventaFacilPrefManager prefManager = new InventaFacilPrefManager(currentContext);

        //If no token is present user is not logged in
        if (prefManager.isTokenPresent() == false) return false;

        Call<String> call = inventaFacilAPI.catalogVersion(prefManager.getCraftedToken());
        Response<String> response;

        try {
             response = call.execute();
        }catch (Exception e){
            return true;    //Any issues while performing the request is taken to mean user is offline
        }

        //If the server denies the request because of invalid authentication (Code 401)
        // that means the token is now invalid and the user is logged out
        if (response.isSuccessful() == false && response.code() == 401){
            prefManager.saveNewToken(null);
            return false;
        }

        return true;
    }

    private Exception statusExceptionCreator(int statusCode) {
        switch(statusCode){
            case 500: return new NetworkErrorException("Code 500 was returned, server is currently experiencing problems") ;

            case 401: return new NetworkErrorException("Code 401 received, server is unable to authorize using the current token");

            default: return new NetworkErrorException("Code " + statusCode + " received");
        }
    }

}
