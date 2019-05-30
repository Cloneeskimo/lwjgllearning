package engine.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
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
    private static final Vector3f DEFAULT_COLOR = new Vector3f(1.0f, 1.0f, 1.0f);

    //Instance Data
    private final int vaoID;
    private final List<Integer> vboIDs;
    private final int vertexCount;
    private Material material;

    //Constructor
    public Mesh(float[] positions, float[] textureCoords, float[] normals, int[] indices) {
        FloatBuffer posBuffer = null;
        FloatBuffer textureCoordsBuffer = null;
        FloatBuffer normalVectorsBuffer = null;
        IntBuffer idxBuffer = null;
        try {
            //Set vertex count
            vertexCount = indices.length; //assumes triangles
            this.vboIDs = new ArrayList();

            //Create and bind VAO
            vaoID = glGenVertexArrays();
            glBindVertexArray(vaoID);

            //Position VBO Buffer
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();

            //Position VBO creation, data storage
            int vbo = glGenBuffers();
            this.vboIDs.add(vbo);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW); //put vertices in VBO
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); //put vbo in vao

            //Texture Coordinates VBO Buffer
            textureCoordsBuffer = MemoryUtil.memAllocFloat(textureCoords.length);
            textureCoordsBuffer.put(textureCoords).flip();

            //Texture Coordinates VBO creation, data storage
            vbo = glGenBuffers();
            this.vboIDs.add(vbo);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, textureCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0); //(2 components - x, y)

            //Normal Vectors VBO Buffer
            normalVectorsBuffer = MemoryUtil.memAllocFloat(normals.length);
            normalVectorsBuffer.put(normals).flip();

            //Normal Vectors VBO Creation, data storage
            vbo = glGenBuffers();
            this.vboIDs.add(vbo);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, normalVectorsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0); //put in VAO

            //Index VBO Buffer
            idxBuffer = MemoryUtil.memAllocInt(indices.length);
            idxBuffer.put(indices).flip();

            //Index VBO creation, data storage
            vbo = glGenBuffers();
            this.vboIDs.add(vbo);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vbo);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STATIC_DRAW);

            //Unbind VBO, VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {

            //Free buffer memory
            if (posBuffer != null) MemoryUtil.memFree(posBuffer);
            if (textureCoordsBuffer != null) MemoryUtil.memFree(textureCoordsBuffer);
            if (normalVectorsBuffer != null) MemoryUtil.memFree(normalVectorsBuffer);
            if (idxBuffer != null) MemoryUtil.memFree(idxBuffer);
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
    }

    //Post-Render
    private void postRender() {

        //restore state
        glDisableVertexAttribArray(this.vaoID);
        glDisableVertexAttribArray(0);
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(2);
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
}
