package com.example.android.project1;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

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
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private Movies mObject;
    private MovieDatabase movieDatabase;
    private ListView listView;
    private ArrayAdapter<String> adapter;
    private List<String> strReviews;
    //youtube links to be stored in str
    private List<String> str;
    private ViewHolder v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        movieDatabase = new MovieDatabase(getApplicationContext());

        //setup ViewHolder
        v = new ViewHolder();
        v.title = (TextView) findViewById(R.id.first_layout_textView);
        v.releaseYear = (TextView) findViewById(R.id.release_date);
        v.trailer = (TextView) findViewById(R.id.trailer_text);
        v.overview = (TextView) findViewById(R.id.movie_overview);
        v.voteAverage = (TextView) findViewById(R.id.user_voting);
        v.postImage = (ImageView) findViewById(R.id.imageView_poster);
        v.reviewText = (TextView) findViewById(R.id.review_data);

        //start Intent for trailer
        Intent intent = getIntent();
        mObject = (Movies) intent.getSerializableExtra("obj");
        Button favBtn = (Button) findViewById(R.id.favBtn);
        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SQLiteDatabase db = movieDatabase.getWritableDatabase();
                ContentValues cv = new ContentValues();
                cv.put(movieDatabase.DB_MOV_COL_FAV, 1);
                int id = db.update(movieDatabase.DB_MOV_TABLE, cv, " _id = ?", new String[]{Integer.toString(mObject.getMovieID())});
            }
        });
        //for movie trailers.
        listView = (ListView) findViewById(R.id.list_view_trailers);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(str.get(pos)));
                startActivity(intent);
            }
        });

        updateDetails(mObject);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    //view holder
    static class ViewHolder {
        private TextView title;
        private TextView releaseYear;
        private TextView trailer;
        private TextView overview;
        private TextView voteAverage;
        private ImageView postImage;
        private TextView reviewText;
    }

    private void updateDetails(Movies movies) {
        if (movies != null) {
            //set the view
            v.title.setText(movies.getTitle());
            v.releaseYear.setText(movies.getReleaseDate().substring(0, 4));
            v.trailer.setText(R.string.trailer_text);
            v.overview.setText(movies.getOverview());
            v.voteAverage.setText(movies.getRatings().concat("/10"));
            //set poster image
            Picasso.with(getApplicationContext())
                    .load("https://image.tmdb.org/t/p/" + "w185" + movies.getPoster())
                    .into(v.postImage);

            //this code has been removed as its used for movie trailers.
            //which is not a requirement for stage 1
            if (listView != null) {
                MyAsynTask task = new MyAsynTask();
                task.execute(mObject.getMovieID());
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //these lines are yet not required and should be used for stage 2
        MyAsynTask task = new MyAsynTask();
        task.execute(mObject.getMovieID());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_setting) {
            startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            return true;
        }
        if(id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        super.onOptionsItemSelected(item);
        return true;
    }

    //this method has been removed, as stage 1 does not require movie trailers.
    private void refreshAdapter(List<String> arr) {
        List<String> arra = new ArrayList<String>();
        for (int i = 0; i < arr.size(); i++) {
            int d = i + 1;
            arra.add("Trailer " + d);
        }
        adapter = new ArrayAdapter<String>(
                getApplicationContext(),
                R.layout.trailer_layout,
                R.id.text_view_listView,
                arra
        );
        //set the adapter
        listView.setAdapter(adapter);
    }

    private class MyAsynTask extends AsyncTask<Integer, Void, List<String>> {
        private String jsonStr;
        private BufferedReader reader;
        HttpURLConnection urlConnection;

        @Override
        protected List<String> doInBackground(Integer... params) {
            //http://api.themoviedb.org/3/movie/284052?append_to_response=trailers,reviews&api_key=927191753c89d36c971dded337bc19ae
            String baseUrl = "http://api.themoviedb.org/3/movie/";
            String id = params[0].toString();
            String APPEND_PARAM = "append_to_response";
            //baseUrl = baseUrl.concat(id).concat("/videos");
            baseUrl = baseUrl.concat(id);
            String API_PARAM = "api_key";
            String API_KEY = "927191753c89d36c971dded337bc19ae";
            /*Uri uri = Uri.parse(baseUrl).buildUpon()
                    .appendQueryParameter(API_PARAM, API_KEY)
                    .build();*/

            Uri uri = Uri.parse(baseUrl)
                    .buildUpon()
                    .appendQueryParameter(APPEND_PARAM, "trailers,reviews")
                    .appendQueryParameter(API_PARAM, API_KEY)
                    .build();
            try {
                URL url = new URL(uri.toString());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream is = urlConnection.getInputStream();
                if (is == null) {
                    return null;
                }

                String data;
                reader = new BufferedReader(new InputStreamReader(is));
                StringBuffer strBuffer = new StringBuffer();

                while ((data = reader.readLine()) != null) {
                    strBuffer.append(data + "\n");
                }

                jsonStr = strBuffer.toString();
            } catch (Exception x) {
                Log.e("DetailActivity", "" + x);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }

                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException x) {
                    Log.e("ClosingReader", "" + x);
                }
            }
            try {
                return getParsedJsonData(jsonStr);
            } catch (JSONException ex) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(List<String> arr) {
            if (arr != null) {
                str = new ArrayList<String>();
                for (int i = 0; i < arr.size(); i++) {
                    str.add("https://www.youtube.com/watch?v=".concat(arr.get(i)));
                }
                refreshAdapter(str);
                if (strReviews.size() > 0) {
                    v.reviewText.setText(strReviews.get(0));
                } else {
                    v.reviewText.setText("Sorry! No reviews for this movie : (");
                }
            }
        }

        private List<String> getParsedJsonData(String str) throws JSONException {
            JSONObject obj = new JSONObject(str);
            obj = obj.getJSONObject("trailers");
            JSONArray jsonArray = obj.getJSONArray("youtube");
            List<String> strArray = new ArrayList<String>();
            strReviews = new ArrayList<String>();
            String name;

            for (int i = 0; i < jsonArray.length(); i++) {
                obj = jsonArray.getJSONObject(i);
                name = obj.getString("type");

                if (name.equals("Trailer")) {
                    strArray.add(obj.getString("source"));
                }

            }

            obj = new JSONObject(str);
            obj = obj.getJSONObject("reviews");
            jsonArray = obj.getJSONArray("results");
            for (int i = 0; i < jsonArray.length(); i++) {
                obj = jsonArray.getJSONObject(i);
                strReviews.add(obj.getString("content"));
            }

            return strArray;
        }
    }

}
