package edu.cmu.sv.rottentomatoes;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xingwei on 12/7/15.
 */
public class MoviesListActivity extends ListActivity {

    private static final String IMDB_BASE_URL = "http://m.imdb.com/title/";

    private List<Movie> moviesList;
    private ArrayAdapter<Movie> moviesAdapter;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_view);

        moviesList = MainActivity.publicMovieList;
        moviesAdapter = new MoviesAdapter(this, R.layout.list_row, moviesList);

        setListAdapter(moviesAdapter);

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);
        Movie movie = moviesAdapter.getItem(position);
//
//        String imdbId = movie.imdbId;
//        if (imdbId==null || imdbId.length()==0) {
//            longToast("No Movies Found");
//            return;
//        }
//
//        String imdbUrl = IMDB_BASE_URL + movie.imdbId;
//
//        Intent imdbIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imdbUrl));
//        startActivity(imdbIntent);
        longToast("Item selected!");

    }

    public void longToast(CharSequence message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

}
