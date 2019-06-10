package engine.gameitem;

import engine.Utils;
import engine.graphics.FontTexture;
import engine.graphics.Material;
import engine.graphics.Mesh;
import engine.graphics.Texture;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class TextItem extends GameItem {

    //Static Data
    private static final float ZPOS = 0.0f;
    private static final int VERTICES_PER_QUAD = 4;

    //Instance Data
    private final FontTexture fontTexture;
    private String text;

    //Constructor
    public TextItem(String text, FontTexture fontTexture) throws Exception {
        super();
        this.text = text;
        this.fontTexture = fontTexture;
        this.setMesh(buildMesh(), true);
    }

    //Mesh-Building Method
    private Mesh buildMesh() {

        //create lists for vao attributes
        List<Float> positions = new ArrayList<>();
        List<Float> textureCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        float[] normalsArr = new float[0];

        //get text characters and calculate length of text
        char[] characters = this.text.toCharArray();
        int numChars = characters.length;

        //create each character
        float startX = 0;
        for (int i = 0; i < numChars; i++) { //SCREEN SPACE

            //get current character info
            FontTexture.CharInfo charInfo = this.fontTexture.getCharInfo(characters[i]);

            //top-left vertex
            positions.add(startX); //x
            positions.add(0.0f); //y
            positions.add(ZPOS); //z
            textureCoords.add((float)charInfo.getStartX() / (float)fontTexture.getWidth()); //x
            textureCoords.add(0.0f); //y
            indices.add(i * VERTICES_PER_QUAD);

            //bottom-left vertex
            positions.add(startX); //x
            positions.add((float)this.fontTexture.getHeight()); //y
            positions.add(ZPOS); //z
            textureCoords.add((float)charInfo.getStartX() / (float)fontTexture.getWidth()); //x
            textureCoords.add(1.0f); //y
            indices.add(i * VERTICES_PER_QUAD + 1);

            //bottom-right vertex
            positions.add(startX + charInfo.getWidth()); //x
            positions.add((float)this.fontTexture.getHeight()); //y
            positions.add(ZPOS); //z
            textureCoords.add((float)(charInfo.getStartX() + charInfo.getWidth()) / (float)fontTexture.getWidth()); //x
            textureCoords.add(1.0f); //y
            indices.add(i * VERTICES_PER_QUAD + 2);

            //top-right vertex
            positions.add(startX + charInfo.getWidth()); //x
            positions.add(0.0f); //y
            positions.add(ZPOS); //z
            textureCoords.add((float)(charInfo.getStartX() + charInfo.getWidth()) / (float)fontTexture.getWidth()); //x
            textureCoords.add(0.0f); //y
            indices.add(i * VERTICES_PER_QUAD + 3);

            //add indices for top-left and bottom-right vertices (they get repeated)
            indices.add(i * VERTICES_PER_QUAD);
            indices.add(i * VERTICES_PER_QUAD + 2);

            //iterate startX
            startX += charInfo.getWidth();
        }

        //convert lists to arrays
        float[] positionsArr = Utils.listToArray(positions);
        float[] textureCoordsArr = Utils.listToArray(textureCoords);
        int[] indicesArr = indices.stream().mapToInt(i->i).toArray();

        //create mesh and return it
        Mesh mesh = new Mesh(positionsArr, textureCoordsArr, normalsArr, indicesArr);
        mesh.setMaterial(new Material(fontTexture.getTexture()));
        return mesh;
    }

    //Accessors
    public String getText() { return this.text; }

    //Mutators
    public void setText(String text) {
        this.text = text;
        this.getMesh().deleteBuffers();
        this.setMesh(buildMesh(), true);
    }
}
