package engine;

import engine.gameitem.GameItem;
import engine.gameitem.SkyBox;
import engine.graphics.Mesh;
import engine.graphics.light.SceneLighting;
import engine.graphics.weather.Fog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scene {

    //Data
    private Map<Mesh, List<GameItem>> meshMap;
    private SkyBox skyBox;
    private SceneLighting lighting;
    private Fog fog;

    //Constructor
    public Scene() { this.meshMap = new HashMap<>(); }

    //Accessors
    public Map<Mesh, List<GameItem>> getMeshMap() { return this.meshMap; }
    public SkyBox getSkyBox() { return this.skyBox; }
    public SceneLighting getLighting() { return this.lighting; }
    public Fog getFog() { return this.fog; }

    //Mutators
    public void setSkyBox(SkyBox skyBox) { this.skyBox = skyBox; }
    public void setLighting(SceneLighting lighting) { this.lighting = lighting; }
    public void setGameItems(GameItem[] gameItems) {

        //sort game items by mesh for optimal rendering
        int n = gameItems != null ? gameItems.length : 0;
        for (int i = 0; i < n; i ++) {
            GameItem gi = gameItems[i];
            Mesh m = gi.getMesh();
            List<GameItem> l = meshMap.get(m);
            if (l == null) {
                l = new ArrayList<>();
                meshMap.put(m, l);
            }
            l.add(gi);
        }
    }
    public void setFog(Fog fog) { this.fog = fog; }
}
