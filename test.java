class test {
    public static void main(String[] a) {
        int x;
        int y;

        x = 1;

        while(x < 3){
            y = x + 1;
            x = y + 1;
        }
    }
}

class A {
    int a;
    B bi;
    public int foo(int i, int j) {return 0;}
    public boolean bla(boolean b, B bi) {return true;}
    public B foobla() {return bi;}
}

class B extends A {
    int b;
    A alpha;
    int a;
    public A testA(A a, B b) {return alpha;}
    public B testB(int i, boolean j, B b) {return b;}
    public int foo(int i, int j) {return 0;}
    public B foobla() {return bi;}
}

class C extends B {

}