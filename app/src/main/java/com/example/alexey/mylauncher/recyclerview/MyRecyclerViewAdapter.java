package com.example.alexey.mylauncher.recyclerview;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alexey.mylauncher.R;
import com.example.alexey.mylauncher.recyclerview.items.App;
import com.example.alexey.mylauncher.recyclerview.items.Contact;
import com.example.alexey.mylauncher.recyclerview.items.Header;
import com.example.alexey.mylauncher.recyclerview.items.Item;
import com.example.alexey.mylauncher.recyclerview.items.ItemType;

import java.util.List;


public class MyRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Item> itemList;

    public MyRecyclerViewAdapter(@NonNull List<Item> itemList) {
        this.itemList = itemList;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return itemList.get(position).getId();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ItemType.APP.getId() || viewType == ItemType.CONTACT.getId())
            return new AppViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_app, parent, false));
        return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_header, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == ItemType.APP.getId()) {
            App app = (App) itemList.get(position);
            AppViewHolder appHolder = (AppViewHolder) holder;
            appHolder.item = app;
            appHolder.name.setText(app.getName());
            appHolder.icon.setImageDrawable(app.getIcon());
        } else if (getItemViewType(position) == ItemType.CONTACT.getId()) {
            Contact contact = (Contact) itemList.get(position);
            AppViewHolder contactHolder = (AppViewHolder) holder;
            contactHolder.item = contact;
            contactHolder.name.setText(contact.getName());
            contactHolder.icon.setImageDrawable(contact.getPhoto());
        } else {
            Header header = (Header) itemList.get(position);
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.item = header;
            headerHolder.name.setText(header.getName());
        }
        holder.itemView.setOnClickListener(itemList.get(position));
        holder.itemView.setOnLongClickListener(itemList.get(position));
        holder.itemView.setOnCreateContextMenuListener(itemList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public Item item;

        HeaderViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.header);
        }
    }

    private class AppViewHolder extends RecyclerView.ViewHolder {
        public final TextView name;
        public final ImageView icon;
        public Item item;

        AppViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.name);
            icon = (ImageView) view.findViewById(R.id.icon);
        }
    }
}