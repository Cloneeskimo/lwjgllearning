package engine.graphics;

import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

public class Texture {

    //Data
    private final int id;
    private final int width, height;

    //Constructors
    public Texture(String fileName) throws Exception { //from filename

        //creat buffer
        ByteBuffer buffer;

        //load texture from file
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1); //buffer to hold width
            IntBuffer h = stack.mallocInt(1); //buffer to hold height
            IntBuffer channels = stack.mallocInt(1); //buffer to hold the channel amount

            URL url = Texture.class.getResource(fileName);
            File file = Paths.get(url.toURI()).toFile();
            String filePath = file.getAbsolutePath();
            buffer = stbi_load(filePath, w, h, channels, 4); //four channels: r, g, b, a -
            if (buffer == null) throw new Exception("Image file [" + filePath + "] not loaded: " + stbi_failure_reason());

            this.width = w.get();
            this.height = h.get();
        }

        //set id
        this.id = createTexture(buffer);

        //free image memory
        stbi_image_free(buffer);
    }

    public Texture(ByteBuffer imageBuffer) throws Exception { //from buffer

        //create buffer
        ByteBuffer buffer;

        //load texture from memory
        try (MemoryStack stack = MemoryStack.stackPush()) {

            //allocate space for width, height, channels
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            //load image from memory
            buffer = stbi_load_from_memory(imageBuffer, w, h, channels, 4);
            if (buffer == null) throw new Exception("Image file not loaded: " + stbi_failure_reason());

            //set width and height
            this.width = w.get();
            this.height = h.get();
        }

        //create texture
        this.id = createTexture(buffer);

        //free image memory
        stbi_image_free(buffer);
    }

    //Texture Creation Method
    private int createTexture(ByteBuffer buffer) {

        //Create Texture
        int textureId = glGenTextures(); //Create new OpenGL Texture
        glBindTexture(GL_TEXTURE_2D, textureId); //Bind the texture for work
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1); //Tell OpenGL that each component is one byte in size
        //Sometimes, before using glTexImage2D, filtering parameters are set up. They refer to how
        //the image will be drawn when scaling and how pixels will be interpolated.
        //if those parameters are not set, the texture will not be displayed. They may look something like this:
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        //These basically say that when a pixel is drawn with no direct one-to-one association to a texture coordinate,
        //it will pick the nearest texture coordinate point. Instead of doing this, we can generate mipmaps as well, which
        //we do before glTexImage2D.
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer); //Unpack data
        //Parameters for glTexImage2D:
        //(@target) - target texture (its type) - GL_TEXTURE_2D in this case
        //(@level) - the level-of-detail number. level 0 is the base image level. level n is the nth mipmap reduction image
        //(@internal format) - specifies the number of color components in the color (RGBA in this case)
        //(@width), (@height) - specifies the width and height of the texture image
        //(@border) - this value must be zero
        //(@format) - specifies the format of the pixel data (RGBA in this case)
        //(@type) - specifies the data type of the pixel data. we are using unsigned bytes for this.
        //(@data) - the buffer storing the data
        glGenerateMipmap(GL_TEXTURE_2D); //Generate MipMaps

        //return texture ID
        return textureId;
    }

    //Accessors
    public int getID() { return this.id; }
    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }

    //Other Methods
    public void bind() { glBindTexture(GL_TEXTURE_2D, this.id); }
    public void cleanup() { glDeleteTextures(this.id); }
}
