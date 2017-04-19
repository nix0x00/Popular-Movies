package com.example.android.project1;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by abspk on 17/11/2016.
 */

public class MoviesAdapter extends BaseAdapter {

    Context context;
    ArrayList<Movies> moviesList;

    //constructor, set the context
    public MoviesAdapter(Activity context, ArrayList<Movies> movies) {
        this.context = context;
        this.moviesList = movies;
    }

    @Override
    public int getCount() {
        return moviesList.size();
    }

    @Override
    public Movies getItem(int pos) {
        return moviesList.get(pos);
    }

    @Override
    public long getItemId(int id) {
        return 0;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent) {

        Movies movie = getItem(pos);

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.movie_items, parent, false);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.grid_view_imageView);
        Picasso.with(context)
                .load("https://image.tmdb.org/t/p/" + "w185" + movie.getPoster())
                .placeholder(R.drawable.place_holder)
                .error(R.drawable.error)
                .into(imageView);
        return view;
    }
}
