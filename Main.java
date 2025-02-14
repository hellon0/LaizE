import java.util.Scanner;
import java.io.File;
import java.io.FileNotFoundException;

public class Main {

    static String readFile(String path) {
        String result = "";

        try {
            File fileObject = new File(path);
            Scanner reader = new Scanner(fileObject);

            while(reader.hasNextLine()) {
                String data = reader.nextLine();
                result += data + "\n";
            }

            reader.close();
            return result;
        }
        catch (FileNotFoundException e) {
            System.out.println("Error: Unknown file path");
            e.printStackTrace();
        }

        return null;
    }
    public static void main(String[] args) throws Exception {
        File file = new File("src\\CodeWindow.txt");
        String path = file.getAbsolutePath();
        System.out.println(path);
        

        LaizE.execute(readFile(path));
    }
}
