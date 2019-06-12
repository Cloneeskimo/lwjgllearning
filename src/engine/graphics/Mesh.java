package engine.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import engine.gameitem.GameItem;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

public class Mesh {

    //Static Data
    public static final int MAX_WEIGHTS = 4;

    //Instance Data
    private final int vaoID;
    private final List<Integer> vboIDs;
    private final int vertexCount;
    private Material material;

    //Static Constructor
    public Mesh(float[] positions, float[] texCoords, float[] normals, int[] indices) {
        this(positions, texCoords, normals, indices, createEmptyIntArray(MAX_WEIGHTS * positions.length / 3, 0),
                createEmptyFloatArray(MAX_WEIGHTS * positions.length / 3, 0));
    }

    //Animated Constructor
    public Mesh(float[] positions, float[] textureCoords, float[] normals, int[] indices, int jointIndices[], float[] weights) {

        //create buffers
        FloatBuffer positionBuffer = null;
        FloatBuffer texCoordsBuffer = null;
        FloatBuffer normalVectorsBuffer= null;
        FloatBuffer weightsBuffer = null;
        IntBuffer indicesBuffer = null;
        IntBuffer jointIndicesBuffer = null;

        //fill vao
        try {

            //set vertex count and create vbo list
            vertexCount = indices.length;
            this.vboIDs = new ArrayList();

            //create and bind VAO
            vaoID = glGenVertexArrays();
            glBindVertexArray(vaoID);

            //position VBO Buffer
            positionBuffer = MemoryUtil.memAllocFloat(positions.length);
            positionBuffer.put(positions).flip();

            //position VBO creation, data storage
            int vbo = glGenBuffers();
            this.vboIDs.add(vbo);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, positionBuffer, GL_STATIC_DRAW); //put vertices in VBO
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            //texture coordinates VBO buffer
            texCoordsBuffer = MemoryUtil.memAllocFloat(textureCoords.length);
            texCoordsBuffer.put(textureCoords).flip();

            //texture coordinates VBO creation, data storage
            vbo = glGenBuffers();
            this.vboIDs.add(vbo);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);

            //normal vectors VBO buffer
            normalVectorsBuffer = MemoryUtil.memAllocFloat(normals.length);
            normalVectorsBuffer.put(normals).flip();

            //normal vectors VBO creation, data storage
            vbo = glGenBuffers();
            this.vboIDs.add(vbo);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, normalVectorsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            //weights buffer
            weightsBuffer = MemoryUtil.memAllocFloat(weights.length);
            weightsBuffer.put(weights).flip();

            //weights VBO creation, data storage
            vbo = glGenBuffers();
            this.vboIDs.add(vbo);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);

            //joint indices buffer
            jointIndicesBuffer = MemoryUtil.memAllocInt(jointIndices.length);
            jointIndicesBuffer.put(jointIndices).flip();

            //joint indices VBO creation, data storage
            vbo = glGenBuffers();
            this.vboIDs.add(vbo);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, jointIndices, GL_STATIC_DRAW);
            glVertexAttribPointer(4, 4, GL_FLOAT, false, 0, 0);

            //Index VBO Buffer
            indicesBuffer = MemoryUtil.memAllocInt(indices.length);
            indicesBuffer.put(indices).flip();

            //Index VBO creation, data storage
            vbo = glGenBuffers();
            this.vboIDs.add(vbo);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            //Unbind VBO, VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {

            //Free buffer memory
            if (positionBuffer != null) MemoryUtil.memFree(positionBuffer);
            if (texCoordsBuffer != null) MemoryUtil.memFree(texCoordsBuffer);
            if (normalVectorsBuffer != null) MemoryUtil.memFree(normalVectorsBuffer);
            if (weightsBuffer != null) MemoryUtil.memFree(weightsBuffer);
            if (jointIndicesBuffer != null) MemoryUtil.memFree(jointIndicesBuffer);
            if (indicesBuffer != null) MemoryUtil.memFree(indicesBuffer);
        }
    }

    //Accessors
    public Material getMaterial() { return this.material; }
    public int getVaoID() { return this.vaoID; }
    public int getVertexCount() { return this.vertexCount; }

    //Mutators
    public void setMaterial(Material material) { this.material = material; }

    //Render Method
    public void render() {

        //render this mesh singly
        this.preRender();
        glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0);
        this.postRender();
    }

    //Render List Method
    public void renderList(List<GameItem> gameItems, Consumer<GameItem> consumer) {

        //render multiple meshes
        this.preRender();
        for (GameItem gi : gameItems) {

            //set up data required by game item
            consumer.accept(gi);
            glDrawElements(GL_TRIANGLES, this.vertexCount, GL_UNSIGNED_INT, 0);
        }
        this.postRender();
    }

    //Pre-Render
    private void preRender() {

        //bind texture
        if (this.material.isTextured()) {
            glActiveTexture(GL_TEXTURE0); //activate first texture bank
            glBindTexture(GL_TEXTURE_2D, this.material.getTexture().getID()); //bind texture
        }

        //bind normal map
        if (this.material.hasNormalMap()) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, this.material.getNormalMap().getID());
        }

        //bind VAO and attribute arrays
        glBindVertexArray(this.vaoID);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
    }

    //Post-Render
    private void postRender() {

        //restore state
        glDisableVertexAttribArray(this.vaoID);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
        glEnableVertexAttribArray(3);
        glEnableVertexAttribArray(4);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    //Cleanup Method
    public void cleanup() {

        //Delete Buffers
        this.deleteBuffers();

        //Cleanup texture
        this.material.cleanup();
    }

    //Buffer Deletion Method
    public void deleteBuffers() {

        //Disable VAO
        glDisableVertexAttribArray(0);

        //Delete VBOs, VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        for (int vboID : this.vboIDs) glDeleteBuffers(vboID);
        glBindVertexArray(0);
        glDeleteVertexArrays(this.vaoID);
    }

    //Empty Array Creation Methods
    private static float[] createEmptyFloatArray(int length, float defaultValue) {
        float[] result = new float[length];
        Arrays.fill(result, defaultValue);
        return result;
    }

    private static int[] createEmptyIntArray(int length, int defaultValue) {
        int[] result = new int[length];
        Arrays.fill(result, defaultValue);
        return result;
    }
}
