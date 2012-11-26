package enerj.rt;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

public class PrecisionRuntimeNoisy extends PrecisionRuntimeDefault {
	protected final String CONSTS_FILE = "enerjnoiseconsts.json";

	// Probabilities.
	protected long INVPROB_SRAM_WRITE_FAILURE = (long)Math.pow(10, 4.94);
	protected long INVPROB_SRAM_READ_UPSET = (long)Math.pow(10, 7.4);
	// FPU characteristics (mantissa bits).
	protected final int MB_FLOAT_PRECISE = 23;
	protected int MB_FLOAT_APPROX = 8;
	protected final int MB_DOUBLE_PRECISE = 52;
	protected int MB_DOUBLE_APPROX = 16;
	// DRAM storage decay.
	protected long INVPROB_DRAM_FLIP_PER_SECOND = (long)Math.pow(10, 5);
    // Operation timing errors.
    protected int TIMING_ERROR_MODE = 2;
    protected float TIMING_ERROR_PROB_PERCENT = 1.5f;

	// Indicates that the approximation should not be used;
	protected final int DISABLED = 0;

	public PrecisionRuntimeNoisy() {
		super();
		System.err.println("Initializing noisy EnerJ runtime.");

		FileReader fr = null;
		try {
			fr = new FileReader(CONSTS_FILE);
		} catch (IOException exc) {
			System.err.println("   Constants file not found; using defaults.");
		}

		if (fr != null) {
			try {
				JSONObject json = new JSONObject(new JSONTokener(fr));
				INVPROB_SRAM_WRITE_FAILURE =
					json.getLong("INVPROB_SRAM_WRITE_FAILURE");
				INVPROB_SRAM_READ_UPSET =
					json.getLong("INVPROB_SRAM_READ_UPSET");
				MB_FLOAT_APPROX = json.getInt("MB_FLOAT_APPROX");
				MB_DOUBLE_APPROX = json.getInt("MB_DOUBLE_APPROX");
				INVPROB_DRAM_FLIP_PER_SECOND =
					json.getLong("INVPROB_DRAM_FLIP_PER_SECOND");
				TIMING_ERROR_MODE = json.getInt("TIMING_ERROR_MODE");
				TIMING_ERROR_PROB_PERCENT =
                    (float)json.getDouble("TIMING_ERROR_PROB_PERCENT");
			} catch (JSONException exc) {
				System.err.println("   JSON not readable!");
			}
		}

		System.err.println("   SRAM WF: " + INVPROB_SRAM_WRITE_FAILURE);
		System.err.println("   SRAM RU: " + INVPROB_SRAM_READ_UPSET);
		System.err.println("   float bits: " + MB_FLOAT_APPROX);
		System.err.println("   double bits: " + MB_DOUBLE_APPROX);
		System.err.println("   DRAM decay: " + INVPROB_DRAM_FLIP_PER_SECOND);
        System.err.println("   timing error mode: " + TIMING_ERROR_MODE);
        System.err.println("   timing error prob: " + TIMING_ERROR_PROB_PERCENT);
	}


	// Error injection helpers.

    private int numBytes(Object value) {
        if (value instanceof Byte) return 1;
        else if (value instanceof Short) return 2;
        else if (value instanceof Integer) return 4;
        else if (value instanceof Long) return 8;
        else if (value instanceof Float) return 4;
        else if (value instanceof Double) return 8;
        else if (value instanceof Character) return 2;
        else if (value instanceof Boolean) return 1;
        else assert false;
        return 0;
    }

    private long toBits(Object value) {
		if (value instanceof Byte || value instanceof Short ||
				value instanceof Integer || value instanceof Long) {
			return ((Number)value).longValue();
		} else if (value instanceof Float) {
			return Float.floatToRawIntBits((Float)value);
		} else if (value instanceof Double) {
			return Double.doubleToRawLongBits((Double)value);
		} else if (value instanceof Character) {
			return ((Character)value).charValue();
		} else if (value instanceof Boolean) {
			if ((Boolean)value) {
				return -1;
			} else {
				return 0;
			}
		} else {
			// Non-primitive type.
            assert false;
            return 0;
		}
    }

