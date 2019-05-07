package engine;

import java.io.InputStream;
import java.util.Scanner;

public class Utils {

    //A method used to retrieve the contents of a file based on the class path
    public static String loadResources(String fileName) throws Exception {
        String result;
        try (InputStream in = Class.forName(Utils.class.getName()).getResourceAsStream(fileName);
             Scanner scanner = new Scanner(in, "UTF-8")) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }
}
