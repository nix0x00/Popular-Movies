package com.example.android.project1;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by abspk on 17/11/2016.
 */

public class MainFragment extends Fragment {

    private MoviesAdapter adapter;
    GridView gridView;
    private String str;
    ArrayList<Movies> moviesArray;
    ArrayList<Movies> favMovies;
    private MovieDatabase mDatabase;

    //constructor
    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        //updateView();
        //check the network connectivity
        checkNetwork();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //setup GridView
        gridView = (GridView) rootView.findViewById(R.id.main_fragment_grid_view);
        mDatabase = new MovieDatabase(getActivity());

        //listener
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);

                //check if the settings is of fav movies or other
                if(str.equals("fav")) {
                    intent.putExtra("obj", favMovies.get(pos));
                } else {
                    intent.putExtra("obj", moviesArray.get(pos));
                }
                startActivity(intent);
            }
        });
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //checkNetwork();
        NetworkState ns = new NetworkState(getActivity());
        if (!ns.isNetworkWorking()) {
            AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
            ad.setTitle(R.string.alert_dialogTitle);
            ad.setMessage(R.string.network_error);
            ad.setCancelable(true);
            ad.show();
        }
    }

    //check if internet is working or not
    private void checkNetwork() {
        NetworkState ns = new NetworkState(getActivity());
        if (moviesArray == null) {
            if (ns.isNetworkWorking()) {
                updateView();
            } else {
                AlertDialog.Builder ad = new AlertDialog.Builder(getActivity());
                ad.setTitle(R.string.alert_dialogTitle);
                ad.setMessage(R.string.network_error);
                ad.setCancelable(true);
                ad.show();
            }
        } else {
            updateView();
        }
    }

    //save the data
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putSerializable("movie", moviesArray);
    }

    //restore the fragment state
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            moviesArray = (ArrayList<Movies>) savedInstanceState.getSerializable("movie");
        }
    }

    private void movieAdapter(ArrayList<Movies> movies) {
        adapter = new MoviesAdapter(getActivity(), movies);
        gridView.setAdapter(adapter);
    }

    private void updateView() {
        GetMovies gm = new GetMovies();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        str = preferences.getString(getString(R.string.pref_sync_key),
                getString(R.string.pref_sync_defaultValue));

        if (str.equals("popular")) {
            gm.execute("popularity.desc");
        } else if (str.equals("fav")) {
            SQLiteDatabase sqlDB = mDatabase.getReadableDatabase();
            Cursor cursor = sqlDB.query(mDatabase.DB_MOV_TABLE, new String[]{"mObj"}, " isFav = ?", new String[]{Integer.toString(1)}, null, null, null);
            favMovies = new ArrayList<Movies>();
            Movies m;
            String poster, title, overview, ratings, releaseDate;
            int movieID;
            while (cursor.moveToNext()) {
                byte[] bytes = cursor.getBlob(cursor.getColumnIndex("mObj"));
                String bytesStr = new String(bytes);

                try {
                    JSONObject obj = new JSONObject(bytesStr);
                    poster = obj.getString("poster");
                    title = obj.getString("title");
                    overview = obj.getString("overview");
                    ratings = obj.getString("ratings");
                    releaseDate = obj.getString("releaseDate");
                    movieID = obj.getInt("movieID");

                    m = new Movies(poster, title, overview, ratings, releaseDate, movieID);
                    favMovies.add(m);
                    Log.d("SQL", "...................................." + m.getPoster());
                } catch (JSONException x) {
                    Log.d("SQL", "...................................." + x);
                }
            }
            movieAdapter(favMovies);
        } else {
            gm.execute("vote_average.desc");
        }

        //movieAdapter(moviesArray);
    }

    private void updateDatabase(Movies[] movies) {
        Gson gson = new Gson();
        ContentValues cv = new ContentValues(); // key, value
        for (int i = 0; i < movies.length; i++) {
            cv.put(mDatabase.DB_MOV_COL_ID, movies[i].getMovieID());
            cv.put(mDatabase.DB_MOV_COL_OBJ, gson.toJson(movies[i]));
            cv.put(mDatabase.DB_MOV_COL_NAME, movies[i].getTitle());

            SQLiteDatabase db = mDatabase.getWritableDatabase();
            long id = db.insert(mDatabase.DB_MOV_TABLE, null, cv);
            if (id != -1) {
                Log.d("SQL", ".......... INSERTED");
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting) {
            startActivity(new Intent(getActivity(), SettingsActivity.class));
            return true;
        }
        //return true;
        return super.onOptionsItemSelected(item);
    }

    private class GetMovies extends AsyncTask<String, Void, Movies[]> {

        HttpURLConnection urlConnection;
        BufferedReader reader;
        String jsonStr;

        //constructor
        public GetMovies() {

        }

        @Override
        protected Movies[] doInBackground(String... params) {
            final String API_PARAM = "api_key";
            //final String LANG_PARAM = "language";
            //final String SORT_PARAM = "sort_by";

            //below url has been deprecated as its no longer required as suggested by forum mentors
            //String baseUrl = "https://api.themoviedb.org/3/discover/movie";
            String baseUrl = "https://api.themoviedb.org/3/movie/popular";
            String baseUrlVote = "https://api.themoviedb.org/3/movie/top_rated";
            final String API_KEY = "927191753c89d36c971dded337bc19ae";

            //check for internet connection, if its not connected then exit the app
            NetworkState ns = new NetworkState(getActivity());
            if (!ns.isNetworkWorking()) {
                Activity a = getActivity();
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity(), "Network Error", Toast.LENGTH_LONG).show();
                        try {
                            wait(1000);
                        } catch (Exception x) {

                        }
                        //getActivity().finish();
                    }
                });
                return null;
            }

            Uri uri = new Uri.Builder()
                    .appendQueryParameter(API_PARAM, API_KEY)
                    .build();

            try {
                URL url;
                if (params[0].equals("popularity.desc")) {
                    url = new URL(baseUrl.concat(uri.toString()));
                } else {
                    url = new URL(baseUrlVote.concat(uri.toString()));
                }

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                //Log.v("JSON DATA", "............................" + url);
                InputStream is = urlConnection.getInputStream();
                if (is == null) {
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(is));
                StringBuffer strBuffer = new StringBuffer();
                String data;
                while ((data = reader.readLine()) != null) {
                    strBuffer.append(data + "\n");
                }

                if (strBuffer.length() == 0) {
                    return null;
                }
                jsonStr = strBuffer.toString();

            } catch (Exception x) {
                //Log.e("MainFragment", "Error: " + x);
                //x.printStackTrace();
            } finally {

                //close the connections
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException x) {

                }
            }

            try {
                return fetchJsonData(jsonStr);
            } catch (JSONException e) {
                //Log.e("doInBackground", "" + e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Movies[] movies) {
            if (movies != null) {
                for (int i = 0; i < movies.length; i++) {
                    moviesArray.add(movies[i]);
                }

                //set the adapter
                movieAdapter(moviesArray);
                updateDatabase(movies);
            } else {
                Toast.makeText(getActivity(), "Something went wrong! Please try again.", Toast.LENGTH_LONG)
                        .show();
            }
        }

        //extract data from json string
        private Movies[] fetchJsonData(String jsonStr) throws JSONException {
            Movies[] images;
            String poster, title, overview, ratings, releaseDate;
            int movieID;
            JSONObject obj = new JSONObject(jsonStr);
            JSONArray jsonArray = obj.getJSONArray("results");

            //images = new String[jsonArray.length()];
            images = new Movies[jsonArray.length()];

            //refresh or recreate reinitialize the array to remove previous data and add fresh data
            //to avoid repetition of data
            moviesArray = new ArrayList<Movies>();

            //loop through to retrieve & store data
            for (int i = 0; i < jsonArray.length(); i++) {
                obj = jsonArray.getJSONObject(i);
                poster = obj.getString("poster_path");
                title = obj.getString("original_title");
                overview = obj.getString("overview");
                ratings = obj.getString("vote_average");
                releaseDate = obj.getString("release_date");
                movieID = obj.getInt("id");
                //images[i] = "";
                images[i] = new Movies(poster, title, overview, ratings, releaseDate, movieID);
            }

            //Log.v("fetchJsonData", "........." + images[3]);
            return images;
        }
    }
}
