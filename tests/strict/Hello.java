package strict;

import enerj.lang.*;

@Approximable
public class Hello {
    public static void main(String[] args) {
        @Approx int x = 5;
        int y = 10;
        // y = x; <-- error!
        x = y;
        System.out.println("Hello world!");
        
        @Precise int z = 9;
        
        // ok
        x = z;
        
        //:: (assignment.type.incompatible)
        z = x;
        
        @Precise Hello po = new Hello();
        @Approx Hello ao = new @Approx Hello();
        
        //:: (assignment.type.incompatible)
        po = ao;
    }
}
