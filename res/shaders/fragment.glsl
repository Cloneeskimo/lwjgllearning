//directive that states which version of GLSL we are using
#version 330

//Variables
in  vec2 textureCoordsOut; //color passed from vertex shader
out vec4 fragColor; //output variable definition

//Uniforms
uniform sampler2D textureSampler; //use this to define which texture unit/bank of the graphics card we will store the texture in
uniform vec3 color; //color to use for drawing (if useColor is set to 1)
uniform int useColor; //represents whether to use color or a texture to draw

void main() {
    if (useColor == 1) {
        fragColor = vec4(color, 1);
    } else {
        fragColor = texture(textureSampler, textureCoordsOut); //assigns the color based on the coordinates of the texture
    }
}
