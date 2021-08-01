package com.example.groceryapp.Model;

public class ModelOrderedItem {



    private String pId,name,price,cost,quantity;

    public ModelOrderedItem() {
    }

    public ModelOrderedItem(String pId, String name, String price, String cost, String quantity) {
        this.pId = pId;
        this.name = name;
        this.price = price;
        this.cost = cost;
        this.quantity = quantity;
    }

    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }
}
