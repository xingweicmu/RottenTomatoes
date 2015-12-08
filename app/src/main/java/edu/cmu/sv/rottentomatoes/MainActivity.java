package edu.cmu.sv.rottentomatoes;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final String EMPTY_STRING = "";

    private EditText searchEditText;
    private Button searchButton;
    private ListView listView;
    private GenericSeeker<Movie> movieSeeker = new MovieSeeker();
    private ProgressDialog progressDialog;
    public static List<Movie> publicMovieList;
    public static Movie selectedOne;
    public static final String MY_PREFS_NAME = "movies";

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.findAllViewsById();

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = searchEditText.getText().toString();
                longToast("search!" + query);
                performSearch(query);
            }
        });

        searchEditText.setOnFocusChangeListener(new DftTextOnFocusListener(getString(R.string.search)));

    }
    private class PerformMovieSearchTask extends AsyncTask<String, Void, List<Movie>> {

        @Override
        protected List<Movie> doInBackground(String... params) {
            String query = params[0];
            List<Movie> result =  movieSeeker.find(query);

            //write local file
            try {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/movies";
                final File dir = new File(path);
                dir.mkdirs();
                final File file = new File(dir, query+"_file");
                file.createNewFile();
                final FileOutputStream fos = new FileOutputStream(file);
                ObjectOutputStream out = new ObjectOutputStream(fos);
                out.writeObject(result);
                out.close();
                fos.close();
                Log.d("Test##", "write success");
            }
            catch (IOException e) {
                Log.e("Exception", "File write failed: " + e.toString());
            }

            // Write to Cache(SharedPreference, and local file)
            SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString(query, query+"_file");
            editor.commit();

            return result;
        }

        @Override
        protected void onPostExecute(final List<Movie> result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (progressDialog!=null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }
                    if (result!=null) {

                        publicMovieList = result;
//                        Intent intent = new Intent(MainActivity.this, MoviesListActivity.class);
//                        startActivity(intent);
                        ArrayAdapter<Movie> moviesAdapter = new MoviesAdapter(MainActivity.this, R.layout.list_row, result);
                        listView.setAdapter(moviesAdapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                                Movie movie = result.get(position);
                                selectedOne = movie;
                                longToast("Item selected! "+movie.name);
                                // show detail Dialog
                                showDetailDialog(movie.overview);
                            }
                        });
                    }
                }
            });
        }

    }

    public void showDetailDialog(String content) {
        new AlertDialog.Builder(MainActivity.this)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setTitle("Movie Detail")
                .setMessage(content)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    private void performSearch(String query) {

        // Before doing the querying, try to load from local files
        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        String cached = prefs.getString(query, null);
        if(cached != null) {
            //Read from local files
            longToast("Read from cached file " + cached);

            //Deserialize from file
            final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/movies";
            final File dir = new File(path+"/"+cached);
            try {
                FileInputStream fileIn = new FileInputStream(dir);
                ObjectInputStream in = new ObjectInputStream(fileIn);
//                longToast("Deserialized Data:" + in.readObject().toString());
                publicMovieList = (ArrayList<Movie>)in.readObject();
                in.close();
                fileIn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Render out the data
            ArrayAdapter<Movie> moviesAdapter = new MoviesAdapter(MainActivity.this, R.layout.list_row, publicMovieList);
            listView.setAdapter(moviesAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    Movie movie = publicMovieList.get(position);
                    selectedOne = movie;
                    longToast("Item selected! "+movie.name);
                    // show detail Dialog
                    showDetailDialog(movie.overview);
                }
            });

        }
        else {
            // Invoke remote API
            if(isNetworkAvailable()) {
                progressDialog = ProgressDialog.show(MainActivity.this,
                        "Please wait...", "Retrieving data...", true, true);

                PerformMovieSearchTask task = new PerformMovieSearchTask();
                task.execute(query);
                progressDialog.setOnCancelListener(new CancelTaskOnCancelListener(task));
            }
            else{
                new AlertDialog.Builder(MainActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Alert")
                        .setMessage("Please check you network connection")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class CancelTaskOnCancelListener implements DialogInterface.OnCancelListener {
        private AsyncTask<?, ?, ?> task;
        public CancelTaskOnCancelListener(AsyncTask<?, ?, ?> task) {
            this.task = task;
        }
        @Override
        public void onCancel(DialogInterface dialog) {
            if (task!=null) {
                task.cancel(true);
            }
        }
    }

    private void findAllViewsById() {
        searchEditText = (EditText) findViewById(R.id.editText);
        searchButton = (Button) findViewById(R.id.button);
        listView = (ListView)findViewById(R.id.listView);
    }

    public void longToast(CharSequence message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    private class DftTextOnFocusListener implements View.OnFocusChangeListener {

        private String defaultText;

        public DftTextOnFocusListener(String defaultText) {
            this.defaultText = defaultText;
        }

        public void onFocusChange(View v, boolean hasFocus) {
            if (v instanceof EditText) {
                EditText focusedEditText = (EditText) v;
                // handle obtaining focus
                if (hasFocus) {
                    if (focusedEditText.getText().toString().equals(defaultText)) {
                        focusedEditText.setText(EMPTY_STRING);
                    }
                }
                // handle losing focus
                else {
                    if (focusedEditText.getText().toString().equals(EMPTY_STRING)) {
                        focusedEditText.setText(defaultText);
                    }
                }
            }
        }

    }

}