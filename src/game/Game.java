package game;

import engine.*;
import engine.gameitem.AnimGameItem;
import engine.gameitem.GameItem;
import engine.graphics.*;
import engine.graphics.light.DirectionalLight;
import engine.graphics.light.SceneLighting;
import engine.graphics.loaders.md5.MD5AnimModel;
import engine.graphics.loaders.md5.MD5Loader;
import engine.graphics.loaders.md5.MD5Model;
import engine.graphics.loaders.obj.OBJLoader;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

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
    private Hud hud;
    private float directionalLightAngle;
    private float directionalLightAngleInc = 0;
    private AnimGameItem monster;

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

        //setup plane/quad beneath cube
        Mesh quadMesh = OBJLoader.loadMesh("/models/plane.obj");
        Material quadMaterial = new Material(new Vector4f(1.0f, 1.0f, 1.0f, 10.0f), 1f);
        quadMesh.setMaterial(quadMaterial);
        GameItem quad = new GameItem(quadMesh);
        quad.setPosition(0, -1, 0);
        quad.setScale(9f);

        //add monster
        MD5Model md5Model = MD5Model.parse("/models/monster.md5mesh");
        MD5AnimModel md5AnimModel = MD5AnimModel.parse("/models/monster.md5anim");
        this.monster = MD5Loader.process(md5Model, md5AnimModel, new Vector4f(1, 1, 1, 1));
        monster.setScale(0.05f);
        monster.setRotation(90, 0, 0);

        //add items to scene
        this.scene.setGameItems(new GameItem[] { monster, quad });

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
        if (window.isKeyPressed(GLFW_KEY_ENTER)) this.monster.nextFrame();
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
