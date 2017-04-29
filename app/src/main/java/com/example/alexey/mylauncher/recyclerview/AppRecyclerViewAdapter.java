package com.example.alexey.mylauncher.recyclerview;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexey.mylauncher.main.MainActivity;
import com.example.alexey.mylauncher.R;

import java.util.List;

public class AppRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static interface Type {
        static final int APP = 0;
        static final int HEADER = 1;
    }
    private final MainActivity activity;
    private final List<AppInfo> appList;
    private CreateContextMenuListener createContextMenuListenerListener;
    private Toast toast;

    public AppRecyclerViewAdapter(MainActivity activity, List<AppInfo> appList, CreateContextMenuListener createContextMenuListenerListener) {
        this.activity = activity;
        this.appList = appList;
        this.createContextMenuListenerListener = createContextMenuListenerListener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return appList.get(position).hashCode();
    }

    @Override
    public int getItemViewType(int position) {
        return appList.get(position).packageName == null ? Type.HEADER : Type.APP;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case Type.HEADER:
                 return new HeaderViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_header, parent, false));
            default:
                return new AppViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_app, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case Type.APP:
                AppViewHolder avh = (AppViewHolder) holder;
                avh.nameView.setText(appList.get(position).appName);
                avh.iconView.setImageDrawable(appList.get(position).icon);
                break;
            case Type.HEADER:
                HeaderViewHolder hvh = (HeaderViewHolder) holder;
                hvh.nameView.setText(appList.get(position).appName);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    private void showToast(Context context, String text) {
        if (toast != null)
            toast.cancel();
        toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    public class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener, View.OnCreateContextMenuListener {
        public final View view;
        public final ImageView iconView;
        public final TextView nameView;

        public AppViewHolder(View view) {
            super(view);
            this.view = view;
            iconView = (ImageView) view.findViewById(R.id.icon);
            nameView = (TextView) view.findViewById(R.id.name);
            view.setOnCreateContextMenuListener(this);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public String toString() {
            return (String) nameView.getText();
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            activity.click(appList.get(position));
            showToast(v.getContext(), (String) nameView.getText());
            Intent intent = v.getContext().getPackageManager()
                    .getLaunchIntentForPackage(appList.get(position).packageName);
            v.getContext().startActivity(intent);
        }

        @Override
        public boolean onLongClick(View v) {
            v.showContextMenu();
            return true;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, final View v, ContextMenu.ContextMenuInfo menuInfo) {
            int position = getAdapterPosition();
            createContextMenuListenerListener.create(menu, appList.get(position));
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final View view;
        public final TextView nameView;

        public HeaderViewHolder(View view) {
            super(view);
            this.view = view;
            nameView = (TextView) view.findViewById(R.id.header);
        }

        @Override
        public String toString() {
            return (String) nameView.getText();
        }
    }

    public interface CreateContextMenuListener {
        public void create(ContextMenu menu, AppInfo appInfo);
    }
}