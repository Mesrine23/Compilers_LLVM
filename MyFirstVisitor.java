import syntaxtree.*;
import visitor.GJDepthFirst;
import java.util.Arrays;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

class MyFirstVisitor extends GJDepthFirst<String, String> {

    public SymbolTable symbolTable = new SymbolTable();

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        symbolTable.MainName = classname;
        symbolTable.classOrder.put(classname,null);
        n.f14.accept(this,classname);

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        if(symbolTable.classOrder.containsKey(classname))
            throw new Exception("\n\n~~~~~Typecheck error~~~~~\nIn class declaration -> redefinition of class");
        symbolTable.classOrder.put(classname,null);
        n.f3.accept(this, classname);
        n.f4.accept(this, classname);

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        String classname = n.f1.accept(this, null);
        String extention = n.f3.accept(this,null);
        if(classname.equals(extention))
            throw new Exception("\n\n~~~~~Typecheck error~~~~~\nIn class extends declaration -> class extends itself");
        if(symbolTable.classOrder.containsKey(classname) || !symbolTable.classOrder.containsKey(extention))
            throw new Exception("\n\n~~~~~Typecheck error~~~~~\nIn class extends declaration -> redefiniton of class OR mother class not defined exists");
        String inheritance = symbolTable.classOrder.get(extention);
        if(inheritance != null)
            extention += "-" + inheritance;
        symbolTable.classOrder.put(classname,extention);
        n.f5.accept(this, classname);
        n.f6.accept(this, classname + "~" + extention);

        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
//  <[methName,className],[retType,[argList],[meth_varList]]> ~> Create new class for this
    @Override
    public String visit(MethodDeclaration n, String argu) throws Exception {
        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);

        String[] splt = argu.split("~");
        String className = splt[0];
        String extention = null;
        if(splt.length == 2)
            extention = splt[1];

        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";
        boolean notEmpty = argumentList.contains(" ");
        LinkedHashMap<LinkedList<String>, String> argList = new LinkedHashMap<>();
        List<String> list = Arrays.asList(argumentList.split(","));
        if(notEmpty) {
            for (int i = 0; i < list.size(); ++i) {
                LinkedList<String> key = new LinkedList<>();
                String[] words = list.get(i).split(" ");
                key.add(words[1]);
                key.add(myName);
                if(!argList.containsKey(key)) {
                    argList.put(key,words[0]);
                }
                else {
                    throw new Exception("\n\n~~~~~Typecheck error~~~~~\nIn method declaration -> key exists in argument list");
                }
                argList.put(key, words[0]);
            }
        }

        LinkedHashMap<LinkedList<String>,String> varList = new LinkedHashMap<>();
        String variable = "";
        for (Node node: n.f7.nodes) {
            variable = node.accept(this, "~");
            String[] words = variable.split(" ");
            LinkedList<String> key = new LinkedList<>();
            key.add(words[1]);
            key.add(myName);
            if(!varList.containsKey(key) && !argList.containsKey(key)) {
                varList.put(key,words[0]);
            }
            else {
                throw new Exception("\n\n~~~~~Typecheck error~~~~~\nIn method declaration -> key exists either in variable list or in argument list");
            }
        }

        LinkedList<String> key = new LinkedList<>();
        key.add(myName);
        key.add(className);
        if(!symbolTable.methodDecl.containsKey(key)) {
            MethodSymTable input = new MethodSymTable();
            input.Type = myType;
            input.argList = argList;
            input.varList = varList;
            if(splt.length == 2) {
                LinkedList<String> test = new LinkedList<>();
                test.add(myName);
                test.add(extention);
                if(symbolTable.methodDecl.containsKey(test) && !(input.isSame(symbolTable.methodDecl.get(test))))
                    throw new Exception("\n\n~~~~~Typecheck error~~~~~\nIn method declaration -> argument missmatch between mother and child class");
            }
            symbolTable.insertMethodSymbol(key, input);
        }
        else{
            throw new Exception("\n\n~~~~~Typecheck error~~~~~\nIn method declaration -> method already exists");
        }

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n, String argu) throws Exception {   //add <(varname,class) , (type)>
        if(argu.equals("~")){
            String type = n.f0.accept(this, null);
            String id = n.f1.accept(this,null);
            return type + " " + id;
        }

        String type = n.f0.accept(this, null);
        String id = n.f1.accept(this, null);
        LinkedList<String> key = new LinkedList<>();
        key.add(id);
        key.add(argu);
        if(!symbolTable.varDecl.containsKey(key)) {
            symbolTable.insertVarSymbol(key, type);
        }
        else {
            throw new Exception("\n\n~~~~~Typecheck error~~~~~\nIn variable declaration -> variable already exists in scope");
        }
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception {
        String ret = n.f0.accept(this, null);
        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ( FormalParameterTerm() )*
     */
    @Override
    public String visit(FormalParameterTail n, String argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += "," + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, String argu) throws Exception{
        String type = n.f0.accept(this, null);
        String name = n.f1.accept(this, null);
        return type + " " + name;
    }

    @Override
    public String visit(Type n, String argu) throws Exception {
        return (argu != null) ? (n.f0.accept(this, argu) + argu) : n.f0.accept(this, argu);
    }

    public String visit(ArrayType n, String argu) throws Exception {
        return n.f0.accept(this,null);
    }

    @Override
    public String visit(BooleanArrayType n, String argu) throws Exception {
        return "boolean[]";
    }

    @Override
    public String visit(IntegerArrayType n, String argu) throws Exception {
        return "int[]";
    }

    public String visit(BooleanType n, String argu) {
        return "boolean";
    }

    public String visit(IntegerType n, String argu) {
        return ("int");
    }

    @Override
    public String visit(Identifier n, String argu) {
        return n.f0.toString();
    }
}
