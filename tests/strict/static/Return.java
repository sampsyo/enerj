package strict.stat;

import enerj.lang.*;

public class Return {
	
	public static void main(String[] args) {
		new Return().tests();
	}
	
	void tests() {
		// testReturn();
		// testReturnRecv();
		testArgs();
	}
	
	/*
	void testReturn() {
		// as initializer
		@Precise MyApp ps = bar();
		assert ps.toString().equals("Precise!") : "Didn't get expected precise result!";

		// as assignment
		@Approx MyApp as;
		as = bar();
		assert as.toString().equals("Approx!") : "Didn't get expected approximate result!";

		// as initializer
		@Approx MyApp as2 = bar();
		assert as2.toString().equals("Approx!") : "Didn't get expected approximate result!";
	}

	void testReturnRecv() {
		@Precise Return pr = new @Precise Return();
		
		// as initializer
		@Precise MyApp ps = pr.bar();
		assert ps.toString().equals("Precise!") : "Didn't get expected precise result!";

		// as assignment
		@Approx MyApp as = pr.bar();
		assert as.toString().equals("Approx!") : "Didn't get expected approximate result!";

		
		@Approx Return ar = new @Approx Return();
		
		// this combination is not possible currently. TODO?
		// @Precise MyApp as2 = ar.bar();
		// assert as2.toString().equals("Approx!") : "Didn't get expected approximate result!";

		@Approx MyApp as3 = ar.bar();
		assert as3.toString().equals("Approx!") : "Didn't get expected approximate result!";
	}*/

	void testArgs() {
		@Precise MyApp pt = new @Precise MyApp("Precise target");
		
		@Precise MyApp pps = pt.foo(bar());
		assert pps.toString().equals("Precise!") : "Didn't get expected precise result!";

		// as assignment
		@Approx MyApp pas;
		pas = pt.foo(bar());
		assert pas.toString().equals("Approx!") : "Didn't get expected approximate result!";

		@Approx MyApp at = new @Approx MyApp("Approx target");
		
		@Precise MyApp aps = at.foo(bar());
		assert aps.toString().equals("Precise!") : "Didn't get expected precise result!";

		// as assignment
		@Approx MyApp aas = at.foo(bar());
		assert aas.toString().equals("Approx!") : "Didn't get expected approximate result!";
	}
	
	@Precise MyApp bar() { return new @Precise MyApp("Precise!"); }
	@Approx MyApp bar_APPROX() { return new @Approx MyApp("Approx!");	}
	
	@Approximable
	class MyApp {
		String s;
		MyApp(String p) { s = p; }
		@Override
		public String toString() { return s; }
		
		@Precise MyApp foo(@Precise MyApp par) { return new @Precise MyApp(s + "new precise and parameter " + par.toString()); }
		@Approx MyApp foo_APPROX(@Approx MyApp par) { return new @Approx MyApp(s + "new approx and parameter " + par.toString());	}		
	}
}