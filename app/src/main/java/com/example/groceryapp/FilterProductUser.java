package com.example.groceryapp;

import android.widget.Filter;

import com.example.groceryapp.Adapter.AdapterProductSeller;
import com.example.groceryapp.Adapter.AdapterProductUser;
import com.example.groceryapp.Model.ModelProduct;

import java.util.ArrayList;

public class FilterProductUser extends Filter {

    private AdapterProductUser adapter;
    private ArrayList<ModelProduct> filterList;

    public FilterProductUser(AdapterProductUser adapter, ArrayList<ModelProduct> filterList) {
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

            ArrayList<ModelProduct> modelProducts = new ArrayList<>();
            for (int i=0; i<filterList.size();i++)
            {
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint)||
                filterList.get(i).getProductCategory().toUpperCase().contains(constraint)){
                    modelProducts.add(filterList.get(i));
            }
            }
            results.count = modelProducts.size();
            results.values = modelProducts;

        }else {
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {

        adapter.productsList = (ArrayList<ModelProduct>)results.values;
        adapter.notifyDataSetChanged();

    }
}
