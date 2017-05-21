package com.example.alexey.mylauncher.main;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alexey.mylauncher.MyContentProvider;
import com.example.alexey.mylauncher.R;
import com.example.alexey.mylauncher.recyclerview.items.ItemType;

import java.util.ArrayList;


public class MainFragment extends Fragment {
    private static final Uri INSERT_URI = Uri.parse("content://com.example.alexey.mylauncher/uri/insert");

    private final static String PAGE_NUMBER = "page_number";
    private int pageNumber;
    private Toast toast;

    public MainFragment() {}

    public static MainFragment newInstance(int pageNumber) {
        MainFragment fragment = new MainFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PAGE_NUMBER, pageNumber);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(PAGE_NUMBER, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view;
        switch (pageNumber) {
            case 1:
                view = inflater.inflate(R.layout.fragment_favorites_app, container, false);
                initUri(view);
                break;
            default:
                view = inflater.inflate(R.layout.fragment_list_app, container, false);
                break;
        }
        initList(pageNumber, view);
        return view;
    }

    private void initUri(@NonNull final View view) {
        final AutoCompleteTextView uriText = (AutoCompleteTextView) view.findViewById(R.id.uri_text);
        final ArrayList<String> uriList = ((MainActivity)getActivity()).getUriList();
        final ArrayAdapter<String> adapter = ((MainActivity)getActivity()).getUriAdapter();
        uriText.setAdapter(adapter);
        uriText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uriText.getText().toString()));
                        startActivity(intent);
                        String s = uriText.getText().toString();
                        uriList.remove(s);
                        adapter.remove(s);
                        uriList.add(0, s);
                        adapter.insert(s, 0);
                        ContentValues values = new ContentValues();
                        values.put(MyContentProvider.URI_NAME, s);
                        getContext().getContentResolver().insert(INSERT_URI, values);
                        int uriCount = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(view.getContext()).getString("uri_count", "10"));
                        while (uriList.size() > uriCount) {
                            adapter.remove(uriList.get(uriCount));
                            uriList.remove(uriCount);
                        }
                        uriText.setText("");
                        return true;
                    } catch (ActivityNotFoundException ignored) {
                        if (uriText.getText().toString().equals(""))
                            return false;
                        if (toast != null)
                            toast.cancel();
                        toast = Toast.makeText(view.getContext(), "\"" + uriText.getText().toString() + "\" - не URI", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
                return false;
            }
        });
    }

    private void initList(int pageNumber, View view) {
        final RecyclerView list;
        switch (pageNumber) {
            case 1:
                list = (RecyclerView) view.findViewById(R.id.app_favorite_list);
                list.setAdapter(((MainActivity)getActivity()).getFavoritesAdapter());
                break;
            default:
                list = (RecyclerView) view.findViewById(R.id.app_list);
                list.setAdapter(((MainActivity)getActivity()).getAppAdapter());
                break;
        }
        final int columnCount;
        if (PreferenceManager.getDefaultSharedPreferences(view.getContext()).getString("column_count", "1").equals("1"))
            columnCount = getResources().getInteger(R.integer.column_count_46);
        else
            columnCount = getResources().getInteger(R.integer.column_count_57);
        ((GridLayoutManager)list.getLayoutManager()).setSpanCount(columnCount);
        ((GridLayoutManager)list.getLayoutManager()).setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (list.getAdapter().getItemViewType(position) == ItemType.CONTACT.getId()
                        || list.getAdapter().getItemViewType(position) == ItemType.APP.getId()) {
                    return 1;
                } else if (list.getAdapter().getItemViewType(position) == ItemType.HEADER.getId()) {
                    return columnCount;
                }
                return 0;
            }
        });
    }
}
