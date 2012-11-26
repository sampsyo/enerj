package enerj.rt;

import checkers.runtime.rt.Runtime;
import java.lang.ref.PhantomReference;


public interface PrecisionRuntime extends Runtime {
	/**
	 * Set whether the referenced object is approximate.
	 *
	 * @param o Object that should be catalogged.
	 * @param approx Whether the object is approximate.
	 * @param heap Whether the object is on the heap (as opposed to the stack).
  	 * @param preciseSize The precise memory (in bytes) used by the object.
  	 * @param approxSize The approximate memory used by the object.
	 */
	PhantomReference<Object> setApproximate(
	    Object o, boolean approx, boolean heap,
	    int preciseSize, int approxSize
	);

	/**
	 * Query whether the referenced object is approximate.
	 *
	 * @param o The object to test.
	 * @return True, iff the referenced object is approximate.
	 */
	boolean isApproximate(Object o);

	// Simulated operations.
	public enum NumberKind { INT, BYTE, DOUBLE, FLOAT, LONG, SHORT }

	public enum ArithOperator { PLUS, MINUS, MULTIPLY, DIVIDE, BITXOR }

	public Number binaryOp(Number lhs, Number rhs, ArithOperator op, NumberKind nk, boolean approx);

	public <T> T countLogicalOp(T value);

	// Instrumented memory accesses.
	public enum MemKind { VARIABLE, FIELD, ARRAYEL }

	public <T> T storeValue(T value, boolean approx, MemKind kind);
	public <T> T loadValue(T value, boolean approx, MemKind kind);
	public <T> T loadLocal(Reference<T> ref, boolean approx);
	public <T> T loadArray(Object array, int index, boolean approx);
	public <T> T loadField(Object obj, String fieldname, boolean approx);
	public <T> T storeLocal(Reference<T> ref, boolean approx, T rhs);
	public <T> T storeArray(Object array, int index, boolean approx, T rhs);
	public <T> T storeField(Object obj, String fieldname, boolean approx, T rhs);

	// Fancier assignments.
	public <T extends Number> T assignopLocal(Reference<T> var, ArithOperator op, Number rhs, boolean returnOld, NumberKind nk, boolean approx);
	public <T extends Number> T assignopArray(Object array, int index, ArithOperator op, Number rhs, boolean returnOld, NumberKind nk, boolean approx);
	public <T extends Number> T assignopField(Object obj, String fieldname, ArithOperator op, Number rhs, boolean returnOld, NumberKind nk, boolean approx);
}
