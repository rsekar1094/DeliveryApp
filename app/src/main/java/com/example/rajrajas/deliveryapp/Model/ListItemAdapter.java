package com.example.rajrajas.deliveryapp.Model;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rajrajas.deliveryapp.Activity.ViewActivity;
import com.example.rajrajas.deliveryapp.R;
import com.example.rajrajas.deliveryapp.databinding.CompletedConsigneeListBinding;
import com.example.rajrajas.deliveryapp.databinding.ConsigneeListBinding;

import java.util.Collections;
import java.util.List;

/**
 * Created by rajrajas on 5/17/2017.
 */

public class ListItemAdapter extends RecyclerView.Adapter<ItemViewHolder> {
    private List<ListItem> pending_data = Collections.emptyList();
    private List<ListItem> completed_data = Collections.emptyList();
    private LayoutInflater inflater;
    private Context context;
    private ListItem l;
    private String status_message;

    public ListItemAdapter(Context context, List<ListItem> data, String status_message) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        if (status_message.equals("Not Delivered List"))
            this.pending_data = data;
        else
            this.completed_data = data;
        this.status_message = status_message;

    }

    public void delete(int position, String status_message) {
        if (!status_message.equals("Not Delivered List")) {
            completed_data.remove(position);
            notifyItemRemoved(position);
        }
    }


    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (status_message.equals("Not Delivered List")) {
            final ConsigneeListBinding consigneeListBinding = ConsigneeListBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);

            return new ItemViewHolder(context, consigneeListBinding);
        } else {
            final CompletedConsigneeListBinding consigneeListBinding = CompletedConsigneeListBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);

            return new ItemViewHolder(context, consigneeListBinding);
        }

    }

    @Override
    public void onBindViewHolder(final ItemViewHolder holder, int position) {
        if (status_message.equals("Not Delivered List")) {
            holder.bind(pending_data.get(position));
            if (pending_data.get(position).getDescription().equals(""))
                ((TextView) holder.itemView.findViewById(R.id.short_description_text)).setText("No Comments");

        } else {
            holder.bind(completed_data.get(position));
            if (completed_data.get(position).getDescription().equals(""))
                ((TextView) holder.itemView.findViewById(R.id.short_description_text)).setText("No Comments");
        }
    }

    @Override
    public int getItemCount() {
        if (status_message.equals("Not Delivered List"))
            return pending_data.size();
        else
            return completed_data.size();
    }


}

