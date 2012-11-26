package enerj.lang;

public class Endorsements {
	private Endorsements() {
		throw new AssertionError("Class Endorsements shouldn't be instantiated!");
	}
	/*
    @SuppressWarnings("precision")
    public static <T extends @Top Object> @Precise T endorse(T ref) {
    	// TODO: runtime check possible?
        return ref;
    }*/

    @SuppressWarnings("precision")
    public static @Precise int endorse(@Top int param) {
    	// TODO: runtime check possible?
        return param;
    }

    @SuppressWarnings("precision")
    public static @Precise float endorse(@Top float param) {
    	// TODO: runtime check possible?
        return param;
    }

    @SuppressWarnings("precision")
    public static @Precise boolean endorse(@Top boolean param) {
    	// TODO: runtime check possible?
        return param;
    }

    @SuppressWarnings("precision")
    public static @Precise byte endorse(@Top byte param) {
    	// TODO: runtime check possible?
        return param;
    }

    @SuppressWarnings("precision")
    public static @Precise short endorse(@Top short param) {
    	// TODO: runtime check possible?
        return param;
    }

    @SuppressWarnings("precision")
    public static @Precise long endorse(@Top long param) {
    	// TODO: runtime check possible?
        return param;
    }

    @SuppressWarnings("precision")
    public static @Precise char endorse(@Top char param) {
    	// TODO: runtime check possible?
        return param;
    }

    @SuppressWarnings("precision")
    public static @Precise double endorse(@Top double param) {
    	// TODO: runtime check possible?
        return param;
    }
}
