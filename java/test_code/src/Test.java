/**
 * Created by newind on 17-2-22.
 */
public class Test {
    private static byte random(){
        return (byte) (Math.random() * 100);
    }

    public static void main(String[] argv){
        CycleTest cycleTest = new CycleTest(new byte[] {2,2});
        while (!cycleTest.isEqual()){
            byte b = random();
            cycleTest.put(b);
            System.out.print(b);
        }
    }
}
