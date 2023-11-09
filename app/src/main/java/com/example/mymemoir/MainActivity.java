package com.example.mymemoir;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.mymemoir.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList <Memory> memoryArrayList;
    MemoryAdapter memoryAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        memoryArrayList = new ArrayList<>();


        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        memoryAdapter = new MemoryAdapter(memoryArrayList);
        binding.recyclerView.setAdapter(memoryAdapter);

        getData();

    }


    private void getData() {
            // recyclerView da veriyi başlığıyla gösterip tıklanıp girilir hale getirme aşamasının ilk kısmı gerekli verileri çekip
            //recyclerViewTextView da göstereceğim verileri gerekli alanlara ekliyorum..
        try {
            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Memories",MODE_PRIVATE,null);
            Cursor cursor = sqLiteDatabase.rawQuery("SELECT*FROM memories",null);
            int titleIx = cursor.getColumnIndex("memoryTitle");
            int idIx = cursor.getColumnIndex("id");
            int imageIx = cursor.getColumnIndex("image");


            while (cursor.moveToNext()) {

                String memoryTitle = cursor.getString(titleIx);
                int id = cursor.getInt(idIx);

                byte[] imageBytes = cursor.getBlob(imageIx);
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);


                Memory memory = new Memory(memoryTitle,id,imageBitmap);
                memoryArrayList.add(memory);
            }
            memoryAdapter.notifyDataSetChanged();

            cursor.close();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.memory_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addMemory) {
            Intent intent =  new Intent(MainActivity.this,MemoryDetailsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }



}