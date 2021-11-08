package it.dibis.quickgreenpass;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;

public class LoadAndViewHtmlFile {

    /**
     * Revision control id
     */
    public static final String CVSID = "$Id: LoadAndViewHtmlFile.java,v 1.0 17/03/2020 23:53 adalborgo@gmail.com $";

    //--- Variables ---//
    private static Context context;
    private static String filename;
    private static String header;
    private static boolean zoomKeys;

    private static boolean error = false;

    public LoadAndViewHtmlFile(Context context) {
        this.context = context;
    }

    public boolean loadData(String filename, String header, boolean zoomKeys) {
        this.filename = filename;
        this.header = header;
        this.zoomKeys = zoomKeys;
        new LoadFileAsyncMode(filename);
        return error;
    }

    public boolean getError() {
        return error;
    }

    //------------------------------------------------------------------------------------------//
    //---------------------------- Inner class LoadFileAsyncMode ------------------------------//
    //------------------------------------------------------------------------------------------//
    /**
     * Download one file from URL with no activity and progress bar
     */
    private static class LoadFileAsyncMode extends AsyncTask<String, Integer, Long> { // AsyncTask<Params, Progress, Result>

        private final static String TAG = "LoadFileAsyncMode"; // .class.getSimpleName();

        private String stringFile = null;

        public LoadFileAsyncMode(String filename) {
            execute(filename);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Downloading a file to string in background thread
         */
        @Override
        protected Long doInBackground(String... urlStrings) {
            StringBuilder sb = new StringBuilder();
            error = false;
            try {
                Reader reader = new InputStreamReader(context.getAssets().open(filename));
                BufferedReader inbuf = new BufferedReader(reader);
                String line = null;
                do {
                    line = inbuf.readLine();
                    if (line != null && line.length() > 0) {
                        sb.append(line);
                    }
                } while (line != null);

                inbuf.close(); // Close file

            } catch (Exception e) {
                // Log.e("!!! Error: ", e.getMessage());
                error = true;
            }

            stringFile = sb.toString();
            return (long) stringFile.length();
        }

        /**
         * Updating in progress...
         */
        protected void onProgressUpdate(Integer... progress) { }

        /**
         * After completing background task, startActivity 'HtmlTextView'
         */
        @Override
        protected void onPostExecute(Long result) {
            if (result > 0) {
                error = false;
                Intent htmlTextView = new Intent(context, HtmlTextView.class);
                htmlTextView.putExtra("HEADER", header);
                htmlTextView.putExtra("TEXT", stringFile);
                htmlTextView.putExtra("ZOOM", zoomKeys);
                context.startActivity(htmlTextView);
            } else {
                error = true; // Result: length of file
                // Log.d(TAG, ">>>> File not found!!! <<<<");
            }
        }
    }

}
