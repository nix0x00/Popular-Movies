package com.example.android.project1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by abspk on 25/11/2016.
 */

public class DetailAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<Movies> moviesArrayList;

    public DetailAdapter(Context context, ArrayList<Movies> movies) {
        this.context = context;
        this.moviesArrayList = movies;
    }

    @Override
    public int getCount() {
        return moviesArrayList.size();
    }

    @Override
    public Movies getItem(int pos) {
        return moviesArrayList.get(pos);
    }

    @Override
    public long getItemId(int id) {
        return 0;
    }

    @Override
    public View getView(int pos, View view, ViewGroup viewGroup) {
        Movies m = getItem(pos);

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.trailer_layout, viewGroup);
        }

        ImageView img = (ImageView) view.findViewById(R.id.image_for_trailer);
        TextView textView = (TextView) view.findViewById(R.id.text_view_listView);

        Picasso.with(context).load(R.drawable.play_icon).into(img);

        return view;
    }
}
