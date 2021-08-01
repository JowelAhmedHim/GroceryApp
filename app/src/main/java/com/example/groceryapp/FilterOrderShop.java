package com.example.groceryapp;

import android.widget.Filter;

import com.example.groceryapp.Adapter.AdapterOrderShop;
import com.example.groceryapp.Adapter.AdapterProductSeller;
import com.example.groceryapp.Model.ModelOrderShop;
import com.example.groceryapp.Model.ModelProduct;

import java.util.ArrayList;

public class FilterOrderShop extends Filter {

    private AdapterOrderShop adapter;
    private ArrayList<ModelOrderShop> filterList;

    public FilterOrderShop(AdapterOrderShop adapter, ArrayList<ModelOrderShop> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults results = new FilterResults();

        //validate data for search query
        if (constraint != null && constraint.length()>0)
        {
            //change to upper case for case sensitive
            constraint = constraint.toString().toUpperCase();

            //store filtered list

            ArrayList<ModelOrderShop> filerModels = new ArrayList<>();
            for (int i=0; i<filterList.size();i++)
            {
                if (filterList.get(i).getOrderStatus().toUpperCase().contains(constraint))
                {
                    filerModels.add(filterList.get(i));
                }
            }
            results.count = filerModels.size();
            results.values = filerModels;

        }else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.orderShopArrayList = (ArrayList<ModelOrderShop>)results.values;
        adapter.notifyDataSetChanged();

    }
}
