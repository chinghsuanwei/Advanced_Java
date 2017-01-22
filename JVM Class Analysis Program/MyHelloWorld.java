/** 
 * The HelloWorldApp class implements an application that
 * simply displays "Hello World!" to the standard output.
 */
class MyHelloWorld {
    int data;
    MyHelloWorld(int i) {
        data = i;
    }
    void testPrint() {
        System.out.println("Test " + data); 
    }
    public static void main(String[] args) {
        MyHelloWorld mine = new MyHelloWorld(10);
        mine.testPrint();
        System.out.println("Hello World!"); //Display the string.
    }
}
