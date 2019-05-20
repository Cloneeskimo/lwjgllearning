package game;

import engine.IHud;
import engine.Window;
import engine.gameitem.GameItem;
import engine.gameitem.TextItem;
import engine.graphics.FontTexture;
import engine.graphics.Material;
import engine.graphics.Mesh;
import engine.graphics.OBJLoader;
import org.joml.Vector4f;

import java.awt.*;

public class Hud implements IHud {

    //Static Data
    private static final Font FONT = new Font("Arial", Font.PLAIN, 40);
    private static final String CHARSET = "ISO-8859-1";

    //Instance Data
    private final GameItem[] gameItems;
    private final TextItem statusText;
    private final GameItem compass;

    //Constructor
    public Hud(String statusText) throws Exception {

        //create status text
        FontTexture fontTexture = new FontTexture(FONT, CHARSET);
        this.statusText = new TextItem(statusText, fontTexture);
        this.statusText.getMesh().getMaterial().setAmbientColor(new Vector4f(1, 1, 1, 1));

        //create compass
        Mesh compassMesh = OBJLoader.loadMesh("/models/compass.obj");
        Material compassMaterial = new Material();
        compassMaterial.setAmbientColor(new Vector4f(1, 0, 0, 1));
        compassMesh.setMaterial(compassMaterial);
        this.compass = new GameItem(compassMesh);
        this.compass.setScale(40.0f);
        this.compass.setRotation(0f, 0f, 180f);

        //create game items array
        this.gameItems = new GameItem[]{ this.statusText, this.compass };
    }

    //Window Resizing Updating Method
    public void updateSize(Window window) {
        this.statusText.setPosition(10f, window.getHeight() - 50f, 0);
        this.compass.setPosition(window.getWidth() - 50f, 50f, 0);
    }

    //Accessors
    @Override
    public GameItem[] getGameItems() { return this.gameItems; }

    //Mutators
    public void setStatusText(String statusText) { this.statusText.setText(statusText); }
    public void setCompassRotation(float angle) { this.compass.setRotation(0, 0, 180 + angle); }

}
