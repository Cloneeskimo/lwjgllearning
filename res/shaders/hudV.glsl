
//GLSL Version
#version 330

//Inputs
layout (location=0) in vec3 position;
layout (location=1) in vec2 textureCoordsVertex;
layout (location=2) in vec3 vertexNormal;

//Outputs
out vec2 textureCoordsFrag;

//Uniforms
uniform mat4 projectionModel; //orthographic * model

//Main Function
void main() {
    gl_Position = projectionModel * vec4(position, 1.0);
    textureCoordsFrag = textureCoordsVertex;
}
