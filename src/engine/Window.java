package engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    //Statics
    public static boolean MOUSE_GRABBED = false;

    //Data
    private final String title; //window title
    private int width; //window width
    private int height; //window height
    private long windowHandle; //handle to GL window
    private boolean resized; //whether the window has been resized or not
    private boolean vSync; //whether to use V-Sync or not
    private int polgyonMode = GL_FILL; //polygon mode
    private boolean cullFace = true; //face culling

    //Constructor
    public Window(String title, int width, int height, boolean vSync) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.vSync = vSync;
        this.resized = false;
    }

    //Initialization Method
    public void init() {

        //setup an error callback. The default implementation will
        //print the error message in System.err
        GLFWErrorCallback.createPrint(System.err).set();

        //initialize GLFW - most GLFW functions will not work before
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        //set window hints
        glfwDefaultWindowHints(); //optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); //window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); //window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        //create the window
        this.windowHandle = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (this.windowHandle == NULL) throw new RuntimeException("Failed to create the GLFW window");

        //get window frame size
        try (MemoryStack stack = MemoryStack.stackPush()) {

            //allocate space for width, height
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);

            //load image from memory
            glfwGetFramebufferSize(this.windowHandle, w, h);

            //set width and height
            this.width = w.get();
            this.height = h.get();
        }

        //setup resize callback
        glfwSetFramebufferSizeCallback(this.windowHandle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });

        //setup a key callback. It will be called every time a key is pressed, repeated, or released
        glfwSetKeyCallback(this.windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) //close window
                glfwSetWindowShouldClose(window, true); //We will detect this in the rendering loop
            if (key == GLFW_KEY_1 && action == GLFW_RELEASE) //change polygon mode
                this.togglePolygonMode();
            if (key == GLFW_KEY_2 && action == GLFW_RELEASE)
                this.toggleMouseGrab();
            if (key == GLFW_KEY_3 && action == GLFW_RELEASE)
                this.toggleCullFace();
        });

        //get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        //center the window
        glfwSetWindowPos(this.windowHandle, (vidmode.width() - this.width) / 2, (vidmode.height() - this.height) / 2);

        //make the OpenGL context current
        glfwMakeContextCurrent(this.windowHandle);

        //v-sync
        if (this.isvSync()) {
            glfwSwapInterval(1);
        }

        //make window visible, create capabilities
        glfwShowWindow(this.windowHandle);
        GL.createCapabilities();

        //set clear color
        glClearColor(0.0f, 0.3f, 0.6f, 0.0f);
        glEnable(GL_DEPTH_TEST);

        //enable support for transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        //enable face culling
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
    }

    //Other Methods
    public void update() {
        glfwSwapBuffers(this.windowHandle);
        glfwPollEvents(); //this is where our key callback will be called from
    }

    private void togglePolygonMode() {
        this.polgyonMode = (polgyonMode == GL_LINE ? GL_FILL : GL_LINE);
        glPolygonMode(GL_FRONT_AND_BACK, this.polgyonMode);
    }

    private void toggleMouseGrab() {
        Window.MOUSE_GRABBED = !Window.MOUSE_GRABBED;
        glfwSetInputMode(this.windowHandle, GLFW_CURSOR, Window.MOUSE_GRABBED ? GLFW_CURSOR_DISABLED : GLFW_CURSOR_NORMAL);
    }

    private void toggleCullFace() {
        this.cullFace = !this.cullFace;
        if (this.cullFace) {
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }
        else glDisable(GL_CULL_FACE);
    }

    //Accessors
    public boolean isvSync() { return this.vSync; }
    public boolean isKeyPressed(int keyCode) { return glfwGetKey(this.windowHandle, keyCode) == GLFW_PRESS; }
    public boolean shouldClose() { return glfwWindowShouldClose(this.windowHandle); }
    public boolean isResized() { return this.resized; }
    public long getWindowHandle() { return this.windowHandle; }
    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }

    //Mutators
    public void setResized(boolean resized) { this.resized = resized; }
}
