
//GLSL Version
#version 330

//VAO Inputs
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoordsVertex;
layout (location = 2) in vec4 vertexNormal;

//Output
out vec2 textureCoordsFrag;

//Uniforms
uniform mat4 modelView;
uniform mat4 projection;

//Main Method
void main() {

    //set position and pass through texture coordinates
    gl_Position = projection * modelView * vec4(position, 1.0);
    textureCoordsFrag = textureCoordsVertex;
}
