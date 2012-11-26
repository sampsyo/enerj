package strict;

import enerj.lang.*;

@Approximable
public class RuntimeTest {

	RuntimeTest(int p) {
		System.out.println("The constructor!");
		
		if (p > 2) {
			@Context RuntimeTest another = new @Context RuntimeTest(0);
			another.meth(p);
		}
	}

	static Object bar(@Top RuntimeTest p) {
		int i=78;
		p.meth(i);
		return new Object();
	}
	
	void meth(int p) {
		System.err.println("Precise" + p);
	}
	
	void meth_APPROX(int p) {
		System.err.println("Approx" + p);
	}
	
	public static void main(String[] args) {
		int arg = 5;

		Object o = bar(new @Approx RuntimeTest(arg));
	}
}
