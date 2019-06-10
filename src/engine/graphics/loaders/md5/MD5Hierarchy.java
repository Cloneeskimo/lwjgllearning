package engine.graphics.loaders.md5;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MD5Hierarchy {

    //Data
    private List<MD5HierarchyData> hierarchyData;

    //Accessor
    public List<MD5HierarchyData> getHierarchyData() { return this.hierarchyData; }

    //Mutators
    public void setHierarchyData(List<MD5HierarchyData> hierarchyData) { this.hierarchyData = hierarchyData; }

    //String Conversion Method
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("hierarchy [" + System.lineSeparator());
        for (MD5HierarchyData data : this.hierarchyData) {
            s.append(data).append(System.lineSeparator());
        }
        s.append("]").append(System.lineSeparator());
        return s.toString();
    }

    //Parser Method
    public static MD5Hierarchy parse(List<String> blockBody) {
        MD5Hierarchy result = new MD5Hierarchy();
        List<MD5HierarchyData> data = new ArrayList<>();
        result.setHierarchyData(data);
        for (String line : blockBody) {
            MD5HierarchyData d = MD5HierarchyData.parse(line);
            if (d != null) data.add(d);
        }
        return result;
    }

    //Hierarchy Data Inner Class
    public static class MD5HierarchyData {

        //Static Data
        private static final Pattern PATTERN_HIERARCHY = Pattern.compile("\\s*\\\"([^\\\"]+)\\\"\\s*([-]?\\d+)\\s*(\\d+)\\s*(\\d+).*");

        //Data
        private String name;
        private int parentIndex;
        private int startIndex;
        private int flags;

        //Accessors
        public String getName() { return this.name; }
        public int getParentIndex() { return this.parentIndex; }
        public int getStartIndex() { return this.startIndex; }
        public int getFlags() { return this.flags; }

        //Mutators
        public void setName(String name) { this.name = name; }
        public void setParentIndex(int parentIndex) { this.parentIndex = parentIndex; }
        public void setStartIndex(int startIndex) { this.startIndex = startIndex; }
        public void setFlags(int flags) { this.flags = flags; }

        //String Conversion Method
        @Override
        public String toString() {
            return "[name: " + name + ", parentIndex: " + parentIndex + ", flags: " + flags + ", startIndex: " +
                    startIndex + "]";
        }

        //Parser Method
        public static MD5HierarchyData parse(String line) {
            MD5HierarchyData result = null;
            Matcher matcher = PATTERN_HIERARCHY.matcher(line);
            if (matcher.matches()) {
                result = new MD5HierarchyData();
                result.setName(matcher.group(1));
                result.setParentIndex(Integer.parseInt(matcher.group(2)));
                result.setFlags(Integer.parseInt(matcher.group(3)));
                result.setStartIndex(Integer.parseInt(matcher.group(4)));
            }
            return result;
        }
    }
}
