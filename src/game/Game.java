package game;

import engine.*;
import engine.gameitem.GameItem;
import engine.gameitem.SkyBox;
import engine.gameitem.Terrain;
import engine.graphics.*;
import engine.graphics.light.DirectionalLight;
import engine.graphics.light.LightPoint;
import engine.graphics.light.SceneLighting;
import engine.graphics.light.SpotLight;
import engine.graphics.weather.Fog;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;

public class Game implements IGameLogic {

    //Static Data
    private static final float MOUSE_SENSITIVITY = 0.45f;
    private static final float CAMERA_SPEED = 0.05f;

    //Non-Lighting Instance Data
    private final Vector3f cameraInc;
    private final Renderer renderer;
    private final Camera camera;

    //Scene and HUD
    private Scene scene;
    private GameItem item;
    private Hud hud;
    private float directionalLightAngle;
    private float directionalLightAngleInc;

    //Constructor
    public Game() {
        this.renderer = new Renderer();
        this.camera = new Camera();
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.directionalLightAngle = 45;
    }

    //Initialization Method
    @Override
    public void init(Window window) throws Exception {

        //initialize renderer and create scene
        this.renderer.init(window);
        this.scene = new Scene();

        //setup cube
        float reflectance = 1f;
        Mesh itemMesh = OBJLoader.loadMesh("/models/pillar.obj");
        Texture itemTexture = new Texture("/textures/templepillar.png");
        Material itemMaterial = new Material(itemTexture, reflectance);
        itemMesh.setMaterial(itemMaterial);
        this.item = new GameItem(itemMesh);
        this.item.setPosition(0, 0, 0);
        this.item.setRotation(90, 0, 45);
        this.item.setScale(0.5f);

        //setup plane/quad beneath cube
        Mesh quadMesh = OBJLoader.loadMesh("/models/plane.obj");
        Material quadMaterial = new Material(new Vector4f(1.0f, 1.0f, 1.0f, 10.0f), reflectance);
        quadMesh.setMaterial(quadMaterial);
        GameItem quad = new GameItem(quadMesh);
        quad.setPosition(0, -1, 0);
        quad.setScale(2.5f);

        //add items to scene
        this.scene.setGameItems(new GameItem[] { this.item, quad });

        //setup lights
        setupLights();

        //create hud
        this.hud = new Hud("FPS: N/A POS: N/A");
        this.hud.updateSize(window);

        //move camera
        this.camera.getPosition().z = 2.0f;
    }

    //Light Setup Method
    private void setupLights() {

        //create lighting
        SceneLighting lighting = new SceneLighting();
        this.scene.setLighting(lighting);

        //ambient light
        lighting.setAmbientLight(new Vector3f(0.3f, 0.3f, 0.3f));
        lighting.setSkyBoxLight(new Vector3f(1.0f, 1.0f, 1.0f));

        //directional light
        float lightIntensity = 1.0f;
        Vector3f lightDirection = new Vector3f(0, 1, 1);
        DirectionalLight dl = new DirectionalLight(new Vector3f(1, 1, 1), lightDirection, lightIntensity);
        dl.setShadowPosMult(5);
        dl.setOrthoCoords(-10.0f, 10.0f,-10.0f, 10.0f, -1.0f, 20.0f);
        lighting.setDirectionalLight(dl);
    }

    //Input Method
    @Override
    public void input(Window window) {

        //camera movement
        this.cameraInc.set(0, 0, 0);
        this.directionalLightAngleInc = 0.0f;
        if (window.isKeyPressed(GLFW_KEY_W)) cameraInc.z -= 1;
        if (window.isKeyPressed(GLFW_KEY_S)) cameraInc.z += 1;
        if (window.isKeyPressed(GLFW_KEY_A)) cameraInc.x -= 1;
        if (window.isKeyPressed(GLFW_KEY_D)) cameraInc.x += 1;
        if (window.isKeyPressed(GLFW_KEY_SPACE)) cameraInc.y += 1;
        if (window.isKeyPressed(GLFW_KEY_LEFT_SHIFT)) cameraInc.y -= 1;
        if (window.isKeyPressed(GLFW_KEY_LEFT)) this.directionalLightAngleInc -= 1f;
        if (window.isKeyPressed(GLFW_KEY_RIGHT)) this.directionalLightAngleInc += 1f;
    }

    //Update Method
    @Override
    public void update(float interval, MouseInput mouseInput) {

        //update camera position
        camera.movePosition(cameraInc.x * CAMERA_SPEED, cameraInc.y * CAMERA_SPEED, cameraInc.z * CAMERA_SPEED);

        //update camera rotation and compass
        if (Window.MOUSE_GRABBED) {
            Vector2f rotVec = mouseInput.getDisplVec();
            this.camera.moveRotation(rotVec.x * MOUSE_SENSITIVITY, rotVec.y * MOUSE_SENSITIVITY, 0);
            Vector3f rotation = this.camera.getRotation();
            if (rotation.x > 90) rotation.x = 90;
            if (rotation.x < -90) rotation.x = -90;
            this.hud.setCompassRotation(camera.getRotation().y);
        }

        //update cube rotation
        float rotY = this.item.getRotation().y;
        rotY += 0.5f;
        if (rotY >= 360) rotY -= 360;
        this.item.getRotation().y = rotY;

        //update directional light direction
        this.directionalLightAngle += this.directionalLightAngleInc;
        if (this.directionalLightAngle < 0) this.directionalLightAngle = 0;
        if (this.directionalLightAngle > 180) this.directionalLightAngle = 180;
        float z = (float)Math.cos(Math.toRadians(directionalLightAngle));
        float y = (float)Math.sin(Math.toRadians(directionalLightAngle));
        Vector3f lightDir = this.scene.getLighting().getDirectionalLight().getDirection();
        lightDir.x = 0;
        lightDir.y = y;
        lightDir.z = z;
        lightDir.normalize();
        float angle = (float)Math.toDegrees(Math.acos(lightDir.z));

        //update text
        Vector3f cameraPos = this.camera.getPosition();
        this.hud.setStatusText("FPS: " + GameEngine.CURRENT_FPS + " LIGHT ANGLE: " + angle);
    }

    //Render Method
    @Override
    public void render(Window window) {
        if (window.isResized()) this.hud.updateSize(window);
        this.renderer.render(window, this.camera, this.scene, this.hud);
    }

    //Cleanup Method
    @Override
    public void cleanup() {
        this.renderer.cleanup();
        if (this.scene != null) {
            Map<Mesh, List<GameItem>> meshMap = this.scene.getMeshMap();
            for (Mesh mesh : meshMap.keySet()) mesh.cleanup();
        }
        if (this.hud != null) this.hud.cleanup();
    }
}
