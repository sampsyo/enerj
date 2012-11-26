package enerj.rt;

public class PrecisionRuntimeRoot {

	public static final PrecisionRuntime impl;

	static {
		System.out.println("Loading PrecisionRuntimeRoot");
		
		String runtimeClass = System.getProperty("PrecisionRuntime");
		PrecisionRuntime newimpl;
		if (runtimeClass != null) {
			/* try to create an instance of this class */
			try {
				newimpl = (PrecisionRuntime)
					Class.forName(runtimeClass).newInstance();
			} catch (Exception e) {
				System.err.println("WARNING: the specified Precision Runtime Implementation class ("
								+ runtimeClass
								+ ") could not be instantiated, using the default instead.");
				// System.err.println(e);
				newimpl = new PrecisionRuntimeDefault();
			}
		} else {
			newimpl = new PrecisionRuntimeDefault();
		}
		impl = newimpl;
	}
	
	/*
	static dynCall(Object receiver, String name, Object[] args) {
		look at precision of receiver, then either call name_PREC or name_APPROX
	}
	
	static initObject(Object o, boolean precision) {
		get class, iterate through fields, set precision of fields
	}
	*/
}