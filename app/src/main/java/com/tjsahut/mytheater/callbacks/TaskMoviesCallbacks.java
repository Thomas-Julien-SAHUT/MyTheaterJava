package com.tjsahut.mytheater.callbacks;

import com.tjsahut.mytheater.objects.Movie;

import java.util.ArrayList;


public interface TaskMoviesCallbacks {

    void updateListView(ArrayList<Movie> movies);

    void finishNoNetwork();
}
