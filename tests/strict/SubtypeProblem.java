package strict;

import enerj.lang.*;

/**
 * Under the assumption that we take the static type of a receiver to decide
 * whether to take the precise or approximate implementation of a method, this
 * class demonstrates why, for reference types, we should not make Precise be a
 * subtype of Approx.
 * We create an approximate alias to a precise Data object and then perform
 * an approximate instead of a precise calculation.
 * 
 * The alternative would be to use the runtime type to decide whether to call
 * the approximate or precise version of a method.
 * We would introduce one main method "m" that performs the dispatch at runtime to
 * "m_PREC" or "m_APPROX", depending on the type of the object.
 * With this assumption, we should use Precise <: Approx.
 * 
 * 
 * @author wmdietl
 * 
 */
@Approximable
public class SubtypeProblem {
    @Approximable
	class Data {
		@Context
		float data;

		void calc() {
			data += 0.1;
		}

		void calc_APPROX() {
			++data;
		}
	}

	@Context
	Data field;

	void setField(@Context Data p) {
		field = p;
	}

	@Context
	Data getField() {
		return field;
	}

	void problem() {
		@Precise SubtypeProblem prec = new @Precise SubtypeProblem();
		@Precise Data thedata = new @Precise Data();
		prec.setField(thedata);
		
		thedata.calc();
		
		//:: (assignment.type.incompatible)
		@Approx SubtypeProblem app = prec;
		@Approx Data appdata = app.getField();
		
		appdata.calc();
	}
}