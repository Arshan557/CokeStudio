package com.arshan.cokestudio;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arshan.cokestudio.Constants.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity {

    private Toolbar toolbar;
    private CoordinatorLayout coordinatorLayout;
    public static final int progress_bar_type = 0;
    private List<SongsPojo> songsPojoList = new ArrayList<>();
    private SongsAdapter songsAdapter;
    private static final int REQUEST_WRITE_STORAGE = 1;
    private static final int REQUEST_CODE_ASK_PERMISSIONS = 1;
    boolean mobileNwInfo = false;

    private String TAG = MainActivity.class.getSimpleName();
    private ProgressDialog pDialog;
    private RecyclerView recyclerView;
    private ImageView play, download, refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        play = (ImageView) findViewById(R.id.play_button);
        download = (ImageView) findViewById(R.id.download_button);
        refresh = (ImageView) findViewById(R.id.refresh);
        recyclerView = (RecyclerView) findViewById(R.id.songs_recycle);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.mainCoordinate);

        //Checking network connection
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            mobileNwInfo = conMgr.getActiveNetworkInfo().isConnected();
        } catch (NullPointerException e) {
            mobileNwInfo = false;
        }

        //Asking permissions for Marshmallow
        boolean hasPermission = (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        if (!hasPermission) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA},
                    REQUEST_WRITE_STORAGE);
        }
        boolean hasCallPermission = (ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED);
        if (!hasCallPermission) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.CALL_PHONE"}, REQUEST_CODE_ASK_PERMISSIONS);
        }

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new GetSongs().execute();
            }
        });

        //Make call to Async
        new GetSongs().execute();

        //Recycle view starts
        songsAdapter = new SongsAdapter(getApplicationContext(), songsPojoList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(songsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setSearchableInfo( searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.hint));

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener(){
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        Log.w("myApp", "onQueryTextSubmit::"+query);
                        return false;
                    }
                    @Override
                    public boolean onQueryTextChange(String newText) {
                        Log.w("myApp", "onQueryTextChange::"+newText);
                        songsAdapter.getFilter().filter(newText);
                        recyclerView.invalidate();
                        return true;
                    }
                });
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading Sudarshan's profile. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.download_profile) {
            if (mobileNwInfo == false) {
                final boolean mnwI = mobileNwInfo;
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Please enable WiFi/Mobile data", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mnwI == true) {
                                    Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Connected!", Snackbar.LENGTH_SHORT);
                                    snackbar1.show();
                                } else {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Sorry! Not yet connected", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                            }
                        });
                snackbar.show();
            } else {
                boolean hasPermission = (ContextCompat.checkSelfPermission(MainActivity.this,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
                if (!hasPermission) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA},
                            REQUEST_WRITE_STORAGE);
                } else {
                    new DownloadFileFromURL().execute(Constants.RESUME_DOWNLOAD_PATH);
                }
            }
        }  else if(id == R.id.linkedIn) {
            if (mobileNwInfo == false) {
                final boolean mnwI = mobileNwInfo;
                Snackbar snackbar = Snackbar.make(coordinatorLayout, "Please enable WiFi/Mobile data", Snackbar.LENGTH_LONG)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (mnwI == true) {
                                    Snackbar snackbar1 = Snackbar.make(coordinatorLayout, "Connected!", Snackbar.LENGTH_SHORT);
                                    snackbar1.show();
                                } else {
                                    Snackbar snackbar = Snackbar.make(coordinatorLayout, "Sorry! Not yet connected", Snackbar.LENGTH_LONG);
                                    snackbar.show();
                                }
                            }
                        });
                snackbar.show();
            } else {
                Toast.makeText(MainActivity.this, "Opening Sudarshan's LinkedIn. Please wait...", Toast.LENGTH_LONG).show();
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.LINKEDIN_URL));
                startActivity(browserIntent);
            }
        } else if(id == R.id.call) {
            boolean hasPermission = (ContextCompat.checkSelfPermission(MainActivity.this,
                    android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED);
            if (!hasPermission) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.CALL_PHONE"}, REQUEST_CODE_ASK_PERMISSIONS);
            } else {
                Intent in = new Intent(Intent.ACTION_CALL, Uri.parse("tel:+918106886588"));
                try {
                    Toast.makeText(MainActivity.this, "Calling Sudarshan..", Toast.LENGTH_SHORT).show();
                    startActivity(in);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getApplicationContext(), "Failed to call", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetSongs extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Loading Songs...");
            pDialog.setCancelable(false);
            pDialog.show();
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(Constants.SONGS_URL);

            if (jsonStr != null) {
                try {
                    //JSONObject jsonObj = new JSONObject(jsonStr);
                    // Getting JSON Array node
                    JSONArray songs = new JSONArray(jsonStr);

                    // looping through All News
                    for (int i = 0; i < songs.length(); i++) {
                        JSONObject c = songs.getJSONObject(i);

                        String song = c.getString("song");
                        String songUrl = c.getString("url");
                        String artists = c.getString("artists");
                        String cover_image = c.getString("cover_image");

                        SongsPojo songsPojo = new SongsPojo(song, songUrl, cover_image, artists);
                        songsPojoList.add(songsPojo);
                        //Log.d("details",""+song+songUrl+artists+cover_image);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                } catch (Exception e) {
                    Log.e(TAG, "Exception " + e.getLocalizedMessage());
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Check your internet settings",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            songsAdapter.notifyDataSetChanged();
        }

    }

    //Class to download profile
    class DownloadFileFromURL extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }
        @Override
        protected String doInBackground(String... f_url) {
            int count = 0;
            try {
                URL url = new URL(f_url[0]);
                HttpURLConnection conection = (HttpURLConnection) url.openConnection();
                conection.setRequestMethod("GET");
                conection.setDoOutput(true);
                conection.connect();
                // this will be useful so that you can show a tipical 0-100% progress bar
                int lenghtOfFile = conection.getContentLength();
                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                File folder = new File(Environment.getExternalStorageDirectory().toString(), "Sudarshan");
                folder.mkdir();
                File myFile = new File(folder,"sudarshan_cv.pdf");
                try {
                    myFile.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                // Output stream
                FileOutputStream output = new FileOutputStream(myFile);
                byte data[] = new byte[1024];
                long total = 0;
                while ((count = input.read(data)) > 0) {
                    total += count;
                    // publishing the progress....
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    // writing data to file
                    output.write(data, 0, count);
                }
                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }
        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "CV saved in Sudarshan folder", Snackbar.LENGTH_LONG)
                    .setAction("OPEN", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            try {
                                File file = new File(Environment.getExternalStorageDirectory()
                                        + "/Sudarshan/sudarshan_cv.pdf");
                                if (!file.isDirectory())
                                    file.mkdir();
                                Intent testIntent = new Intent("com.adobe.reader");
                                testIntent.setType("application/pdf");
                                testIntent.setAction(Intent.ACTION_VIEW);
                                Uri uri = Uri.fromFile(file);
                                testIntent.setDataAndType(uri, "application/pdf");
                                startActivity(Intent.createChooser(testIntent, "Choose app to open"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
            snackbar.setActionTextColor(Color.RED);
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.YELLOW);
            snackbar.show();
        }
    }
}
