package engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    //Data
    private final String title; //window title
    private int width; //window width
    private int height; //window height
    private long windowHandle; //handle to GL window
    private boolean resized; //whether the window has been resized or not
    private boolean vSync; //whether to use V-Sync or not

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

        //Setup an error callback. The default implementation will
        //print the error message in System.err
        GLFWErrorCallback.createPrint(System.err).set();

        //Initialize GLFW. Most GLFW functions will not work before
        if (!glfwInit()) throw new IllegalStateException("Unable to initialize GLFW");

        //Set window hints
        glfwDefaultWindowHints(); //optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); //window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); //window will be resizable
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        //Create the window
        this.windowHandle = glfwCreateWindow(this.width, this.height, this.title, NULL, NULL);
        if (this.windowHandle == NULL) throw new RuntimeException("Failed to create the GLFW window");

        //Setup resize callback
        glfwSetFramebufferSizeCallback(this.windowHandle, (window, width, height) -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });

        //Setup a key callback. It will be called every time a key is pressed, repeated, or released
        glfwSetKeyCallback(this.windowHandle, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true); //We will detect this in the rendering loop
        });

        //Get the resolution of the primary monitor
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

        //Center the window
        glfwSetWindowPos(this.windowHandle, (vidmode.width() - this.width) / 2, (vidmode.height() - this.height) / 2);

        //Make the OpenGL context current
        glfwMakeContextCurrent(this.windowHandle);

        //V-Sync
        if (this.isvSync()) {
            glfwSwapInterval(1);
        }

        //Make window visible, create capabilities
        glfwShowWindow(this.windowHandle);
        GL.createCapabilities();

        //Set clear color
        glClearColor(1.0f, 1.0f, 1.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
    }

    //Other Methods
    public void update() {
        glfwSwapBuffers(this.windowHandle);
        glfwPollEvents(); //this is where our key callback will be called from
    }

    //Accessors
    public boolean isvSync() { return this.vSync; }
    public boolean isKeyPressed(int keyCode) { return glfwGetKey(this.windowHandle, keyCode) == GLFW_PRESS; }
    public boolean shouldClose() { return glfwWindowShouldClose(this.windowHandle); }
    public String getTitle() { return this.title; }
    public int getWidth() { return this.width; }
    public int getHeight() { return this.height; }
    public boolean isResized() { return this.resized; }

    //Mutators
    public void setClearColor(float r, float g, float b, float a) { glClearColor(r, g, b, a); }
    public void setResized(boolean resized) { this.resized = resized; }
    public void setvSync(boolean vSync) { this.vSync = vSync; }
}
