package com.tjsahut.mytheater.callbacks;

import com.tjsahut.mytheater.objects.Theater;

import java.util.ArrayList;

public interface TaskTheaterCallbacks {

    void finishNoNetwork();

    void onLoadOver(ArrayList<Theater> theaters, boolean isFavorite, boolean isGeoSearch);
}
