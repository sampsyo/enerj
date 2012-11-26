import enerj.lang.*;

public class Hello {
	void m() {
		@Approx Object ao = new @Approx Object();
		@Precise Object po = new @Precise Object();
		
		ao = po;
		
		//:: (assignment.type.incompatible)
		po = ao;
	} 
}