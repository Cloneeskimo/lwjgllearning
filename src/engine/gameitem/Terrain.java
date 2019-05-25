package engine.gameitem;

import engine.graphics.HeightMap;
import engine.graphics.Texture;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Paths;

import static org.lwjgl.stb.STBImage.*;

public class Terrain {

    //Data
    private final GameItem[] chunks;
    private final HeightMap heightMap;
    private final Box2D[][] boundingBoxes;
    private final int terrainSize;
    private final int verticesPerRow;
    private final int verticesPerCol;

    //Constructor
    public Terrain(int terrainSize, float scale, float minY, float maxY, String heightMapFile, String textureFile, int textureInc) throws Exception {

        //initialize terrain size and chunka array
        this.terrainSize = terrainSize;
        this.chunks = new GameItem[terrainSize * terrainSize];

        //get width and height of image
        ByteBuffer buf = null;
        int w, h;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer wBuf = stack.mallocInt(1);
            IntBuffer hBuf = stack.mallocInt(1);
            IntBuffer channelsBuff = stack.mallocInt(1);

            URL url = Texture.class.getResource(heightMapFile);
            File file = Paths.get(url.toURI()).toFile();
            String filePath = file.getAbsolutePath();
            buf = stbi_load(filePath, wBuf, hBuf, channelsBuff, 4);
            if (buf == null) throw new Exception("Image file [" + filePath + "] not loaded: " + stbi_failure_reason());

            w = wBuf.get();
            h = hBuf.get();
        }

        //set vertcies per row and column
        this.verticesPerRow = w - 1;
        this.verticesPerCol = h - 1;

        //create height map and bounding boxes array
        this.heightMap = new HeightMap(minY, maxY, buf, w, h, textureFile, textureInc);
        this.boundingBoxes = new Box2D[terrainSize][terrainSize];

        //add chunks
        for (int row = 0; row < terrainSize; row++) {
            for (int col = 0; col < terrainSize; col++) {
                float xDisplacement = (col - ((float)terrainSize - 1) / 2.0f) * scale * heightMap.getXLength();
                float zDisplacement = (row - ((float)terrainSize - 1) / 2.0f) * scale * heightMap.getZLength();

                GameItem chunk = new GameItem(heightMap.getMesh());
                chunk.setScale(scale);
                chunk.setPosition(xDisplacement, 0, zDisplacement);
                chunks[row * terrainSize + col] = chunk;
                boundingBoxes[row][col] = getBoundingBox(chunk);
            }
        }

        //free image memory
        stbi_image_free(buf);
    }

    //Height Calculation Method
    public float getHeight(Vector3f position) {

        //set variables
        boolean found = false;
        float result = Float.MIN_VALUE;
        Box2D boundingBox = null;
        GameItem chunk = null;

        //find chunk that contains this position
        for (int row= 0; row < this.terrainSize && !found; row++) {
            for (int col = 0; col < this.terrainSize && !found; col++) {
                boundingBox = boundingBoxes[row][col];
                if (boundingBox.contains(position.x, position.z)) {
                    chunk = this.chunks[row * terrainSize * col];
                    found = boundingBox.contains(position.x, position.z);
                }
            }
        }

        //calculate the height of the terrain on that position if found
        if (found) {
            Vector3f[] triangle = getTriangle(position, boundingBox, chunk); //calculate the triangle we are currently in
            result = interpolateHeight(triangle[0], triangle[1], triangle[2], position.x, position.z);
        }

        //return result (minimum float value if not foud)
        return result;
    }

    //Triangle Calculation Method
    //gets the triangle below a certain position of the terrain
    protected Vector3f[] getTriangle(Vector3f position, Box2D boundingBox, GameItem chunk) {

        //get the column and row of the heightmap associated with the current position
        float cellWidth = boundingBox.w / (float)verticesPerCol;
        float cellHeight = boundingBox.h / (float)verticesPerRow;
        int col = (int) ((position.x - boundingBox.x) / cellWidth);
        int row = (int) ((position.z - boundingBox.y) / cellHeight);

        //get first two triangle vertices
        Vector3f[] triangle = new Vector3f[3];
        triangle[1] = new Vector3f(boundingBox.x + col * cellWidth,
                getWorldHeight(row + 1, col, chunk),
                boundingBox.y + (row + 1) * cellHeight);
        triangle[2] = new Vector3f(boundingBox.x + (col + 1) * cellWidth,
                getWorldHeight(row, col + 1, chunk),
                boundingBox.y + row * cellHeight);

        //get final triangle vertex by determining if the z is above or below the diagonal
        if (position.z < getDiagonalZCoord(triangle[1].x, triangle[1].z, triangle[2].x, triangle[2].z, position.x)) {
            triangle[0] = new Vector3f(boundingBox.x + col * cellWidth,
                    getWorldHeight(row, col, chunk),
                    boundingBox.y + row * cellHeight);
        } else {
            triangle[0] = new Vector3f(boundingBox.x + (col + 1) * cellWidth,
                    getWorldHeight(row + 2, col + 1, chunk),
                    boundingBox.y + (row + 1) * cellHeight);
        }

        //return triangle
        return triangle;
    }

    //Diagonal Z Coordinate Calculation Method
    //gets position.x's corresponding z coordinate in the triangle
    protected float getDiagonalZCoord(float x1, float z1, float x2, float z2, float x) {
        return ((z1 - z2) / (x1 - x2)) * (x - x1) + z1;
    }

    //World Height Calculation Method
    //calculates the height of a single vertex
    protected float getWorldHeight(int row, int col, GameItem chunk) {
        float y = heightMap.getHeight(row, col);
        return y * chunk.getScale() + chunk.getPosition().y;
    }

    //Height Interpolation Method
    //interpolates the height of a point in a plane using three triangular points to define the plane
    protected float interpolateHeight(Vector3f pA, Vector3f pB, Vector3f pC, float x, float z) {

        //plane equation: ax + by + cz + d = 0
        float a = (pB.y - pA.y) * (pC.z - pA.z) - (pC.y - pA.y) * (pB.z - pA.z);
        float b = (pB.z - pA.z) * (pC.x - pA.x) - (pC.z - pA.z) * (pB.x - pA.x);
        float c = (pB.x - pA.x) * (pC.y - pA.y) - (pC.x - pA.x) * (pB.y - pA.y);
        float d = -(a * pA.x + b * pA.y + c * pA.z);
        float y = (-d - a * x - c * z) / b;
        return y;
    }

    //Bounding Box Calculation Method
    private Box2D getBoundingBox(GameItem chunk) {

        //get scale and position
        float scale = chunk.getScale();
        Vector3f position = chunk.getPosition();

        //calculate bounding box
        float x = HeightMap.STARTX * scale + position.x; //x
        float y = HeightMap.STARTZ * scale + position.z; //y
        float w = Math.abs(HeightMap.STARTX * 2) * scale; //w
        float h = Math.abs(HeightMap.STARTZ * 2) * scale; //h

        //create box object and return it
        Box2D b = new Box2D(x, y, w, h);
        return b;
    }

    //Accessor
    public GameItem[] getChunks() { return this.chunks; }

    //Box2D Class
    static class Box2D {

        //Data
        public float x, y;
        public float w, h;

        //Constructor
        public Box2D(float x, float y, float w, float h) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        //Contains Method
        public boolean contains(float thisX, float thisY) {
            return thisX >= this.x
                    && thisY >= this.y
                    && thisX <= this.x + this.w
                    && thisY <= this.y + this.h;
        }
    }
}
