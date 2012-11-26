package strict;

import enerj.lang.*;

public class OperatorTest {
    public static void main(String argv[]) {
        @Approx int i = 10;
        @Approx int j = 5;
        
        //:: (assignment.type.incompatible)
        @Precise int res1 = i + j;
        
        // Should be OK.
        @Approx int res2 = i + j;
    }
}
