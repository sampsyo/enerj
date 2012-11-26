package strict;

import enerj.lang.*;

import java.util.List;
import java.util.LinkedList;

public class Approximability {
    @Approximable
    public static class ApproximableClass {
        void meth() {
            List<Object> l = new LinkedList<Object>();
            //:: (argument.type.incompatible)
            l.add(this);
        }
    }
    
    public static class UnapproximableClass {
        void meth() {
            List<Object> l = new LinkedList<Object>();
            // ok: this is @Precise
            l.add(this);
        }
    }
    
    public static void main(String[] argv) {
        // ok
        @Approx ApproximableClass obj1;
        //:: (type.invalid.unapproximable)
        @Approx UnapproximableClass obj2;
    }
}
