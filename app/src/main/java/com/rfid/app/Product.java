package com.rfid.app;

public class Product {
    public int product_id;
    public String tag_id;
    public String name;
    public int stock;
    public int count;
    public String color;

    public Product(int product_id, String tag_id, String name, int stock, int count, String color) {
        this.product_id = product_id;
        this.tag_id = tag_id;
        this.name = name;
        this.stock = stock;
        this.count = count;
        this.color = color;
    }

    public Product() {
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void increaseCountByOne() {
        this.count += 1;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Product{" +
                "product_id=" + product_id +
                ", tag_id='" + tag_id + '\'' +
                ", name='" + name + '\'' +
                ", stock=" + stock +
                ", count=" + count +
                ", color='" + color + '\'' +
                '}';
    }
}
