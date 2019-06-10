package engine.graphics.loaders.md5;

import java.util.List;

public class MD5ModelHeader {

    //Data
    private String version;
    private String commandLine;
    private int numJoints;
    private int numMeshes;

    //Accessors
    public String getVersion() { return this.version; }
    public String getCommandLine() { return this.commandLine; }
    public int getNumJoints() { return this.numJoints; }
    public int getNumMeshes() { return this.numMeshes; }

    //Mutators
    public void setVersion(String version) { this.version = version; }
    public void setCommandLine(String commandLine) { this.commandLine = commandLine; }
    public void setNumJoints(int numJoints) { this.numJoints = numJoints; }
    public void setNumMeshes(int numMeshes) { this.numMeshes = numMeshes; }

    //String Conversion Method
    @Override
    public String toString() {
        return "[version: " + version + ", commandLine: " + commandLine + ", numJoints: " + numJoints + ", numMeshes: "
                + numMeshes + "]";
    }

    //Parser Method
    public static MD5ModelHeader parse(List<String> lines) throws  Exception {

        //create items
        MD5ModelHeader result = new MD5ModelHeader();
        int numLines = lines != null ? lines.size() : 0;
        if (numLines == 0) throw new Exception("Cannot parse empty file");

        //parse lines
        boolean finish = false;
        for (int i = 0; i < numLines && !finish; i++) {
            String line = lines.get(i);
            String[] tokens = line.split("\\s+");
            int numTokens = tokens != null ? tokens.length : 0;
            if (numTokens > 1) {
                String paramName = tokens[0];
                String paramValue = tokens[1];

                switch (paramName) {
                    case "MD5Version":
                        result.setVersion(paramValue);
                        break;
                    case "commandLine":
                        result.setCommandLine(paramValue);
                        break;
                    case "numJoints":
                        result.setNumJoints(Integer.parseInt(paramValue));
                        break;
                    case "numMeshes":
                        result.setNumMeshes(Integer.parseInt(paramValue));
                        break;
                    case "joints":
                        finish = true;
                        break;
                }
            }
        }

        //return created header
        return result;
    }
}
