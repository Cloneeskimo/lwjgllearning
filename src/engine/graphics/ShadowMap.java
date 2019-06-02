package engine.graphics;

import static org.lwjgl.opengl.ARBFramebufferObject.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL30.*;

public class ShadowMap {

    //Static Data
    public static final int SHADOW_MAP_WIDTH = 1024;
    public static final int SHADOW_MAP_HEIGHT = 1024;

    //Data
    private final int depthMapFBO;
    private final Texture depthMap;

    //Constructor
    public ShadowMap() throws Exception { //default

        //create an FBO to render the depth map
        this.depthMapFBO = glGenFramebuffers();

        //create depth map texture
        this.depthMap = new Texture(SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT, GL_DEPTH_COMPONENT);

        //attach the depth map texture to the FBO
        glBindFramebuffer(GL_FRAMEBUFFER, this.depthMapFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.depthMap.getID(), 0);

        //set only depth
        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus((GL_FRAMEBUFFER)) != GL_FRAMEBUFFER_COMPLETE) {
            throw new Exception("Could not create Framebuffer");
        }

        //unbind
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    //Accessors
    public Texture getDepthMap() { return this.depthMap; }
    public int getDepthMapFBO() { return this.depthMapFBO; }

    //Cleanup Method
    public void cleanup() {
        glDeleteFramebuffers(this.depthMapFBO);
        depthMap.cleanup();
    }
}
