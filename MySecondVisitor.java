import syntaxtree.*;
import visitor.GJDepthFirst;

import java.util.*;

class MySecondVisitor extends GJDepthFirst<String, String> {
    public SymbolTable symbolTable;
    public String mother_class;
    public String curr_class;
    public String curr_method;

    public MySecondVisitor(SymbolTable ST){
        this.symbolTable = new SymbolTable();
        this.symbolTable = ST;
    }

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
        if(!symbolTable.checkCorrectness())
            throw new Exception("\n\n~~~~~Typecheck error~~~~~\nIn second visitor -> check correctness of symbol table\n");
        symbolTable.printST();
        symbolTable.Offsets();
        this.curr_class = n.f1.accept(this,"~give_string");
        this.mother_class = null;
        this.curr_method = null;
        n.f15.accept(this,null);

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
        this.curr_class = n.f1.accept(this,"~give_string");
        this.mother_class = null;
        n.f4.accept(this,null);
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
        this.curr_class = n.f1.accept(this,"~give_string");
        this.mother_class = symbolTable.classOrder.get(curr_class);
        n.f6.accept(this,null);
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
    @Override
    public String visit(MethodDeclaration n, String argu) throws Exception {
        curr_method = n.f2.accept(this,"~give_string");
        String type = n.f1.accept(this,null);
        String expr;
        if (type.equals("int")) {
            expr = n.f10.accept(this, "~give_int");
            if (!type.equals(expr))
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn method declaration -> shoulda return int\n");
        }
        else if (type.equals("boolean")) {
            expr = n.f10.accept(this, "~give_bool");
            if (!type.equals(expr))
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn method declaration -> shoulda return boolean\n");
        }
        else if (type.equals("boolean[]")){
            expr = n.f10.accept(this, "~~give_array_type");
            if (!expr.equals("array~boolean"))
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn method declaration -> shoulda return boolean[]\n");
        }
        else if (type.equals("int[]")) {
            expr = n.f10.accept(this, "~~give_array_type");
            if (!expr.equals("array~int"))
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn method declaration -> shoulda return int[]\n");
        }
        else {
            expr = n.f10.accept(this, "~give_id_class");
            if (!type.equals(expr)) {
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn method declaration -> shoulda return class type\n");
            }
        }

        n.f8.accept(this,null);
        return null;
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    @Override
    public String visit(Block n, String argu) throws Exception {
        n.f1.accept(this,null);
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "="
     * f2 -> Expression()
     * f3 -> ";"
     */
    @Override
    public String visit(AssignmentStatement n, String argu) throws Exception {
        String id = n.f0.accept(this, "~give_string");
        String type = symbolTable.retType(id,curr_class,mother_class,curr_method);

        String expr;
        if (type.equals("int")) {
            expr = n.f2.accept(this, "~give_int");
            if (!type.equals(expr))
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn assignment statement -> shoulda return int\n");
        }
        else if (type.equals("boolean")) {
            expr = n.f2.accept(this, "~give_bool");
            if (!type.equals(expr))
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn assignment statement -> shoulda return boolean\n");
        }
        else {
            expr = n.f2.accept(this, "~give_id_class");
            if(expr=="this")
                expr = this.curr_class;
            int flag=0;
            if (!type.equals(expr)) {
                String mum = symbolTable.classOrder.get(expr);
                if(mum==null)
                    throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn assignment statement -> attribute doensn't exist in \"this\" class\n");
                String[] mums = mum.split("\\-");
                for(int i=0 ; i < mums.length ; i++) {
                    if (type.equals(mums[i])) {
                        flag = 1;
                        break;
                    }
                }
                if (flag==0)
                    throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn assignment statement -> shoulda return class type\n");
            }
        }
        return null;
    }

    /**
     * f0 -> Identifier()
     * f1 -> "["
     * f2 -> Expression()
     * f3 -> "]"
     * f4 -> "="
     * f5 -> Expression()
     * f6 -> ";"
     */
    @Override
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
        String id = n.f0.accept(this,"~give_string");
        String type = symbolTable.retType(id,curr_class,mother_class,curr_method);
        String index = n.f2.accept(this,"~give_int");

        if(!index.equals("int"))
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn array assignment statement -> shoulda return int\n");

        String expr;
        if(type.equals("int"))
            expr = n.f2.accept(this,"~give_int");
        else if(type.equals("boolean"))
            expr = n.f2.accept(this,"~give_bool");
        else
            expr = n.f2.accept(this,"~give_id_class");

        if(!type.equals(expr + "[]"))
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn array assignment statement -> missmatch array type\n");

        return null;
    }

    /**
     * f0 -> "if"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     * f5 -> "else"
     * f6 -> Statement()
     */
    @Override
    public String visit(IfStatement n, String argu) throws Exception {
        n.f2.accept(this,"~give_bool");
        n.f4.accept(this,null);
        n.f6.accept(this,null);
        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    @Override
    public String visit(WhileStatement n, String argu) throws Exception {
        String expr = n.f2.accept(this,"~give_bool");
        n.f4.accept(this,null);
        return null;
    }

    /**
     * f0 -> "System.out.println"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> ";"
     */
    public String visit(PrintStatement n, String argu) throws Exception {
        String type = n.f2.accept(this,"~give_id_class");
        if (!type.equals("int") && !type.equals("boolean"))
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn print statement -> type must be either 'int' or 'boolean'\n");
        return null;
    }

    /**
     * f0 -> AndExpression() -> bool
     *       | CompareExpression() -> int
     *       | PlusExpression() -> int
     *       | MinusExpression() -> int
     *       | TimesExpression() -> int
     *       | ArrayLookup() -> int ? bool
     *       | ArrayLength() -> int
     *       | MessageSend() -> everything
     *       | Clause() -> everything
     */
    @Override
    public String visit(Expression n, String argu) throws Exception {
        return n.f0.accept(this,argu);
    }

    /**
     * f0 -> "!"
     * f1 -> Clause()
     */
    @Override
    public String visit(NotExpression n, String argu) throws Exception {
        String clause = n.f1.accept(this,argu);
        if (!clause.equals("boolean"))
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn not expression -> shoulda return boolean\n");
        return clause;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(PlusExpression n, String argu) throws Exception {
        String expr1 = n.f0.accept(this,"~give_int");
        String expr2 = n.f2.accept(this,"~give_int");
        if(expr1.equals("int") && expr2.equals("int"))
            return "int";
        else
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn plus expression -> expressions must be int\n");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(MinusExpression n, String argu) throws Exception {
        String expr1 = n.f0.accept(this,"~give_int");
        String expr2 = n.f2.accept(this,"~give_int");
        if(expr1.equals("int") && expr2.equals("int"))
            return "int";
        else
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn minus expression -> expressions must be int\n");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(TimesExpression n, String argu) throws Exception {
        String expr1 = n.f0.accept(this,"~give_int");
        String expr2 = n.f2.accept(this,"~give_int");
        if(expr1.equals("int") && expr2.equals("int"))
            return "int";
        else
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn times expression -> expressions must be int\n");
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    @Override
    public String visit(CompareExpression n, String argu) throws Exception {
        String expr1 = n.f0.accept(this,"~give_int");
        String expr2 = n.f2.accept(this,"~give_int");
        if(expr1.equals("int") && expr2.equals("int"))
            return "boolean";
        else
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn compare expression -> expressions must be int\n");
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    @Override
    public String visit(AndExpression n, String argu) throws Exception {
        String clause1 = n.f0.accept(this,"~give_bool");
        String clause2 = n.f2.accept(this,"~give_bool");
        if(!clause1.equals("boolean") || !clause2.equals("boolean"))
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn and expression -> clause must be boolean\n");
        return "boolean";
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    @Override
    public String visit(Clause n, String argu) throws Exception {
        return n.f0.accept(this,argu);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    @Override
    public String visit(ArrayLookup n, String argu) throws Exception {
        String id = n.f0.accept(this,"~give_array_type");
        String expr = n.f2.accept(this,"~give_int");
        if (expr.equals("int"))
        {
            if (id.equals("array~int") || id.equals("array~boolean")) {
                String[] splt = id.split("~");
                return splt[1];
            }
            else if (id.equals("boolean[]"))
                return "boolean";
            else if (id.equals("int[]"))
                return "int";
            else
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn array lookup -> wrong array type\n");
        }
        else {
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn array lookup -> check expression type and identifier type\n");
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    @Override
    public String visit(ArrayLength n, String argu) throws Exception {
        String expr = n.f0.accept(this,"~give_array_type");
        if(!expr.equals("array~int") && !expr.equals("array~boolean"))
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn array length -> expression must be array\n");
        return "int";
    }

    /**
     * f0 -> BooleanArrayAllocationExpression()
     *       | IntegerArrayAllocationExpression()
     */
    @Override
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        return n.f0.accept(this,null);
    }

    /**
     * f0 -> "new"
     * f1 -> "boolean"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    @Override
    public String visit(BooleanArrayAllocationExpression n, String argu) throws Exception {
        String expr = n.f3.accept(this,"~give_int");
        if(!expr.equals("int"))
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn boolean array allocation expression -> expression must be int\n");
        return "boolean[]";
    }

    /**
     * f0 -> "new"
     * f1 -> "int"
     * f2 -> "["
     * f3 -> Expression()
     * f4 -> "]"
     */
    @Override
    public String visit(IntegerArrayAllocationExpression n, String argu) throws Exception {
        String expr = n.f3.accept(this,"~give_int");
        if(!expr.equals("int"))
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn integer array allocation expression -> expression must be int\n");
        return "int[]";
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    @Override
    public String visit(AllocationExpression n, String argu) throws Exception {
        String cl = n.f1.accept(this,"~give_string");
        if(cl.equals(this.symbolTable.MainName) || !symbolTable.classOrder.containsKey(cl))
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn allocation expression -> problem with type\n");
        return cl;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    @Override
    public String visit(BracketExpression n, String argu) throws Exception {
        return n.f1.accept(this,argu);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )?
     * f5 -> ")"
     */
    @Override
    public String visit(MessageSend n, String argu) throws Exception {
        String prime_expr = n.f0.accept(this,"~give_string");
        String classCheck;
        if(prime_expr.equals("this"))
            classCheck = this.curr_class;
        else if (symbolTable.classOrder.containsKey(prime_expr))
            classCheck = prime_expr;
        else {
            classCheck = symbolTable.retType(prime_expr,this.curr_class,this.mother_class,this.curr_method);
            if(classCheck==null)
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn message send -> classCheck error\n");
        }

        String id = n.f2.accept(this,"~give_string");

        LinkedList<String> key = new LinkedList<>();
        key.add(id);
        key.add(classCheck);
        MethodSymTable methInfo = new MethodSymTable();
        if(symbolTable.methodDecl.containsKey(key)) {
            methInfo = symbolTable.methodDecl.get(key);
        }
        else if (symbolTable.classOrder.get(classCheck)!=null) {
            int flag=0;
            String mum = symbolTable.classOrder.get(classCheck);
            String[] mums = mum.split("\\-");
            for(int i=0 ; i < mums.length ; ++i) {
                LinkedList<String> key2 = new LinkedList<>();
                key2.add(id);
                key2.add(mums[i]);
                if(symbolTable.methodDecl.containsKey(key2)){
                    methInfo = symbolTable.methodDecl.get(key2);
                    flag=1;
                    break;
                }
            }
            if(flag==0)
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn message send -> key doesnt exist in any mother's method declaration list\n");
        }
        else
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn message send -> key doesnt exist in method declaration list\n");

        LinkedList<String> list1 = new LinkedList<>();
        for (Map.Entry<LinkedList<String>, String> check1 : methInfo.argList.entrySet())
            list1.add(check1.getValue());


        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";
        boolean notEmpty;
        if(argumentList == "")
            notEmpty = false;
        else
            notEmpty = true;
        List<String> argL = Arrays.asList(argumentList.split(","));
        LinkedList<String> list2 = new LinkedList<>();
        if(notEmpty){
            for (int i = 0; i < argL.size(); ++i)
                list2.add(argL.get(i));
        }

        if(list2.size()!=list1.size()) {
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn message send -> size missmatch between argument and call list\n");
        }

        for (int i=0 ; i<list1.size() ; ++i) {
            String check;
            if(list2.get(i).equals("this"))
                check = this.curr_class;
            else
                check = list2.get(i);
            if (!list1.get(i).equals(check)) {
                String mum = symbolTable.classOrder.get(check);
                if(mum==null)
                    throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn message send -> content missmatch between argument and call list (1)\n");
                String[] mums = mum.split("\\-");
                int flag=0;
                for(int j=0 ; j < mums.length ; ++j){
                    if(mums[j].equals(list1.get(i))) {
                        flag = 1;
                        break;
                    }
                }
                if(flag==0)
                    throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn message send -> content missmatch between argument and call list (2)\n");
            }
        }
        return methInfo.Type;
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    @Override
    public String visit(ExpressionList n ,String argu) throws Exception {
        String ret = n.f0.accept(this,"~give_id_class");
        if(n.f1 != null) {
            ret += n.f1.accept(this,"~give_id_class");
        }
        return ret;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    @Override
    public String visit(ExpressionTail n, String argu) throws Exception {
        String ret = "";
        for (Node node: n.f0.nodes) {
            ret += "," + node.accept(this,"~give_id_class");
        }
        return ret;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public String visit(ExpressionTerm n, String argu) throws Exception {
        return n.f1.accept(this,"~give_id_class");
    }

    @Override
    public String visit(Identifier n, String argu) throws Exception {
        if(argu.equals("~give_int")){
            String type = symbolTable.retType(n.f0.toString(),curr_class,mother_class,curr_method);
            if(!type.equals("int"))
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn identifier -> should be int\n");
            return "int";
        }
        else if(argu.equals("~give_bool")) {
            String type = symbolTable.retType(n.f0.toString(),curr_class,mother_class,curr_method);
            if(!type.equals("boolean"))
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn identifier -> should be boolean\n");
            return "boolean";
        }
        else if (argu.equals("~give_array_type")){
            String type = symbolTable.retType(n.f0.toString(),curr_class,mother_class,curr_method);
            if(type.equals("int[]")) {
                return "array~int";
            }
            else if (type.equals("boolean[]")) {
                return "array~boolean";
            }
            else
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn identifier -> should be array type\n");
        }
        else if (argu.equals("~give_class_type")) {
            if(symbolTable.classOrder.containsKey(n.f0.toString()))
                return n.f0.toString();
            else
                throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn identifier -> identifier doesnt exists\n");
        }
        else if (argu.equals("~give_string")) {
            return n.f0.toString();
        }
        else if (argu.equals("~give_id_class")) {
            return symbolTable.retType(n.f0.toString(),curr_class,mother_class,curr_method);
        }
        else
            throw new Exception("\n\n~~~~~Semantic error~~~~~\nIn identifier -> no command given from argu\n");
    }

    @Override
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return "int";
    }

    @Override
    public String visit(IntegerType n, String argu) {
        return "int";
    }

    @Override
    public String visit(IntegerArrayType n, String argu) throws Exception {
        return "int[]";
    }

    @Override
    public String visit(BooleanType n, String argu) {
        return "boolean";
    }

    @Override
    public String visit(BooleanArrayType n, String argu) throws Exception {
        return "boolean[]";
    }

    @Override
    public String visit(TrueLiteral n, String argu) {
        return "boolean";
    }

    @Override
    public String visit(FalseLiteral n, String argu) {
        return "boolean";
    }

    @Override
    public String visit(ThisExpression n, String argu) {
        return "this";
    }

    @Override
    public String visit(ArrayType n, String argu) throws Exception {
        return n.f0.accept(this,null);
    }

    /**
     * f0 -> ArrayType()
     *       | BooleanType()
     *       | IntegerType()
     *       | Identifier()
     */
    @Override
    public String visit(Type n, String argu) throws Exception {
        return n.f0.accept(this,"~give_string");
    }

    /**
     * f0 -> IntegerLiteral()
     *       | TrueLiteral()
     *       | FalseLiteral()
     *       | Identifier()
     *       | ThisExpression()
     *       | ArrayAllocationExpression()
     *       | AllocationExpression()
     *       | BracketExpression()
     */
    public String visit(PrimaryExpression n, String argu) throws Exception {
        return n.f0.accept(this, argu);
    }
}