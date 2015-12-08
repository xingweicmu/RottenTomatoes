package edu.cmu.sv.rottentomatoes;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by xingwei on 12/7/15.
 */
public class MovieSeeker extends GenericSeeker<Movie> {

    private static final String MOVIE_SEARCH_PATH = "Movie.search/";

    public ArrayList<Movie> find(String query) {
        ArrayList<Movie> moviesList = retrieveMoviesList(query);
        return moviesList;
    }

    public ArrayList<Movie> find(String query, int maxResults) {
        ArrayList<Movie> moviesList = retrieveMoviesList(query);
        return retrieveFirstResults(moviesList, maxResults);
    }

    private ArrayList<Movie> retrieveMoviesList(String query) {
//        String url = constructSearchUrl(query);
        query = query.replace(" ", "%20");
        String url = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=9txsnh3qkb5ufnphhqv5tv5z&q="+query+"s&page_limit=6";
        String response = httpRetriever.retrieve(url);
        Log.d(getClass().getSimpleName(), response);
        return jsonParser.parseMoviesResponse(response);
//        return new ArrayList<Movie>();
    }

    @Override
    public String retrieveSearchMethodPath() {
        return MOVIE_SEARCH_PATH;
    }

}
