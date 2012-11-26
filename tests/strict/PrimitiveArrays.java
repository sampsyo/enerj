package strict;

import enerj.lang.*;

public class PrimitiveArrays {
	void m() {
		@Approx int [] aia = new @Approx int [10];
		@Precise int [] pia = new @Precise int [10];

		// default meaning for array dimensions is also precise
		@Approx int @Precise [] aipa = aia;
		@Precise int @Precise [] pipa = pia;
		
		@Approx int ai = 5;
		@Precise int pi = 6;
		
		//:: (assignment.type.incompatible)
		pia = aia;
		//:: (assignment.type.incompatible)
		pia = aipa;
		//:: (assignment.type.incompatible)
		pipa = aipa;
		//:: (assignment.type.incompatible)
		pipa = aia;

		//:: (assignment.type.incompatible)
		aia = pia;
		
		pia[0] = pi;
		aia[0] = ai;
		aia[0] = pi;
		
		//:: (assignment.type.incompatible)
		pia[0] = ai;
		

		@Approx int [][] aiaa = new @Approx int [10][10];
		@Precise int [][] piaa = new @Precise int [10][10];

		aiaa[0] = aia;
		piaa[0] = pia;
		
		//:: (assignment.type.incompatible)
		aiaa[0] = pia;
		//:: (assignment.type.incompatible)
		piaa[0] = aia;
	}
	
	
	void wf() {
		// TODO: Forbid @Approx as array dimension annotation?
		@Approx int @Precise [] aipa = new @Approx int @Precise [10];
		@Precise int @Approx [] piaa = new @Precise int @Approx [10];

	}
}