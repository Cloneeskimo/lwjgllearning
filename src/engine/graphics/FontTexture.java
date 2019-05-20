package engine.graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Map;

public class FontTexture {

    //Static Data
    private static final String IMAGE_FORMAT = "png";

    //Instance Data
    private final Font font;
    private final String charSetName;
    private final Map<Character, CharInfo> charMap;
    private Texture texture;
    private int width, height;

    //Constructor
    public FontTexture(Font font, String charSetName) throws  Exception {
        this.font = font;
        this.charSetName = charSetName;
        this.charMap = new HashMap<>();
        this.buildTexture();
    }

    //Texture Building Method
    private void buildTexture() throws Exception {

        //get font metrics for each character for the selected font by using image
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2D = image.createGraphics();
        g2D.setFont(this.font);
        FontMetrics fontMetrics = g2D.getFontMetrics();

        //get characters and initialize width and height
        String allChars = getAllAvailableChars();
        this.width = this.height = 0;

        //loop through each character
        for (char c : allChars.toCharArray()) {

            //get size for each character and update global image size
            CharInfo charInfo = new CharInfo(width, fontMetrics.charWidth(c));
            this.charMap.put(c, charInfo);
            this.width += charInfo.getWidth();
            this.height = Math.max(this.height, fontMetrics.getHeight());
        }

        //dispose
        g2D.dispose();

        //create image associated with the charset
        //this image will not have a power of two, but that's ok for most modern cards
        //and can be circumvented by adding extra space
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        g2D = image.createGraphics();
        g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2D.setFont(this.font);
        fontMetrics = g2D.getFontMetrics();
        g2D.setColor(Color.WHITE);
        g2D.drawString(allChars, 0, fontMetrics.getAscent());
        g2D.dispose();

        //flush image into a byte buffer
        ByteBuffer buffer = null;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(image, IMAGE_FORMAT, out);
            out.flush();
            byte[] data = out.toByteArray();
            buffer = ByteBuffer.allocateDirect(data.length);
            buffer.put(data, 0, data.length);
            buffer.flip();
        }

        //create texture
        this.texture = new Texture(buffer);
    }

    //Other Methods
    private String getAllAvailableChars() {

        //create charset encoder and stringbuilder for result
        CharsetEncoder ce = Charset.forName(this.charSetName).newEncoder();
        StringBuilder result = new StringBuilder();

        //loop through each character
        for (char c = 0; c < Character.MAX_VALUE; c++) {
            if (ce.canEncode(c)) result.append(c);
        }

        //return string
        return result.toString();
    }

    //Accessors
    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }
    public Texture getTexture() { return this.texture; }
    public CharInfo getCharInfo(char c) { return this.charMap.get(c); }

    //CharInfo Inner Class
    public static class CharInfo {

        //Data
        private final int startX;
        private final int width;

        //Constructor
        public CharInfo(int startX, int width) {
            this.startX = startX;
            this.width = width;
        }

        //Accessors
        public int getStartX() { return this.startX; }
        public int getWidth() { return this.width; }
    }
}