package com.example.alexey.mylauncher.recyclerview.items;

public enum ItemType {
    HEADER(0), APP(1), CONTACT(2);

    private int id;

    ItemType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
