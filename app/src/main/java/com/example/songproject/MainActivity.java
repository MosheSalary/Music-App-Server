package com.example.songproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.songproject.Model.UploadSong;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    // bro I refactored this code siuuuuuuuuuuuuuuuuuuuuuuuuuu ;) !
    ProgressBar progressBar;
    Uri audioUri;
    StorageReference mStoragef;
    StorageTask mUploadTask;
    DatabaseReference referenceSongs;
    MediaMetadataRetriever mediaMetadataRetriever;
    byte [] art;
    String title1, artist1, album_art1 = "" , durations1,songsCategorey;
    TextView title, artist, album, durations, dataa, textViewImage;
    ImageView album_art;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewImage = findViewById(R.id.textViedSongsFilesSelected);
        progressBar = findViewById(R.id.progressbar);
        title = findViewById(R.id.title);
        artist = findViewById(R.id.artist);
        durations = findViewById(R.id.durtion);
        album = findViewById(R.id.album);
        dataa = findViewById(R.id.dataa);
        album_art = findViewById(R.id.imageview);
        mediaMetadataRetriever = new MediaMetadataRetriever();
        referenceSongs = FirebaseDatabase.getInstance().getReference().child("songs");
        mStoragef = FirebaseStorage.getInstance().getReference().child("songs");
        Spinner spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);
        List<String> categories = new ArrayList<>();


        categories.add("Love Songs");
        categories.add("Cool Songs");
        categories.add("Party Songs");
        categories.add("Birthday Songs");
        categories.add("Special Songs");


        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this , android.R.layout.simple_spinner_item , categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        songsCategorey = adapterView.getItemAtPosition(i).toString();
        Toast.makeText(this, "Selected: "+songsCategorey, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}

    public void openAudioFiles (View v){
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("audio/*");
        startActivityForResult(i , 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101 && resultCode == RESULT_OK && data.getData() != null){
            audioUri = data.getData();
            String fileName = getFileName(audioUri);
            textViewImage.setText(fileName);
            mediaMetadataRetriever.setDataSource( this , audioUri);
            art = mediaMetadataRetriever.getEmbeddedPicture();
            Bitmap bitmap = BitmapFactory.decodeByteArray(art , 0 , art.length);
            album_art.setImageBitmap(bitmap);
            album.setText(mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_ALBUM));
            artist.setText(mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_ARTIST));
            dataa.setText(mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_GENRE));
            durations.setText(mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_DURATION));
            title.setText(mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_TITLE));
            artist1 = (mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_ARTIST));
            title1 = (mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_TITLE));
            durations1 = (mediaMetadataRetriever.extractMetadata(mediaMetadataRetriever.METADATA_KEY_DURATION));
        }
    }

    @SuppressLint("Range")
    private String getFileName(Uri uri){
        String result = null;
        if(uri.getScheme().equals("context")){
            Cursor cursor = getContentResolver().query(uri , null , null , null, null);
            try {
                if(cursor != null && cursor.moveToFirst()){
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally {
                cursor.close();
            }
        }
        if (result == null){
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if(cut != -1){
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public void uploadFileTofirebase (View v){
        if(textViewImage.equals("No file selected")){
            Toast.makeText(this, "please selecte an image !", Toast.LENGTH_SHORT).show();
        }else {
            uploadFile();
        }
    }

    private void uploadFile() {
        if(audioUri != null){
            Toast.makeText(this, "upload plese wait !", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference storageReference = mStoragef.child(System.currentTimeMillis()+"."+getFileExtension(audioUri));
            mUploadTask = storageReference.putFile(audioUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {

                        @Override
                        public void onSuccess(Uri uri) {
                            UploadSong uploadSong = new UploadSong(songsCategorey,title1,artist1,album_art1,durations1,uri.toString());
                            String UploadId = referenceSongs.push().getKey();
                            referenceSongs.child(UploadId).setValue(uploadSong);
                        }
                    });
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                    double progress = (100.0* snapshot.getBytesTransferred()/snapshot.getTotalByteCount());
                    progressBar.setProgress((int) progress);
                }
            });
        }else {
            Toast.makeText(this, "No file selected to uploads :(", Toast.LENGTH_SHORT).show();
        }
    }
    private String getFileExtension(Uri audioUri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(audioUri));
    }

    public void openAlbumUploadsActivity(View v){
        Intent in = new Intent(MainActivity.this, UploadAlbumActivity.class);
        startActivity(in);
    }
}