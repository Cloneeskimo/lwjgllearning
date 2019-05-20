package engine.graphics.light;

import org.joml.Vector3f;

public class SceneLighting {

    //Data
    private Vector3f ambientLight;
    private LightPoint[] lightPoints;
    private SpotLight[] spotLights;
    private DirectionalLight directionalLight;

    //Accessors
    public Vector3f getAmbientLight() { return this.ambientLight; }
    public LightPoint[] getLightPoints() { return this.lightPoints; }
    public SpotLight[] getSpotLights() { return this.spotLights;}
    public DirectionalLight getDirectionalLight() { return this.directionalLight; }

    //Mutators
    public void setAmbientLight(Vector3f ambientLight) { this.ambientLight = ambientLight; }
    public void setLightPoints(LightPoint[] lightPoints) { this.lightPoints = lightPoints; }
    public void setSpotLights(SpotLight[] spotLights) { this.spotLights = spotLights; }
    public void setDirectionalLight(DirectionalLight directionalLight) { this.directionalLight = directionalLight; }
    public void setLights(Vector3f ambientLight, LightPoint[] lightPoints, SpotLight[] spotLights, DirectionalLight directionalLight) {
        this.ambientLight = ambientLight;
        this.lightPoints = lightPoints;
        this.spotLights = spotLights;
        this.directionalLight = directionalLight;
    }
}
