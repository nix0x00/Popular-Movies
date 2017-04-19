package com.example.android.project1;

import java.io.Serializable;

/**
 * Created by abspk on 17/11/2016.
 */

public class Movies implements Serializable {
    String poster, title, overview, ratings, releaseDate;
    int movieID;

    public Movies(String poster, String title, String overview, String ratings, String releaseDate, int movieID) {
        this.poster = poster;
        this.title = title;
        this.overview = overview;
        this.ratings = ratings;
        this.releaseDate = releaseDate;
        this.movieID = movieID;
    }

    public String toString() {
        return poster + title + overview + ratings + releaseDate;
    }

    public String getPoster() {
        return poster;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getRatings() {
        return ratings;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public int getMovieID() {
        return movieID;
    }
}
