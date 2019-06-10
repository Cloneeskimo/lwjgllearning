package engine.graphics.loaders.md5;

import engine.Utils;

import java.util.ArrayList;
import java.util.List;

public class MD5Frame {

    //Data
    private int id;
    private float[] frameData;

    //Accessors
    public int getId() { return this.id; }
    public float[] getFrameData() { return this.frameData; }

    //Mutators
    public void setId(int id) { this.id = id; }
    public void setFrameData(float[] frameData) { this.frameData = frameData; }

    //String Conversion Method
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("frame " + id + " [data: " + System.lineSeparator());
        for (float data : this.frameData) s.append(data).append(System.lineSeparator());
        s.append("]").append(System.lineSeparator());
        return s.toString();
    }

    //Parser Method
    public static MD5Frame parse(String blockId, List<String> blockBody) throws Exception {
        MD5Frame result = new MD5Frame();
        String[] tokens = blockId.trim().split("\\s+");
        if (tokens != null && tokens.length >= 2) {
            result.setId(Integer.parseInt(tokens[1]));
        } else throw new Exception("Wrong frame definition: " + blockId);

        List<Float> data = new ArrayList<>();
        for (String line : blockBody) {
            List<Float> lineData = parseLine(line);
            if (lineData != null) data.addAll(lineData);
        }
        float[] dataArr = Utils.listToArray(data);
        result.setFrameData(dataArr);
        return result;
    }

    //Line Parsing Method
    private static List<Float> parseLine(String line) {
        String[] tokens = line.trim().split("\\s+");
        List<Float> data = new ArrayList<>();
        for (String token : tokens) data.add(Float.parseFloat(token));
        return data;
    }
}
