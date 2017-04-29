package com.example.alexey.mylauncher.welcome;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alexey.mylauncher.R;

public class WelcomeFragment extends Fragment {
    private final static String PAGE_NUMBER = "page_number";
    private int pageNumber;

    public WelcomeFragment() {
    }

    public static WelcomeFragment newInstance(int pageNumber) {
        WelcomeFragment fragment = new WelcomeFragment();
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
        switch (pageNumber) {
            case 1:
                return inflater.inflate(R.layout.fragment_column_count, container, false);
            case 2:
                return inflater.inflate(R.layout.fragment_theme, container, false);
            default:
                return inflater.inflate(R.layout.fragment_welcome, container, false);
        }
    }
}
