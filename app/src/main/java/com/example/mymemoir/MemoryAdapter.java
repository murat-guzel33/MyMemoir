package com.example.mymemoir;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mymemoir.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.MemoryHolder> {

    ArrayList <Memory> memoryArrayList;

    public MemoryAdapter(ArrayList<Memory> memoryArrayList) {
        this.memoryArrayList = memoryArrayList;
    }

    @NonNull
    @Override
    public MemoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new MemoryHolder(recyclerRowBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull MemoryHolder holder, int position) {
        holder.binding.recyclerViewImageView.setImageBitmap(memoryArrayList.get(position).imageBitmap);
        holder.binding.recyclerViewTextView.setText(memoryArrayList.get(position).memoryTitle);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),MemoryDetailsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("memoryId",memoryArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return memoryArrayList.size();
    }

    public class MemoryHolder extends RecyclerView.ViewHolder {

        private RecyclerRowBinding binding;
        public MemoryHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

        }
    }
}
