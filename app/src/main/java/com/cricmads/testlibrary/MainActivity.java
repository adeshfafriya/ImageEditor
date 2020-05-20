package com.cricmads.testlibrary;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.cricmads.testlibrary.databinding.ActivityMainBinding;
import com.theartofdev.edmodo.cropper.ImageAdapter;
import com.theartofdev.edmodo.cropper.SelectedImageActivity;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity  {
    ActivityMainBinding binding;
    private int PICK_FROM_CAMERA = 1;
    private int PICK_FROM_GALLERY = 2;
    private int GALLERY_ACTIVITY = 3;
    private Uri uriFromCamera;
    private ArrayList<Uri> uriResultList = new ArrayList<>();
    private ArrayList<String> uriListFromGallery = new ArrayList<>();
    private boolean isOnlyOne, isFromCamera;
    private MyAdapter adapter;
    private Long tsLong;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDialog();
            }
        });
        adapter = new MyAdapter(uriResultList, this);
        binding.recycler.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recycler.setAdapter(adapter);

    }

    private void startDialog() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.pick_dialog, null);
        builder.setView(dialogLayout);
        final AlertDialog dialog = builder.create();
        dialog.show();


        dialogLayout.findViewById(R.id.text_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tsLong = System.currentTimeMillis()/1000;
                String ts = tsLong.toString();
                dialog.dismiss();
                Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File file = new File(Environment.getExternalStorageDirectory(), ts+".png");
                Uri uri = FileProvider.getUriForFile(MainActivity.this, getApplicationContext().getPackageName() + ".provider", file);
                takePicture.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                startActivityForResult(takePicture, PICK_FROM_CAMERA);
            }
        });
        dialogLayout.findViewById(R.id.text_gallery).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickPhoto.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                uriListFromGallery.clear();
                startActivityForResult(pickPhoto, PICK_FROM_GALLERY);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FROM_CAMERA && resultCode == RESULT_OK){
            isOnlyOne = true;
            isFromCamera = true;
            File file = new File(Environment.getExternalStorageDirectory(), tsLong.toString()+".png");
            uriFromCamera = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".provider", file);
            Intent intent = new Intent(MainActivity.this, SelectedImageActivity.class);
            uriListFromGallery.add(uriFromCamera.toString());
            intent.putExtra("uri", uriListFromGallery);
            startActivityForResult(intent, GALLERY_ACTIVITY);
        }
        if (requestCode == PICK_FROM_GALLERY && resultCode == RESULT_OK && data != null && data.getClipData() != null){
            isFromCamera = false;
            ClipData mClipData = data.getClipData();
            int count = mClipData.getItemCount();
            for (int i = 0; i<count;++i){
                ClipData.Item item = mClipData.getItemAt(i);
                Uri uri = item.getUri();
                uriListFromGallery.add(uri.toString());
            }
            isOnlyOne = count <= 1;
            Intent intent = new Intent(MainActivity.this, SelectedImageActivity.class);
            intent.putExtra("uri", uriListFromGallery);
            startActivityForResult(intent, GALLERY_ACTIVITY);
        }
        if (requestCode == GALLERY_ACTIVITY && resultCode == RESULT_OK && data != null){

//            ArrayList<String> stringArrayList = data.getStringArrayListExtra("resultUri");
//            if (stringArrayList != null) {
//                for (String uriString : stringArrayList){
//                    uriResultList.add(Uri.parse(uriString));
//                    adapter.notifyDataSetChanged();
//                }
//            }
            ClipData clipData = data.getParcelableExtra("resultUriClipData");
            Log.e("URIdata", clipData.getItemCount()+ "");
            for (int i = 0; i<clipData.getItemCount(); ++i){
                ClipData.Item item = clipData.getItemAt(i);
                Uri uri = item.getUri();
                uriResultList.add(uri);
            }
            adapter.notifyDataSetChanged();
        }
    }

}
