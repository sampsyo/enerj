package strict;

import enerj.lang.*;

public class Primitive {
    public static void main(String[] args) {
    }
    
    void m() {
    	@Precise int p;
    	@Approx int a;
    	
    	p = 5;
    	a = 6;
    	a = p;
    	
    	//:: (assignment.type.incompatible)
    	p = a;

    	//:: warning: (cast.unsafe)
    	p = (@Precise int) a;
    	
    	// ok
    	a = p;
    }
}
