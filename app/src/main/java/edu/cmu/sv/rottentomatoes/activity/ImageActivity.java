package edu.cmu.sv.rottentomatoes.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.LinkedHashMap;

import edu.cmu.sv.rottentomatoes.R;
import edu.cmu.sv.rottentomatoes.utils.FlushedInputStream;
import edu.cmu.sv.rottentomatoes.utils.HttpRetriever;

/**
 * Created by xingwei on 12/7/15.
 */
public class ImageActivity extends Activity {

    private HttpRetriever httpRetriever = new HttpRetriever();
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
                String type = MainActivity.selectedOne.imagesList.get(counter).size;

                Toast.makeText(ImageActivity.this, type+":"+url, Toast.LENGTH_SHORT).show();
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

        Button prevButton = (Button)findViewById(R.id.previousButton);
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set url a new value
                counter--;
                if(counter < 0)
                    counter = MainActivity.selectedOne.imagesList.size() - 1;
                url = MainActivity.selectedOne.imagesList.get(counter).url;
                String type = MainActivity.selectedOne.imagesList.get(counter).size;

                Toast.makeText(ImageActivity.this, type+":"+url, Toast.LENGTH_SHORT).show();
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
            Toast.makeText(ImageActivity.this, "Image saved with success",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(ImageActivity.this,
                    "Error during image saving", Toast.LENGTH_SHORT).show();
        }
    }


}
