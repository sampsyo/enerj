package strict;

import enerj.lang.*;

public class Creation {
    @Approximable
    class AObject {}
    
	class CreationPrecise {
		CreationPrecise(@Precise AObject par) {}
	
		void testRefs() {
			@Approx AObject ao = new @Approx AObject();
			@Precise AObject po = new @Precise AObject();
			@Precise AObject ipo = new AObject();
			@Context AObject co = new @Context AObject();
			
			//:: (new.top.forbidden)
			@Top AObject to = new @Top AObject();
			
			@Precise Object rpo = new @Precise CreationPrecise(po);
			//:: (argument.type.incompatible)
			rpo = new @Precise CreationPrecise(ao);
		}
	}
	
	@Approximable
	class CreationContextPrim {
		@Context int cif;
		@Precise int pif;
		@Approx int aif;

		CreationContextPrim(@Context int par) {
			cif = par;
			//:: (assignment.type.incompatible)
			pif = par;
			// works b/c of primitive type
			aif = par;
		}

		void testPrimitives() {
			@Approx int ai = 5;
			@Precise int pi = 6;
			@Context int ci = 7;
		
			@Approx CreationContextPrim ac = new @Approx CreationContextPrim(ai);
			@Precise CreationContextPrim pc = new @Precise CreationContextPrim(pi);
			@Context CreationContextPrim cc = new @Context CreationContextPrim(ci);
			
			// b/c of primitive subtyping
			ac = new @Approx CreationContextPrim(pi);
			//:: (argument.type.incompatible)
			pc = new @Precise CreationContextPrim(ai);
		}
	}

	@Approximable
	class CreationContextRef {
		@Context AObject cof;
		@Precise AObject pof;
		@Approx AObject aof;

		CreationContextRef(@Context AObject par) {
			cof = par;
			//:: (assignment.type.incompatible)
			pof = par;
			//:: (assignment.type.incompatible)
			aof = par;
		}
		
		@Approx AObject ao;
		@Precise AObject po;
		@Context AObject co;
		
		void testRefs() {
			@Approx CreationContextRef ac = new @Approx CreationContextRef(ao);
			@Precise CreationContextRef pc = new @Precise CreationContextRef(po);
			@Context CreationContextRef cc = new @Context CreationContextRef(co);
			
			//:: (argument.type.incompatible)
			ac = new @Approx CreationContextRef(po);
			//:: (argument.type.incompatible)
			pc = new @Precise CreationContextRef(ao);
		}
	}
}