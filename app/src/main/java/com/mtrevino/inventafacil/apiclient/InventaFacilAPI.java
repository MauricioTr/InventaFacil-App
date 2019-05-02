package com.mtrevino.inventafacil.apiclient;


import com.mtrevino.inventafacil.objects.Catalog;
import com.mtrevino.inventafacil.objects.Depot;
import com.mtrevino.inventafacil.objects.Inventory;

import java.util.ArrayList;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;


public interface InventaFacilAPI {

    @Multipart
    @POST("public/users/login")
    Call<String> login(@Part("username") RequestBody username, @Part("password") RequestBody password);

    @GET("users/getuserpermissions")
    Call<ArrayList<Depot>> userPermissions(@Header("Authorization") String authorization);

    @GET("get-newest-catalogversion")
    Call<String> catalogVersion(@Header("Authorization") String authorization);


    @GET("downloadcatalog")
    Call<Catalog> downloadNewestCatalog(@Header("Authorization") String authorization);

    @POST("uploadinventory")
    Call<String> uploadInventory(@Header("Authorization") String authorization, @Body Inventory inventory);
}
