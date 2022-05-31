import syntaxtree.*;
import visitor.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.LinkedList;

public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 1) {
            System.err.println("Usage: java Main <inputFile>");
            System.exit(1);
        }
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(args[0]);
            MiniJavaParser parser = new MiniJavaParser(fis);

            Goal root = parser.Goal();

            MyFirstVisitor eval = new MyFirstVisitor();
            root.accept(eval, null);

            eval.symbolTable.Offsets();
//            eval.symbolTable.printST();
//            System.out.println();
            //eval.symbolTable.print_llvm_vtables();

            llvmVisitor eval2 = new llvmVisitor(eval.symbolTable);
            root.accept(eval2,null);
        }
        catch(ParseException ex){
            System.out.println(ex.getMessage());
        }
        catch(FileNotFoundException ex){
            System.err.println(ex.getMessage());
        }
        finally{
            try{
                if(fis != null) fis.close();
            }
            catch(IOException ex){
                System.err.println(ex.getMessage());
            }
        }
    }
}