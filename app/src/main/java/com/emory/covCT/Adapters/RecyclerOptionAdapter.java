package com.emory.covCT.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emory.covCT.Image_Upload;
import com.emory.covCT.OptionModal;
import com.emory.covCT.R;

import java.util.ArrayList;

public class RecyclerOptionAdapter extends RecyclerView.Adapter<RecyclerOptionAdapter.Viewholder> {

    private Context context;
    private ArrayList<OptionModal> optionModalArrayList;


    public RecyclerOptionAdapter(Context context, ArrayList<OptionModal> optionModalArrayList) {
        this.context = context;
        this.optionModalArrayList = optionModalArrayList;
    }

    @NonNull
    @Override
    public RecyclerOptionAdapter.Viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_option, parent, false);
        return new Viewholder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerOptionAdapter.Viewholder holder, int position) {
            OptionModal option = optionModalArrayList.get(position);
            holder.image.setImageResource(option.getImage());
            holder.title.setText(option.getTitle());
            holder.desc.setText(option.getDesc());
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                   context.startActivity(new Intent(context, Image_Upload.class));
                }
            });
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, Image_Upload.class));
            }
        });
        holder.desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, Image_Upload.class));
            }
        });
    }

    @Override
    public int getItemCount() {
        return optionModalArrayList.size();
    }


    public class Viewholder extends RecyclerView.ViewHolder {
        private ImageView image;
        private TextView title,desc;

        public Viewholder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.option_image);
            title = itemView.findViewById(R.id.option_text1);
            desc = itemView.findViewById(R.id.option_text2);
        }
    }
}
