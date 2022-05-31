import syntaxtree.*;
import visitor.GJDepthFirst;

import java.lang.String;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;


class llvmVisitor extends GJDepthFirst<String, String> {
    public SymbolTable symbolTable;
    public int assignCounter;
    public int nsz;
    public int if_stmnt;
    public int exp_res;
    public int oob;
    public int loop;
    public String current_class;
    public String current_method;
    public LinkedHashMap<String,String> local_variables;
    public LinkedHashMap<String, String> info;

    public llvmVisitor(SymbolTable ST){
        //this.symbolTable = new SymbolTable();
        this.symbolTable = ST;
        this.assignCounter = 0;
        this.nsz = 0;
        this.if_stmnt = 0;
        this.exp_res = 0;
        this.oob = 0;
        this.loop = 0;
        this.info = new LinkedHashMap<>();
        info.put("%this","i8*");
        this.local_variables = new LinkedHashMap<>();
    }

    public static boolean isNumeric(String number){
        if(number == null)
            return false;
        try {
            double d = Double.parseDouble(number);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public String string_search_for_value(String id) {
        String type = null;
        if (local_variables.containsKey(id)) {
            String temp = local_variables.get(id);
            if(temp.equals("i32") || temp.equals("i1") || temp.equals("i32*"))
                System.out.println("\t%_" + (assignCounter) + " = load " + local_variables.get(id) + ", " + local_variables.get(id) + "* %" + id);
            else
                System.out.println("\t%_" + (assignCounter) + " = load i8*, i8** %" + id);
            type = local_variables.get(id);
            id = "%_" + (assignCounter++);
        } else {
            LinkedList<String> vars = symbolTable.var_Table.get(current_class);
            for (int i = 0; i < vars.size(); ++i) {
                if (vars.get(i).contains("." + id)) {
                    String[] temp = vars.get(i).split("\\.");
                    LinkedList<String> key = new LinkedList<>();
                    key.add(temp[1]);
                    key.add(temp[0]);
                    type = symbolTable.varDecl.get(key);
                    if (type.equals("int"))
                        type = "i32";
                    else if (type.equals("boolean"))
                        type = "i1";
                    else if (type.equals("int[]") || type.equals("boolean[]"))
                        type = "i32*";
                    else
                        type = "i8*";
                    System.out.println("\t%_" + (assignCounter++) + " = getelementptr i8, i8* %this, i32 " + (8 + symbolTable.cl_variable_offset.get(vars.get(i))));
                    System.out.println("\t%_" + (assignCounter) + " = bitcast i8* %_" + ((assignCounter++) - 1) + " to " + type + "*"); // ERASED AN '*', ADD IT IF NEEDED
                    System.out.println("\t%_" + assignCounter + " = load " + type + ", " + type + "* %_" + (assignCounter-1));
                    id = "%_" + (assignCounter++);
                    break;
                }
            }
        }
//        System.out.println("IN FUNCTION");
//        System.out.println("id: " + id);
//        System.out.println("type: " + type);
        return id + "~" + type;
    }

    public void string_search_to_store(String id, String get, String type) {
        if(!type.equals("i32") && !type.equals("i1") && !type.equals("i32*"))
            type = "i8*";
        if(local_variables.containsKey(id)) {
            System.out.println("\tstore " + type + " " + get + ", " + type + "* %" + id);
        }
        else{
            String ptr_to_store=null;
            String var_type=null;
            LinkedList<String> vars = symbolTable.var_Table.get(current_class);
            for(int i=0 ; i<vars.size() ; ++i) {
                if(vars.get(i).contains("." + id)){
                    String[] temp = vars.get(i).split("\\.");
//                           System.out.println(temp[0]);
//                           System.out.println(temp[1]);
                    LinkedList<String> key = new LinkedList<>();
                    key.add(temp[1]);
                    key.add(temp[0]);
                    var_type = symbolTable.varDecl.get(key);
                    if(var_type.equals("int"))
                        var_type = "i32";
                    else if (type.equals("boolean"))
                        var_type = "i1";
                    else if (var_type.equals("int[]") || var_type.equals("boolean[]"))
                        var_type = "i32*";
                    else
                        var_type = "i8*";
                    System.out.println("\t%_" + (assignCounter++) + " = getelementptr i8, i8* %this, i32 " + (8+symbolTable.cl_variable_offset.get(vars.get(i))));
                    System.out.println("\t%_" + (assignCounter) + " = bitcast i8* %_" + (assignCounter-1) + " to " + type + "*");
                    ptr_to_store = "%_" + (assignCounter++);
                    break;
                }
            }
            System.out.println("\tstore " + type + " " + get + ", " + type + "* " + ptr_to_store);
        }
        //return null;
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
        current_class = n.f1.accept(this,null);
        current_method = null;
        System.out.println("@." + n.f1.accept(this,null) + "_vtable = global [0 x i8*] []\n");
        System.out.println();
        symbolTable.print_llvm_vtables();
        System.out.println();
        System.out.println("declare i8* @calloc(i32, i32)\ndeclare i32 @printf(i8*, ...)\ndeclare void @exit(i32)\n");
        System.out.println("@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"\n");
        System.out.println("define void @print_int(i32 %i) {\n\t%_str = bitcast [4 x i8]* @_cint to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n\tret void\n}\n");
        System.out.println("define void @throw_oob() {\n\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)\n\tret void\n}\n");
        System.out.println("define void @throw_nsz() {\n\t%_str = bitcast [15 x i8]* @_cNSZ to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)\n\tret void\n}\n");
        System.out.println("define i32 @main() {\n");
        n.f14.accept(this,null);
        System.out.println();
        n.f15.accept(this,null);
        System.out.println("\tret i32 0");
        System.out.println("\n}\n");

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
    public String visit(ClassDeclaration n, String argu) throws Exception {
        current_class = n.f1.accept(this,null);
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
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        current_class = n.f1.accept(this,null);
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
    public String visit(MethodDeclaration n, String argu) throws Exception {
        assignCounter = 0;
        this.current_method = n.f2.accept(this,null);
        String curr_argList = "";
        this.local_variables.clear();
        String fun_type = n.f1.accept(this,null);
        if(fun_type.equals("int"))
            fun_type = "i32";
        else if (fun_type.equals("boolean"))
            fun_type = "i1";
        else if (fun_type.equals("int[]") || fun_type.equals("boolean[]"))
            fun_type = "i32*";
        else
            fun_type = "i8*";
        String fun_name = n.f2.accept(this,null);
        curr_argList = n.f4.present() ? n.f4.accept(this, null) : "";
        if(curr_argList.equals(""))
            System.out.println("define " + fun_type + " @" + current_class + "." + fun_name + "(i8* %this) {");
        else {
            String[] argList = curr_argList.split(",");
            System.out.print("define " + fun_type + " @" + current_class + "." + fun_name + "(i8* %this");
            //System.out.println("define " + fun_type + " @" + current_class + "." + fun_name + "(i8* %this, " + curr_argList + ") {\n");
            for(int i=0 ; i<argList.length ; ++i) {
                String[] temp = argList[i].split(" ");
                if(!temp[0].equals("i32") && !temp[0].equals("i1") && !temp[0].equals("i32*"))
                    temp[0] = "i8*";
                System.out.print(", " + temp[0] + " " + temp[1]);
            }
            System.out.println(") {");
            for(int i=0 ; i<argList.length ; ++i){
                String[] temp = argList[i].split(" ");
                local_variables.put(temp[1].replace("%.",""),temp[0]); // key = identifier && value = type
                if(!temp[0].equals("i32") && !temp[0].equals("i1") && !temp[0].equals("i32*"))
                    temp[0] = "i8*";
                System.out.println("\t" + temp[1].replace("%.","%") + " = alloca " + temp[0]);
                System.out.println("\tstore " + temp[0] + " " + temp[1] + ", " + temp[0] + "* " + temp[1].replace("%.","%"));
            }
        }
        n.f7.accept(this,null);
        //System.out.println(this.local_variables);
        n.f8.accept(this,null);
        String ret = n.f10.accept(this,null);
        if(ret.contains("%"))
            System.out.println("\tret " + fun_type + " " + ret);
        else if (isNumeric(ret))
            System.out.println("\tret i32 " + ret);
        else if (ret.equals("true"))
            System.out.println("\tret i1 1");
        else if (ret.equals("false"))
            System.out.println("\tret i1 ");
        else {  // identifier
            String[] temp = string_search_for_value(ret).split("~");
            ret = temp[0];
            System.out.println("\tret " + fun_type + " " + ret);
        }
        System.out.println("}\n");
        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n, String argu) throws Exception {   //add <(varname,class) , (type)>
        String type = n.f0.accept(this, null);
        String id = n.f1.accept(this, null);
        if (type.equals("int")) {
            type = "i32";
            System.out.println("\t%" + id + " = alloca i32");
        }
        else if (type.equals("boolean")) {
            type = "i1";
            System.out.println("\t%" + id + " = alloca i1");
        }
        else if (type.equals("int[]") || type.equals("boolean[]")) {
            type = "i32*";
            System.out.println("\t%" + id + " = alloca i32*");
        }
        else {
            //type = "i8*";
            System.out.println("\t%" + id + " = alloca i8*");
        }
        if(current_method!=null)
            local_variables.put(id,type);
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
        String get = n.f2.accept(this,null);
        if (current_class.equals(symbolTable.MainName)) {
            if (get.indexOf("%") == 0) {
                String[] temp = info.get(get).split("~");
                String type;
                if(!temp[0].equals("i32") && !temp[0].equals("i1") && !temp[0].equals("i32*"))
                    type = "i8*";
                else
                    type = temp[0];
                System.out.println("\tstore " + type + " " + (get) + ", " + type + "* %" + n.f0.accept(this, null));
            } else if (get.equals("true")) {
                System.out.println("\tstore i1 1, i1* %" + n.f0.accept(this, null));
            } else if (get.equals("false")) {
                System.out.println("\tstore i1 0, i1* %" + n.f0.accept(this, null));
            } else if (isNumeric(get)) {
                System.out.println("\tstore i32 " + get + ", i32* %" + n.f0.accept(this, null));
            } else {
                String type = null;
                LinkedList<String> key = new LinkedList<>();
                key.add(get);
                key.add(current_class);
                type = symbolTable.varDecl.get(key);
                if (type.equals("int"))
                    type = "i32";
                else if (type.equals("boolean"))
                    type = "i1";
                else if (type.equals("int[]") || type.equals("boolean[]"))
                    type = "i32*";
                else
                    type = "i8*";
                System.out.println("\t%_" + (assignCounter) + " = load " + type + ", " + type + "* %" + get);
                System.out.println("\tstore " + type + " %_" + (assignCounter++) + ", " + type + "* %" + n.f0.accept(this, null));
            }
        }
        else {
            String type=null;
            String reg=null;
            if (get.contains("%")) {
                String[] temp = info.get(get).split("~");
                reg = get;
                if(!temp[0].equals("i32") && !temp[0].equals("i1") && !temp[0].equals("i32*"))
                    type = "i8*";
                else
                    type = temp[0];
                //System.out.println("AFTER ALL THE STORE IS: " + get);
            } else if (get.equals("true")) {
                type = "i1";
                reg = "1";
            } else if (get.equals("false")) {
                type = "i1";
                reg = "0";
            } else if (isNumeric(get)) {
                type = "i32";
                reg = get;
            }
            else {  // if identifier
                String[] data = string_search_for_value(get).split("~");
                reg = data[0];
                type = data[1];
            }
            String id = n.f0.accept(this,null);
            string_search_to_store(id,reg,type);
        }
        System.out.println();
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
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
        System.out.println("\t%_" + (assignCounter) + " = load i32*, i32** %" + n.f0.accept(this,null));
        String address = "%_" + (assignCounter++);
        System.out.println("\t%_" + assignCounter + " = load i32, i32* %_" + (assignCounter-1));
        String size = "%_" + (assignCounter++);
        String index = n.f2.accept(this,null);
        if(!isNumeric(index) && !index.contains("%")) {
            if(current_class.equals(symbolTable.MainName)) {
                System.out.println("%_" + assignCounter + " = load i32, i32* " + index);
                index = "%_" + (assignCounter++);
            }
            else {
                String[] temp = string_search_for_value(index).split("~");
                index = temp[0];
            }
        }
        System.out.println("\t%_" + (assignCounter++) + " = icmp sge i32 " + index + ", 0");
        System.out.println("\t%_" + (assignCounter++) + " = icmp slt i32 " + index + ", " + size);
        System.out.println("\t%_" + assignCounter + " = and i1 %_" + (assignCounter-2) + ", %_" + (assignCounter-1));
        int oob = this.oob;
        this.oob++;
        System.out.println("\tbr i1 %_" + (assignCounter++) + ", label %oob_ok_" + oob + ", label %oob_err_" + oob + "\n");
        System.out.println("\toob_err_" + oob + ":");
        System.out.println("\tcall void @throw_oob()");
        System.out.println("\tbr label %oob_ok_" + oob + "\n");
        System.out.println("\toob_ok_" + oob + ":");
        System.out.println("\t%_" + (assignCounter++) + " = add i32 1, " + index);
        System.out.println("\t%_" + assignCounter + " = getelementptr i32, i32* " + address + ", i32 %_" + (assignCounter-1));
        String ptr_to_store = "%_" + (assignCounter++);
        String expr = n.f5.accept(this,null);
        if(expr.equals("true"))
            expr = "1";
        else if (expr.equals("false"))
            expr = "0";
        else if (!isNumeric(expr) && !expr.contains("%")) {    // if expr == identifier
            if (current_class.equals(symbolTable.MainName))
                expr = "%" + expr;
            else {
                String[] temp = string_search_for_value(expr).split("~");
                expr = temp[0];
            }
        }
        System.out.println("\tstore i32 " + expr + ", i32* " + ptr_to_store + "\n");
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
    public String visit(IfStatement n, String argu) throws Exception {
        String expr_reg = n.f2.accept(this,null);
        int if_stmnt = (this.if_stmnt++);
        if(expr_reg.equals("false")) {
            expr_reg = "0";
        }
        else if (expr_reg.equals("true")) {
            expr_reg = "1";
        }
        else if (!expr_reg.contains("%")) { // identifier
            if(current_class.equals(symbolTable.MainName)) {
                System.out.println("%_" + assignCounter + " = load i1, i1* " + expr_reg);
                expr_reg = "%_" + (assignCounter++);
            }
            else {
                String[] temp = string_search_for_value(expr_reg).split("~");
                expr_reg = temp[0];
            }
        }
        System.out.println("\tbr i1 " + expr_reg + ", label %if_then_" + if_stmnt + ", label %if_else_" + if_stmnt + "\n");
        System.out.println("\tif_else_" + if_stmnt + ":");
        n.f6.accept(this,null);
        System.out.println("\tbr label %if_end_" + if_stmnt + "\n");
        System.out.println("\tif_then_" + if_stmnt + ":");
        n.f4.accept(this,null);
        System.out.println("\tbr label %if_end_" + if_stmnt + "\n");
        System.out.println("\tif_end_" + if_stmnt + ":\n");
        return null;
    }

    /**
     * f0 -> "while"
     * f1 -> "("
     * f2 -> Expression()
     * f3 -> ")"
     * f4 -> Statement()
     */
    public String visit(WhileStatement n, String argu) throws Exception {
        // loop_check_#
        // loop_enter_#
        // loop_exit_#
        int loop = this.loop++;
        System.out.println("\tbr label %loop_check_" + loop);
        System.out.println("\n\tloop_check_" + loop + ":");
        String expr = n.f2.accept(this,null);
        // expr can be::>   true || false || identifier || %_#
        // ! java doesn't support integers in while condition !
        if(expr.equals("false")){
            System.out.println("\tbr label %loop_exit_" + loop);
        }
        else if(expr.equals("true")) {
            System.out.println("\tbr label %loop_enter_" + loop);
        }
        else {
            String cond;
            if (!expr.contains("%")) {
                System.out.println("\t%_" + assignCounter + " = load i1, i1* %" + expr);
                cond = "%_" + assignCounter++ ;
            }
            else
                cond = expr;
            System.out.println("\tbr i1 " + cond + ", label %loop_enter_" + loop + ", label %loop_exit_" + loop);
            ++assignCounter;
        }
        System.out.println("\n\tloop_enter_" + loop + ":");
        n.f4.accept(this,null);
        System.out.println("\tbr label %loop_check_" + loop);
        System.out.println("\n\tloop_exit_" + loop + ":\n");
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
        String print = n.f2.accept(this,null);
        if(!isNumeric(print) && !print.contains("%")) {
            if (current_class.equals(symbolTable.MainName)) {
                System.out.println("\t%_" + (assignCounter) + " = load i32, i32* %" + print);
                print = "%_" + (assignCounter++);
                System.out.println("\tcall void (i32) @print_int(i32 " + print + ")\n");
            }
            else {
                String[] temp = string_search_for_value(print).split("~");
                print = temp[0];
                System.out.println("\tcall void (i32) @print_int(i32 " + print + ")\n");
            }
        }
        else
            System.out.println("\tcall void (i32) @print_int(i32 " + print + ")\n");
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
    public String visit(NotExpression n, String argu) throws Exception {
        String clause = n.f1.accept(this,null);
        String key;
        String value = "i1";
        if(clause.equals("false")){
            System.out.println("\tstore i1 1, i1* %_" + assignCounter);
            key = "%_" + (assignCounter++);
        }
        else if(clause.equals("true")){
            System.out.println("\tstore i1 0, i1* %_" + assignCounter);
            key = "%_" + (assignCounter++);
        }
        else if (clause.contains("%")) {
            System.out.println("\t%_" + assignCounter + " = xor i1 " + clause + ", 1");
            key = "%_" + (assignCounter++);
        }
        else {  // identifier
            if(current_class.equals(symbolTable.MainName)){
                System.out.println("\t%_" + (assignCounter++) + " = load i1, i1* %" + clause);
                System.out.println("\t%_" + (assignCounter) + " = xor i1 %_" + (assignCounter-1) + ", 1");
                key = "%_" + (assignCounter++);
            }
            else {
                String[] temp = string_search_for_value(clause).split("~");
                String temp_reg = temp[0];
                System.out.println("\t%_" + assignCounter + " = xor i1 " + temp_reg + ", 1");
                key = "%_" + (assignCounter++);
            }
        }
        info.put(key,value);
        return key;
    }

    /**
     * f0 -> "("
     * f1 -> Expression()
     * f2 -> ")"
     */
    public String visit(BracketExpression n, String argu) throws Exception {
        return n.f1.accept(this,null);
    }

    /**
     * f0 -> "{"
     * f1 -> ( Statement() )*
     * f2 -> "}"
     */
    public String visit(Block n, String argu) throws Exception {
        n.f1.accept(this,null);
        return null;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( ExpressionList() )
     * f5 -> ")"
     */
    public String visit(MessageSend n, String argu) throws Exception {
        String expr = n.f0.accept(this,null); // either identifier or %_# (register from a new array/object allocation)
//        System.out.println("~~~");
//        System.out.println(expr);
//        System.out.println("~~~");
        String cl = null;
        String obj_ptr=null;
        String fun = n.f2.accept(this,null);
        if(current_class.equals(symbolTable.MainName)){
            if (!expr.contains("%")) { // identifier
                LinkedList<String> key = new LinkedList<>();
                key.add(expr);
                key.add(current_class);
                cl = symbolTable.varDecl.get(key);
                expr = "%" + expr;
                System.out.println("\t%_" + assignCounter + " = load i8*, i8** " + expr);
                obj_ptr = "%_" + (assignCounter++);
            } else {  // register %_#
                obj_ptr = expr;
                String[] temp = info.get(expr).split("~");
                cl = temp[1];
            }
        }
        else {
            if (!expr.contains("%")) {
                String[] temp = string_search_for_value(expr).split("~");
                obj_ptr = temp[0];
                if(local_variables.containsKey(expr)){
                    cl = local_variables.get(expr);
                }
                else {
                    LinkedList<String> funs = symbolTable.v_Table.get(current_class);
                    for (int i = 0; i < funs.size(); ++i) {
                        if (funs.get(i).contains("." + fun)) {
                            String[] temp1 = funs.get(i).split("\\.");
                            cl = temp1[0];
                            break;
                        }
                    }
                }
                expr = "%" + expr;
            } else {
                obj_ptr = "%this";
                LinkedList<String> funs = symbolTable.v_Table.get(current_class);
                for (int i = 0; i < funs.size(); ++i) {
                    if (funs.get(i).contains("." + fun)) {
                        String[] temp1 = funs.get(i).split("\\.");
                        cl = temp1[0];
                        break;
                    }
                }
            }
        }
        System.out.println("\t%_" + (assignCounter++) + " = bitcast i8* " + obj_ptr + " to i8***");
        System.out.println("\t%_" + (assignCounter) + " = load i8**, i8*** %_" + ((assignCounter++)-1));
        //System.out.println(cl + "." + fun);
        String fun_type = symbolTable.fun_types.get(cl + "." + fun);
        //System.out.println(fun_type);
        if(fun_type.equals("int"))
            fun_type = "i32";
        else if (fun_type.equals("boolean"))
            fun_type = "i1";
        else if (fun_type.equals("int[]") || fun_type.equals("boolean[]"))
            fun_type = "i32*";
        else
            fun_type = "i8*";
        int index = 0;
        LinkedList<String> vtable = symbolTable.v_Table.get(cl);
        for(int i=0 ; i<vtable.size() ; ++i) {
            //System.out.println(vtable.get(i) + " -> " + index);
            if(vtable.get(i).contains("." + fun))
                break;
            index++;
        }
        System.out.println("\t%_" + assignCounter + " = getelementptr i8*, i8** %_" + ((assignCounter++)-1) + ", i32 " + index);
        System.out.println("\t%_" + assignCounter + " = load i8*, i8** %_" + ((assignCounter++)-1));
        LinkedList<String> key = new LinkedList<>();
        key.add(fun);
        key.add(cl);
        MethodSymTable methInfo = symbolTable.methodDecl.get(key);
        LinkedHashMap<LinkedList<String>, String> argList = methInfo.argList;
        String[] args = new String[argList.size()];
        int count=0;
        for (Map.Entry<LinkedList<String>, String> argums : argList.entrySet()) {
            args[count] = argums.getValue();
            if (args[count].equals("int"))
                args[count] = "i32";
            else if (args[count].equals("boolean"))
                args[count] = "i1";
            else if (args[count].equals("int[]") || args[count].equals("boolean[]"))
                args[count] = "i32*";
            else
                args[count] = "i8*";
            count++;
        }
        System.out.print("\t%_" + assignCounter + " = bitcast i8* %_" + ((assignCounter)-1) + " to " + fun_type + " (i8*");
        String call_reg_type = "%_" + (assignCounter++);
        for(int z=0 ; z<args.length ; ++z)
            System.out.print(", " + args[z]);
        System.out.println(")*");
        String expressionList = n.f4.present() ? n.f4.accept(this, null) : "";
        String[] expressions = expressionList.split(",");
        for(int z=0 ; z<args.length ; ++z) {
            String fun_expr = expressions[z];
            if(fun_expr.equals("false")) {
                expressions[z] = "0";
            }
            else if (fun_expr.equals("true")) {
                //System.out.println("\tstore i1 0, i1* %_" + assignCounter);
                expressions[z] = "1";
            }
            else if (!isNumeric(fun_expr) && !fun_expr.contains("%")) { // identifier
                if(current_class.equals(symbolTable.MainName)) {
                    System.out.println("%_" + assignCounter + " = load " + args[z] + ", " + args[z] + "* %" +fun_expr);
                    expressions[z] = "%_" + (assignCounter++);
                }
                else {
                    String[] temp = string_search_for_value(fun_expr).split("~");
                    expressions[z] = temp[0];
                }
            }
        }
        System.out.print("\t%_" + assignCounter + " = call " + fun_type + " " + call_reg_type + "(i8* " + obj_ptr);
        for(int z=0 ; z<args.length ; ++z) {
            System.out.print(", " + args[z] + " " + expressions[z]);
        }
        System.out.println(")\n");
        String ret = "%_" + (assignCounter++);
        info.put(ret,fun_type);
        return ret;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        String index = n.f2.accept(this,null);
        if(!isNumeric(index) && !index.contains("%")){
            if(current_class.equals(symbolTable.MainName)) {
                index = "%" + index;
            }
            else {
                index = string_search_for_value(index);
            }
        }
        String array = n.f0.accept(this,null);
        String address=null;
        if(!array.contains("%")) {
            if (current_class.equals(symbolTable.MainName)) {
                System.out.println("\t%_" + (assignCounter) + " = load i32*, i32** %" + array);
                address = "%_" + (assignCounter++);
            }
            else {
                if(local_variables.containsKey(array)){
                    System.out.println("\t%_" + (assignCounter) + " = load i32*, i32** " + array);
                    address = "%_" + (assignCounter++);
                }
                else {
                    LinkedList<String> vars = symbolTable.var_Table.get(current_class);
                    for (int i = 0; i < vars.size(); ++i) {
                        if (vars.get(i).contains("." + array)) {
                            System.out.println("\t%_" + (assignCounter++) + " = getelementptr i8, i8* %this, i32 " + (8 + symbolTable.cl_variable_offset.get(vars.get(i))));
                            System.out.println("\t%_" + (assignCounter) + " = bitcast i8* %_" + ((assignCounter++) - 1) + " to i32**");
                            System.out.println("\t%_" + (assignCounter) + " = load i32*, i32** " + (assignCounter-1));
                            address = "%_" + (assignCounter++);
                            break;
                        }
                    }
                }
            }
        }
        System.out.println("\t%_" + assignCounter + " = load i32, i32* %_" + (assignCounter-1));
        String size = "%_" + (assignCounter++);
        System.out.println("\t%_" + (assignCounter++) + " = icmp sge i32 " + index + ", 0");
        System.out.println("\t%_" + (assignCounter++) + " = icmp slt i32 " + index + ", " + size);
        System.out.println("\t%_" + assignCounter + " = and i1 %_" + (assignCounter-2) + ", %_" + (assignCounter-1));
        int oob = this.oob;
        this.oob++;
        System.out.println("\tbr i1 %_" + (assignCounter++) + ", label %oob_ok_" + oob + ", label %oob_err_" + oob + "\n");
        System.out.println("\toob_err_" + oob + ":");
        System.out.println("\tcall void @throw_oob()");
        System.out.println("\tbr label %oob_ok_" + oob + "\n");
        System.out.println("\toob_ok_" + oob + ":");
        System.out.println("\t%_" + (assignCounter++) + " = add i32 1, " + index);
        System.out.println("\t%_" + assignCounter + " = getelementptr i32, i32* " + address + ", i32 %_" + ((assignCounter++)-1));
        System.out.println("\t%_" + assignCounter + " = load i32, i32* %_" + (assignCounter-1));
        return "%_" + (assignCounter++);
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "."
     * f2 -> "length"
     */
    public String visit(ArrayLength n, String argu) throws Exception {
        String array = n.f0.accept(this,null);
        if(!array.contains("%")) {
            if (current_class.equals(symbolTable.MainName)){
                array = "%" + array;
            }
            else {
                if(local_variables.containsKey(array)){
                    System.out.println("\t%_" + (assignCounter) + " = load i32*, i32** " + array);
                    array = "%_" + (assignCounter++);
                }
                else {
                    LinkedList<String> vars = symbolTable.var_Table.get(current_class);
                    for (int i = 0; i < vars.size(); ++i) {
                        if (vars.get(i).contains("." + array)) {
                            System.out.println("\t%_" + (assignCounter++) + " = getelementptr i8, i8* %this, i32 " + (8 + symbolTable.cl_variable_offset.get(vars.get(i))));
                            System.out.println("\t%_" + (assignCounter) + " = bitcast i8* %_" + ((assignCounter++) - 1) + " to i32**");
                            System.out.println("\t%_" + (assignCounter) + " = load i32*, i32** " + (assignCounter-1));
                            array = "%_" + (assignCounter++);
                            break;
                        }
                    }
                }
            }
        }
        System.out.println("\t%_" + (assignCounter++) + " = load i32*, i32** " + array);
        System.out.println("\t%_" + assignCounter + " = load i32, i32* %_" + (assignCounter-1));
        return "%_" + (assignCounter++);
    }

    /**
     * f0 -> Clause()
     * f1 -> "&&"
     * f2 -> Clause()
     */
    public String visit(AndExpression n, String argu) throws Exception {
        // clause will return either identifier or register (%_)
        int[] label = {exp_res++, exp_res++, exp_res++, exp_res++};
        String cond1 = n.f0.accept(this,null);
        if(cond1.equals("false")) {
            cond1 = "0";
        }
        else if (cond1.equals("true")){
            cond1 = "1";
        }
        else if(!cond1.contains("%")) {
            if (current_class.equals(symbolTable.MainName)){
                cond1 = "%" + cond1;
                System.out.println("\t%_" + assignCounter + " = load i1, i1* " + cond1);
                cond1 = "%_" + assignCounter;
                assignCounter++;
            }
            else {
                String[] temp = string_search_for_value(cond1).split("~");
                cond1 = temp[0];
            }
        }
        System.out.println("\tbr i1 " + cond1 + ", label %exp_res_" + label[1] + ", label %exp_res_" + label[0] + "\n");
        System.out.println("\texp_res_" + label[0] + ":");
        System.out.println("\tbr label %exp_res_" + label[3] + "\n");
        System.out.println("\texp_res_" + label[1] + ":");
        String cond2 = n.f2.accept(this,null);
        if(cond2.equals("false")) {
            cond2 = "0";
        }
        else if (cond2.equals("true")){
            cond2 = "1";
        }
        else if(!cond2.contains("%")) {
            if (current_class.equals(symbolTable.MainName)){
                cond2 = "%" + cond2;
                System.out.println("\t%_" + assignCounter + " = load i1, i1* " + cond2);
                cond2 = "%_" + assignCounter;
                assignCounter++;
            }
            else {
                String[] temp = string_search_for_value(cond2).split("~");
                cond2 = temp[0];
            }
        }
        System.out.println("\tbr label %exp_res_" + label[2] + "\n");
        System.out.println("\texp_res_" + label[2] + ":");
        System.out.println("\tbr label %exp_res_" + label[3] + "\n");
        System.out.println("\texp_res_" + label[3] + ":");
        System.out.println("\t%_" + (assignCounter) + " = phi i1 [ 0, %exp_res_" + label[0] + " ], [ " + cond2 + ", %exp_res_" + label[2] + " ]");
        String key = "%_" + (assignCounter++);
        info.put(key,"i1");
        return key;
    }


    /**
     * f0 -> PrimaryExpression()
     * f1 -> "<"
     * f2 -> PrimaryExpression()
     */
    public String visit(CompareExpression n, String argu) throws Exception {
        // primary expression will return (1) number, (2) identifier or (3) register (%_)
        if (current_class.equals(symbolTable.MainName)) {
            String left = n.f0.accept(this, null);
            String final_left;
            if (!isNumeric(left) && !left.contains("%")) {
                System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
                final_left = "%_" + assignCounter;
                assignCounter++;
            } else
                final_left = left;

            String right = n.f2.accept(this, null);
            String final_right;
            if (!isNumeric(right) && !right.contains("%")) {
                System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + right);
                final_right = "%_" + assignCounter;
                assignCounter++;
            } else
                final_right = right;
            System.out.println("\t%_" + assignCounter + " = icmp slt i32 " + final_left + ", " + final_right);
            String key = "%_" + (assignCounter++);
            info.put(key, "i1");
            return key;
        }
        else {
            String left = n.f0.accept(this, null);
            if (!isNumeric(left) && !left.contains("%")) { // left = identifier
                //System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
                String[] temp = string_search_for_value(left).split("~");
                left = temp[0];
            }
            String right = n.f2.accept(this, null);
            if (!isNumeric(right) && !right.contains("%")) { // right = identifier
                String[] temp = string_search_for_value(right).split("~");
                right = temp[0];
            }
            System.out.println("\t%_" + assignCounter + " = icmp slt i32 " + left + ", " + right);
            String key = "%_" + (assignCounter++);
            info.put(key, "i1");
            return key;
        }
    }

    /**
     * f0 -> NotExpression()
     *       | PrimaryExpression()
     */
    @Override
    public String visit(Clause n, String argu) throws Exception {
        //System.out.println("this is clause with argu: " + argu);
        return n.f0.accept(this,argu);
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
        String get = n.f3.accept(this,null);
        if(!isNumeric(get) && !get.contains("%")){
            if(current_class.equals(symbolTable.MainName)){
                System.out.println("\t%_" + assignCounter + " = load i32, i32 %" + get);
                get = "%_" + (assignCounter++);
            }
            else {
                String[] temp = string_search_for_value(get).split("~");
                get = temp[0];
            }
        }
        System.out.println("\t%_" + assignCounter + " = add i32 1, " + get);
        assignCounter++;
        System.out.println("\t%_" + assignCounter + " = icmp sge i32 %_" + (assignCounter-1) + ", 1");
        System.out.println("\tbr i1 %_" + assignCounter + ", label %nsz_ok_0, label %nsz_err_" + (nsz) + "\n");
        System.out.println("\tnsz_err_" + (nsz) + ":\n\tcall void @throw_nsz()\n\tbr label %nsz_ok_" + (nsz) + "\n");
        System.out.println("\tnsz_ok_" + (nsz) + ":\n");
        this.nsz++;
        assignCounter++;
        System.out.println("\t%_" + assignCounter + " = call i8* @calloc(i32 %_" + (assignCounter-2) + ", i32 4)");
        assignCounter++;
        System.out.println("\t%_" + assignCounter + " = bitcast i8* %_" + (assignCounter-1) + " to i32*");
        System.out.println("\tstore i32 " + (get) + ", i32* %_" + (assignCounter));
        System.out.println();
        String key = "%_" + assignCounter;
        String value = "i32*";
        info.put(key,value);
        assignCounter++;
        return key;
    }

    /**
     * f0 -> "new"
     * f1 -> Identifier()
     * f2 -> "("
     * f3 -> ")"
     */
    public String visit(AllocationExpression n, String argu) throws Exception {
        String cl = n.f1.accept(this,null);
        Integer heap_size = symbolTable.cl_variable_offset.get(cl);
        System.out.println("\t%_" + (assignCounter) + " = call i8* @calloc(i32 1, i32 " + (heap_size+8) + ")");
        String key = "%_" + (assignCounter++);
        String value = "i8*~" + cl;
        info.put(key,value);
        System.out.println("\t%_" + assignCounter + " = bitcast i8* %_" + ((assignCounter++)-1) + " to i8***");
        System.out.println("\t%_" + assignCounter + " = getelementptr [" + symbolTable.v_Table.get(cl).size() + " x i8*], [" + symbolTable.v_Table.get(cl).size() + " x i8*]* @." + cl + "_vtable, i32 0, i32 0");
        System.out.println("\tstore i8** %_" + assignCounter + ", i8*** %_" + ((assignCounter++)-1));
        return key;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        if (current_class.equals(symbolTable.MainName)){
            String left = n.f0.accept(this, null);
            if (!isNumeric(left) && !left.contains("%")) { // left = identifier
                System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
                left = "%_" + (assignCounter++);
            }
            String right = n.f2.accept(this, null);
            if (!isNumeric(right) && !right.contains("%")) { // right = identifier
                System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + right);
                right = "%_" + (assignCounter++);
            }
            String key = "%_" + assignCounter;
            String value = "i32";
            info.put(key, value);
            assignCounter++;
            System.out.println("\t" + key + " = add i32 " + left + ", " + right);
            return key;
        }
        else {
            String left = n.f0.accept(this, null);
            if (!isNumeric(left) && !left.contains("%")) { // left = identifier
                //System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
                String[] temp = string_search_for_value(left).split("~");
                left = temp[0];
            }
            String right = n.f2.accept(this, null);
            if (!isNumeric(right) && !right.contains("%")) { // right = identifier
                String[] temp = string_search_for_value(right).split("~");
                right = temp[0];
            }
            String key = "%_" + assignCounter;
            String value = "i32";
            info.put(key, value);
            assignCounter++;
            System.out.println("\t" + key + " = add i32 " + left + ", " + right);
            return key;
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {
        if (current_class.equals(symbolTable.MainName)){
            String left = n.f0.accept(this, null);
            if (!isNumeric(left) && !left.contains("%")) { // left = identifier
                System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
                left = "%_" + (assignCounter++);
            }
            String right = n.f2.accept(this, null);
            if (!isNumeric(right) && !right.contains("%")) { // right = identifier
                System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + right);
                right = "%_" + (assignCounter++);
            }
            String key = "%_" + assignCounter;
            String value = "i32";
            info.put(key, value);
            assignCounter++;
            System.out.println("\t" + key + " = sub i32 " + left + ", " + right);
            return key;
        }
        else {
            String left = n.f0.accept(this, null);
            if (!isNumeric(left) && !left.contains("%")) { // left = identifier
                //System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
                String[] temp = string_search_for_value(left).split("~");
                left = temp[0];
            }
            String right = n.f2.accept(this, null);
            if (!isNumeric(right) && !right.contains("%")) { // right = identifier
                String[] temp = string_search_for_value(right).split("~");
                right = temp[0];
            }
            String key = "%_" + assignCounter;
            String value = "i32";
            info.put(key, value);
            assignCounter++;
            System.out.println("\t" + key + " = sub i32 " + left + ", " + right);
            return key;
        }
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {
        if (current_class.equals(symbolTable.MainName)){
            String left = n.f0.accept(this, null);
            if (!isNumeric(left) && !left.contains("%")) { // left = identifier
                System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
                left = "%_" + (assignCounter++);
            }
            String right = n.f2.accept(this, null);
            if (!isNumeric(right) && !right.contains("%")) { // right = identifier
                System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + right);
                right = "%_" + (assignCounter++);
            }
            String key = "%_" + assignCounter;
            String value = "i32";
            info.put(key, value);
            assignCounter++;
            System.out.println("\t" + key + " = mul i32 " + left + ", " + right);
            return key;
        }
        else {
            String left = n.f0.accept(this, null);
            if (!isNumeric(left) && !left.contains("%")) { // left = identifier
                //System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
                String[] temp = string_search_for_value(left).split("~");
                left = temp[0];
            }
            String right = n.f2.accept(this, null);
            if (!isNumeric(right) && !right.contains("%")) { // right = identifier
                String[] temp = string_search_for_value(right).split("~");
                right = temp[0];
            }
            String key = "%_" + assignCounter;
            String value = "i32";
            info.put(key, value);
            assignCounter++;
            System.out.println("\t" + key + " = mul i32 " + left + ", " + right);
            return key;
        }
    }

    /**
     * f0 -> Expression()
     * f1 -> ExpressionTail()
     */
    public String visit(ExpressionList n, String argu) throws Exception {
        String ret = n.f0.accept(this, null);
        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
     */
    public String visit(ExpressionTail n, String argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += "," + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    public String visit(ExpressionTerm n, String argu) throws Exception {
        return n.f1.accept(this,null);
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterList n, String argu) throws Exception {
        String ret = n.f0.accept(this, null);
        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    public String visit(FormalParameter n, String argu) throws Exception {
        String type = n.f0.accept(this,null);
        if(type.equals("int"))
            type = "i32";
        else if (type.equals("boolean"))
            type = "i1";
        else if (type.equals("int[]") || type.equals("boolean[]"))
            type = "i32*";
        return (type + " %." + n.f1.accept(this,null));
    }

    /**
     * f0 -> ( FormalParameterTerm() )*
     */
    public String visit(FormalParameterTail n, String argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += "," + node.accept(this, null);
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

    @Override
    public String visit(Identifier n, String argu) throws Exception {
        return n.f0.toString();
    }

    @Override
    public String visit(IntegerLiteral n, String argu) throws Exception {
        return n.f0.toString();
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
        return "true";
    }

    @Override
    public String visit(FalseLiteral n, String argu) {
        return "false";
    }

    @Override
    public String visit(ThisExpression n, String argu) {
        return "%this";
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
        //System.out.println("this is primo expr with argu: " + argu);
        return n.f0.accept(this, argu);
    }

}