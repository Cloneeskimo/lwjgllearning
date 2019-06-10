package engine.gameitem;

import engine.graphics.Material;
import engine.graphics.Mesh;
import engine.graphics.loaders.obj.OBJLoader;
import engine.graphics.Texture;

public class SkyBox extends GameItem {

    //Constructor
    public SkyBox(String objModel, String textureFile) throws Exception {
        Mesh m = OBJLoader.loadMesh(objModel);
        Texture t = new Texture(textureFile);
        m.setMaterial(new Material(t, 0.0f));
        this.setMesh(m, true);
        this.setPosition(0, 0, 0);
    }
}
