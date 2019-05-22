package engine;

import engine.gameitem.GameItem;
import engine.gameitem.SkyBox;
import engine.graphics.light.SceneLighting;

public class Scene {

    //Data
    private GameItem[] gameItems;
    private SkyBox skyBox;
    private SceneLighting lighting;

    //Accessors
    public GameItem[] getGameItems() { return this.gameItems; }
    public SkyBox getSkyBox() { return this.skyBox; }
    public SceneLighting getLighting() { return this.lighting; }

    //Mutators
    public void setGameItems(GameItem[] gameItems) { this.gameItems = gameItems; }
    public void setSkyBox(SkyBox skyBox) { this.skyBox = skyBox; }
    public void setLighting(SceneLighting lighting) { this.lighting = lighting; }
}
