package edu.cmu.sv.rottentomatoes;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;

/**
 * Created by xingwei on 12/7/15.
 */
public class ImageActivity extends Activity {

    private HttpRetriever httpRetriever = new HttpRetriever();
//    private String url = "http://content6.flixster.com/movie/11/13/43/11134356_tmb.jpg";
    private String url = MainActivity.selectedOne.imagesList.get(0).url;
    private ImageView imageView;
    int counter = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);

        imageView = (ImageView) findViewById(R.id.imageView1);

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

        Button nextButton = (Button)findViewById(R.id.btnChangeImage);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set url a new value
                counter++;
                if(counter >= MainActivity.selectedOne.imagesList.size())
                    counter = 0;
                url = MainActivity.selectedOne.imagesList.get(counter).url;

                Toast.makeText(ImageActivity.this, url, Toast.LENGTH_SHORT).show();
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
        });

    }

    private Bitmap fetchBitmapFromCache(String url) {

        synchronized (bitmapCache) {
            final Bitmap bitmap = bitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in cache
                // Move element to first position, so that it is removed last
                bitmapCache.remove(url);
                bitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

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


    private LinkedHashMap<String, Bitmap> bitmapCache = new LinkedHashMap<String, Bitmap>();

    private void addBitmapToCache(String url, Bitmap bitmap) {
        if (bitmap != null) {
            synchronized (bitmapCache) {
                bitmapCache.put(url, bitmap);
            }
        }
    }


}
