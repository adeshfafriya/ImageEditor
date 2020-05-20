package com.theartofdev.edmodo.cropper;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private ArrayList<Uri> resultList;
    private Context context;
    private ArrayList<Boolean> isSelectedList;
    private ImageAdapterInterface listener;

    ImageAdapter(ArrayList<Uri> resultList, Context context, ArrayList<Boolean> isSelectedList, ImageAdapterInterface listener) {
        this.resultList = resultList;
        this.context = context;
        this.isSelectedList = isSelectedList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_small_image, parent, false);
        return new ImageViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, final int position) {
        Uri uri = resultList.get(position);
        Glide.with(context).load(uri).into(holder.imageView);

        if (isSelectedList.get(position))
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.eazyPrimary));
        else
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(R.color.lightgrey));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.setPager(position);
            }
        });

    }

    @Override
    public int getItemCount() {
        return resultList.size();
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private CardView cardView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.card_image);
            cardView = itemView.findViewById(R.id.card_view);
        }
    }

    public interface ImageAdapterInterface {
        void setPager(int position);
    }
}
