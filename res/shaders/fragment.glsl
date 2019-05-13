//GLSL Version
#version 330

//Ins
in vec2 textureCoordsFrag; //color passed from vertex shader
in vec3 mvVertexNormal;    //vertex normal vector from vertex shader for lighting calculations (model view space)
in vec3 mvVertexPos;       //vertex position from vertex shader for lighting calculations (model view space)

//Out
out vec4 fragColor;        //output variable definition

//Attenuation Struct (takes into account distance for lighting)
struct Attenuation {
    float constant;
    float linear;
    float exponent;
};

//Light Point Struct
struct LightPoint { //assumed to be in view coordinates
    vec3 color;
    vec3 position;
    float intensity; //0.0f - 1.0f
    Attenuation attenuation;
};

/*
    a material is defined by a set of colors (if we don't use texture to color the fragments)
    ambient, diffuse, specular components
    a material is also defined by a flag that controls if it has an associated texture or not, and
    a reflectance index.
*/
//Material Struct
struct Material {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    int hasTexture;
    float reflectance;
};

//Uniforms
uniform sampler2D textureSampler; //use this to define which texture unit/bank of the graphics card we will store the texture in
uniform vec3 ambientLight;        //a color which will affect every fragment in the same way (ambient light)
uniform float specularPower;      //exponent used in calculation specular light
uniform Material material;        //material characteristics
uniform LightPoint lightPoint;    //a point of light

//Global Variables - use these for the material so that we do not do redundant texture lookups
//if the material uses a texture instead of a color
vec4 ambientC; //ambient color
vec4 diffuseC; //diffuse color
vec4 specularC; //specular color

//Color Setup Function
void setupColors(Material material, vec2 textureCoordinate) {

    //setup global variables according to whether the material has a texture or not
    if (material.hasTexture == 1) { //if texture
        ambientC = texture(textureSampler, textureCoordinate); //set ambient base to texture sampled color
        diffuseC = ambientC; //set diffuse base to texture sampled color
        specularC = ambientC; //set specular base to texture sampled color
    } else { //otherwise, set all the bases to the material's self-defined colors;
        ambientC = material.ambient;
        diffuseC = material.diffuse;
        specularC = material.specular;
    }
}

//Light Calculating Function
vec4 calculateLight(LightPoint light, vec3 position, vec3 normal) {

    //create diffuse and specular vectors
    vec4 diffuseColor = vec4(0, 0, 0, 0);
    vec4 specularColor = vec4(0, 0, 0, 0);

    //diffuse light calculation
    vec3 lightDirection = lightPoint.position - position; //calculate vector from fragment to light position
    vec3 toLightSource = normalize(lightDirection); //normalize
    float diffuseFactor = max(dot(normal, toLightSource), 0.0);
    /*
        above, we calculate the diffuse factor by doing the dot product of the fragment's normal vector
        and the vector from the fragment to the light source. Essentially, this checks how parallel the
        two vectors are. The more parallel, the higher the factor, the more bright. In other words, the more
        that the fragment faces the light, the brighter it will be.
    */
    diffuseColor = diffuseC * vec4(light.color, 1.0) * light.intensity * diffuseFactor; //calculate final diffuse color
    /*
        above, diffuse color takes into account the base diffuse color as provided by the material, the light's color, the light's intensity,
        and the diffuse factor just calculated based on how much the fragment faces the light
    */

    //specular light
    vec3 cameraDirection = normalize(-position); //calculate camera direction
    vec3 fromLightSource = -toLightSource; //vector from light source to fragment
    vec3 reflectedLight = normalize(reflect(fromLightSource, normal)); //calculate reflected light
    float specularFactor = max(dot(cameraDirection, reflectedLight), 0.0); //the more the camera points to the material, the higher the specular factor
    specularFactor = pow(specularFactor, specularPower); //raise to power
    specularColor = specularC * specularFactor * material.reflectance * vec4(light.color, 1.0); //calculate final specular color
    /*
        above, specular color takes into account the base specular color as provided by the material, the factor just calculated by camera
        position, the material's ability to reflect, and the light's color.
    */

    //attenuation
    float distance = length(lightDirection);
    float attenuationInv = (light.attenuation.constant) + (light.attenuation.linear * distance) + (light.attenuation.exponent * distance * distance);
    return (diffuseColor + specularColor) / attenuationInv; //return final color
    /*
        above, we calculate the distance the fragment is from the light. then, we take that into account by calculating the
        attenuation suffered by the light in its travel to the vertex we are processing, and dividing the final color by that.
    */
}

//Main Function
void main() {

    setupColors(material, textureCoordsFrag); //setup the color bases we will be working with (texture or manually provided?)
    vec4 diffuseSpecularComp = calculateLight(lightPoint, mvVertexPos, mvVertexNormal); //calculate diffuse and specular light with setup color
    fragColor = ambientC * vec4(ambientLight, 1) + diffuseSpecularComp; //add ambient light (unaffected by atten.) and return final fragment color
}
