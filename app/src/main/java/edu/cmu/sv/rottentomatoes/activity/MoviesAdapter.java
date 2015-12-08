package edu.cmu.sv.rottentomatoes.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;
import java.util.List;

import edu.cmu.sv.rottentomatoes.R;
import edu.cmu.sv.rottentomatoes.model.Movie;
import edu.cmu.sv.rottentomatoes.utils.FlushedInputStream;
import edu.cmu.sv.rottentomatoes.utils.HttpRetriever;

/**
 * Created by xingwei on 12/7/15.
 */
public class MoviesAdapter extends ArrayAdapter<Movie> {

    private HttpRetriever httpRetriever = new HttpRetriever();

    private List<Movie> movieDataItems;

    private Activity context;

    public MoviesAdapter(Activity context, int textViewResourceId, List<Movie> movieDataItems) {
        super(context, textViewResourceId, movieDataItems);
        this.context = context;
        this.movieDataItems = movieDataItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.list_row, null);
        }

        final Movie movie = movieDataItems.get(position);

        if (movie != null) {

            // name
            TextView nameTextView = (TextView) view.findViewById(R.id.name_text_view);
            nameTextView.setText(movie.name);

            // rating
            TextView ratingTextView = (TextView) view.findViewById(R.id.rating_text_view);
            ratingTextView.setText("Rating: " + movie.rating);

            // released
            TextView releasedTextView = (TextView) view.findViewById(R.id.released_text_view);
            releasedTextView.setText("Release Year: " + movie.released);

            // certification
            TextView certificationTextView = (TextView) view.findViewById(R.id.certification_text_view);
            certificationTextView.setText("Certification: " + movie.certification);

            // language
            TextView languageTextView = (TextView) view.findViewById(R.id.language_text_view);
            languageTextView.setText("Run Time: " + movie.runtime);

            // thumb image
            ImageView imageView = (ImageView) view.findViewById(R.id.movie_thumb_icon);
            String url = movie.retrieveThumbnail();

            if (url!=null) {
                Bitmap bitmap = fetchBitmapFromCache(url);
                if (bitmap==null) {
                    new BitmapDownloaderTask(imageView).execute(url);
                }
                else {
                    imageView.setImageBitmap(bitmap);
                }
            }
            else {
                imageView.setImageBitmap(null);
            }

        }


        ImageView deleteImg=(ImageView)view.findViewById(R.id.movie_thumb_icon);

        deleteImg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Toast.makeText(context, movie.id, Toast.LENGTH_SHORT).show();
                //Try to find the id and set the selected one
                for(int i = 0; i < MainActivity.publicMovieList.size(); i++) {
                    if(movie.id.equals(MainActivity.publicMovieList.get(i).id)) {
                        MainActivity.selectedOne = MainActivity.publicMovieList.get(i);
                    }
                }

                // Jump to picture viewer activity
                if(MainActivity.selectedOne != null) {
                    Intent intent = new Intent(context, ImageActivity.class);
                    context.startActivity(intent);
                }

            }
        });

        return view;

    }

    private LinkedHashMap<String, Bitmap> bitmapCache = new LinkedHashMap<String, Bitmap>();

    private void addBitmapToCache(String url, Bitmap bitmap) {

        // Write image to external storage
        url = url.replace("/", "").replace(":", "").replace(".","");
        File sdCardDirectory = Environment.getExternalStorageDirectory();
        File image = new File(sdCardDirectory, "/movies/"+url+".png");
        boolean success = false;

        // Encode the file as a PNG image.
        FileOutputStream outStream;
        try {

            outStream = new FileOutputStream(image);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);

            outStream.flush();
            outStream.close();
            success = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (success) {
            Toast.makeText(context, "Image saved with success",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context,
                    "Error during image saving", Toast.LENGTH_SHORT).show();
        }


    }

    private Bitmap fetchBitmapFromCache(String url) {

        url = url.replace("/", "").replace(":", "").replace(".","");
        Bitmap bitmap = BitmapFactory.decodeFile("/sdcard/movies/"+url+".png");
        if(bitmap != null)
            return bitmap;
        return null;

    }

    private class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {

        private String url;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            InputStream is = httpRetriever.retrieveStream(url);
            if (is==null) {
                return null;
            }
            return BitmapFactory.decodeStream(new FlushedInputStream(is));
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            addBitmapToCache(url, bitmap);

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

}
