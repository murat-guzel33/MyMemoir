package com.example.mymemoir;

import android.graphics.Bitmap;

public class Memory {

    String memoryTitle;
    int id;
    Bitmap imageBitmap;



    public Memory(String memoryTitle, int id, Bitmap imageBitmap) {
        this.memoryTitle = memoryTitle;
        this.id = id;
        this.imageBitmap = imageBitmap;

    }
}
