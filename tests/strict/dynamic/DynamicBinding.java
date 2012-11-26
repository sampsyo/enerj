import enerj.lang.*;

@Approximable
public class DynamicBinding {

	public static void main(String[] args) {
		testBar();
		testFoo();
		testBla();
	}
	
	/* The simplest case without parameters. */
	static void testBar() {	
		DynamicBinding m = new DynamicBinding();
		assert m.bar().equals("Precise!") : "Didn't get expected precise result!";
		
		@Top DynamicBinding am = new @Approx DynamicBinding();
		assert am.bar().equals("Approx!") : "Didn't get expected approximate result!";

		@Top DynamicBinding apm = new @Precise DynamicBinding();
		assert apm.bar().equals("Precise!") : "Didn't get expected precise result!";
	}

	String bar() { return "Precise!"; }
	String bar_APPROX() { return "Approx!";	}
	
	@Approximable
	static class AObject {}

	/* We have an _APPROX method with compatible types -> substitute */
	static void testFoo() {
		AObject o = new AObject();
		DynamicBinding m = new DynamicBinding();
		assert m.foo(o).equals("Precise!") : "Didn't get expected precise result!";
		
		@Approx DynamicBinding am = new @Approx DynamicBinding();
		assert am.foo(o).equals("Approx!") : "Didn't get expected approximate result!";		
	}
	
	String foo(@Precise AObject o) { return "Precise!"; }
	String foo_APPROX(@Top AObject o) { return "Approx!"; }

	
	/* We have an _APPROX method, however without compatible types -> don't substitute */
	static void testBla() {
		@Approx AObject o = new @Approx AObject();
		DynamicBinding m = new DynamicBinding();
		assert m.bla(o).equals("Precise!") : "Didn't get expected precise result!";
		
		@Approx DynamicBinding am = new @Approx DynamicBinding();
		assert am.bla(o).equals("Precise!") : "Didn't get expected precise result!";
	}
	
	String bla(@Top AObject o) { return "Precise!"; }
	String bla_APPROX(@Precise AObject o) { return "Approx!"; }
	
}
