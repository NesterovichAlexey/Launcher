package com.example.alexey.mylauncher.recyclerview.items;

import android.support.annotation.NonNull;

public class Header extends Item {

    public Header(@NonNull String name) {
        super(ItemType.HEADER.getId(), name);
    }
}
