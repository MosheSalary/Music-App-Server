package com.example.songproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.songproject.Model.Constants;
import com.example.songproject.Model.Upload;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadAlbumActivity extends AppCompatActivity implements View.OnClickListener {
    // did a refactor wooooooooooooooooooooo siuuuuuuuuu :) !!!
    private Button buttonChoose;
    private Button buttonUpload;
    private EditText editTextName;
    private ImageView imageView;
    private String songsCategory;
    private static final int PICK_IMAGE_REQUEST = 234;
    private Uri filePath;
    private StorageReference storageReference;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_album);

        buttonChoose = findViewById(R.id.buttonChoose);
        buttonUpload = findViewById(R.id.buttonupload);
        editTextName = findViewById(R.id.edit_text);
        imageView = findViewById(R.id.imageview);

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.DATABASE_PATH_UPLOADS);
        Spinner spinner = findViewById(R.id.spinner);

        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);

        List<String> categories = new ArrayList<>();
        categories.add("Love Songs");
        categories.add("Cool Songs");
        categories.add("Party Songs");
        categories.add("Birthday Songs");
        categories.add("Special Songs");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                songsCategory = adapterView.getItemAtPosition(i).toString();
                Toast.makeText(UploadAlbumActivity.this, "Selected: " + songsCategory, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == buttonChoose) {
            showFileChooser();
        } else if (view == buttonUpload) {
            uploadFile();
        }
    }

    private void uploadFile() {
        if (filePath != null) {
            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference storageReference = this.storageReference.child(Constants.STORAGE_PATH_UPLOADS + System.currentTimeMillis() + "." + getFileExtension(filePath));
            storageReference.putFile(filePath)
                    .addOnSuccessListener(taskSnapshot -> storageReference.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String url = uri.toString();
                                Upload upload = new Upload(editTextName.getText().toString().trim(), url, songsCategory);
                                String uploadId = databaseReference.push().getKey();
                                databaseReference.child(uploadId).setValue(upload);
                                progressDialog.dismiss();
                                Toast.makeText(UploadAlbumActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(UploadAlbumActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    })
                    .addOnProgressListener(snapshot -> {
                        double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                        progressDialog.setMessage("Uploaded " + ((int) progress) + "%...");
                    });
        } else {
            Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }
}
