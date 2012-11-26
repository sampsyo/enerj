package strict;

import enerj.lang.*;

@Approximable
class AObject {}

@Approximable
class MyGenerics<X extends @Top AObject> {
	@Precise Object po = new @Precise Object();
	
}

public class ContextGenerics {	
	@Context MyGenerics<@Context AObject> clco;
	@Approx MyGenerics<@Context AObject> alco;
	@Precise MyGenerics<@Context AObject> plco;
	@Context MyGenerics<@Approx AObject> clao;
	@Context MyGenerics<@Precise AObject> clpo;
	
	void contextGenerics() {
		// TODO: change default for upper bounds to TOP!
	}

}
