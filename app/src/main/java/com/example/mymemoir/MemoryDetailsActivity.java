package com.example.mymemoir;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mymemoir.databinding.ActivityMemoryDetailsBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class MemoryDetailsActivity extends AppCompatActivity {

        private ActivityMemoryDetailsBinding binding;

        ActivityResultLauncher <Intent> activityResultLauncher;
        ActivityResultLauncher <String> permissionLauncher;
        Bitmap selectedImage;
        SQLiteDatabase database;






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemoryDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();

        database = this.openOrCreateDatabase("Memories",MODE_PRIVATE,null);

        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.equals("new")) {

            //new memory
            // kullanıcı yeni bir veri kaydetmek istiyor veri girişi olacak..
            binding.memoryText.setText("");
            binding.memoryTitle.setText("");
            binding.saveButton.setVisibility(View.VISIBLE);
            binding.imageView.setImageResource(R.drawable.image);

        }   else {
            // old
            // kullanıcı eskiden kaydettiği verileri görmek istiyor cursorla verileri cekip gerekli alanları doldurucam..
            int memoryId = intent.getIntExtra("memoryId",0);
            binding.saveButton.setVisibility(View.INVISIBLE);
            binding.memoryText.setEnabled(false);
            // Metin rengini ve alfa (saydamlık) değerini ayarla
            binding.memoryText.setTextColor(Color.BLACK); // Metin rengini siyah olarak ayarla
            binding.memoryText.setAlpha(1.0f); // Alfa (saydamlık) değerini 1.0 (tamamen opak) olarak ayarla
            binding.memoryTitle.setEnabled(false);
            binding.memoryTitle.setTextColor(Color.BLACK);
            binding.memoryTitle.setAlpha(1.0f);
            binding.imageView.setEnabled(false);

            try {
                Cursor cursor = database.rawQuery("SELECT*FROM memories WHERE id = ? ", new String[] {String.valueOf(memoryId)});
                int memoryTitleIx = cursor.getColumnIndex("memoryTitle");
                int memoryTextIx = cursor.getColumnIndex("memoryText");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {
                    binding.memoryTitle.setText(cursor.getString(memoryTitleIx));
                    binding.memoryText.setText(cursor.getString(memoryTextIx));
                    byte [] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0, bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }


                cursor.close();


            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void save(View view) {


        String memoryTitle = binding.memoryTitle.getText().toString();
        String userMemoryText = binding.memoryText.getText().toString();
        int maxCharacters = 100; // Metni kaç karakterle sınırlamak istediğinizi belirleyin
        if (userMemoryText.length() > maxCharacters) {

            String truncatedText = userMemoryText.substring(0,maxCharacters);
            binding.memoryText.setText(truncatedText);
        } else {
            binding.memoryText.setText(userMemoryText);
        }

        Bitmap smallImage = makeSmallerImage(selectedImage,300);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,100,outputStream);
        byte[] byteArray = outputStream.toByteArray();

       try {
            //veri tabanı oluştruyorum verileri kaydedicem..
           database.execSQL("CREATE TABLE IF NOT EXISTS memories (id INTEGER PRIMARY KEY,memoryTitle VARCHAR,memoryText VARCHAR,image BLOB)");
           String sqlString =  "INSERT INTO memories (memoryTitle,memoryText,image) VALUES (?,?,?)";
           SQLiteStatement sqLiteStatement =database.compileStatement(sqlString);
           sqLiteStatement.bindString(1,memoryTitle);
           sqLiteStatement.bindString(2,userMemoryText);
           sqLiteStatement.bindBlob(3,byteArray);
           sqLiteStatement.execute();

       }catch (Exception e) {
        e.printStackTrace();
       }

       Intent intent = new Intent(MemoryDetailsActivity.this,MainActivity.class);
       intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
       startActivity(intent);


    }

        public Bitmap makeSmallerImage (Bitmap image, int maximumSize) {

        int widht = image.getWidth();
        int height = image.getHeight();
        float bitmapRatio =(float) widht/(float) height;
        if (bitmapRatio > 1) {
            //landscape image
            widht = maximumSize;
            height = (int) (widht/bitmapRatio);
        } else {
            //portrait image
            height = maximumSize;
            widht = (int) (height*bitmapRatio);
        }

            return image.createScaledBitmap(image,widht,height,true);


        }

    public void selectImage(View view) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 33++ ---> READ_MEDİA_IMAGES

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
                        }
                    }).show();


                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

                }

            } else {
                //gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
        }


        } else {
            //Android 32---> READ_EXTARNAL_STORGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                        }
                    }).show();
                    //request permission

                } else {
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                }

            } else {
                //gallery
                Intent intentToGallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }



    }

    private void registerLauncher () {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Intent intentFromResult =result.getData();
            if (intentFromResult != null) {
                Uri imageData =intentFromResult.getData();
                //binding.imageView.setImageURI(imageData);

                try {
                    if (Build.VERSION.SDK_INT >= 28) {

                        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageData);
                        selectedImage = ImageDecoder.decodeBitmap(source);
                        binding.imageView.setImageBitmap(selectedImage);
                    } else {
                        selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(),imageData);
                        binding.imageView.setImageBitmap(selectedImage);

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
             }
              }
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if(result) {
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                } else {
                        //permission denied
                    Toast.makeText(MemoryDetailsActivity.this,"Permission needed!",Toast.LENGTH_LONG).show();

                }
            }
        });
    }

}