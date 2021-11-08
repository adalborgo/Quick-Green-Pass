package it.dibis.quickgreenpass;

/**
 * SharedData.java
 * Singleton Object for data sharing
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 */
public class SharedData {
    /**
     * Revision control id
     */
    public static final String CVSID = "$Id: it.dibis.quickgreenpass.SharedData.java,v 0.1 06/10/2021 23:59:59 adalborgo@gmail.com $";

    // Singleton
    private static SharedData sharedData = null;
    static final int DEFAULT_FONT_SIZE = 120;

    //--- Variables ---//
    private float zoomFactor = 1.0f;
    int fontSize = DEFAULT_FONT_SIZE;
    private boolean dataChanged = false; // Has the data been changed?

    /**
     * A private Constructor prevents any other class from instantiating
     * <p>
     * DO NOT instantiate with the operator new, use:
     * SharedData sharedData = getSingletonObject(); // Create the Singleton Object
     */
    private SharedData() { /* Default constructor */ }

    /**
     * @return sharedData
     */
    public static synchronized SharedData getDataFlash() {
        if (sharedData == null) sharedData = new SharedData();
        return sharedData;
    }

    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    // Set/get zoomFactor
    public void setZoomFactor(float zoomFactor) { this.zoomFactor = zoomFactor; }
    public float getZoomFactor() { return this.zoomFactor; }

    // Set/get fontSize
    public void setFontSize(int fontSize) { this.fontSize = fontSize; }
    public int getFontSize() { return this.fontSize; }

    // Set/get dataChanged
    public void setDataChanged(boolean state) {
        this.dataChanged = state;
    }
    public boolean getDataChanged() {
        return this.dataChanged;
    }
}
