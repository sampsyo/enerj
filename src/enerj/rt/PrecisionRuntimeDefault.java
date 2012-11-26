package enerj.rt;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;
import java.util.Map;
import java.util.HashMap;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import plume.WeakIdentityHashMap;

import java.util.List;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import org.json.JSONStringer;
import org.json.JSONException;
import java.io.FileWriter;
import java.io.IOException;

class CreationInfo {
	public Object creator;
	public boolean approx;
	public int preciseSize;
	public int approxSize;
}


class ApproximationInformation {
	public long created;
	public List<Long> read;
	public List<Long> write;
	public long collected;
	public boolean approx;
	public boolean heap;
	public int preciseSize;
	public int approxSize;

	// to safe a bit of space we only store the creation time.
	ApproximationInformation(long t, boolean approx, boolean heap,
	                         int preciseSize, int approxSize) {
		created = t;
		read = new LinkedList<Long>();
		write= new LinkedList<Long>();
		this.approx = approx;
		this.heap = heap;
		this.preciseSize = preciseSize;
		this.approxSize = approxSize;
	}
}

class PrecisionRuntimeDefault implements PrecisionRuntime {

	// This map *only* contains approximate objects. That is,
	// info.get(???).approx == true
	private Map<Object, ApproximationInformation> info = new WeakIdentityHashMap<Object, ApproximationInformation>();

	// This "parallel" map is used just to receive object finalization events.
	// Phantom references can't be dereferenced, so they can't be used to look
	// up information. But there are the only way to truly know exactly when
	// an object is about to be deallocated. This map contains *all* objects,
	// even precise ones.
	private Map<PhantomReference<Object>, ApproximationInformation> phantomInfo =
	    new HashMap<PhantomReference<Object>, ApproximationInformation>();
	private ReferenceQueue<Object> referenceQueue = new ReferenceQueue<Object>();

	long startup;

	private Map<String, Integer> preciseOpCounts = new HashMap<String, Integer>();
	private Map<String, Integer> approxOpCounts  = new HashMap<String, Integer>();
	private Map<String, Long> approxFootprint = new HashMap<String, Long>();
	private Map<String, Long> preciseFootprint = new HashMap<String, Long>();

	private static boolean debug = "true".equals(System.getenv("EnerJDebug"));

	@Override
	public PhantomReference<Object> setApproximate(
	    Object o, boolean approx, boolean heap, int preciseSize, int approxSize
	) {
		if (debug) {
			System.out.println("EnerJ: Add object " + System.identityHashCode(o) + " to system.");
		}
		long time = System.currentTimeMillis();
		ApproximationInformation infoObj =
		    new ApproximationInformation(time, approx, heap,
		                                 preciseSize, approxSize);
		PhantomReference<Object> phantomRef = new PhantomReference<Object>(o, referenceQueue);

		// Add to bookkeeping maps.
	    synchronized (this) {
	        if (approx)
	            info.put(o, infoObj);
    		phantomInfo.put(phantomRef, infoObj);
    	}

    	return phantomRef;
	}

	@Override
	public boolean isApproximate(Object o) {
		if (debug) {
			System.out.println("EnerJ: Determine whether \"" + (o!=null ? System.identityHashCode(o):"null") + "\" is approximate");
		}
		boolean approx;
		synchronized (this) {
		    approx = info.containsKey(o);
		}
		return approx;
	}

