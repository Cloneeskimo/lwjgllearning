package engine.graphics;

import engine.Utils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import java.util.ArrayList;
import java.util.List;

//A utility class used to load a mesh (engine.graphics.Mesh) from a .obj file
public class OBJLoader {

    //Static Method for loading an OBJ into a mesh
    public static Mesh loadMesh(String fileName) throws Exception {

        //Read OBJ file in
        List<String> file = Utils.readEntireFile(fileName);

        //ArrayLists for the things read from the OBJ file
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Vector2f> textures = new ArrayList<>();
        List<Face> faces = new ArrayList<>();

        //Parse lines of file
        for (String line : file) {
            String[] tokens = line.split("\\s+");
            switch (tokens[0]) {
                case "v": //Geometric Vertex
                    Vector3f vVertex = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    vertices.add(vVertex);
                    break;
                case "vt": //Texture Coordinate
                    Vector2f vTextureCoords = new Vector2f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]));
                    textures.add(vTextureCoords);
                    break;
                case "vn": //Normal Vector
                    Vector3f vNormalVector = new Vector3f(
                            Float.parseFloat(tokens[1]),
                            Float.parseFloat(tokens[2]),
                            Float.parseFloat(tokens[3]));
                    normals.add(vNormalVector);
                    break;
                case "f": //Face
                    faces.add(new Face(tokens[1], tokens[2], tokens[3]));
                    break;
                default: //Incorrect Line
                    System.out.println("Uninterpretable line in object file: '" + line + "' - will be ignored");
                    break;
            }
        }

        return reoorderLists(vertices, normals, textures, faces);
    }

    //Other Methods
    private static Mesh reoorderLists(List<Vector3f> vertices, List<Vector3f> normals, List<Vector2f> textures, List<Face> faces) {

        List<Integer> indices = new ArrayList();

        //Create arrays
        float[] posArr = new float[vertices.size() * 3];
        float[] textureCoordsArr = new float[vertices.size() * 2];
        float[] normalVectorsArr = new float[vertices.size() * 3];

        //Fill position array in the order it has been declared
        int i = 0;
        for (Vector3f vertex : vertices) {
            posArr[i * 3] = vertex.x;
            posArr[i * 3 + 1] = vertex.y;
            posArr[i * 3 + 2] = vertex.z;
            i++;
        }

        for (Face face : faces) {
            IndexGroup[] indexGroups = face.getIndexGroups();
            for (IndexGroup indexGroup : indexGroups) processIndexGroup(indexGroup, textures, normals, indices, textureCoordsArr, normalVectorsArr);
        }

        int[] indicesArr = new int[indices.size()];
        indicesArr = indices.stream().mapToInt((Integer v) -> v).toArray();
        return new Mesh(posArr, textureCoordsArr, normalVectorsArr, indicesArr);
    }

    private static void processIndexGroup(IndexGroup indexGroup, List<Vector2f> textures, List<Vector3f> normals, List<Integer> indices, float[] textureCoordsArr, float[] normalVectorsArr) {

        //Get index for vertex coordinates
        int posIndex = indexGroup.posIndex;
        indices.add(posIndex);

        //Reorder texture coordinates
        if (indexGroup.textureCoordIndex >= 0) {
            Vector2f textureCoord = textures.get(indexGroup.textureCoordIndex);
            textureCoordsArr[posIndex * 2] = textureCoord.x;
            textureCoordsArr[posIndex * 2 + 1] = 1 - textureCoord.y;
        }

        //Reorder normal vectors
        if (indexGroup.normalVectorIndex >= 0) {
            Vector3f normalVector = normals.get(indexGroup.normalVectorIndex);
            normalVectorsArr[posIndex * 3] = normalVector.x;
            normalVectorsArr[posIndex * 3 + 1] = normalVector.y;
            normalVectorsArr[posIndex * 3 + 2] = normalVector.z;
        }
    }

    //Faces are composed of a list of indices groups, in this case, since we are dealing with
    //triangles, it will hold three index groups.
    protected static class Face {

        //List of IndexGroups for a face triangle (3 vertices per face)
        IndexGroup[] indexGroups = new IndexGroup[3];

        //Constructor
        public Face(String group1, String group2, String group3) {
            indexGroups = new IndexGroup[3];
            indexGroups[0] = parseGroup(group1);
            indexGroups[1] = parseGroup(group2);
            indexGroups[2] = parseGroup(group3);
        }

        //Parse a string into an index group
        private IndexGroup parseGroup(String groupString) {
            IndexGroup group = new IndexGroup();

            String[] tokens = groupString.split("/"); //split line into tokens by slashes
            int length = tokens.length; //capture its length

            group.posIndex = Integer.parseInt(tokens[0]) - 1; //position

            if (length > 1) { //texture coordinates
                String textureCoord = tokens[1];
                group.textureCoordIndex = textureCoord.length() > 0 ? Integer.parseInt(textureCoord) - 1 : IndexGroup.NO_VALUE;
            }

            if (length > 2) group.normalVectorIndex = Integer.parseInt(tokens[2]) - 1; //normal vector

            return group;
        }

        //Accessor
        public IndexGroup[] getIndexGroups() { return this.indexGroups; }
    }

    //IndexGroup will hold information for a single index group (used in the Face class)
    protected static class IndexGroup {

        //Static Data
        public static final int NO_VALUE = -1;

        //Data
        public int posIndex;
        public int textureCoordIndex;
        public int normalVectorIndex;

        //Constructor
        public IndexGroup() { posIndex = textureCoordIndex = normalVectorIndex = NO_VALUE; }
    }
}
