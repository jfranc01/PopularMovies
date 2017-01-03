package com.example.joel.popularmovies.model;

/**
 * Created by joel on 2017-01-02.
 */

public class Trailer {
    private String mTrailerId;
    private String mKey;
    private String mName;
    private String mSite;

    public String getmTrailerId() {
        return mTrailerId;
    }

    public void setmTrailerId(String mTrailerId) {
        this.mTrailerId = mTrailerId;
    }

    public String getmKey() {
        return mKey;
    }

    public void setmKey(String mKey) {
        this.mKey = mKey;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getSite() {
        return mSite;
    }

    public void setSite(String site) {
        this.mSite = site;
    }
}
