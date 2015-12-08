package edu.cmu.sv.rottentomatoes.controller;

import android.util.Log;

import java.util.ArrayList;

import edu.cmu.sv.rottentomatoes.model.Movie;

/**
 * Created by xingwei on 12/7/15.
 */
public class MovieSeeker extends GenericSeeker<Movie> {

    private static final String MOVIE_SEARCH_PATH = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=9txsnh3qkb5ufnphhqv5tv5z&q=";
    private static final String PAGE_LIMIT = "s&page_limit=6";

    public ArrayList<Movie> find(String query) {
        ArrayList<Movie> moviesList = retrieveMoviesList(query);
        return moviesList;
    }

    public ArrayList<Movie> find(String query, int maxResults) {
        ArrayList<Movie> moviesList = retrieveMoviesList(query);
        return retrieveFirstResults(moviesList, maxResults);
    }

    private ArrayList<Movie> retrieveMoviesList(String query) {
        // Contruct search URL
        query = query.replace(" ", "%20");
        String url = MOVIE_SEARCH_PATH + query + PAGE_LIMIT;

        String response = httpRetriever.retrieve(url);
        Log.d(getClass().getSimpleName(), response);
        return jsonParser.parseMoviesResponse(response);
    }

    @Override
    public String retrieveSearchMethodPath() {
        return MOVIE_SEARCH_PATH;
    }

}
