package engine.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.system.MemoryUtil;

public class Mesh {

    //Data
    private final int vaoID;
    private final List<Integer> vboIDs;
    private final int vertexCount;
    private final Texture texture;

    //Constructor
    //PRE-REQ: positions must follow standard array protocol, must have its count
    //equal to its length, must be divisible by 3 (processed as triangles)
    public Mesh(float[] positions, float[] textureCoords, int[] indices, Texture texture) {
        FloatBuffer posBuffer = null;
        FloatBuffer textureCoordsBuffer = null;
        IntBuffer idxBuffer = null;
        try {
            //Set texture
            this.texture = texture;

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

            //Texture VBO Buffer
            textureCoordsBuffer = MemoryUtil.memAllocFloat(textureCoords.length);
            textureCoordsBuffer.put(textureCoords).flip();

            //Texture VBO creation, data storage
            vbo = glGenBuffers();
            this.vboIDs.add(vbo);
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, textureCoordsBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0); //(2 components - x, y)

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
            if (idxBuffer != null) MemoryUtil.memFree(idxBuffer);
        }
    }

    //Accessors
    public int getVaoID() { return this.vaoID; }
    public int getVertexCount() { return this.vertexCount; }

    //Other Methods
    public void cleanup() {

        //Disable vbo at index 0 of vao
        glDisableVertexAttribArray(0);

        //Delete VBO, VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0); //first make sure isn't bound
        for (int vbo : this.vboIDs) glDeleteBuffers(vbo);
        glBindVertexArray(0); //first make sure isn't bound
        glDeleteVertexArrays(vaoID); //delete
    }

    public void render() {

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, texture.getID());

        //Bind to the VAO
        glBindVertexArray(getVaoID());
        glEnableVertexAttribArray(0); //enable our position vbo at index 0 in the vao
        glEnableVertexAttribArray(1); //enable our color vbo at index 1 in the vao

        //Draw Mesh
        //(@mode) - specifies the primitives for rendering, triangles in this case.
        //(@count) - specifies the number of elements to be rendered.
        //(@type) - specifies the type of value in the indices data, integers in this case
        //(@indicies) - specifies the offset to apply to the indices data to start rendering
        glDrawElements(GL_TRIANGLES, getVertexCount(), GL_UNSIGNED_INT, 0);

        //Restore state (unbind)
        glDisableVertexAttribArray(0); //disable our position vbo at index 0 in the vao
        glDisableVertexAttribArray(1); //disable our color vbo at index 1 in the vao
        glBindVertexArray(0);
    }
}
