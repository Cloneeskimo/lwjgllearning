package engine.graphics.loaders.md5;

import engine.Utils;
import engine.gameitem.AnimGameItem;
import engine.gameitem.GameItem;
import engine.graphics.Material;
import engine.graphics.Mesh;
import engine.graphics.Texture;
import engine.graphics.anim.AnimVertex;
import engine.graphics.anim.AnimatedFrame;
import org.joml.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MD5Loader {

    //Static Data
    private static final String NORMAL_FILE_SUFFIX = "_normal";

    //Model Processing Method
    public static AnimGameItem process(MD5Model md5Model, MD5AnimModel animModel, Vector4f defaultColor) throws Exception {
        List<Matrix4f> invJointMatrices = calcInJointMatrices(md5Model);
        List<AnimatedFrame> animatedFrames = processAnimationFrames(md5Model, animModel, invJointMatrices);

        List<Mesh> meshes = new ArrayList<>();
        for (MD5Mesh md5Mesh : md5Model.getMeshes()) {
            Mesh mesh = generateMesh(md5Model, md5Mesh);
            handleTexture(mesh, md5Mesh, defaultColor);
            meshes.add(mesh);
        }

        Mesh[] meshesArr = new Mesh[meshes.size()];
        meshesArr = meshes.toArray(meshesArr);

        return new AnimGameItem(meshesArr, animatedFrames, invJointMatrices);
    }

    //Joint Matrices Calculation Method
    private static List<Matrix4f> calcInJointMatrices(MD5Model md5Model) {
        List<Matrix4f> result = new ArrayList<>();

        List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();
        for (MD5JointInfo.MD5JointData joint : joints) {
            Matrix4f mat = new Matrix4f().translate(joint.getPosition())
                    .rotate(joint.getOrientation())
                    .invert();
            result.add(mat);
        }
        return result;
    }

    //Animation Frame List Processing Method
    private static List<AnimatedFrame> processAnimationFrames(MD5Model md5Model, MD5AnimModel animModel,
                                                              List<Matrix4f> invJointMatrices) {
        List<AnimatedFrame> animatedFrames = new ArrayList<>();
        List<MD5Frame> frames = animModel.getFrames();
        for (MD5Frame frame : frames) {
            AnimatedFrame data = processAnimationFrame(md5Model, animModel, frame, invJointMatrices);
            animatedFrames.add(data);
        }
        return animatedFrames;
    }

    //Single Animation Frame Processing Method
    private static AnimatedFrame processAnimationFrame(MD5Model md5Model, MD5AnimModel animModel, MD5Frame frame,
                                                       List<Matrix4f> invJointMatrices) {
        //get appropriate data
        AnimatedFrame result = new AnimatedFrame();
        MD5BaseFrame baseFrame = animModel.getBaseFrame();
        List<MD5Hierarchy.MD5HierarchyData> hierarchyList = animModel.getHierarchy().getHierarchyData();
        List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();
        int numJoints = joints.size();
        float[] frameData = frame.getFrameData();

        //apply transformations according to flags
        for (int i = 0; i < numJoints; i++) {

            //get appropriate data
            MD5JointInfo.MD5JointData joint = joints.get(i);
            MD5BaseFrame.MD5BaseFrameData baseFrameData = baseFrame.getFrameData().get(i);
            Vector3f position = baseFrameData.getPosition();
            Quaternionf orientation = baseFrameData.getOrientation();
            int flags = hierarchyList.get(i).getFlags();
            int startIndex = hierarchyList.get(i).getStartIndex();

            //check flags
            if ((flags & 1) > 0) position.x = frameData[startIndex++];
            if ((flags & 2) > 0) position.y = frameData[startIndex++];
            if ((flags & 4) > 0) position.z = frameData[startIndex++];
            if ((flags & 8) > 0) orientation.x = frameData[startIndex++];
            if ((flags & 16) > 0) orientation.y = frameData[startIndex++];
            if ((flags & 32) > 0) orientation.z = frameData[startIndex++];

            //update w component of orientation
            orientation = MD5Utils.calculateQuaternion(new Vector3f(orientation.x, orientation.y, orientation.z));

            //calculate translation and rotation matrices for this joint
            Matrix4f translateMat = new Matrix4f().translate(position);
            Matrix4f rotationMat = new Matrix4f().rotate(orientation);
            Matrix4f jointMat = translateMat.mul(rotationMat);

            //joint position is relative to joint's parent index position. use parent matrices
            //to transform it to model space
            if (joint.getParentIndex() > -1) {
                Matrix4f parentMatrix = result.getLocalJointMatrices()[joint.getParentIndex()];
                jointMat = new Matrix4f(parentMatrix).mul(jointMat);
            }

            //set matrix
            result.setMatrix(i, jointMat, invJointMatrices.get(i));
        }

        //return frame
        return result;
    }

    //Mesh Generation Method
    private static Mesh generateMesh(MD5Model md5Model, MD5Mesh md5Mesh) throws Exception {

        //create lists
        List<AnimVertex> vertices = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        List<MD5Mesh.MD5Vertex> md5Vertices = md5Mesh.getVertices();
        List<MD5Mesh.MD5Weight> weights = md5Mesh.getWeights();
        List<MD5JointInfo.MD5JointData> joints = md5Model.getJointInfo().getJoints();

        //process vertices for binding pose
        for (MD5Mesh.MD5Vertex md5Vertex : md5Vertices) {

            //create and add vertex
            AnimVertex vertex = new AnimVertex();
            vertices.add(vertex);

            //set texture coordinates and position
            vertex.position = new Vector3f();
            vertex.texCoords = md5Vertex.getTexCoords();

            //get starting weight and number of weights
            int startWeight = md5Vertex.getStartWeight();
            int numWeights = md5Vertex.getWeightCount();

            //create joint indices and weights arrays and initalize to -1
            vertex.jointIndices = new int[numWeights];
            Arrays.fill(vertex.jointIndices, -1);
            vertex.weights = new float[numWeights];
            Arrays.fill(vertex.weights, -1);

            //loop through weights to calculate vertex position
            for (int i = startWeight; i < startWeight + numWeights; i++) {
                MD5Mesh.MD5Weight weight = weights.get(i);
                MD5JointInfo.MD5JointData joint = joints.get(weight.getJointIndex());
                Vector3f rotatedPos = new Vector3f(weight.getPosition()).rotate(joint.getOrientation());
                Vector3f acumPos = new Vector3f(joint.getPosition()).add(rotatedPos);
                acumPos.mul(weight.getBias());
                vertex.position.add(acumPos);
                vertex.jointIndices[i - startWeight] = weight.getJointIndex();
                vertex.weights[i - startWeight] = weight.getBias();
            }
        }

        //process triangles
        for (MD5Mesh.MD5Triangle tri : md5Mesh.getTriangles()) {
            indices.add(tri.getV0());
            indices.add(tri.getV1());
            indices.add(tri.getV2());

            AnimVertex v0 = vertices.get(tri.getV0());
            AnimVertex v1 = vertices.get(tri.getV1());
            AnimVertex v2 = vertices.get(tri.getV2());
            Vector3f pos0 = v0.position;
            Vector3f pos1 = v1.position;
            Vector3f pos2 = v2.position;

            Vector3f normal = (new Vector3f(pos2).sub(pos0)).cross(new Vector3f(pos1).sub(pos0));
            v0.normal.add(normal);
            v1.normal.add(normal);
            v2.normal.add(normal);
        }

        //normalize result
        for (AnimVertex v : vertices) v.normal.normalize();

        //create and return mesh
        return createMesh(vertices, indices);
    }

    //Mesh Creation Method
    private static Mesh createMesh(List<AnimVertex> vertices, List<Integer> indices) {

        //create lists
        List<Float> positions = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Integer> jointIndices = new ArrayList<>();
        List<Float> weights = new ArrayList<>();

        //process each vertex
        for (AnimVertex v : vertices) {

            //positions
            positions.add(v.position.x);
            positions.add(v.position.y);
            positions.add(v.position.z);

            //texture coordinates
            texCoords.add(v.texCoords.x);
            texCoords.add(v.texCoords.y);

            //normal vectors
            normals.add(v.normal.x);
            normals.add(v.normal.y);
            normals.add(v.normal.z);

            //weights
            int numWeights = v.weights.length;
            for (int i = 0; i < Mesh.MAX_WEIGHTS; i++) {
                if (i < numWeights) {
                    jointIndices.add(v.jointIndices[i]);
                    weights.add(v.weights[i]);
                } else {
                    jointIndices.add(-1);
                    weights.add(-1.0f);
                }
            }
        }

        //convert to arrays
        float[] positionsArr = Utils.listToArray(positions);
        float[] texCoordsArr = Utils.listToArray(texCoords);
        float[] normalsArr = Utils.listToArray(normals);
        int[] indicesArr = Utils.listToArrayI(indices);
        int[] jointIndicesArr = Utils.listToArrayI(jointIndices);
        float[] weightsArr = Utils.listToArray(weights);

        //create and return mesh
        return new Mesh(positionsArr, texCoordsArr, normalsArr, indicesArr, jointIndicesArr, weightsArr);
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
}
