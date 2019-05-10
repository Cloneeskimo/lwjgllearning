package engine;

import org.joml.Vector2d;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class MouseInput {

    //Data
    private final Vector2d previousPos;
    private final Vector2d currentPos;
    private final Vector2f displVec;
    private boolean inWindow = false;
    private boolean leftButtonPressed = false;
    private boolean rightButtonPressed = false;

    //Constructor
    public MouseInput() {
        this.previousPos = new Vector2d(-1, -1);
        this.currentPos = new Vector2d(0, 0);
        displVec = new Vector2f();
    }

    //Initialize Method
    public void init(Window window) {

        //Set position callback
        glfwSetCursorPosCallback(window.getWindowHandle(), (windowHandle, xpos, ypos) -> {
           this.currentPos.x = xpos;
           this.currentPos.y = ypos;
        });

        //Set inWindow callback
        glfwSetCursorEnterCallback(window.getWindowHandle(), (windowHandle, entered) -> {
            this.inWindow = entered;
        });

        //Set click callback
        glfwSetMouseButtonCallback(window.getWindowHandle(), (windowHandle, button, action, mode) -> {
            this.leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            this.rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    //Accessors
    public Vector2f getDisplVec() { return this.displVec; }
    public boolean isLeftButtonPressed() { return this.leftButtonPressed; }
    public boolean isRightButtonPressed() { return this.rightButtonPressed; }

    //Other Methods
    public void input(Window window) {
        displVec.x = displVec.y = 0;
        if (inWindow) {
            double deltaX = currentPos.x - previousPos.x;
            double deltaY = currentPos.y - previousPos.y;
            if (deltaX != 0) displVec.y = (float)deltaX;
            if (deltaY != 0) displVec.x = (float)deltaY;
        }
        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;
    }
}
