import syntaxtree.*;
import visitor.GJDepthFirst;

import java.lang.String;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import static java.lang.String.valueOf;

class llvmVisitor extends GJDepthFirst<String, String> {
    public SymbolTable symbolTable;
    public int assignCounter;
    public int nsz;
    public int if_stmnt;
    public int exp_res;
    public int oob;
    public int loop;

    public LinkedHashMap<String, String> info;

    public llvmVisitor(SymbolTable ST){
        this.symbolTable = new SymbolTable();
        this.symbolTable = ST;
        this.assignCounter = 0;
        this.nsz = 0;
        this.if_stmnt = 0;
        this.exp_res = 0;
        this.oob = 0;
        this.loop = 0;
        this.info = new LinkedHashMap<>();
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
        System.out.println("@." + n.f1.accept(this,null) + "_vtable = global [0 x i8*] []\n");
        System.out.println("declare i8* @calloc(i32, i32)\ndeclare i32 @printf(i8*, ...)\ndeclare void @exit(i32)\n");
        System.out.println("@_cint = constant [4 x i8] c\"%d\\0a\\00\"\n@_cOOB = constant [15 x i8] c\"Out of bounds\\0a\\00\"\n@_cNSZ = constant [15 x i8] c\"Negative size\\0a\\00\"\n");
        System.out.println("define void @print_int(i32 %i) {\n\t%_str = bitcast [4 x i8]* @_cint to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str, i32 %i)\n\tret void\n}\n");
        System.out.println("define void @throw_oob() {\n\t%_str = bitcast [15 x i8]* @_cOOB to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)\n\tret void\n}\n");
        System.out.println("define void @throw_nsz() {\n\t%_str = bitcast [15 x i8]* @_cNSZ to i8*\n\tcall i32 (i8*, ...) @printf(i8* %_str)\n\tcall void @exit(i32 1)\n\tret void\n}\n");
        System.out.println("define i32 @main() {\n");
        n.f14.accept(this,"main");
        System.out.println();
        n.f15.accept(this,null);
        System.out.println("\tret i32 0");
        System.out.println("\n}");

        return null;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     * f2 -> ";"
     */
    @Override
    public String visit(VarDeclaration n, String argu) throws Exception {   //add <(varname,class) , (type)>
        if(argu==null)
            return null;
        String type = n.f0.accept(this, null);
        String id = n.f1.accept(this, null);
        if (type.equals("int"))
            System.out.println("\t%" + id + " = alloca i32");
        else if (type.equals("boolean"))
            System.out.println("\t%" + id + " = alloca i1");
        else if (type.equals("int[]"))
            System.out.println("\t%" + id + " = alloca i32*");
        else if (type.equals("boolean[]"))
            System.out.println("\t%" + id + " = alloca i1*");
        else
            System.out.println("\t%" + id + " = alloca i8*");
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
        if(get.indexOf("%")==0) {
            String type = info.get(get);
            System.out.println("\tstore " + type + " " + (get) + ", " + type + "* %" + n.f0.accept(this, null));
        }
        else if (get.equals("true")){
            System.out.println("\tstore i1 1, i1* %" + n.f0.accept(this,null));
        }
        else if (get.equals("false")){
            System.out.println("\tstore i1 0, i1* %" + n.f0.accept(this,null));
        }
        else if (isNumeric(get)){
            System.out.println("\tstore i32 " + get + ", i32* %" + n.f0.accept(this,null));
        }
        else {
            System.out.println("\t%_" + (assignCounter) + " = load i32, i32* %" + get);
            System.out.println("\tstore i32 %_" + (assignCounter++) + ", i32* %" + n.f0.accept(this,null));
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
        else if (!isNumeric(expr) && expr.indexOf("%")==-1) {    // if expr == identifier
            expr = "%" + expr;
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
            if (expr.indexOf("%")==-1) {
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
        if(!isNumeric(print) && print.indexOf("%")==-1)
            print = "%" + print;
        System.out.println("\tcall void (i32) @print_int(i32 " + print + ")");
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
     * f1 -> "["
     * f2 -> PrimaryExpression()
     * f3 -> "]"
     */
    public String visit(ArrayLookup n, String argu) throws Exception {
        String array = n.f0.accept(this,null);
        if(array.indexOf("%")==-1)
            array = "%" + array;
        System.out.println("\t%_" + (assignCounter) + " = load i32*, i32** " + array);
        String address = "%_" + (assignCounter++);
        System.out.println("\t%_" + assignCounter + " = load i32, i32* %_" + (assignCounter-1));
        String size = "%_" + (assignCounter++);
        String index = n.f2.accept(this,null);
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
        if(array.indexOf("%")==-1)
            array = "%" + array;
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
        String cond1 = n.f0.accept(this,null);
        int[] label = {exp_res++, exp_res++, exp_res++, exp_res++};
        if(cond1.equals("false")) {
            cond1 = "0";
        }
        else if (cond1.equals("true")){
            cond1 = "1";
        }
        else if(cond1.indexOf("%")==-1) {
            cond1 = "%" + cond1;
            System.out.println("\t%_" + assignCounter + " = load i1, i1* " + cond1);
            cond1 = "%_" + assignCounter;
            assignCounter++;
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
        else if(cond2.indexOf("%")==-1) {
            cond2 = "%" + cond2;
            System.out.println("\t%_" + assignCounter + " = load i1, i1* " + cond2);
            cond2 = "%_" + assignCounter;
            assignCounter++;
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
        String left = n.f0.accept(this,null);
        String final_left;
        if(!isNumeric(left) && left.indexOf("%")==-1){
            System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
            final_left = "%_" + assignCounter;
            assignCounter++;
        }
        else
            final_left = left;

        String right = n.f2.accept(this,null);
        String final_right;
        if(!isNumeric(right) && right.indexOf("%")==-1){
            System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + right);
            final_right = "%_" + assignCounter;
            assignCounter++;
        }
        else
            final_right = right;

        System.out.println("\t%_" + assignCounter + " = icmp slt i32 " + final_left + ", " + final_right);

        String key = "%_" + assignCounter;
        info.put(key,"i1");

        return key;
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
     * f0 -> PrimaryExpression()
     * f1 -> "+"
     * f2 -> PrimaryExpression()
     */
    public String visit(PlusExpression n, String argu) throws Exception {
        String left = n.f0.accept(this,null);
        if(!isNumeric(left) && left.indexOf("%")==-1) { // left = identifier
            System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
            left = "%_" + (assignCounter++);
        }
        String right = n.f2.accept(this,null);
        if(!isNumeric(right) && right.indexOf("%")==-1) { // right = identifier
            System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + right);
            right = "%_" + (assignCounter++);
        }
        String key = "%_" + assignCounter;
        String value = "i32";
        info.put(key,value);
        assignCounter++;
        System.out.println("\t"+ key + " = add i32 " + left + ", " + right);
        return key;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "-"
     * f2 -> PrimaryExpression()
     */
    public String visit(MinusExpression n, String argu) throws Exception {
        String left = n.f0.accept(this,null);
        if(!isNumeric(left) && left.indexOf("%")==-1) { // left = identifier
            System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
            left = "%_" + (assignCounter++);
        }
        String right = n.f2.accept(this,null);
        if(!isNumeric(right) && right.indexOf("%")==-1) { // right = identifier
            System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + right);
            right = "%_" + (assignCounter++);
        }
        String key = "%_" + assignCounter;
        String value = "i32";
        info.put(key,value);
        assignCounter++;
        System.out.println("\t"+ key + " = sub i32 " + left + ", " + right);
        return key;
    }

    /**
     * f0 -> PrimaryExpression()
     * f1 -> "*"
     * f2 -> PrimaryExpression()
     */
    public String visit(TimesExpression n, String argu) throws Exception {
        String left = n.f0.accept(this,null);
        if(!isNumeric(left) && left.indexOf("%")==-1) { // left = identifier
            System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + left);
            left = "%_" + (assignCounter++);
        }
        String right = n.f2.accept(this,null);
        if(!isNumeric(right) && right.indexOf("%")==-1) { // right = identifier
            System.out.println("\t%_" + assignCounter + " = load i32, i32* %" + right);
            right = "%_" + (assignCounter++);
        }
        String key = "%_" + assignCounter;
        String value = "i32";
        info.put(key,value);
        assignCounter++;
        System.out.println("\t"+ key + " = mul i32 " + left + ", " + right);
        return key;
    }

    /**
     * f0 -> ","
     * f1 -> Expression()
     */
    @Override
    public String visit(ExpressionTerm n, String argu) throws Exception {
        return n.f1.accept(this,null);
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
        //System.out.println("this is primo expr with argu: " + argu);
        return n.f0.accept(this, argu);
    }
}