    public PrecisionRuntimeDefault() {
        super();

        startup = System.currentTimeMillis();

        final Thread deallocPollThread = new Thread(new Runnable() {
            @Override
            public void run() {
                deallocPoll();
            }
        });
        deallocPollThread.setDaemon(true); // Automatically shut down.
        deallocPollThread.start();

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                cleanUpObjects();
                dumpCounts();
            }
        }));
    }

    /**
     * Mapping Thread IDs to stacks of CreationInfo objects.
     */
    Map<Long, Stack<CreationInfo>> creations = new HashMap<Long, Stack<CreationInfo>>();

	@Override
	public boolean beforeCreation(Object creator, boolean approx,
	                              int preciseSize, int approxSize) {
		if (debug) {
			System.out.println("EnerJ: before creator \"" + System.identityHashCode(creator)
					+ "\" creates new " + (approx ? "approximate" : "precise")
					+ " object");
		}
		CreationInfo c = new CreationInfo();
		c.creator = creator;
		c.approx = approx;
		c.preciseSize = preciseSize;
		c.approxSize = approxSize;

		long tid = Thread.currentThread().getId();

		Stack<CreationInfo> stack = creations.get(tid);
		if (stack==null) {
			stack = new Stack<CreationInfo>();
		}
		stack.push(c);
		creations.put(tid, stack);

		return true;
	}

	@Override
	public boolean enterConstructor(Object created) {
		if (debug) {
			System.out.println("EnerJ: enter constructor for object \"" + System.identityHashCode(created) + "\"");
		}
		Stack<CreationInfo> stack = creations.get(Thread.currentThread().getId());

		if (stack==null) {
			if (debug) {
				System.out.println("EnerJ: enter constructor for object \"" + System.identityHashCode(created) + "\" found a null stack.");
			}
			// probably instantiated from non-EnerJ code
			return false;
		}

		if (stack.size()<=0) {
			if (debug) {
				System.out.println("EnerJ: enter constructor for object \"" + System.identityHashCode(created) + "\" found an empty stack.");
			}
			// probably instantiated from non-EnerJ code
			return false;
		}

		CreationInfo c = stack.pop();

		// we cannot compare c.creator; we assume that there is no thread interleaving
		// between the call of beforeCreation and enterConstructor.
		// TODO: some methods should probably be synchronized, but I think this
		// wouldn't help against this particular problem.
		this.setApproximate(created, c.approx, true,
		                    c.preciseSize, c.approxSize);

		return true;
	}

	@Override
	public boolean afterCreation(Object creator, Object created) {
		if (debug) {
			System.out.println("EnerJ: after creator \"" + System.identityHashCode(creator)
					+ "\" created new object \"" + System.identityHashCode(created) + "\"");
		}
		Stack<CreationInfo> stack = creations.get(Thread.currentThread().getId());
		// Could stack ever be null? I guess not, b/c "afterC" is only called, if "beforeC" was called earlier.

		if (stack.size()<=0) {
			if (debug) {
				System.out.println("EnerJ: after creator \"" + System.identityHashCode(creator)
						+ "\" created new object \"" + System.identityHashCode(created) + "\" found an empty stack.");
			}
			// no worries, the stack must have been emptied in enterConstructor
			return false;
		}

		CreationInfo c = stack.peek();

		if (c.creator == creator) {
			this.setApproximate(created, c.approx, true,
			                    c.preciseSize, c.approxSize);
			if (c.approx) {
				stack.pop();
			}
		} else {
			if (debug) {
				System.out.println("EnerJ: after creator \"" + System.identityHashCode(creator)
						+ "\" created new object \"" + System.identityHashCode(created)
						+ "\" found mismatched creator \"" + c.creator + "\".");
			}
			// if the creators do not match, the entry was already removed.
		}

		return true;
	}

	@Override
	public <T> T wrappedNew(boolean before, T created, Object creator) {
		afterCreation(creator, created);
		return created;
	}

	@Override
	public <T> T newArray(T created, int dims, boolean approx,
	                      int preciseElSize, int approxElSize) {
	    int elems = 1;
	    Object arr = created;
	    for (int i = 0; i < dims; ++i) {
	        elems *= Array.getLength(arr);
	        if (Array.getLength(arr) == 0)
	            break;
	        if (i < dims - 1)
	            arr = Array.get(arr, 0);
	    }

	    if (!approx) {
	        preciseElSize += approxElSize;
	        approxElSize = 0;
	    }

	    this.setApproximate(created, false, true,
	                        preciseElSize*elems, approxElSize*elems);

	    if (debug) {
			System.out.println("EnerJ: created array \"" +
			    System.identityHashCode(created) +
			    "\" with size " + elems);
		}

	    return created;
	}

	/*
	@Override
	public boolean isApproximate(Object o, String field) {
		PrecisionInformation entry = info.get(o);
		return entry != null && entry.isApproximate(field);
	}

	@Override
	public PrecisionInformation createPrecisionInfo() {
		// alternatively, create the pi object in addObject and let the user
		// modify the values afterward
		return new PrecisionInformationDefault();
	}
	*/


	// Counting infrastucture.
	private synchronized void countOperation(String name, boolean approx) {
	    Map<String, Integer> map = null;
	    if (approx)
	        map = approxOpCounts;
	    else
	        map = preciseOpCounts;
	    if (map.containsKey(name)) {
	        map.put(name, map.get(name) + 1); // m[name] += 1
	    } else {
	        map.put(name, 1);
	    }
	}

	private synchronized void countFootprint(String name, boolean approx,
	                                         long amount) {
	    Map<String, Long> map = null;
	    if (approx)
	        map = approxFootprint;
	    else
	        map = preciseFootprint;
	    if (map.containsKey(name)) {
	        map.put(name, map.get(name) + amount);
	    } else {
	        map.put(name, amount);
	    }
	}
	private synchronized void dumpCounts() {
	    JSONStringer stringer = new JSONStringer();
	    try {
    	    stringer.object();

    	    // Output operation counts.
    	    Set<String> ops = new HashSet<String>();
    	    ops.addAll(approxOpCounts.keySet());
    	    ops.addAll(preciseOpCounts.keySet());
    	    stringer.key("operations");
    	    stringer.object();
    	    for (String op : ops) {
    	        int approxCount = 0;
    	        if (approxOpCounts.containsKey(op))
    	            approxCount = approxOpCounts.get(op);
    	        int preciseCount = 0;
    	        if (preciseOpCounts.containsKey(op))
    	            preciseCount = preciseOpCounts.get(op);

    	        stringer.key(op);
    	        stringer.array();
    	        stringer.value(preciseCount);
    	        stringer.value(approxCount);
    	        stringer.endArray();
    	    }
    	    stringer.endObject();

    	    // Output footprint counts.
    	    Set<String> footprints = new HashSet<String>();
    	    footprints.addAll(approxFootprint.keySet());
    	    footprints.addAll(preciseFootprint.keySet());
    	    stringer.key("footprint");
    	    stringer.object();
    	    for (String sec : footprints) {
    	        long approxAmt = approxFootprint.containsKey(sec) ?
    	                         approxFootprint.get(sec) : 0;
    	        long preciseAmt = preciseFootprint.containsKey(sec) ?
    	                          preciseFootprint.get(sec) : 0;

    	        stringer.key(sec);
    	        stringer.array();
    	        stringer.value(preciseAmt);
    	        stringer.value(approxAmt);
    	        stringer.endArray();
    	    }
    	    stringer.endObject();

    	    stringer.endObject();
        } catch (JSONException exc) {
            System.out.println("JSON writing failed!");
        }

	    String out = stringer.toString();
	    if (debug) {
    	    System.out.println(out);
	    }
	    try {
	        FileWriter fstream = new FileWriter("enerjstats.json");
	        fstream.write(out);
	        fstream.close();
	    } catch (IOException exc) {
	        System.out.println("couldn't write stats file!");
	    }
	}

	// Object finalization calls.
	@Override
	public synchronized void endLifetime(PhantomReference<Object> ref) {
	    ApproximationInformation infoObj;
	    if (phantomInfo.containsKey(ref)) {
	        infoObj = phantomInfo.get(ref);
	    } else {
	        // Already collected! Do nothing.
	        return;
	    }
	    infoObj.collected = System.currentTimeMillis();

        // Log this lifetime at an object granularity.
        String memPart = infoObj.heap ? "heap" : "stack";
        long duration = infoObj.collected - infoObj.created;
        countFootprint(memPart + "-objects", infoObj.approx, duration);

        // Log memory usage in byte-seconds.
        countFootprint(memPart + "-bytes", false,
            infoObj.preciseSize * duration);
        countFootprint(memPart + "-bytes", true,
            infoObj.approxSize  * duration);

        if (debug) {
            System.out.println("EnerJ: object collected after " + duration +
                        " (" + (infoObj.approx ? "A" : "P") + ", " +
                        memPart + ")");
        }
	}

	// A thread that waits for finalizations.
	private void deallocPoll() {
	    while (true) {
	        PhantomReference<Object> ref = null;
	        try {
	            ref = (PhantomReference<Object>) referenceQueue.remove();
	        } catch (InterruptedException exc) {
	            // Thread shut down.
	            if (debug)
    	            System.out.println("EnerJ: dealloc thread interrupted!");
	            return;
	        }

	        endLifetime(ref);
	    }
	}

	// Called on shutdown to collect all remaining objects.
	private synchronized void cleanUpObjects() {
	    if (debug)
	        System.out.println("EnerJ: objects remaining at shutdown: " +
	                           phantomInfo.size());
	    for (Map.Entry<PhantomReference<Object>, ApproximationInformation> kv :
	                                        phantomInfo.entrySet()) {
	        endLifetime(kv.getKey());
	    }
	}

	protected String opSymbol(ArithOperator op) {
	    switch (op) {
	    case PLUS: return "+";
	    case MINUS: return "-";
	    case MULTIPLY: return "*";
	    case DIVIDE: return "/";
	    case BITXOR: return "^";
	    }
	    return "(unknown)";
	}

	// Simulated operations.


    // This is a little incongruous, but this just counts some integer
    // operations that we don't want to instrument but are always done
    // precisely.
    @Override
    public <T> T countLogicalOp(T value) {
    	countOperation("INTlogic", false);
    	return value;
    }

	@Override
	public Number binaryOp(Number lhs, Number rhs, ArithOperator op, NumberKind nk, boolean approx) {
	    countOperation(nk + opSymbol(op), approx);

        // Prevent divide-by-zero on approximate data.
        if (approx && op == ArithOperator.DIVIDE && rhs.equals(0)) {
            switch (nk) {
            case DOUBLE:
                return Double.NaN;
            case FLOAT:
                return Float.NaN;
            case LONG:
                return Long.valueOf(0);
            case INT:
            case BYTE:
            case SHORT:
                return Integer.valueOf(0);
            }
        }

	    switch (nk) {
	    case DOUBLE:
	        switch (op) {
	        case PLUS:
	            return (lhs.doubleValue() + rhs.doubleValue());
	        case MINUS:
	            return (lhs.doubleValue() - rhs.doubleValue());
	        case MULTIPLY:
	            return (lhs.doubleValue() * rhs.doubleValue());
	        case DIVIDE:
	            return (lhs.doubleValue() / rhs.doubleValue());
	        }
	    case FLOAT:
	        switch (op) {
	        case PLUS:
	            return (lhs.floatValue() + rhs.floatValue());
	        case MINUS:
	            return (lhs.floatValue() - rhs.floatValue());
	        case MULTIPLY:
	            return (lhs.floatValue() * rhs.floatValue());
	        case DIVIDE:
	            return (lhs.floatValue() / rhs.floatValue());
	        }
	    case LONG:
	        switch (op) {
	        case PLUS:
	            return (lhs.longValue() + rhs.longValue());
	        case MINUS:
	            return (lhs.longValue() - rhs.longValue());
	        case MULTIPLY:
	            return (lhs.longValue() * rhs.longValue());
	        case DIVIDE:
	            return (lhs.longValue() / rhs.longValue());
	        }
	    case INT:
	    case BYTE:
	    case SHORT:
	        switch (op) {
	        case PLUS:
	            return (lhs.intValue() + rhs.intValue());
	        case MINUS:
	            return (lhs.intValue() - rhs.intValue());
	        case MULTIPLY:
	            return (lhs.intValue() * rhs.intValue());
	        case DIVIDE:
	            return (lhs.intValue() / rhs.intValue());
	        case BITXOR:
	            return (lhs.intValue() ^ rhs.intValue());
	        }
	    }
	    System.out.println("binary operation failed!");
	    return null;
	}


	// Look for a field in a class hierarchy.
	protected Field getField(Class<?> class_, String name) {
		while (class_ != null) {
			try {
				return class_.getDeclaredField(name);
			} catch (NoSuchFieldException x) {
				class_ = class_.getSuperclass();
			}
		}
		System.out.println("reflection error! field not found: " + name);
		return null;
	}

	// Simulated accesses.
	@Override
    public <T> T storeValue(T value, boolean approx, MemKind kind) {
	    countOperation("store" + kind, approx);
	    return value;
	}

	@Override
    public <T> T loadValue(T value, boolean approx, MemKind kind) {
	    countOperation("load" + kind, approx);
	    return value;
	}

	@Override
    public <T> T loadLocal(Reference<T> ref, boolean approx) {
		return loadValue(ref.value, approx, MemKind.VARIABLE);
	}

	@Override
    public <T> T loadArray(Object array, int index, boolean approx) {
		return loadValue((T) Array.get(array, index), approx, MemKind.ARRAYEL);
	}

	@Override
    public <T> T loadField(Object obj, String fieldname, boolean approx) {
		try {

			// In static context, allow client to call this method with a Class
	        // object instead of an instance.
	        Class<?> class_;
	        if (obj instanceof Class) {
	            class_ = (Class<?>) obj;
	            obj = null;
	        } else {
	            class_ = obj.getClass();
	        }
	        Field field = getField(class_, fieldname);
	        field.setAccessible(true);

	        // in = obj.fieldname;
	        T val = (T) field.get(obj);

			return loadValue(val, approx, MemKind.FIELD);

	    } catch (IllegalArgumentException x) {
	        System.out.println("reflection error!");
	        return null;
	    } catch (IllegalAccessException x) {
	        System.out.println("reflection error!");
	        return null;
	    }
	}

	@Override
    public <T> T storeLocal(Reference<T> ref, boolean approx, T rhs) {
		ref.value = storeValue(rhs, approx, MemKind.VARIABLE);
		return ref.value;
	}

	@Override
    public <T> T storeArray(Object array, int index, boolean approx, T rhs) {
		T val = storeValue(rhs, approx, MemKind.ARRAYEL);
		Array.set(array, index, val);
		return val;
	}

	@Override
    public <T> T storeField(Object obj, String fieldname, boolean approx, T rhs) {
		T val = storeValue(rhs, approx, MemKind.FIELD);

		try {
			// In static context, allow client to call this method with a Class
	        // object instead of an instance.
	        Class<?> class_;
	        if (obj instanceof Class) {
	            class_ = (Class<?>)obj;
	            obj = null;
	        } else {
	            class_ = obj.getClass();
	        }
	        Field field = getField(class_, fieldname);
	        field.setAccessible(true);

	        // obj.fieldname = val;
    	    field.set(obj, val);

	    } catch (IllegalArgumentException x) {
	        System.out.println("reflection error: illegal argument");
	        return null;
	    } catch (IllegalAccessException x) {
	        System.out.println("reflection error: illegal access");
	        return null;
	    }

		return val;
	}

	// Fancier assignments.
	@Override
    public <T extends Number> T assignopLocal(
	    Reference<T> var,
	    ArithOperator op,
	    Number rhs,
	    boolean returnOld,
	    NumberKind nk,
	    boolean approx
	) {
		T tmp = loadLocal(var, approx);
	    T res = (T) binaryOp(var.value, rhs, op, nk, approx);
	    storeLocal(var, approx, res);
	    if (returnOld)
    	    return tmp;
    	else
    	    return res;
	}

	private Number makeKind(Number num, NumberKind nk) {
	    Number converted = null;
	    switch (nk) {
	    case DOUBLE:
	    	converted = num.doubleValue();
	    	break;
	    case FLOAT:
	    	converted = num.floatValue();
	    	break;
	    case LONG:
	    	converted = num.longValue();
	    	break;
	    case INT:
	    	converted = num.intValue();
	    	break;
	    case SHORT:
	    	converted = num.shortValue();
	    	break;
	    case BYTE:
	    	converted = num.byteValue();
	    	break;
	    default:
	    	assert false;
	    }
	    return converted;
	}

	@Override
    public <T extends Number> T assignopArray(
	    Object array,
	    int index,
	    ArithOperator op,
	    Number rhs,
	    boolean returnOld,
	    NumberKind nk,
	    boolean approx
	) {
	    T tmp = (T) loadArray(array, index, approx);
	    T res = (T) binaryOp(tmp, rhs, op, nk, approx);
	    storeArray(array, index, approx, (T) makeKind(res, nk));
	    if (returnOld)
    	    return tmp;
    	else
    	    return res;
	}

	@Override
    public <T extends Number> T assignopField(
	    Object obj,
	    String fieldname,
	    ArithOperator op,
	    Number rhs,
	    boolean returnOld,
	    NumberKind nk,
	    boolean approx
	) {
		T tmp = (T) loadField(obj, fieldname, approx);
	    T res = (T) binaryOp(tmp, rhs, op, nk, approx);
	    storeField(obj, fieldname, approx, (T) makeKind(res, nk));
	    if (returnOld)
    	    return tmp;
    	else
    	    return res;
	}
}
