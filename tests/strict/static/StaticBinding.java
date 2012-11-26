package strict.stat;

import enerj.lang.*;

@Approximable
public class StaticBinding {
	/*StaticBinding(int p) {		
	}
	
	StaticBinding( ) {
		this(5);
	} */

	public static void main(String[] args) {
		testBar();
		testFoo();
		testBla();
	}
	
	/* The simplest case without parameters. */
	static void testBar() {	
		StaticBinding m = new StaticBinding();
		assert m.bar().equals("Precise!") : "Didn't get expected precise result!";
		
		@Approx StaticBinding am = new @Approx StaticBinding();
		assert am.bar().equals("Approx!") : "Didn't get expected approximate result!";
	}

	String bar() { return "Precise!"; }
	String bar_APPROX() { return "Approx!";	}
	

	/* We have an _APPROX method with compatible types -> substitute */
	static void testFoo() {
		Object o = new Object();
		StaticBinding m = new StaticBinding();
		assert m.foo(o).equals("Precise!") : "Didn't get expected precise result!";
		
		@Approx StaticBinding am = new @Approx StaticBinding();
		assert am.foo(o).equals("Approx!") : "Didn't get expected approximate result!";		
	}
	
	String foo(@Precise Object o) { return "Precise!"; }
	String foo_APPROX(@Top Object o) { return "Approx!"; }

	
	/* We have an _APPROX method, however without compatible types -> don't substitute */
	static void testBla() {
		@Approx StaticBinding o = new @Approx StaticBinding();
		StaticBinding m = new StaticBinding();
		assert m.bla(o).equals("Precise!") : "Didn't get expected precise result!";
		
		@Approx StaticBinding am = new @Approx StaticBinding();
		assert am.bla(o).equals("Precise!") : "Didn't get expected precise result!";
	}
	
	String bla(@Top Object o) { return "Precise!"; }
	String bla_APPROX(@Precise Object o) { return "Approx!"; }
}
