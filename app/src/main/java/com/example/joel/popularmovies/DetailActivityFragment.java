package com.example.joel.popularmovies;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    Intent intent;

    public DetailActivityFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        intent = getActivity().getIntent();
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Movie movie =  null;
        if(intent != null){
            if(intent.hasExtra("movie")){
                movie = intent.getParcelableExtra("movie");
            }
        }

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ImageView poster = (ImageView)rootView.findViewById(R.id.detail_poster);
        TextView title = (TextView)rootView.findViewById(R.id.detail_title);
        TextView release_date = (TextView)rootView.findViewById(R.id.detail_release_date);
        TextView rating  = (TextView)rootView.findViewById(R.id.detail_rating);
        TextView synopsis = (TextView)rootView.findViewById(R.id.detail_sysnopsis);
        title.setText(movie.getmTtile());
        rating.setText(movie.getmRating());
        release_date.setText(movie.getmReleaseDate());
        synopsis.setText(movie.getmSynopsis());
        Picasso.with(getActivity()).load(movie.getmImageUrl()).into(poster);
        return rootView;
    }
}
