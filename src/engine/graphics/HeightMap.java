package engine.graphics;

import engine.Utils;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.stb.STBImage.*;

public class HeightMap {

    //Static Data
    private static final int MAX_COLOR = 255 * 255 * 255;
    public static final float STARTX = -0.5f;
    public static final float STARTZ = -0.5f;

    //Instance Data
    private final float minY, maxY;
    private final Mesh mesh;
    private float[][] heights;

    //Constructor
    public HeightMap(float minY, float maxY, ByteBuffer heightMapImage, int width, int height, String textureFile,  int textureInc) throws Exception {

        //set min and max y values
        this.minY = minY;
        this.maxY = maxY;

        //create height array
        this.heights = new float[height][width];

        //load heightmap texture and calculate x/z increments
        Texture texture = new Texture(textureFile);
        float incx = HeightMap.getXLength() / (width - 1);
        float incz = HeightMap.getZLength() / (height - 1);

        //lists for mesh construction
        List<Float> positions = new ArrayList<>();
        List<Float> textureCoordinates = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        //iterate over the image, creating a vertex per each pixel, setting up the texture coordinates and indices
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {

                //create vertex
                positions.add(STARTX + col * incx); //x
                float currentHeight = this.calculateHeight(col, row, width, heightMapImage);
                positions.add(currentHeight); //y
                this.heights[row][col] = currentHeight;
                positions.add(STARTZ + row * incz); //z

                //set texture coordinates
                textureCoordinates.add((float)textureInc * (float)col / (float)width);
                textureCoordinates.add((float)textureInc * (float)row / (float)height);

                //create indices
                if (col < width - 1 && row < height - 1) {
                    int topLeft = row * width + col;
                    int bottomLeft = (row + 1) * width + col;
                    int bottomRight = (row + 1) * width + col + 1;
                    int topRight = row * width + col + 1;

                    indices.add(topLeft);
                    indices.add(bottomLeft);
                    indices.add(topRight);
                    indices.add(topRight);
                    indices.add(bottomLeft);
                    indices.add(bottomRight);
                }
            }
        }

        //convert to arrays
        float[] posArr = Utils.listToArray(positions);
        float[] textureCoordinatesArr = Utils.listToArray(textureCoordinates);
        float[] normalsArr = calculateNormals(posArr, width, height);
        int[] indicesArr = indices.stream().mapToInt(i -> i).toArray();

        //create mesh
        this.mesh = new Mesh(posArr, textureCoordinatesArr, normalsArr, indicesArr);
        Material material = new Material(texture, 0.0f);
        this.mesh.setMaterial(material);
    }

    //Accessors
    public static float getXLength() { return 2 * Math.abs(-STARTX); }
    public static float getZLength() { return 2 * Math.abs(-STARTZ); }
    public Mesh getMesh() { return this.mesh; }
    public float getHeight(int row, int col) {
        if (row >= 0 && row < this.heights.length) {
            if (col >= 0 && col < this.heights[row].length) return this.heights[row][col];
        }
        return 0;
    }

    //Gets the height for a single pixel of a heightmap
    private float calculateHeight(int x, int z, int width, ByteBuffer buf) {
        byte r = buf.get(x * 4 + 0 + z * 4 * width);
        byte g = buf.get(x * 4 + 1 * z * 4 * width);
        byte b = buf.get(x * 4 + 2 + z * 4 * width);
        byte a = buf.get(x * 4 + 3 + z * 4 * width);
        int argb = ((0xFF & a) << 24) | ((0xFF & r) << 16) | ((0xFF & g) << 8) | (0xFF & b);
        return this.minY + Math.abs(this.maxY - this.minY) * ((float) argb / (float) MAX_COLOR);
    }

    //Normal Calculation Method
    private float[] calculateNormals(float[] posArr, int width, int height) {

        //create vectors and lists
        Vector3f v0 = new Vector3f();
        Vector3f v1 = new Vector3f();
        Vector3f v2 = new Vector3f();
        Vector3f v3 = new Vector3f();
        Vector3f v4 = new Vector3f();
        Vector3f v12 = new Vector3f();
        Vector3f v23 = new Vector3f();
        Vector3f v34 = new Vector3f();
        Vector3f v41 = new Vector3f();
        List<Float> normals = new ArrayList<>();
        Vector3f normal = new Vector3f();

        //calculate normals
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                if (row > 0 && row < height - 1 && col > 0 && col < width - 1) {
                    int i0 = row * width * 3 + col * 3;
                    v0.x = posArr[i0];
                    v0.y = posArr[i0 + 1];
                    v0.z = posArr[i0 + 2];

                    int i1 = row * width * 3 + (col - 1) * 3;
                    v1.x = posArr[i1];
                    v1.y = posArr[i1 + 1];
                    v1.z = posArr[i1 + 2];
                    v1 = v1.sub(v0);

                    int i2 = (row + 1) * width * 3 + col * 3;
                    v2.x = posArr[i2];
                    v2.y = posArr[i2 + 1];
                    v2.z = posArr[i2 + 2];
                    v2 = v2.sub(v0);

                    int i3 = row * width * 3 + (col + 1) * 3;
                    v3.x = posArr[i3];
                    v3.y = posArr[i3 + 1];
                    v3.z = posArr[i3 + 2];
                    v3 = v3.sub(v0);

                    int i4 = (row - 1) * width * 3 + col * 3;
                    v4.x = posArr[i4];
                    v4.y = posArr[i4 + 1];
                    v4.z = posArr[i4 + 2];
                    v4 = v4.sub(v0);

                    v1.cross(v2, v12);
                    v12.normalize();

                    v2.cross(v3, v23);
                    v23.normalize();

                    v3.cross(v4, v34);
                    v34.normalize();

                    v4.cross(v1, v41);
                    v41.normalize();

                    normal = v12.add(v23).add(v34).add(v41);
                    normal.normalize();
                } else {
                    normal.x = normal.z = 0;
                    normal.y = 1;
                }
                normal.normalize();
                normals.add(normal.x);
                normals.add(normal.y);
                normals.add(normal.z);
            }
        }
        return Utils.listToArray(normals);
    }
}
