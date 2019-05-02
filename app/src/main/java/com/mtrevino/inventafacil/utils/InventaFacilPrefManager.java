package com.mtrevino.inventafacil.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class InventaFacilPrefManager {
    private final String TOKEN_PREFERENCE_KEY = "currentToken";
    private final String USERNAME_PREFERENCE_KEY = "loggedInUsername";
    private final String CATALOG_VERSION_PREFERENCE_KEY = "currentCatalogVersion";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public InventaFacilPrefManager(Context givenContext){
        this.prefs = givenContext.getSharedPreferences("appPreferences", 0);
        this.editor = this.prefs.edit();
    }

    public boolean isTokenValid(String tokenToValidate){

        if (tokenToValidate.length() == 36) return true;

        return false;
    }

    public boolean isTokenPresent(){

        //If no token is saved return false
        if (prefs.getString(TOKEN_PREFERENCE_KEY, null) == null) return false;

        return true;
    }

    public void saveNewToken(String tokenToSave){
        if (tokenToSave == null){
            editor.remove(TOKEN_PREFERENCE_KEY);
            editor.commit();
            return;
        }

        editor.putString(TOKEN_PREFERENCE_KEY, tokenToSave);
        editor.commit();
    }

    public String getCraftedToken(){

        String simpleToken = prefs.getString(TOKEN_PREFERENCE_KEY, null);
        String craftedToken;
        if (simpleToken != null) {
            craftedToken = "Bearer " + simpleToken;
            return  craftedToken;
        }
         return "No token is currently saved";
    }

    public void saveNewUsername(String usernameToSave){
        if (usernameToSave == null){
            editor.remove(USERNAME_PREFERENCE_KEY);
            editor.commit();
            return;
        }

        editor.putString(USERNAME_PREFERENCE_KEY, usernameToSave);
        editor.commit();
    }

    public String getCurrentUsername(){

        String username = prefs.getString(USERNAME_PREFERENCE_KEY, "Default");

        return username;
    }

    public void saveNewCatalogVersion(String versionToSave){
        if (versionToSave == null){
            editor.remove(CATALOG_VERSION_PREFERENCE_KEY);
            editor.commit();
            return;
        }

        editor.putString(CATALOG_VERSION_PREFERENCE_KEY, versionToSave);
        editor.commit();
    }

    public String getCurrentCatalogVersion(){

        String currentCatalogVersion = prefs.getString(CATALOG_VERSION_PREFERENCE_KEY, null);

        return currentCatalogVersion;
    }

}
