
//GLSL Version
#version 330

//Input
in vec2 textureCoordsFrag;
in vec3 mvPos;

//Output
out vec4 fragColor;

//Uniforms
uniform sampler2D textureSampler;
uniform vec3 ambientLight;

//Main Method
void main() {
    fragColor = vec4(ambientLight, 1) * texture(textureSampler, textureCoordsFrag);
}
