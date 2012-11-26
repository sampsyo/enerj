package strict;

import enerj.lang.*;

@Approximable
public class Methods {
	void pm(@Precise Methods po) {}
	
	void am(@Approx Methods ao) {}
	
	void cm(@Context Methods co) {}
	
	@Precise Methods po = new @Precise Methods();
	@Approx Methods ao = new @Approx Methods();
	
	void testThis() {	
		this.pm(po);
		this.am(ao);
		
		//:: (argument.type.incompatible)
		this.pm(ao);
		//:: (argument.type.incompatible)
		this.am(po);
		
		//:: (argument.type.incompatible)
		this.cm(ao);
		//:: (argument.type.incompatible)
		this.cm(po);
		this.cm(this);
	}
	
	void testPrecise(@Precise Methods pms) {
		pms.pm(po);
		//:: (argument.type.incompatible)
		pms.pm(ao);
		
		pms.am(ao);
		//:: (argument.type.incompatible)
		pms.am(po);
		
		pms.cm(po);
		//:: (argument.type.incompatible)
		pms.cm(ao);
	}
	
	void testApprox(@Approx Methods ams) {
		ams.pm(po);
		//:: (argument.type.incompatible)
		ams.pm(ao);
		
		ams.am(ao);
		//:: (argument.type.incompatible)
		ams.am(po);
		
		ams.cm(ao);
		//:: (argument.type.incompatible)
		ams.cm(po);
	}
	
	
	void pi(@Precise int pip) {}
	
	void ai(@Approx int aip) {}
	
	void ci(@Context int cip) {}
	
	void testApproxInt(@Approx Methods ams) {
		@Precise int lp = 5;
		@Approx int la = 5;
		@Context int lc = 5;
		
		ams.pi(lp);
		//:: (argument.type.incompatible)
		ams.pi(la);
		
		ams.ai(la);
		// works b/c of primitive subtyping
		ams.ai(lp);
		
		// works b/c of primitive subtyping
		ams.ci(lc);
		// works b/c of primitive subtyping
		ams.ci(lp);
		ams.ci(la);
	}
	void testPreciseInt(@Precise Methods pms) {
		@Precise int lp = 5;
		@Approx int la = 5;
		@Context int lc = 5;
		
		pms.pi(lp);
		//:: (argument.type.incompatible)
		pms.pi(la);
		
		pms.ai(la);
		// works b/c of primitive subtyping
		pms.ai(lp);
		
		pms.ci(lp);
		//:: (argument.type.incompatible)
		pms.ci(lc);
		//:: (argument.type.incompatible)
		pms.ci(la);
	}
}