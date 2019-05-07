package engine.graphics;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

    //Data
    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private final Map<String, Integer> uniforms; //map for GLSL uniforms (global variables)

    //Constructor
    public ShaderProgram() throws Exception {
        this.programId = glCreateProgram();
        if (programId == 0) throw new Exception("Could not create shader program");
        this.uniforms = new HashMap<>();
    }

    //Shader Creation Methods
    public void createVertexShader(String shaderCode) throws Exception {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) throws Exception {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType) throws Exception {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) throw new Exception("Error creating shader. Type: " + shaderType);

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0)
            throw new Exception("Error compiling shader code: " + glGetShaderInfoLog(shaderId, 1024));

        glAttachShader(programId, shaderId);
        return shaderId;
    }

    //Other Methods
    public void link() throws Exception {
        glLinkProgram(programId);
        if (glGetProgrami(programId, GL_LINK_STATUS) == 0)
            throw new Exception("Error linking Shader code: " + glGetProgramInfoLog(programId, 1024));

        if (vertexShaderId != 0) glDetachShader(programId, vertexShaderId);
        if (fragmentShaderId != 0) glDetachShader(programId, fragmentShaderId);

        //This is mainly for debugging purposes, should be removed when game is production-ready
        //Validates that the shader is correct given the current OpenGL state (does not necessarily
        //mean that the shader program is incorrect in general)
        glValidateProgram(programId);
        if (glGetProgrami(programId, GL_VALIDATE_STATUS) == 0)
            System.err.println("Warning validating shader code: " + glGetProgramInfoLog(programId, 1024));
    }

    public void cleanup() { //free resources when they are no longer needed
        unbind();
        if (programId != 0) glDeleteProgram(programId);
    }

    //Uniform Management
    public void createUniform(String uniformName) throws Exception {
        int uniformLocation = glGetUniformLocation(this.programId, uniformName); //get from shader program
        if (uniformLocation < 0) throw new Exception("Could not find uniform: " + uniformName); //invalid uniform
        uniforms.put(uniformName, uniformLocation); //add to map
    }

    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16); //create float buffer (auto-managed, will delete itself)
            value.get(fb); //put matrix value into a float buffer
            glUniformMatrix4fv(this.uniforms.get(uniformName), false, fb);
        }
    }

    //Binding/Unbinding
    public void bind() { glUseProgram(programId); } //activate this program for rendering
    public void unbind() { glUseProgram(0); } //deactivate this program for rendering
}
