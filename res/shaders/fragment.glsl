//directive that states which version of GLSL we are using
#version 330

in  vec2 textureCoordsOut; //color passed from vertex shader
out vec4 fragColor; //output variable definition

//we use this sampler to define which texture unit of the graphics card we will store the texture in
uniform sampler2D textureSampler;

void main() {
    fragColor = texture(textureSampler, textureCoordsOut); //assigns the color based on the coordinates of the texture
}
