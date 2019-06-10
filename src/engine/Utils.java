package engine;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Utils {

    //A method used to retrieve the contents of a file based on the class path
    public static String loadResource(String fileName) throws Exception {
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

    //A method used to convert a list to an array
    public static float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] a = new float[size];
        for (int i = 0; i < size; i++) a[i] = list.get(i);
        return a;
    }

    //A method which returns whether a resource exists or not
    public static boolean resourceFileExists(String fileName) {
        boolean result;
        try (InputStream is = Utils.class.getResourceAsStream(fileName)) {
            result = is != null;
        } catch (Exception e) {
            result = false;
        }
        return result;
    }
}
