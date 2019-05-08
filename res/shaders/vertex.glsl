//directive that states the version of GLSL we are using
#version 330

//specifies the input format for the shader. can be whatever data structure
//we decide to pass in (can be a position, additional data, whatever)
//the vertex shader just receives an array of floats. when we fill the buffer,
//we define the buffer chunks that are going to be processed by the shader.
//In this case, we expect a vector of x, y, z
layout (location=0) in vec3 position;
layout (location=1) in vec2 textureCoords; //processed by fragment shader, so will just be passed through
out vec2 textureCoordsOut;

//uniforms are global GLSL variables
uniform mat4 projection; //projection matrix
uniform mat4 world;      //world matrix

void main()
{
    //In this very basic shader, we are just returning the received position
    //with no transformations applied.
    gl_Position = projection * world * vec4(position, 1.0); //vec4 because some advanced operations require a fourth dimension
    textureCoordsOut = textureCoords;
}