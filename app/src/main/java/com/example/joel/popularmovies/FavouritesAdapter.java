package com.example.joel.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by joel on 2016-12-29.
 */

public class FavouritesAdapter extends CursorAdapter {

    public FavouritesAdapter(Context context, Cursor cursor, int flags){
        super(context, cursor, flags);

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
       View view =  LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView imageView  = (ImageView)view.findViewById(R.id.movie_poster);
        Uri imageUri = Uri.parse(cursor.getString(MainActivityFragment.COL_FAV_IMGURL));
        Picasso.with(context).load(imageUri).into(imageView);
    }
}
