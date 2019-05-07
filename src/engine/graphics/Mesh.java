package engine.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import org.lwjgl.system.MemoryUtil;

public class Mesh {

    //Data
    private final int vaoID;
    private final int posVboID, colorVboID, idxVboID;
    private final int vertexCount;

    //Constructor
    //PRE-REQ: positions must follow standard array protocol, must have its count
    //equal to its length, must be divisible by 3 (processed as triangles)
    public Mesh(float[] positions, float[] colors, int[] indices) {
        FloatBuffer posBuffer = null;
        FloatBuffer colorBuffer = null;
        IntBuffer idxBuffer = null;
        try {

            //Set vertex count
            vertexCount = indices.length; //assumes triangles

            //Create and bind VAO
            vaoID = glGenVertexArrays();
            glBindVertexArray(vaoID);

            //Position VBO Buffer
            posBuffer = MemoryUtil.memAllocFloat(positions.length);
            posBuffer.put(positions).flip();

            //Position VBO creation, data storage
            posVboID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, posVboID);
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW); //put vertices in VBO
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0); //put vbo in vao

            //Color VBO Buffer
            colorBuffer = MemoryUtil.memAllocFloat(colors.length);
            colorBuffer.put(colors).flip();

            //Color VBO creation, data storage
            colorVboID = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, colorVboID);
            glBufferData(GL_ARRAY_BUFFER, colorBuffer, GL_STATIC_DRAW);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

            //Index VBO Buffer
            idxBuffer = MemoryUtil.memAllocInt(indices.length);
            idxBuffer.put(indices).flip();

            //Index VBO creation, data storage
            idxVboID = glGenBuffers();
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboID);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, idxBuffer, GL_STATIC_DRAW);

            //Unbind VBO, VAO
            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);

        } finally {

            //Free buffer memory
            if (posBuffer != null) MemoryUtil.memFree(posBuffer);
            if (colorBuffer != null) MemoryUtil.memFree(colorBuffer);
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
        glDeleteBuffers(posVboID); //delete position vbo
        glDeleteBuffers(idxVboID); //delete index vbo
        glDeleteBuffers(colorVboID); //delete color vbo
        glBindVertexArray(0); //first make sure isn't bound
        glDeleteVertexArrays(vaoID); //delete
    }
}
