class Example {
    public static void main(String[] args) {
        int a;
        int[] intarray;
        boolean b;
        boolean[] booleanarray;
        A alpha;
        B beta;

        a = 3;
    }
}
class A{
    int i;
    boolean flag;
    int j;
    public int foo() {return 1;}
    public boolean fa() {return true;}
    public int[] test() {
        int[] bla;
        return bla;
    }
    public boolean[] bool() {
        boolean[] booli;
        return booli;
    }
    public B bi() {
        B b;
        return b;
    }
}

class B extends A{
    A type;
    int k;
    public int foo() {return 0;}
    public boolean bla() {return false;}
    public B bi() {
        B b;
        return b;
    }
    public boolean[] bool() {
        boolean[] booli;
        return booli;
    }
}

class C extends B {

}

class E {

}
