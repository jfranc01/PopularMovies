package com.example.joel.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.example.joel.popularmovies.model.Movie;

public class MainActivity extends AppCompatActivity implements DetailActivityFragment.Callbacks{

    public static boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Depending on the smallest width, we need to check if the
        //container is part of the layout. If it is part of the layout,
        //then we know it is two pane
        if(findViewById(R.id.movie_detail_container) != null){
            //set the variable to true
            mTwoPane = true;
            //replace the container with a new DetailActivityFragment
            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, new DetailActivityFragment())
                        .commit();
            }
        }
        else{
            mTwoPane = false;
        }
    }

    @Override
    public void onItemClicked(Movie movie) {
        Bundle arguments = new Bundle();
        arguments.putParcelable(DetailActivityFragment.DETAIL_MOVIE, movie);
        //if it is two pane
        if(mTwoPane){
            DetailActivityFragment daf = new DetailActivityFragment();
            daf.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_container,
                    daf, DETAILFRAGMENT_TAG).commit();
        }
        else{
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailActivityFragment.DETAIL_MOVIE, movie);
            startActivity(intent);
        }
    }
}
