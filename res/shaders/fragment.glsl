//directive that states which version of GLSL we are using
#version 330

in  vec3 exColor; //color passed from vertex shader
out vec4 fragColor; //output variable definition

void main() {
    fragColor = vec4(exColor, 1.0); //setting a fixed color for each fragment at the moment
}
