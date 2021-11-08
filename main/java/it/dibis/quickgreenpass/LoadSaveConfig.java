package it.dibis.quickgreenpass;

import android.content.Context;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class LoadSaveConfig {

    /**
     * Revision control id
     */
    public static final String CVSID = "$Id: LoadSaveConfig.java,v 0.2 08/10/2021 23:59:59 adalborgo@gmail.com $";

    static final String CONFIG_FILENAME = "config.prop";
    static final String ZOOMFACTOR = "ZoomFactor";
    static final String FONTSIZE = "FontSize";

    // Get SharedData object (singleton)
    SharedData shared = SharedData.getDataFlash();

    Properties config = new Properties();

    Context context = null;
    String pathname = null;

    public LoadSaveConfig(Context context) {
        this.context = context;
        pathname = context.getExternalFilesDir(null) + File.separator + CONFIG_FILENAME;
    }

    // Load config data
    public void loadData() {
        boolean fileFound = false;

        // Check if pathname exists
        if (MyUtils.checkPathname(pathname)) {
            try {
                FileInputStream fos = new FileInputStream(pathname);
                config.load(fos);
                fos.close();
                fileFound = true;

                // Load and set zoomFactor
                float zoomFactor = MyUtils.stringToFloat(config.getProperty(ZOOMFACTOR));
                shared.setZoomFactor((zoomFactor > 0) ? zoomFactor : 1.0f);

                // Load and set fontSize
                int fontSize = MyUtils.stringToIntPositive(config.getProperty(FONTSIZE));
                shared.setFontSize((fontSize > 0) ? fontSize : Constants.DEFAULT_FONT_SIZE);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // If file config not found or load error, set zoomFactor = 1.0f
        if (!fileFound) {
            shared.setZoomFactor(1.0f);
            shared.setFontSize(Constants.DEFAULT_FONT_SIZE);
        }
    }

    /**
     * Save config data
     *
     * @return
     */
    public boolean saveData() {
        config.setProperty(ZOOMFACTOR, String.valueOf(shared.getZoomFactor()));
        config.setProperty(FONTSIZE, String.valueOf(shared.getFontSize()));

        try {
            FileOutputStream fos = new FileOutputStream(pathname, false);
            config.store(fos, null);
            fos.close();
            shared.setDataChanged(true); // Reset flag
        } catch (Exception e) { // Error
            e.printStackTrace();
            shared.setDataChanged(false);
        }

        return shared.getDataChanged();
    }
}

