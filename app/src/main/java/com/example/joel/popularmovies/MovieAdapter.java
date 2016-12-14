package com.example.joel.popularmovies;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * Created by joel on 2016-12-13.
 * Test to see if this change will be committed again
 */

public class MovieAdapter extends ArrayAdapter<Movie> {

    public MovieAdapter(Activity context, List<Movie> movieList){
        super(context, 0, movieList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Adapters recycle views to adapter views
        //If this is a new view object then inflate the layout
        //if not then this is a view which already had the layout inflated
        if(convertView ==  null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_item, parent, false);
        }
        Movie movie = getItem(position);
        ImageView imageView  = (ImageView)convertView.findViewById(R.id.movie_poster);
        Picasso.with(getContext()).load(movie.getmImageUrl()).into(imageView);
        return convertView;
    }
}
