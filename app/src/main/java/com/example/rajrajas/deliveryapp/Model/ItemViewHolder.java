package com.example.rajrajas.deliveryapp.Model;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

import com.example.rajrajas.deliveryapp.databinding.CompletedConsigneeListBinding;
import com.example.rajrajas.deliveryapp.databinding.ConsigneeListBinding;

/**
 * Created by rajrajas on 5/17/2017.
 */

class ItemViewHolder extends RecyclerView.ViewHolder {

    private Context context;
    private ConsigneeListBinding consigneeListBinding = null;
    private CompletedConsigneeListBinding completedConsigneeListBinding = null;

    ItemViewHolder(Context context, ConsigneeListBinding binding) {
        super(binding.getRoot());
        this.consigneeListBinding = binding;
        this.context = context;
    }

    ItemViewHolder(Context context, CompletedConsigneeListBinding binding) {
        super(binding.getRoot());
        this.completedConsigneeListBinding = binding;
        this.context = context;
    }

    void bind(ListItem item_data)
    {
        if (completedConsigneeListBinding != null)
        {
            completedConsigneeListBinding.setItem(item_data);
            if(item_data.getDescription().equals(""))
            completedConsigneeListBinding.shortDescriptionText.setText("No Comments");
            else
                completedConsigneeListBinding.shortDescriptionText.setText(item_data.getDescription());

        }
        else if (consigneeListBinding != null) {
            consigneeListBinding.setItem(item_data);
            if(item_data.getDescription().equals(""))
                consigneeListBinding.shortDescriptionText.setText("No Comments");
            else
                consigneeListBinding.shortDescriptionText.setText(item_data.getDescription());
        }

    }


}

