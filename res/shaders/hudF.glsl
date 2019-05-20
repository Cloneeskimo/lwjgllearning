
//GLSL Version
#version 330

//Inputs
in vec2 textureCoordsFrag;
in vec3 mvPos;
out vec4 fragColor;

//Uniforms
uniform sampler2D textureSampler;
uniform vec4 color;
uniform int hasTexture; //1 -> true; 0 -> false

//Main Function
void main() {
    if (hasTexture == 1) {
        fragColor = color * texture(textureSampler, textureCoordsFrag);
    } else {
        fragColor = color;
    }
}
