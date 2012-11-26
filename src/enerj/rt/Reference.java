package enerj.rt;

import java.lang.ref.PhantomReference;

import enerj.PrecisionChecker;

public class Reference<T> {
    public T value;

    public boolean approx;

    public boolean primitive; // Did we box a primitive type?

    public PhantomReference<Object> phantom;

    public Reference(T value, boolean approx, boolean primitive) {
        this.value = value;
        this.approx = approx;
        this.primitive = primitive;
        int[] sizes = PrecisionChecker.referenceSizes(this);
        phantom = PrecisionRuntimeRoot.impl.setApproximate(
            this, approx, false, sizes[0], sizes[1]
        );
    }

    public void destroy() {
        PrecisionRuntimeRoot.impl.endLifetime(phantom);
    }
}
