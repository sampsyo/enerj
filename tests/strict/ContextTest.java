package strict;

import enerj.lang.*;

import java.util.List;

@Approximable
public class ContextTest {
	//TODO:: err
	static @Context int sci;
	
	@Context int ci;
	
	void contextPrimitive() {
		// Works because primitive types
		@Approx int ai = this.ci;
		//:: (assignment.type.incompatible)
		@Precise int pi = this.ci;
		
		@Top int ti = this.ci;
		
		@Approx ContextTest ac = new @Approx ContextTest();
		//:: (assignment.type.incompatible)
		this.ci = ac.ci;
		
		ai = ac.ci;
		//:: (assignment.type.incompatible)
		pi = ac.ci;
		
		@Precise ContextTest pc = new @Precise ContextTest();
		// works b/c of primitive subtyping
		this.ci = pc.ci;
		
		// Works because primitive types
		ai = pc.ci;
		pi = pc.ci;
	}
	
	@Context ContextTest cct;
	@Precise ContextTest pct;
	@Approx ContextTest act;
	
	void contextReferences() {
		//:: (assignment.type.incompatible)
		@Approx ContextTest ao = this.cct;
		//:: (assignment.type.incompatible)
		@Precise ContextTest po = this.cct;
		
		@Top int to = this.ci;
		
		@Approx ContextTest ac = new @Approx ContextTest();
		//:: (assignment.type.incompatible)
		this.cct = ac.cct;
		
		ao = ac.cct;
		//:: (assignment.type.incompatible)
		po = ac.cct;
		
		@Precise ContextTest pc = new @Precise ContextTest();
		//:: (assignment.type.incompatible)
		this.cct = pc.cct;
		
		//:: (assignment.type.incompatible)
		ao = pc.cct;
		po = pc.cct;
		
		//:: (assignment.type.incompatible)
		po = ac.cct.cct;
		po = pc.cct.cct;
		
		po = ac.pct.cct;
		po = pc.pct.cct;
		
		//:: (assignment.type.incompatible)
		ao = pc.cct.cct;
		ao = pc.act.cct;
	}
	
	@Context ContextTest[] coa;
	@Precise ContextTest[] poa;
	@Approx ContextTest[] aoa;
	
	void contextArrays() {
		//:: (assignment.type.incompatible)
		@Approx ContextTest ao = this.coa[0];
		//:: (assignment.type.incompatible)
		@Precise ContextTest po = this.coa[0];
		
		@Approx ContextTest ac = new @Approx ContextTest();
		ao = ac.coa[0];
		//:: (assignment.type.incompatible)
		po = ac.coa[0];
		
		@Precise ContextTest pc = new @Precise ContextTest();
		//:: (assignment.type.incompatible)
		ao = pc.coa[0];
		po = pc.coa[0];
		
		this.coa[0] = this.cct;
		//:: (assignment.type.incompatible)
		this.coa[0] = ac.cct;
		//:: (assignment.type.incompatible)
		this.coa[0] = ao;
		//:: (assignment.type.incompatible)
		this.coa[0] = po;
	}
}

