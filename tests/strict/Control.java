package strict;

import enerj.lang.*;

public class Control {
	
	void m() {
		@Approx int aa = 5, ab = 6;
		@Precise int pa = 5, pb = 6;
		@Approx boolean appbool = false;
		
		if ( pa < pb ) {}
		
		appbool =  pa < ab;
		
		//:: (assignment.type.incompatible)
		@Precise boolean comp = aa < pb;
		
		//:: (condition.type.incompatible)
		if ( aa < ab ) {}
		
		//:: (condition.type.incompatible)
		if ( appbool ) {}

		//:: (condition.type.incompatible)
		while ( appbool ) {}
	}
}