
//GLSL Version
#version 330

//VAO Inputs
layout (location=0) in vec3 position;            //position in world space
layout (location=1) in vec2 textureCoordsVertex; //processed by fragment shader, so will just be passed through
layout (location=2) in vec3 vertexNormal;        //normal vector

//Outs
out vec2 textureCoordsFrag;     //texture coordinates
out vec3 mvVertexNormal;        //pass through normal in model view space for lighting
out vec3 mvVertexPos;           //pass through position in model view space for lighting
out vec4 mlightviewVertexPos;   //for shadow calculations
out mat4 modelViewFrag;         //pass through the ModelView matrix for normal maps

//Uniforms
uniform mat4 projection; //projection matrix
uniform mat4 modelView;  //model view matrix (world and view)
uniform mat4 modelLightViewMatrix;
uniform mat4 orthoProjectionMatrix;

//Main Function
void main()
{
    //position
    vec4 mvPos = modelView * vec4(position, 1.0); //convert position to model view space
    gl_Position = projection * mvPos; //convert position to projection space and set it as gl_Position

    //texture coordinates
    textureCoordsFrag = textureCoordsVertex; //just pass through texture coordinates

    //normals - we convert the normal vector to model view, but we first set w to 0 because we are not interested
    //in its translation, only its rotation and scale. setting w to 0 prevents the translation from occuring
    mvVertexNormal = normalize(modelView * vec4(vertexNormal, 0.0)).xyz; //convert normal to modelview space
    mvVertexPos = mvPos.xyz; //pass through vertex position for lighting calculations

    mlightviewVertexPos = orthoProjectionMatrix * modelLightViewMatrix * vec4(position, 1.0);

    //pass through ModelView Matrix
    modelViewFrag = modelView;
}