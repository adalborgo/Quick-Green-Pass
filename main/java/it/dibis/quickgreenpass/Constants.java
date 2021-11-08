package it.dibis.quickgreenpass;

/**
 * Common constants
 *
 * @author Antonio Dal Borgo (adalborgo@gmail.com)
 */
public interface Constants {

    /**
     * Revision control id
     */
    String cvsId = "$Id: it.dibis.listviewimages.Constants.java,v 0.1 01/09/2018 23:59:59 adalborgo $";

    int DEFAULT_FONT_SIZE = 120;
    int MAX_FONT_SIZE = 300;

    String HTML_HEAD1 =
            "<!DOCTYPE HTML>\n" +
                    "<html lang=\"it\">\n" +
                    "<head>\n" +
                    "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">\n" +
                    "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1, user-scalable=yes\">\n";

    String HTML_HEAD2 = "\n</head>\n";

    String HTML_HEAD_BODY_FONTSIZE = HTML_HEAD1 + HTML_HEAD2 + "<body style=\"font-size: ";

    String HTML_FOOTER = "\n</body>\n</html>\n";

    String STYLE_TITLE = ".title { text-align: center; margin-top: 0px; margin-bottom: -0.7em; " +
            "font-weight: bold; color: #003366; }";

}
