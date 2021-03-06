package com.tjsahut.mytheater.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tjsahut.mytheater.MoviesActivity;
import com.tjsahut.mytheater.R;
import com.tjsahut.mytheater.adapters.MovieAdapter;
import com.tjsahut.mytheater.api.APIHelper;
import com.tjsahut.mytheater.callbacks.TaskMoviesCallbacks;
import com.tjsahut.mytheater.objects.DisplayList;
import com.tjsahut.mytheater.objects.Movie;
import com.tjsahut.mytheater.objects.Theater;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MoviesFragment extends ListFragment implements TaskMoviesCallbacks {

    private static final String STATE_ACTIVATED_POSITION = "activated_position";
    static public ArrayList<Movie> currentMovies;
    static private boolean toFinish = false;
    static private boolean dialogPending = false;
    static private boolean toUpdate = false;
    private static Callbacks sDummyCallbacks = new Callbacks() {
        @Override
        public void onItemSelected(int position, Fragment source, View currentView) {
        }

        @Override
        public void setFragment(Fragment fragment) {
        }

        @Override
        public void setIsLoading(Boolean isLoading) {

        }

        @Override
        public void finishNoNetwork() {
            toFinish = true;
        }
    };
    public ArrayList<Movie> movies = null;
    private Callbacks mCallbacks = sDummyCallbacks;
    private int mActivatedPosition = ListView.INVALID_POSITION;
    private LoadMoviesTask mTask;
    private ProgressDialog dialog;
    private Theater theater = null;

    static public ArrayList<Movie> getMovies() {
        return currentMovies;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (movies == null && mTask == null) {
            String theaterCode = getActivity().getIntent().getStringExtra("code");
            mTask = new LoadMoviesTask(this, theaterCode);
            mTask.execute(theaterCode);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
            setActivatedPosition(savedInstanceState.getInt(STATE_ACTIVATED_POSITION));
        }
        return inflater.inflate(R.layout.fragment_movies, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // Add details footer to listView
        TextView text = new TextView(getActivity());
        text.setText(Html.fromHtml("<small><strong>TLJ</strong> : Tous Les Jours (jusqu'à mardi inclus)"));
        text.setGravity(Gravity.CENTER_HORIZONTAL);
        ListView list = getActivity().findViewById(android.R.id.list);
        list.addFooterView(text, null, false);

        if (movies == null && mTask == null) {
            String theaterCode = getActivity().getIntent().getStringExtra("code");
            mTask = new LoadMoviesTask(this, theaterCode);
            mTask.execute(theaterCode);
        }

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (!(activity instanceof Callbacks)) {
            throw new IllegalStateException("Activity must implement fragment's callbacks.");
        }
        mCallbacks = (Callbacks) activity;
        mCallbacks.setFragment(this);
        if (toFinish) {
            mCallbacks.finishNoNetwork();
            toFinish = false;
        }
        if (dialogPending) {
            dialog = new ProgressDialog(activity);
            dialog.setMessage("Chargement des séances en cours...");
            dialog.show();
        }
        if (toUpdate && (movies != null)) {
            updateListView(movies);
            toUpdate = false;
        }
        if (theater != null) {
            ((MoviesActivity) activity).setTheaterLocation(theater);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        mCallbacks = sDummyCallbacks;
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        mCallbacks.onItemSelected(position, this, view);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mActivatedPosition != AdapterView.INVALID_POSITION) {
            outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
        }
    }

    public void setActivateOnItemClick(boolean activateOnItemClick) {
        getListView().setChoiceMode(activateOnItemClick ? AbsListView.CHOICE_MODE_SINGLE : AbsListView.CHOICE_MODE_NONE);
    }

    public void setActivatedPosition(int position) {
        if (position == AdapterView.INVALID_POSITION) {
            getListView().setItemChecked(mActivatedPosition, false);
        } else {
            getListView().setItemChecked(position, true);
        }

        mActivatedPosition = position;
    }

    @Override
    public void finishNoNetwork() {
        mCallbacks.finishNoNetwork();
    }

    public void clear() {
        if (currentMovies != null) {
            currentMovies.clear();
            currentMovies = null;
            movies = null;
            if (getListAdapter() != null) {
                ((MovieAdapter) getListAdapter()).clear();
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PackageManager pm = getActivity().getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN)) {
            getListView().requestFocus();
        }
    }

    @Override
    public void updateListView(ArrayList<Movie> movies) {
        MoviesFragment.currentMovies = movies;
        this.movies = movies;
        if (getActivity() != null) {
            setListAdapter(new MovieAdapter(getActivity(), R.layout.item_theater, movies));
        } else {
            toUpdate = true;
        }
        mTask = null;
    }

    public interface Callbacks {

        public void onItemSelected(int position, Fragment source, View currentView);

        public void setFragment(Fragment fragment);

        public void setIsLoading(Boolean isLoading);

        public void finishNoNetwork();
    }

    private class LoadMoviesTask extends AsyncTask<String, Void, DisplayList> {
        private MoviesFragment fragment;
        private Context ctx;
        private String theaterCode;

        /**
         * Last values retrieved from previous run.
         */
        private String cache;

        /**
         * Timestamp for last update
         */
        private Long lastCacheUpdate;

        private Boolean remoteDataHasChangedFromLocalCache = true;

        LoadMoviesTask(MoviesFragment fragment, String theaterCode) {
            super();
            this.fragment = fragment;
            this.ctx = fragment.getActivity();
            this.theaterCode = theaterCode;
        }

        @Override
        protected void onPreExecute() {
            SharedPreferences sp = ctx.getSharedPreferences("theater-cache", Context.MODE_PRIVATE);

            lastCacheUpdate = sp.getLong(theaterCode + "-date", 0);
            cache = sp.getString(theaterCode, "");
            if (!cache.equals("")) {
                // Display cached values
                try {
                    Log.i("cache-hit", "Getting display datas from cache for " + theaterCode);
                    mCallbacks.setIsLoading(true);
                    ArrayList<Movie> movies = (new APIHelper().formatMoviesList(new JSONArray(cache), theaterCode));
                    fragment.updateListView(movies);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.i("cache-miss", "Remote loading first-time datas for " + theaterCode);
                dialog = new ProgressDialog(ctx);
                dialog.setMessage("Chargement des séances en cours...");
                dialog.show();
                dialogPending = true;
            }
        }

        @Override
        protected DisplayList doInBackground(String... queries) {
            if (!theaterCode.equals(queries[0])) {
                throw new RuntimeException("Fragment misuse: theaterCode differs");
            }
            DisplayList displayList = (new APIHelper()).downloadMoviesList(theaterCode);

            JSONArray jsonResults = displayList.jsonArray;

            String newCache = jsonResults.toString();

            if (cache.equals(newCache)) {
                Log.i("cache-hit", "Remote datas equals local datas; skipping UI update.");
                remoteDataHasChangedFromLocalCache = false;
            } else if (!displayList.noDataConnection) { // Do not overwrite cache with empty datas
                Log.i("cache-miss", "Remote data differs from local datas; updating UI");
                // Store in cache for future use
                // Also store the date of the day
                SharedPreferences.Editor ed = ctx.getSharedPreferences("theater-cache", Context.MODE_PRIVATE).edit();
                ed.putString(theaterCode, jsonResults.toString());
                ed.putLong(theaterCode + "-date", new Date().getTime());
                ed.apply();
                remoteDataHasChangedFromLocalCache = true;
            }

            return displayList;
        }

        @Override
        protected void onPostExecute(DisplayList displayList) {
            mCallbacks.setIsLoading(false);
            if (dialog != null) {
                if (dialog.isShowing())
                    dialog.dismiss();
            }
            dialogPending = false;

            if (displayList.noDataConnection && getActivity() != null) {
                // No data connection, so unable to update.
                // However our cache may be valid.
                Date cacheDate = new Date(lastCacheUpdate);

                Calendar c = Calendar.getInstance();
                // If we're before wednesday and after start of next week, get back one week before setting day to Wednesay
                if (c.get(Calendar.DAY_OF_WEEK) < Calendar.WEDNESDAY) {
                    c.roll(Calendar.WEEK_OF_YEAR, -1);
                }
                c.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                Date lastWednesday = c.getTime();

                if (cacheDate.getTime() > lastWednesday.getTime()) {
                    Toast.makeText(ctx, "No connection, displaying datas from cache.", Toast.LENGTH_SHORT).show();
                    remoteDataHasChangedFromLocalCache = false;
                } else {
                    TextView emptyText = (TextView) getActivity().findViewById(android.R.id.empty);
                    emptyText.setText(R.string.aucune_connexion_internet);
                }
            }

            // Update only if data changed
            if (remoteDataHasChangedFromLocalCache) {
                ArrayList<Movie> movies = (new APIHelper()).formatMoviesList(displayList.jsonArray, theaterCode);
                fragment.updateListView(movies);
            }

            theater = displayList.theater;
            if (getActivity() != null && theater.code != null) {
                ((MoviesActivity) getActivity()).setTheaterLocation(theater);
            }
        }
    }
}
