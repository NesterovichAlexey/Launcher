package com.example.alexey.mylauncher.recyclerview.items;

import android.support.annotation.NonNull;
import android.view.ContextMenu;
import android.view.View;

public abstract class Item implements View.OnClickListener, View.OnLongClickListener, View.OnCreateContextMenuListener {
    private int id;
    protected String name;
    private ClickListener clickListener;
    private LongClickListener longClickListener;
    private CreateContextMenu createContextMenuListener;

    Item(int id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setLongClickListener(LongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    public void setCreateContextMenuListener(CreateContextMenu createContextMenuListener) {
        this.createContextMenuListener = createContextMenuListener;
    }

    @Override
    public void onClick(View v) {
        if (clickListener != null)
            clickListener.onClick(v, this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (createContextMenuListener != null)
            createContextMenuListener.onCreateContextMenu(menu, v, menuInfo, this);
    }

    @Override
    public boolean onLongClick(View v) {
        return longClickListener != null && longClickListener.onLongClick(v, this);
    }

    public static interface ClickListener {
        public void onClick(View v, Item item);
    }

    public static interface LongClickListener {
        public boolean onLongClick(View v, Item item);
    }

    public static interface CreateContextMenu {
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, Item item);
    }
}
