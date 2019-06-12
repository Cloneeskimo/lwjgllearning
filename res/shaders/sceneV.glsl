
//GLSL Version
#version 330

//Constants
const int MAX_WEIGHTS = 4;
const int MAX_JOINTS = 150;

//VAO Inputs
layout (location = 0) in vec3 position;
layout (location = 1) in vec2 textureCoordsVertex;
layout (location = 2) in vec3 vertexNormal;
layout (location = 3) in vec4 jointWeights;
layout (location = 4) in ivec4 jointIndices;

//Outs
out vec2 textureCoordsFrag;     //texture coordinates
out vec3 mvVertexNormal;        //pass through normal in model view space for lighting
out vec3 mvVertexPos;           //pass through position in model view space for lighting
out vec4 mlightviewVertexPos;   //for shadow calculations
out mat4 modelViewFrag;         //pass through the ModelView matrix for normal maps

//Uniforms
uniform mat4 projection;
uniform mat4 modelView;
uniform mat4 modelLightViewMatrix;
uniform mat4 orthoProjectionMatrix;
uniform mat4 jointsMatrix[MAX_JOINTS];

//Main Function
void main()
{
    //calculate position and normal based on weights
    vec4 initPos = vec4(0, 0, 0, 0);
    vec4 initNormal = vec4(0, 0, 0, 0);
    int count = 0;
    for (int i = 0; i < MAX_WEIGHTS; i++) {
        float weight = jointWeights[i];
        if (weight > 0) {
            count++;
            int jointIndex = jointIndices[i];

            //alter position
            vec4 tmpPos = jointsMatrix[jointIndex] * vec4(position, 1.0);
            initPos += weight * tmpPos;

            //alter normal
            vec4 tmpNormal = jointsMatrix[jointIndex] * vec4(vertexNormal, 0.0);
            initNormal += weight * tmpNormal;
        }
    }

    //if no weights, just use passed in position
    if (count == 0) {
        initPos = vec4(position, 1.0);
        initNormal = vec4(vertexNormal, 0.0);
    }

    //set model view position and final gl position
    vec4 mvPos = modelView * initPos;
    gl_Position = projection * mvPos;

    //texture coordinates
    textureCoordsFrag = textureCoordsVertex;

    //normals - we convert the normal vector to model view, but we first set w to 0 because we are not interested
    //in its translation, only its rotation and scale. setting w to 0 prevents the translation from occuring
    mvVertexNormal = normalize(modelView * initNormal).xyz; //convert normal to modelview space
    mvVertexPos = mvPos.xyz; //pass through vertex position for lighting calculations

    //orthographically project light view for shadow calculations
    mlightviewVertexPos = orthoProjectionMatrix * modelLightViewMatrix * vec4(position, 1.0);

    //pass through ModelView Matrix
    modelViewFrag = modelView;
}