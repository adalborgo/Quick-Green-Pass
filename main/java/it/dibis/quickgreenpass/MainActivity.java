package it.dibis.quickgreenpass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.google.android.material.navigation.NavigationView;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import android.annotation.SuppressLint;
import android.util.Log;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, Constants {

    /**
     * Revision control id
     */
    public static final String CVSID = "$Id: it.dibis.quickgreenpass.MainActivity.java,v 0.1 21/10/2021 23:53 adalborgo@gmail.com $";

    private static final String FILENAME = "greenpass.png";

    private static final int LEFT_MARGIN = 10, RIGHT_MARGIN = 10;
    private static final int TOP_MARGIN = 10, BOTTOM_MARGIN = 10;

    private static final float MIN_ZOOM_FACTOR = 0.25f;
    private static final float MAX_ZOOM_FACTOR = 2.0f;

    private final Context context = this;
    private final Activity activity = this;

    private float zoomFactor = 1.0f; // DEFAULT_FONT_SIZE;

    private int widthFree, heightFree;

    private ImageView imageView;
    private Bitmap imageDefault;

    AlertDialog alertBar;
    AlertDialog.Builder progressDialog;

    private String pathname;

    // Get SharedData object (singleton)
    SharedData shared = SharedData.getDataFlash();

    ActivityResultLauncher<Intent> launchGalleryActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            Uri imageUri = data.getData();
                            Bitmap image = decode(imageUri);
                            imageView.setImageBitmap(image);
                            showProgressDialog();
                            saveIfConfirmed(image);
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set content view
        setContentView(R.layout.activity_main);
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        pathname = context.getExternalFilesDir(null) + File.separator + FILENAME;

        //------------ ToolBar ------------//
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set item color:
        navigationView.setItemIconTintList(null);
        navigationView.setItemTextColor(ColorStateList.valueOf(Color.BLACK));
        navigationView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        //---------- End ToolBar ----------//

        // Upload the configuration data
        new LoadSaveConfig(context).loadData();
        zoomFactor = shared.getZoomFactor();
        if (zoomFactor < MIN_ZOOM_FACTOR || zoomFactor > MAX_ZOOM_FACTOR) {
            zoomFactor = 1.0f; // Default value
            shared.setZoomFactor(zoomFactor); // Update
        }

        // Zoom buttons
        ImageView zoomInButton = toolbar.findViewById(R.id.zoom_in_button);
        ImageView zoomOutButton = toolbar.findViewById(R.id.zoom_out_button);
        zoomInButton.setOnClickListener(onButtonClick);
        zoomOutButton.setOnClickListener(onButtonClick);

        imageView = (ImageView) findViewById(R.id.imageview);

        Point screen = MyUtils.getScreenDimension(activity);
        int toolbarHeight = MyUtils.getToolBar(context);
        widthFree = screen.x - (LEFT_MARGIN + RIGHT_MARGIN);
        heightFree = screen.y - (toolbarHeight + TOP_MARGIN + BOTTOM_MARGIN);

        ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();
        params.setMargins(LEFT_MARGIN, toolbarHeight + TOP_MARGIN,
                RIGHT_MARGIN, params.bottomMargin);

        initProgressDialog();

        if (MyUtils.checkPathname(pathname)) {
            imageDefault = adjustImage(pathname);
            imageView.setImageBitmap(resizeBitmap(imageDefault, 0));
        } else {
            openGallery();
        }
    }

    /**
     * Ask for confirmation before saving
     */
    private void saveIfConfirmed(Bitmap image) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), (dialog, id) -> {
                    dialog.cancel();
                    saveImage(image); // Save the selected full image
                    // Before assigning imageDefault, reduce the image if necessary
                    imageDefault = adjustImage(pathname);

                    // Set the zoomFactor value suitable for the screen
                    zoomFactor = ((float) widthFree / (float) imageDefault.getWidth());
                    zoomFactor = (float) Math.round(zoomFactor * 10) / 10; // Round
                    imageView.setImageBitmap(resizeBitmap(imageDefault, 0));

                    // Save new zoomFactor
                    shared.setZoomFactor(zoomFactor);
                    shared.setDataChanged(true);

                    // Hide the progress bar
                    hideProgressDialog();
                })

                .setNegativeButton(getString(R.string.no), (dialog, id) -> {
                    hideProgressDialog();

                    // Reload and resize imageView
                    zoomFactor = shared.getZoomFactor(); // Reload old zoomFactor

                    // Resize imageView imageDefault
                    imageView.setImageBitmap(resizeBitmap(imageDefault, 0));

                    dialog.cancel();
                });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void initProgressDialog() {
        progressDialog = new AlertDialog.Builder(context);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.progress_dialog_layout, null);
        progressDialog.setView(dialogView);
        progressDialog.setCancelable(true);
        alertBar = progressDialog.create();
    }

    public void showProgressDialog() {
        if (alertBar != null) {
            alertBar.show();
        }
    }

    public void hideProgressDialog() {
        if (alertBar != null) {
            alertBar.dismiss();
        }
    }

    /**
     * Upload the file from the gallery
     */
    public void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        launchGalleryActivity.launch(gallery);
    }

    // onButtonClick
    private final View.OnClickListener onButtonClick = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                //--- Zoom buttons ---//
                case R.id.zoom_in_button:
                    if ((zoomFactor + 0.1f) < MAX_ZOOM_FACTOR) {
                        imageView.setImageBitmap(resizeBitmap(imageDefault, +1));

                        // Update zoomFactor
                        shared.setZoomFactor(zoomFactor);
                        shared.setDataChanged(true);
                    }
                    break;

                case R.id.zoom_out_button:
                    if ((zoomFactor - 0.1f) > MIN_ZOOM_FACTOR) {
                        imageView.setImageBitmap(resizeBitmap(imageDefault, -1));

                        // Update zoomFactor
                        shared.setZoomFactor(zoomFactor);
                        shared.setDataChanged(true);
                    }
                    break;
            }
        }
    };

    /**
     * Before exiting, save the data if it has changed
     */
    @Override
    public void onBackPressed() {
        // If the data has changed, save it to the configuration file
        if (shared.getDataChanged())
            new LoadSaveConfig(context).saveData();
        finish();
        moveTaskToBack(true);
    }

    //------------------------ Side menu ------------------------//
    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.gallery:
                openGallery();
                break;

            case R.id.info:
                new LoadAndViewHtmlFile(context).loadData("info.html", "Informazioni", true);
                break;

            case R.id.cancel:
                break;

            case R.id.nav_exit:
                onBackPressed();
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Save the Bitmap image in the local path with FILENAME
     *
     * @param image
     */
    void saveImage(Bitmap image) {
        try {
            FileOutputStream fos = new FileOutputStream(pathname, false);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            Log.d("Error: ", e.toString());
        }
    }

    /**
     * Convert imageUri to Bitmap
     *
     * @param imageUri
     * @return
     */
    Bitmap decode(Uri imageUri) {
        Bitmap bitmap = null;
        ContentResolver contentResolver = getContentResolver();
        try {
            if (Build.VERSION.SDK_INT < 28) {
                bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri);
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(contentResolver, imageUri);
                bitmap = ImageDecoder.decodeBitmap(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    /**
     * @param imagesrc (Bitmap imagesrc = BitmapFactory.decodeFile(new File(pathname).getAbsolutePath()))
     * @param zoomType
     * @return
     */
    private Bitmap resizeBitmap(Bitmap imagesrc, int zoomType) {
        if (imagesrc == null) return null;

        float zoom = zoomFactor; // Default: zoomType = 0;
        if (zoomType > 0) zoom += 0.1f; // zoom in
        if (zoomType < 0) zoom -= 0.1f; //zoom out

        int widthsrc = imagesrc.getWidth(), heightsrc = imagesrc.getHeight();
        int widthZoom = (int) (zoom * widthsrc), heightZoom = (int) (zoom * heightsrc);
        if (widthZoom <= widthFree && heightZoom <= heightFree) {
            zoomFactor = (float) Math.round(zoom * 10) / 10; // Update zoomFactor
            return Bitmap.createScaledBitmap(imagesrc, widthZoom, heightZoom, true);
        } else {
            // Crop the image
            Bitmap imagetmp = Bitmap.createScaledBitmap(imagesrc, widthZoom, heightZoom, true);
            int x = (int) ((widthZoom - widthFree) / 2);
            if (x < 0) x = 0;
            int y = 0;
            int ww = Math.min(widthFree, widthZoom), hh = Math.min(heightFree, heightZoom);
            if ((x + ww) <= imagetmp.getWidth() && (y + hh) <= imagetmp.getHeight()) {
                zoomFactor = (float) Math.round(zoom * 10) / 10; // Update zoomFactor
                return Bitmap.createBitmap(imagetmp, x, y, ww, hh);
            } else {
                return imagesrc;
            }
        }
    }


    /**
     * Reduce the size of the image if it is too large
     *
     * @param pathname
     * @return
     * @see: https://developer.android.com/topic/performance/graphics/load-bitmap#java
     */
    Bitmap adjustImage(String pathname) {
        // Get the dimensions of the image
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(pathname).getAbsolutePath(), options);
        final int width = options.outWidth, height = options.outHeight;

        int widthFactor = width / widthFree;
        if (widthFactor > 2) { // Reduce the size of the image if it is too large
            // Calculate the largest inSampleSize value that is a power of 2
            int reqWidth = width / widthFactor, reqHeight = height / widthFactor;
            int inSampleSize = 1;
            if (width > reqWidth || height > reqHeight) {
                final int halfWidth = width / 2, halfHeight = height / 2;
                while (((halfWidth / inSampleSize) >= reqWidth) && ((halfHeight / inSampleSize) >= reqHeight)) {
                    inSampleSize *= 2;
                }
            }

            // Set inSampleSize: the decoder uses a final value based on powers of 2, any other value will be rounded down to the nearest power of 2.
            options.inSampleSize = inSampleSize;

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(new File(pathname).getAbsolutePath(), options);

        } else { // No change
            return BitmapFactory.decodeFile(new File(pathname).getAbsolutePath());
        }
    }

}
