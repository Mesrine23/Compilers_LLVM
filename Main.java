import syntaxtree.*;
import visitor.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws Exception {
//        if(args.length != 1) {
//            System.err.println("Usage: java Main <inputFile>");
//            System.exit(1);
//        }
        for (int i=0 ; i< args.length ; ++i){
            FileInputStream fis = null;
            try {
                fis = new FileInputStream(args[i]);
                MiniJavaParser parser = new MiniJavaParser(fis);

                Goal root = parser.Goal();

//                Path path = Paths.get(args[i]);
//                Path pathFile = path.getFileName();
//                String ll_file = pathFile.toString().replace(".java",".ll");
//                ll_file = ll_file.replace("/java/","/ll/");
//                File file = new File(ll_file);
//                PrintStream stream = new PrintStream(file);
//                System.setOut(stream);
                String fileName = args[i].replace(".java",".ll");
                fileName = fileName.replace("/java/","/ll/");
                File file = new File(fileName);
                PrintStream stream = new PrintStream(file);
                System.setOut(stream);

                MyFirstVisitor eval = new MyFirstVisitor();
                root.accept(eval, null);

                eval.symbolTable.Offsets();
//            eval.symbolTable.printST();
//            System.out.println();
//            eval.symbolTable.print_llvm_vtables();

                llvmVisitor eval2 = new llvmVisitor(eval.symbolTable);
                root.accept(eval2, null);
            } catch (ParseException ex) {
                System.out.println(ex.getMessage());
            } catch (FileNotFoundException ex) {
                System.err.println(ex.getMessage());
            } finally {
                try {
                    if (fis != null) fis.close();
                } catch (IOException ex) {
                    System.err.println(ex.getMessage());
                }
            }
        }
    }
}