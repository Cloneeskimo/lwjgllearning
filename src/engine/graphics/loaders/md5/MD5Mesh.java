package engine.graphics.loaders.md5;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5Mesh {

    //Static Data
    private static final Pattern PATTERN_SHADER = Pattern.compile("\\s*shader\\s*\\\"([^\\\"]+)\\\"");
    private static final Pattern PATTERN_VERTEX = Pattern.compile("\\s*vert\\s*(\\d+)\\s*\\(\\s*("
            + MD5Utils.FLOAT_REGEXP + ")\\s*(" + MD5Utils.FLOAT_REGEXP + ")\\s*\\)\\s*(\\d+)\\s*(\\d+)");
    private static final Pattern PATTERN_TRI = Pattern.compile("\\s*tri\\s*(\\d+)\\s*(\\d+)\\s*(\\d+)\\s*(\\d+)");
    private static final Pattern PATTERN_WEIGHT = Pattern.compile("\\s*weight\\s*(\\d+)\\s*(\\d+)\\s*" +
            "(" + MD5Utils.FLOAT_REGEXP + ")\\s*" + MD5Utils.VECTOR3_REGEXP );

    //Data
    private String texture;
    private List<MD5Vertex> vertices;
    private List<MD5Triangle> triangles;
    private List<MD5Weight> weights;

    //Accessors
    public String getTexture() { return this.texture; }
    public List<MD5Vertex> getVertices() { return this.vertices; }
    public List<MD5Triangle> getTriangles() { return this.triangles; }
    public List<MD5Weight> getWeights() { return this.weights; }

    //Mutators
    public void setTexture(String texture) { this.texture = texture; }
    public void setVertices(List<MD5Vertex> vertices) { this.vertices = vertices; }
    public void setTriangles(List<MD5Triangle> triangles) { this.triangles = triangles; }
    public void setWeights(List<MD5Weight> weights) { this.weights = weights; }

    //Constructor
    public MD5Mesh() {
        this.vertices = new ArrayList<>();
        this.triangles = new ArrayList<>();
        this.weights = new ArrayList<>();
    }

    //String Conversion Method
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("mesh [" + System.lineSeparator());
        s.append("texture: ").append(this.texture).append(System.lineSeparator());
        s.append("vertices [").append(System.lineSeparator());
        for (MD5Vertex vertex : this.vertices) s.append(vertex).append(System.lineSeparator());
        s.append("]").append(System.lineSeparator());
        s.append("triangles [").append(System.lineSeparator());
        for (MD5Triangle triangle : this.triangles) s.append(triangle).append(System.lineSeparator());
        s.append("]").append(System.lineSeparator());
        s.append("weights [").append(System.lineSeparator());
        for (MD5Weight weight : this.weights) s.append(weight).append(System.lineSeparator());
        s.append("]").append(System.lineSeparator());
        return s.toString();
    }

    //Parser Method
    public static MD5Mesh parse(List<String> meshBlock) {

        //create items
        MD5Mesh result = new MD5Mesh();
        List<MD5Vertex> vertices = result.getVertices();
        List<MD5Triangle> triangles = result.getTriangles();
        List<MD5Weight> weights = result.getWeights();

        //parse each line
        for (String line : meshBlock) {
            if (line.contains("shader")) { //texture
                Matcher textureMatcher = PATTERN_SHADER.matcher(line);
                if (textureMatcher.matches()) result.setTexture(textureMatcher.group(1));
            } else if (line.contains("vert")) { //vertex
                Matcher vertexMatcher = PATTERN_VERTEX.matcher(line);
                if (vertexMatcher.matches()) {
                    MD5Vertex vertex = new MD5Vertex();
                    vertex.setIndex(Integer.parseInt(vertexMatcher.group(1)));
                    float x = Float.parseFloat(vertexMatcher.group(2));
                    float y = Float.parseFloat(vertexMatcher.group(3));
                    vertex.setTexCoords(new Vector2f(x, y));
                    vertex.setStartWeight(Integer.parseInt(vertexMatcher.group(4)));
                    vertex.setWeightCount(Integer.parseInt(vertexMatcher.group(5)));
                    vertices.add(vertex);
                }
            } else if (line.contains("tri")) { //triangle
                Matcher triMatcher = PATTERN_TRI.matcher(line);
                if (triMatcher.matches()) {
                    MD5Triangle triangle = new MD5Triangle();
                    triangle.setIndex(Integer.parseInt(triMatcher.group(1)));
                    triangle.setV0(Integer.parseInt(triMatcher.group(2)));
                    triangle.setV1(Integer.parseInt(triMatcher.group(3)));
                    triangle.setV2(Integer.parseInt(triMatcher.group(4)));
                    triangles.add(triangle);
                }
            } else if (line.contains("weight")) { //weight
                Matcher weightMatcher = PATTERN_WEIGHT.matcher(line);
                if (weightMatcher.matches()) {
                    MD5Weight weight = new MD5Weight();
                    weight.setIndex(Integer.parseInt(weightMatcher.group(1)));
                    weight.setJointIndex(Integer.parseInt(weightMatcher.group(2)));
                    weight.setBias(Float.parseFloat(weightMatcher.group(3)));
                    float x = Float.parseFloat(weightMatcher.group(4));
                    float y = Float.parseFloat(weightMatcher.group(5));
                    float z = Float.parseFloat(weightMatcher.group(6));
                    weight.setPosition(new Vector3f(x, y, z));
                    weights.add(weight);
                }
            }
        }

        //return created MD5Mesh
        return result;
    }

    //Vertex Inner Class
    public static class MD5Vertex {

        //Data
        private int index;
        private int startWeight;
        private int weightCount;
        private Vector2f texCoords;

        //Accessors
        public int getIndex() { return this.index; }
        public int getStartWeight() { return this.startWeight; }
        public int getWeightCount() { return this.weightCount; }
        public Vector2f getTexCoords() { return this.texCoords; }

        //Mutators
        public void setIndex(int index) { this.index = index; }
        public void setStartWeight(int startWeight) { this.startWeight = startWeight; }
        public void setWeightCount(int weightCount) { this.weightCount = weightCount; }
        public void setTexCoords(Vector2f texCoords) { this.texCoords = texCoords; }

        //String Conversion Method
        @Override
        public String toString() {
            return "[index: " + index + ", texCoords: " + texCoords + ", startWeight: " + startWeight +
                    ", weightCount: " + weightCount + "]";
        }
    }

    //Triangle Inner Class
    public static class MD5Triangle {

        //Data
        private int index;
        private int v0, v1, v2;

        //Accessors
        public int getIndex() { return this.index; }
        public int getV0() { return this.v0; }
        public int getV1() { return this.v1; }
        public int getV2() { return this.v2; }

        //Mutators
        public void setIndex(int index) { this.index = index; }
        public void setV0(int v0) { this.v0 = v0; }
        public void setV1(int v1) { this.v1 = v1; }
        public void setV2(int v2) { this.v2 = v2; }

        //String Conversion Method
        @Override
        public String toString() {
            return "[index: " + index + ", v0: " + v0 + ", v1: " + v1 + ", v2: " + v2 + "]";
        }
    }

    //Weight Inner Class
    public static class MD5Weight {

        //Data
        private int index;
        private int jointIndex;
        private float bias;
        private Vector3f position;

        //Accessors
        public int getIndex() { return this.index; }
        public int getJointIndex() { return this.jointIndex; }
        public float getBias() { return this.bias; }
        public Vector3f getPosition() { return this.position; }

        //Mutators
        public void setIndex(int index) { this.index = index; }
        public void setJointIndex(int jointIndex) { this.jointIndex = jointIndex; }
        public void setBias(float bias) { this.bias = bias; }
        public void setPosition(Vector3f position) { this.position = position; }

        //String Conversion Method
        @Override
        public String toString() {
            return "[index: " + index + ", jointIndex: " + jointIndex + ", bias: " + bias + ", position: " + position
                    + "]";
        }
    }
}
