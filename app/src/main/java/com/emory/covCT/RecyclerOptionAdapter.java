package com.emory.covCT;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
            holder.title.setText(option.getDesc());
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
