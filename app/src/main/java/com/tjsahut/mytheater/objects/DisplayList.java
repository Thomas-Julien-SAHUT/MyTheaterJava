package com.tjsahut.mytheater.objects;

import org.json.JSONArray;

public class DisplayList {
    /**
     * Flag set to true when no internet is available on the device.
     */
    public Boolean noDataConnection = false;

    public Theater theater = new Theater();
    public JSONArray jsonArray = new JSONArray();
}
