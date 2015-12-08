package edu.cmu.sv.rottentomatoes;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by xingwei on 12/7/15.
 */
public class jsonParser {

    public ArrayList<Movie> parseMoviesResponse(String response) {
        ArrayList<Movie> movies = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            //                JSONArray array = null;

            if (jsonObject != null) {
                JSONArray array = jsonObject.getJSONArray("movies");
                for (int i = 0; i < array.length(); i++) {
                    JSONObject item = array.getJSONObject(i);

                    String title = item.getString("title");
                    String id = item.getString("id");
                    String year = item.getString("year");
                    String runtime = item.getString("runtime");
                    String rating = item.getString("mpaa_rating");
                    String overview = item.getString("synopsis");
                    Log.d("movie##", "title: " + title);

                    Movie oneMovie = new Movie();
                    oneMovie.name = title;
                    oneMovie.id = id;
                    oneMovie.released = year;
                    oneMovie.runtime = runtime;
                    oneMovie.rating = rating;
                    oneMovie.overview = overview;

                    // Get Image List
                    ArrayList<Image> imageList = new ArrayList<>();
                    JSONObject posters = item.getJSONObject("posters");
                    // thumnail
                    String thumbnailUrl = posters.getString("thumbnail");
                    Image thumbImage = new Image();
                    thumbImage.url = thumbnailUrl;
                    thumbImage.type = Image.TYPE_POSTER;
                    thumbImage.size = Image.SIZE_THUMB;
                    imageList.add(thumbImage);

                    // profile
                    String profileUrl = posters.getString("profile");
                    Image profileImage = new Image();
                    profileImage.url = profileUrl;
                    profileImage.type = Image.TYPE_POSTER;
                    profileImage.size = Image.TYPE_PROFILE;
                    imageList.add(profileImage);

                    // detailed
                    String detailUrl = posters.getString("detailed");
                    Image detailImage = new Image();
                    detailImage.url = detailUrl;
                    detailImage.type = Image.TYPE_POSTER;
                    detailImage.size = Image.SIZE_MID;
                    imageList.add(detailImage);

                    // original
                    String originalUrl = posters.getString("original");
                    Image originalImage = new Image();
                    originalImage.url = detailUrl;
                    originalImage.type = Image.TYPE_POSTER;
                    originalImage.size = Image.SIZE_ORIGINAL;
                    imageList.add(originalImage);

                    oneMovie.imagesList = imageList;

                    movies.add(oneMovie);

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movies;
    }
}
