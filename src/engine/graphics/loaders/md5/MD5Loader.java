package engine.graphics.loaders.md5;

import engine.Utils;
import engine.gameitem.GameItem;
import engine.graphics.Material;
import engine.graphics.Mesh;
import engine.graphics.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

public class MD5Loader {

    //Static Data
    private static final String NORMAL_FILE_SUFFIX = "_normal";

    //Model Processing Method
    public static GameItem process(MD5Model md5Model, Vector4f defaultColor) throws Exception {
        List<MD5Mesh> md5MeshList = md5Model.getMeshes();
        List<Mesh> meshes = new ArrayList<>();
        for (MD5Mesh md5Mesh : md5Model.getMeshes()) {
            Mesh mesh = generateMesh(md5Model, md5Mesh, defaultColor);
            handleTexture(mesh, md5Mesh, defaultColor);
            meshes.add(mesh);
        }
        Mesh[] meshesArr = new Mesh[meshes.size()];
        meshesArr = meshes.toArray(meshesArr);
        GameItem gameItem = new GameItem(meshesArr);

        return gameItem;
    }

    //Mesh Generation Method
    private static Mesh generateMesh(MD5Model md5Model, MD5Mesh md5Mesh, Vector4f defaultColor) throws Exception {

        //create lists
        List<VertexInfo> vertexInfos = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<MD5Mesh.MD5Vertex> vertices = md5Mesh.getVertices();
        List<MD5Mesh.MD5Weight> weights = md5Mesh.getWeights();
        List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();

        //process vertices
        for (MD5Mesh.MD5Vertex vertex : vertices) {
            Vector3f vertexPos = new Vector3f();
            Vector2f vertexTexCoords = vertex.getTexCoords();
            texCoords.add(vertexTexCoords.x);
            texCoords.add(vertexTexCoords.y);

            int startWeight = vertex.getStartWeight();
            int numWeights = vertex.getWeightCount();

            for (int i = startWeight; i < startWeight + numWeights; i++) {
                MD5Mesh.MD5Weight weight = weights.get(i);
                MD5JointInfo.MD5JointData joint = joints.get(weight.getJointIndex());
                Vector3f rotatedPos = new Vector3f(weight.getPosition()).rotate(joint.getOrientation());
                Vector3f acumPos = new Vector3f(joint.getPosition()).add(rotatedPos);
                acumPos.mul(weight.getBias());
                vertexPos.add(acumPos);
            }
            vertexInfos.add(new VertexInfo(vertexPos));
        }

        //process triangles
        for (MD5Mesh.MD5Triangle tri : md5Mesh.getTriangles()) {
            indices.add(tri.getV0());
            indices.add(tri.getV1());
            indices.add(tri.getV2());

            VertexInfo v0 = vertexInfos.get(tri.getV0());
            VertexInfo v1 = vertexInfos.get(tri.getV1());
            VertexInfo v2 = vertexInfos.get(tri.getV2());
            Vector3f pos0 = v0.position;
            Vector3f pos1 = v1.position;
            Vector3f pos2 = v2.position;

            Vector3f normal = (new Vector3f(pos2).sub(pos0)).cross(new Vector3f(pos1).sub(pos0));
            v0.normal.add(normal);
            v1.normal.add(normal);
            v2.normal.add(normal);
        }

        //normalize result
        for (VertexInfo v : vertexInfos) v.normal.normalize();

        //convert to arrays
        float[] positionsArr = VertexInfo.toPositionsArr(vertexInfos);
        float[] texCoordsArr = Utils.listToArray(texCoords);
        float[] normalsArr = VertexInfo.toNormalArr(vertexInfos);
        int[] indicesArr = indices.stream().mapToInt(i -> i).toArray();

        //create and return mesh
        return new Mesh(positionsArr, texCoordsArr, normalsArr, indicesArr);
    }

    //Texture Handling Method
    private static void handleTexture(Mesh mesh, MD5Mesh md5Mesh, Vector4f defaultColor) throws Exception {
        String texturePath = md5Mesh.getTexture();
        if (texturePath != null && texturePath.length() > 0) {
            Texture texture = new Texture(texturePath);
            Material material = new Material(texture);

            //handle normal maps
            int pos = texturePath.lastIndexOf(".");
            if (pos > 0) {
                String basePath = texturePath.substring(0, pos);
                String extension = texturePath.substring(pos, texturePath.length());
                String normalMapFileName = basePath + NORMAL_FILE_SUFFIX + extension;
                if (Utils.resourceFileExists(normalMapFileName)) {
                    Texture normalMap = new Texture(normalMapFileName);
                    material.setNormalMap(normalMap);
                }
            }
            mesh.setMaterial(material);
        } else {
            mesh.setMaterial(new Material(defaultColor, 1));
        }
    }

    //VertexInfo Inner Class
    private static class VertexInfo {

        //Data
        public Vector3f position, normal;

        //Position Constructor
        public VertexInfo(Vector3f position) {
            this.position = position;
            this.normal = new Vector3f(0, 0, 0);
        }

        //Default Constructor
        public VertexInfo() {
            this.position = new Vector3f();
            this.normal = new Vector3f();
        }

        //Positions Array Conversion Method
        public static float[] toPositionsArr(List<VertexInfo> list) {
            int length = list != null ? list.size() * 3 : 0;
            float[] result = new float[length];
            int i = 0;
            for (VertexInfo v : list) {
                result[i] = v.position.x;
                result[i + 1] = v.position.y;
                result[i + 2] = v.position.z;
                i += 3;
            }
            return result;
        }

        //Normals Array Conversion Method
        public static float[] toNormalArr(List<VertexInfo> list) {
            int length = list != null ? list.size() * 3 : 0;
            float[] result = new float[length];
            int i = 0;
            for (VertexInfo v : list) {
                result[i] = v.normal.x;
                result[i + 1] = v.normal.y;
                result[i + 2] = v.normal.z;
                i += 3;
            }
            return result;
        }
    }

}
