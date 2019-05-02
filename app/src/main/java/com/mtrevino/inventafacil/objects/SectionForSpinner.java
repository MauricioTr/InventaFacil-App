package com.mtrevino.inventafacil.objects;

import android.support.annotation.NonNull;

public class SectionForSpinner {

    private int sectionId;
    private String sectionName;

    public SectionForSpinner(int sectionId, String sectionName) {
        this.sectionId = sectionId;
        this.sectionName = sectionName;
    }

    public int getSectionId() {
        return sectionId;
    }

    public void setSectionId(int sectionId) {
        this.sectionId = sectionId;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    @NonNull
    @Override
    public String toString() {
        return sectionName;
    }
}
