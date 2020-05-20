package com.theartofdev.edmodo.cropper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Objects;

public class SelectedImageActivity extends AppCompatActivity implements ImageAdapter.ImageAdapterInterface {
    private WrappingViewPager pager;
    private ArrayList<Uri> uriArrayList = new ArrayList<>();
    private ArrayList<Boolean> isSelectedList = new ArrayList<>();
    private ImagePagerAdapter adapter;
    private ImageAdapter smallImageAdapter;
    private int imageToEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_image);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        pager = findViewById(R.id.pager);
        TextView doneButton = findViewById(R.id.done_button);
        RecyclerView smallImageRecycler = findViewById(R.id.small_recycler);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        ArrayList<String> arrayList = getIntent().getStringArrayListExtra("uri");
        assert arrayList != null;
        for (String uriString : arrayList) {
            uriArrayList.add(Uri.parse(uriString));
            isSelectedList.add(false);
        }
        isSelectedList.set(0, true);

        if (uriArrayList.size()<=1) {
            smallImageRecycler.setVisibility(View.GONE);
            Objects.requireNonNull(getSupportActionBar()).setTitle("Edit Image");
        } else Objects.requireNonNull(getSupportActionBar()).setTitle("Edit Images");

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendResultBack(uriArrayList);
            }
        });
        findViewById(R.id.edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = pager.getCurrentItem();
                launchImageCrop(uriArrayList.get(pos), pos);
            }
        });
        smallImageAdapter = new ImageAdapter(uriArrayList, this, isSelectedList, this);
        smallImageRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        smallImageRecycler.setAdapter(smallImageAdapter);
        adapter = new ImagePagerAdapter(uriArrayList, this);
        pager.setAdapter(adapter);
        setUpPagerListener();

    }

    private void setUpPagerListener() {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeBottomImageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void changeBottomImageSelected(int position) {
        for (int i = 0; i < isSelectedList.size(); ++i) {
            if (i == position)
                isSelectedList.set(i, true);
            else isSelectedList.set(i, false);
        }
        smallImageAdapter.notifyDataSetChanged();
    }

    private void sendResultBack(ArrayList<Uri> uriList) {
        ClipData clipData = ClipData.newUri(getContentResolver(), "Edited images clip data", uriList.get(0));
        if (uriList.size()>1){
            for (int i = 1; i< uriList.size(); i++){
                ClipData.Item item = new ClipData.Item(uriList.get(i));
                clipData.addItem(item);
            }
        }
//        ArrayList<String> resultList = new ArrayList<>();
//        for (Uri uri : uriList) {
//            resultList.add(uri.toString());
//        }
        Intent intent = new Intent();
        intent.putExtra("resultUriClipData", clipData);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void setPager(int position) {
        pager.setCurrentItem(position);
        changeBottomImageSelected(position);
    }

    public class ImagePagerAdapter extends PagerAdapter {
        private ArrayList<Uri> uriList;
        private Context context;


        ImagePagerAdapter(ArrayList<Uri> uriArrayList, Context context) {
            this.uriList = uriArrayList;
            this.context = context;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            final View pageLayout = getLayoutInflater().inflate(R.layout.pager_item, container, false);
            ImageView imageView = pageLayout.findViewById(R.id.imageView);
            Glide.with(context).load(uriList.get(position)).into(imageView);
            container.addView(pageLayout);
            return pageLayout;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return uriList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return (view == object);
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    private void launchImageCrop(Uri uri, int position) {
        imageToEdit = position;
        CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle("Editor").start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (result != null) {
                    Uri resultUri = result.getUri();
                    uriArrayList.set(imageToEdit, resultUri);
                    adapter.notifyDataSetChanged();
                    smallImageAdapter.notifyDataSetChanged();
                    pager.setCurrentItem(imageToEdit);
                }
            }
        }
    }

    protected void setResultCancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResultCancel();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResultCancel();
    }

}
