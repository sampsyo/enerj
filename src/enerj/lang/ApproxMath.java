package enerj.lang;

public class ApproxMath {
    public static @Approx float min(@Approx float a, @Approx float b) {
        @Precise float pa = Endorsements.endorse(a);
        @Precise float pb = Endorsements.endorse(b);
        return Math.min(pa, pb);
    }
    
    public static @Approx int min(@Approx int a, @Approx int b) {
        return Math.min(Endorsements.endorse(a), Endorsements.endorse(b));
    }
    
    public static @Approx int max(@Approx int a, @Approx int b) {
        return Math.max(Endorsements.endorse(a), Endorsements.endorse(b));
    }

    public static @Approx float abs(@Approx float num) {
        @Precise float pnum = Endorsements.endorse(num);
        return Math.abs(pnum);
    }
    
    public static @Approx int abs(@Approx int num) {
        return Math.abs(Endorsements.endorse(num));
    }
    
    public static @Approx double abs(@Approx double num) {
        return Math.abs(Endorsements.endorse(num));
    }

    public static @Approx boolean isNaN(@Approx float num) {
        @Precise float pnum = Endorsements.endorse(num);
        return Float.isNaN(pnum);
    }
    
    public static @Approx double sqrt(@Approx double num) {
    	return Math.sqrt(Endorsements.endorse(num));
    }
}
