package engine.graphics.loaders.md5;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5BaseFrame {

    //Data
    private List<MD5BaseFrameData> frameData;

    //Accessor
    public List<MD5BaseFrameData> getFrameData() { return this.frameData; }

    //Mutator
    public void setFrameData(List<MD5BaseFrameData> frameData) { this.frameData = frameData; }

    //String Conversion Method
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("base frame [" + System.lineSeparator());
        for (MD5BaseFrameData data : this.frameData) {
            s.append(frameData).append(System.lineSeparator());
        }
        s.append("]").append(System.lineSeparator());
        return s.toString();
    }

    //Parser Method
    public static MD5BaseFrame parse(List<String> blockBody) {
        MD5BaseFrame result = new MD5BaseFrame();

        List<MD5BaseFrameData> frameDataList = new ArrayList<>();
        result.setFrameData(frameDataList);

        for (String line : blockBody) {
            MD5BaseFrameData frameInfo = MD5BaseFrameData.parse(line);
            if (frameInfo != null) frameDataList.add(frameInfo);
        }

        return result;
    }

    //Base Frame Data Class
    public static class MD5BaseFrameData {

        //Static Data
        private static final Pattern PATTERN_BASEFRAME = Pattern.compile("\\s*" + MD5Utils.VECTOR3_REGEXP + "\\s*" +
                MD5Utils.VECTOR3_REGEXP + ".*");

        //Data
        private Vector3f position;
        private Quaternionf orientation;

        //Accessors
        public Vector3f getPosition() { return this.position; }
        public Quaternionf getOrientation() { return this.orientation; }

        //Mutators
        public void setPosition(Vector3f position) { this.position = position; }
        public void setOrientation(Vector3f orientation) { this.orientation = MD5Utils.calculateQuaternion(orientation); }

        //String Conversion Method
        @Override
        public String toString() { return "[position: " + this.position + ", orientation: " + this.orientation + "]"; }

        //Parser Method
        public static MD5BaseFrameData parse(String line) {
            Matcher matcher = PATTERN_BASEFRAME.matcher(line);
            MD5BaseFrameData result = null;
            if (matcher.matches()) {
                result = new MD5BaseFrameData();
                float x = Float.parseFloat(matcher.group(1));
                float y = Float.parseFloat(matcher.group(2));
                float z = Float.parseFloat(matcher.group(3));
                result.setPosition(new Vector3f(x, y, z));
                x = Float.parseFloat(matcher.group(4));
                y = Float.parseFloat(matcher.group(5));
                z = Float.parseFloat(matcher.group(6));
                result.setOrientation(new Vector3f(x, y, z));
            }

            return result;
        }
    }
}
