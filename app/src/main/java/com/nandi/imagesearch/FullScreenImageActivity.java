package com.nandi.imagesearch;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nandi.imagesearch.R;
import com.nandi.imagesearch.model.Data;
import com.squareup.picasso.Picasso;

/**
 * A full-screen activity showing the selected image and its title
 */
public class FullScreenImageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen_image);

        // get the extras passed through intent
        Bundle bundle = getIntent().getExtras();
        TextView titleTextView = (TextView) findViewById(R.id.titleText);
        ImageView imgView = (ImageView) findViewById(R.id.imageView);

        if(bundle != null) {
            Data data = (Data) bundle.get("selectedImgData");
            String title = data.title;
            if(title!=null){
                titleTextView.setText(title);
            }

            String link = data.link;
            // append h to end of image name to load high quality image
            if(link.contains(".jpg")) {
                link.replace(".jpg","h.jpg");
            } else {
                link.replace(".png","h.png");
            }

            Uri uri = Uri.parse(link);
            // using picasso library to load image into the image view
            Picasso.with(this).load(uri).into(imgView);
        }
    }
}
