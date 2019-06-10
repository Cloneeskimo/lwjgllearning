package engine.graphics.loaders.md5;

import java.util.List;

public class MD5AnimHeader {

    //Data
    private String version;
    private String commandLine;
    private int numFrames;
    private int numJoints;
    private int frameRate;
    private int numAnimatedComponents;

    //String Conversion Method
    @Override
    public String toString() {
        return "animHeader: [version: " + version + ", commandLine: " + commandLine + ", numFrames: " + numFrames +
                ", frameRate: " + frameRate + ", numAnimatedComponents:" + numAnimatedComponents + "]";
    }

    //Parser
    public static MD5AnimHeader parse(List<String> lines) throws  Exception {

        //create header object
        MD5AnimHeader ah = new MD5AnimHeader();

        //count lines
        int numLines = lines != null ? lines.size() : 0;
        if (numLines == 0) throw new Exception("Cannot parse empty file");

        //parse file
        boolean done = false;
        for (int i = 0; i < numLines && !done; i++) {
            String line = lines.get(i);
            String[] tokens = line.split("\\s+");
            int numTokens = tokens != null ? tokens.length : 0;
            if (numTokens > 1) {

                //get parameter name and value
                String paramName = tokens[0];
                String paramValue = tokens[1];

                //parse parameter
                switch (paramName) {
                    case "MD5Version":
                        ah.setVersion(paramValue);
                        break;
                    case "commandline":
                        ah.setCommandLine(paramValue);
                        break;
                    case "numFrames":
                        ah.setNumFrames(Integer.parseInt(paramValue));
                        break;
                    case "numJoints":
                        ah.setNumJoints(Integer.parseInt(paramValue));
                        break;
                    case "frameRate":
                        ah.setFrameRate(Integer.parseInt(paramValue));
                        break;
                    case "numAnimatedComponents":
                        ah.setNumAnimatedComponents(Integer.parseInt(paramValue));
                        break;
                    case "hierarchy":
                        done = true;
                        break;
                    default:
                        System.out.println("unexcepted line, ignoring: '" + line + "'");
                }
            }
        }

        //return created header
        return ah;
    }

    //Accessors
    private String getVersion() { return this.version; }
    private String getCommandLine() { return this.commandLine; }
    private int getNumFrames() { return this.numFrames; }
    private int getNumJoints() { return this.numJoints; }
    private int getFrameRate() { return this.frameRate; }
    private int getNumAnimatedComponents() { return this.numAnimatedComponents; }

    //Mutators
    private void setVersion(String version) { this.version = version; }
    private void setCommandLine(String commandLine) { this.commandLine = commandLine; }
    private void setNumFrames(int numFrames) { this.numFrames = numFrames; }
    private void setNumJoints(int numJoints) { this.numJoints = numJoints; }
    private void setFrameRate(int frameRate) { this.frameRate = frameRate; }
    private void setNumAnimatedComponents(int numAnimatedComponents) { this.numAnimatedComponents = numAnimatedComponents; }
}
