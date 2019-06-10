package engine.graphics.loaders.md5;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5BoundInfo {

    //Data
    private List<MD5Bound> bounds;

    //Accessor
    public List<MD5Bound> getBounds() { return this.bounds; }

    //Mutator
    public void setBounds(List<MD5Bound> bounds) { this.bounds = bounds; }

    //String Conversion Method
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("bounds [" + System.lineSeparator());
        for (MD5Bound bound : this.bounds) s.append(bound).append(System.lineSeparator());
        s.append("]").append(System.lineSeparator());
        return s.toString();
    }

    //Parser Method
    public static MD5BoundInfo parse(List<String> blockBody) {
        MD5BoundInfo result = new MD5BoundInfo();
        List<MD5Bound> bounds = new ArrayList<>();
        for (String line : blockBody) {
            MD5Bound bound = MD5Bound.parse(line);
            if (bound != null) bounds.add(bound);
        }
        result.setBounds(bounds);
        return result;
    }

    //Bound Inner Class
    private static class MD5Bound {

        //Static Data
        private static final Pattern PATTERN_BOUND = Pattern.compile("\\s*" + MD5Utils.VECTOR3_REGEXP + "\\s*" +
                MD5Utils.VECTOR3_REGEXP + ".*");

        //Data
        private Vector3f minBound, maxBound;

        //Accessors
        public Vector3f getMinBound() { return this.minBound; }
        public Vector3f getMaxBound() { return this.maxBound; }

        //Mutators
        public void setMinBound(Vector3f minBound) { this.minBound = minBound; }
        public void setMaxBound(Vector3f maxBound) { this.maxBound = maxBound; }

        //String Conversion Method
        @Override
        public String toString() {
            return "[minBound: " + minBound + ", maxBound: " + maxBound + "]";
        }

        //Parser Method
        public static MD5Bound parse(String line) {
            MD5Bound result = null;
            Matcher matcher = PATTERN_BOUND.matcher(line);
            if (matcher.matches()) {
                result = new MD5Bound();
                float x = Float.parseFloat(matcher.group(1));
                float y = Float.parseFloat(matcher.group(2));
                float z = Float.parseFloat(matcher.group(3));
                result.setMinBound(new Vector3f(x, y, z));

                x = Float.parseFloat(matcher.group(4));
                y = Float.parseFloat(matcher.group(5));
                z = Float.parseFloat(matcher.group(6));
                result.setMaxBound(new Vector3f(x, y, z));
            }
            return result;
        }
    }
}
