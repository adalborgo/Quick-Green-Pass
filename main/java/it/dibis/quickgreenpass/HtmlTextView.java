package it.dibis.quickgreenpass;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HtmlTextView extends AppCompatActivity implements Constants {

    // Revision control id
    public static final String CVSID = "$Id: HtmlView.java,v 0.3 26/06/2020 23:53 adalborgo@gmail.com $";

    WebView webView = null;

    boolean backMode = false;
    String header = null;
    String style = null;
    String textToView;
    boolean zoomKeys = true; // Default is true

    int fontSize = DEFAULT_FONT_SIZE;

    private TextView header_activity;

    private ImageView zoomInButton;
    private ImageView zoomOutButton;

    // Get SharedData object (singleton)
    SharedData shared = SharedData.getDataFlash();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.html_view);

        ///-----------------------------------------///
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // Back button pressed
                if (backMode) onBackPressed();
                else finish();
            }
        });

        header_activity = toolbar.findViewById(R.id.header_activity);
        zoomInButton = toolbar.findViewById(R.id.zoom_in_button);
        zoomOutButton = toolbar.findViewById(R.id.zoom_out_button);
        zoomInButton.setOnClickListener(onButtonClick);
        zoomOutButton.setOnClickListener(onButtonClick);
        ///-----------------------------------------///

        fontSize = shared.getFontSize();

        // Init webView
        webView = (WebView) findViewById(R.id.webview);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                webView.scrollTo(0, 0);
            }
        });

        // Get intent data
        Intent mainIntent = getIntent();
        Bundle extras = mainIntent.getExtras();
        if (extras != null) {
            backMode = mainIntent.getExtras().getBoolean("BACK");
            header = mainIntent.getExtras().getString("HEADER");
            style = mainIntent.getExtras().getString("STYLE");
            textToView = mainIntent.getExtras().getString("TEXT");
            zoomKeys = mainIntent.getExtras().getBoolean("ZOOM");
            if (header != null && header.length() > 0) header_activity.setText(header);
        } else {
            finish();
        }

        if (zoomKeys) {
            zoomInButton.setVisibility(View.VISIBLE);
            zoomOutButton.setVisibility(View.VISIBLE);
        } else {
            zoomInButton.setVisibility(View.INVISIBLE);
            zoomOutButton.setVisibility(View.INVISIBLE);
        }

        loadPage(fontSize);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.destroy();
    }

    private void loadPage(int fontSize) {
        String fullHtml = HTML_HEAD1 +
                ((style != null && style.length() > 14) ?
                        "<STYLE TYPE=\"text/css\">\n" + style + "\n</STYLE>" : "") +
                HTML_HEAD2 +
                "<body style=\"font-size: " + fontSize + "%;\">\n" +
                textToView + HTML_FOOTER;

        webView.clearCache(true);
        webView.loadDataWithBaseURL(null, fullHtml, "text/html", "UTF-8", null);
    }

    // onButtonClick
    private View.OnClickListener onButtonClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            switch (v.getId()) {

                //--- Zoom buttons ---//
                case R.id.zoom_in_button:
                    if (fontSize < MAX_FONT_SIZE) fontSize += 10;
                    // Update fontSize
                    shared.setFontSize(fontSize);
                    shared.setDataChanged(true);
                    loadPage(fontSize);
                    break;

                case R.id.zoom_out_button:
                    if (fontSize > 10) fontSize -= 10;
                    // Update fontSize
                    shared.setFontSize(fontSize);
                    shared.setDataChanged(true);
                    loadPage(fontSize);
                    break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        webView.clearCache(true);
        Intent endActivity = new Intent(getApplicationContext(), MainActivity.class);
        endActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        endActivity.putExtra("BUTTONS", true);
        startActivity(endActivity);
    }

}
