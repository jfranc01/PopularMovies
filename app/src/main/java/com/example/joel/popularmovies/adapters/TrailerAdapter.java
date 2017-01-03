package com.example.joel.popularmovies.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.joel.popularmovies.R;
import com.example.joel.popularmovies.model.Trailer;

import java.util.List;

/**
 * Created by joel on 2017-01-02.
 */

public class TrailerAdapter extends ArrayAdapter<Trailer> {

    public TrailerAdapter(Activity context, List<Trailer> trailerList){
        super(context, 0, trailerList);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView ==  null){
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.trailer_item, parent, false);
        }
        Trailer trailer = getItem(position);
        TextView trailerName = (TextView) convertView.findViewById(R.id.trailer_name);
        trailerName.setText(trailer.getName());
        return convertView;
    }
}
