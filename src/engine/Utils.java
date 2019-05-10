package engine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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

    //A method used to read the entirety of a file into a string list
    public static List<String> readEntireFile(String fileName) throws Exception {
        List<String> file = new ArrayList<>();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(Class.forName(Utils.class.getName()).getResourceAsStream(fileName)))) {
            String line;
            while ((line = in.readLine()) != null) file.add(line);
        }
        return file;
    }
}