    private Object fromBits(long bits, Object oldValue) {
		if (oldValue instanceof Byte) {
			return (byte)bits;
		} else if (oldValue instanceof Short) {
			return (short)bits;
		} else if (oldValue instanceof Integer) {
			return (int)bits;
		} else if (oldValue instanceof Long) {
			return bits;
		} else if (oldValue instanceof Float) {
			return (Float.intBitsToFloat((int)bits));
		} else if (oldValue instanceof Double) {
			return (Double.longBitsToDouble(bits));
		} else if (oldValue instanceof Character) {
			return (char)bits;
		} else if (oldValue instanceof Boolean) {
			return (bits != 0);
		} else {
			assert false;
			return null;
		}
    }

	private <T> T bitError(T value, long invProb) {
        if (!isPrimitive(value))
            return value;

		long bits = toBits(value);
		int width = numBytes(value);

		// Inject errors.
		for (int bitpos = 0; bitpos < width * 8; ++bitpos) {
			long mask = 1 << bitpos;
			if ((long)(Math.random() * invProb) == 0) {
				// Bit error!
				bits = bits ^ mask;
			}
		}

        return (T) fromBits(bits, value);
	}

	private Number narrowMantissa(Number num, NumberKind nk) {
		if (nk == NumberKind.FLOAT) {

			if (MB_FLOAT_APPROX == DISABLED)
				return num;

			int bits = Float.floatToRawIntBits(num.floatValue());
			int mask = 0;
			for (int i = 0; i < MB_FLOAT_PRECISE - MB_FLOAT_APPROX; ++i) {
				mask |= (1 << i);
			}
			return Float.intBitsToFloat(bits & ~mask);

		} else if (nk == NumberKind.DOUBLE) {

			if (MB_DOUBLE_APPROX == DISABLED)
				return num;

			long bits = Double.doubleToRawLongBits(num.doubleValue());
			long mask = 0;
			for (int i = 0; i < MB_DOUBLE_PRECISE - MB_DOUBLE_APPROX; ++i) {
				mask |= (1L << i);
			}
			return Double.longBitsToDouble(bits & ~mask);
			// bits 51 and down

		} else {
			assert false;
			return null;
		}
	}

	private boolean isPrimitive(Object o) {
		return (
			o instanceof Number ||
			o instanceof Boolean ||
			o instanceof Character
		);
	}

	private Map<String, Long> dataAges = new WeakHashMap<String, Long>();

	private void dramRefresh(String key, Object value) {
		if (isPrimitive(value)) {
			dataAges.put(key, System.currentTimeMillis());
		}
	}

	private <T> T dramAgedRead(String key, T value) {
		if (!isPrimitive(value) || INVPROB_DRAM_FLIP_PER_SECOND == DISABLED) {
			return value;
		}

		// How old is the data?
		long age;
		if (dataAges.containsKey(key)) {
			age = System.currentTimeMillis() - dataAges.get(key);
			if (age == 0) {
				return value;
			}
		} else {
			return value;
		}

		// Inject error.
		long invprob = INVPROB_DRAM_FLIP_PER_SECOND * 1000L / age;
		value = bitError(value, invprob);

		// Data is refreshed.
		dramRefresh(key, value);

		return value;
	}

	private String dramKey(Object obj, String field) {
		return System.identityHashCode(obj) + field;
	}

	private String dramKey(Object array, int index) {
		return "array" + System.identityHashCode(array) + "idx" + index;
	}

    // Timing errors for arithmetic.
    private HashMap<Class<?>, Number> lastValues = new HashMap<Class<?>, Number>();
    private Number timingError(Number num) {
        long bits;
        if (Math.random()*100 < TIMING_ERROR_PROB_PERCENT) {
            switch (TIMING_ERROR_MODE) {
            case DISABLED:
                return num;

            case 1: // Single bit flip.
                bits = toBits(num);
                int errpos = (int)(Math.random() * numBytes(num) * 8);
                bits = bits ^ (1 << errpos);
                return (Number)fromBits(bits, num);

            case 2: // Random value.
                bits = 0;
                for (int i = 0; i < numBytes(num); ++i) {
                    byte b = (byte)(Math.random() * Byte.MAX_VALUE);
                    bits |= ((long)b) << (i*8);
                    if (Math.random() < 0.5)
                        bits |= 1L << ((i+1)*8-1); // Sign bit.
                }
                return (Number)fromBits(bits, num);

            case 3: // Last value.
                if (lastValues.containsKey(num.getClass()))
                    return lastValues.get(num.getClass());
                else
                    return (Number)fromBits(0, num);

            default:
                assert false;
                return null;
            }
        } else {
            return num;
        }
    }


