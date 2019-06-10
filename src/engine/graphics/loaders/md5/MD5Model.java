package engine.graphics.loaders.md5;

import engine.Utils;

import java.util.ArrayList;
import java.util.List;

public class MD5Model {

    //Data
    private MD5JointInfo jointInfo;
    private MD5ModelHeader header;
    private List<MD5Mesh> meshes;

    //Constructor
    public MD5Model() { this.meshes = new ArrayList<>(); }

    //Accessors
    public MD5JointInfo getJointInfo() { return this.jointInfo; }
    public MD5ModelHeader getHeader() { return this.header; }
    public List<MD5Mesh> getMeshes() { return this.meshes; }

    //Mutators
    public void setJointInfo(MD5JointInfo jointInfo) { this.jointInfo = jointInfo; }
    public void setHeader(MD5ModelHeader header) { this.header = header; }
    public void setMeshes(List<MD5Mesh> meshes) { this.meshes = meshes; }

    //String Conversion Method
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("MD5MeshModel: " + System.lineSeparator());
        s.append(this.header).append(System.lineSeparator());
        s.append(this.jointInfo).append(System.lineSeparator());
        for (MD5Mesh mesh : this.meshes) s.append(mesh).append(System.lineSeparator());
        return s.toString();
    }

    //Parser Method
    public static MD5Model parse(String meshModelFile) throws Exception {

        //load file
        List<String> lines = Utils.readEntireFile(meshModelFile);
        MD5Model result = new MD5Model();
        int numLines = lines != null ? lines.size() : 0;
        if (numLines == 0) throw new Exception("Cannot parse empty file");

        //parse header
        boolean end = false;
        int start = 0;
        for (int i = 0; i < numLines && !end; i++) {
            String line = lines.get(i);
            end = line.trim().endsWith("{");
            start = i;
        }
        if (!end) throw new Exception("Cannot find header");
        List<String> headerBlock = lines.subList(0, start);
        MD5ModelHeader header = MD5ModelHeader.parse(headerBlock);
        result.setHeader(header);

        //parse rest of block
        int blockStart = 0;
        boolean inBlock = false;
        String blockId = "";
        for (int i = start; i < numLines; i++) {
            String line = lines.get(i);
            if (line.endsWith("{")) {
                blockStart = i;
                blockId = line.substring(0, line.lastIndexOf(" "));
                inBlock = true;
            } else if (inBlock && line.endsWith("}")) {
                List<String> blockBody = lines.subList(blockStart + 1, i);
                parseBlock(result, blockId, blockBody);
                inBlock = false;
            }
        }

        //return parsed model
        return result;
    }

    //Block Parsing Method
    private static void parseBlock(MD5Model model, String blockId, List<String> blockBody) throws Exception {
        switch (blockId) {
            case "joints":
                MD5JointInfo jointInfo = MD5JointInfo.parse(blockBody);
                model.setJointInfo(jointInfo);
                break;
            case "mesh":
                MD5Mesh mesh = MD5Mesh.parse(blockBody);
                model.getMeshes().add(mesh);
                break;
            default:
                break;
        }
    }
}