	// Runtime operations.

	@Override
	public Number binaryOp(Number lhs, Number rhs, ArithOperator op,
						   NumberKind nk, boolean approx) {
		// DEBUG
		/*
		if ((nk == NumberKind.DOUBLE || nk == NumberKind.FLOAT) && !approx) {
			// Why wasn't it approximate?
			Thread.dumpStack();
		}
		*/

        // Floating point width.
		if (approx && (nk == NumberKind.DOUBLE || nk == NumberKind.FLOAT)) {
			lhs = narrowMantissa(lhs, nk);
			rhs = narrowMantissa(rhs, nk);
		}

		Number res = super.binaryOp(lhs, rhs, op, nk, approx);

        // Floating point width again.
		if (approx && (nk == NumberKind.DOUBLE || nk == NumberKind.FLOAT)) {
			res = narrowMantissa(res, nk);
		}

        // Timing errors on ALU.
        if (approx/* && !(nk == NumberKind.DOUBLE || nk == NumberKind.FLOAT)*/) {
            res = timingError(res);
            if (TIMING_ERROR_MODE == 3) // Last value mode.
                lastValues.put(res.getClass(), res);
        }

		return res;
	}

	@Override
	public <T> T storeValue(T value, boolean approx, MemKind kind) {
		if (kind == MemKind.VARIABLE && approx &&
				INVPROB_SRAM_WRITE_FAILURE != DISABLED) {
			// Approximate access to local variable. Inject SRAM write
			// failures.
			value = bitError(value, INVPROB_SRAM_WRITE_FAILURE);
		}

	    T res = super.storeValue(value, approx, kind);

	    return res;
	}


	// SRAM read upsets.
	@Override
	public <T> T loadLocal(Reference<T> ref, boolean approx) {
		T res = super.loadLocal(ref, approx);
		if (approx && INVPROB_SRAM_READ_UPSET != DISABLED) {
			// Approximate read from local variable. Inject SRAM read upsets.
			res = bitError(res, INVPROB_SRAM_READ_UPSET);
			ref.value = res;
		}
		return res;
	}

	// DRAM decay.
	@Override
	public <T> T loadArray(Object array, int index, boolean approx) {
		T res = super.loadArray(array, index, approx);
		if (approx) {
			T aged = dramAgedRead(dramKey(array, index), res);
			if (aged != res) {
				res = aged;

				Array.set(array, index, aged);
			}
		}
		return res;
	}

	@Override
	public <T> T loadField(Object obj, String fieldname, boolean approx) {
		T res = super.loadField(obj, fieldname, approx);
		if (approx) {
			T aged = dramAgedRead(dramKey(obj, fieldname), res);
			if (aged != res) {
				res = aged;

				Class<?> class_;
		        if (obj instanceof Class) {
		            class_ = (Class<?>) obj;
		            obj = null;
		        } else {
		            class_ = obj.getClass();
		        }
		        Field field = getField(class_, fieldname);
		        field.setAccessible(true);
		        try {
		        	field.set(obj, res);
		        } catch (IllegalArgumentException x) {
			        System.out.println("reflection error!");
			        return null;
			    } catch (IllegalAccessException x) {
			        System.out.println("reflection error!");
			        return null;
			    }
			}
		}
		return res;
	}

	@Override
	public <T> T storeArray(Object array, int index, boolean approx, T rhs) {
		T res = super.storeArray(array, index, approx, rhs);
		dramRefresh(dramKey(array, index), res);
		return res;
	}

	@Override
	public <T> T storeField(Object obj, String fieldname, boolean approx, T rhs) {
		T res = super.storeField(obj, fieldname, approx, rhs);
		dramRefresh(dramKey(obj, fieldname), res);
		return res;
	}
}
