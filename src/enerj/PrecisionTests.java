package enerj;
import java.io.File;
import java.util.Collection;

import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runners.Parameterized.Parameters;

import tests.ParameterizedCheckerTest;

/**
 * JUnit tests for the precision checker.
 */
public class PrecisionTests {
    /* The class name of the Checker to use.
     * Careful, class CheckerTest has also a field by this name.
     * Set this field in a subclass to a different checker, e.g. see GUTITests.
     */
    protected static String checkerName = "enerj.PrecisionChecker";

    public static void main(String[] args) {
        // org.junit.runner.JUnitCore.main("GUT.GUTTests");
        org.junit.runner.JUnitCore jc = new org.junit.runner.JUnitCore();
        Result run = jc.run(PrecisionTestsStrict.class);

        if( run.wasSuccessful() ) {
            System.out.println("Run was successful with " + run.getRunCount() + " test(s)!");
        } else {
            System.out.println("Run had " + run.getFailureCount() + " failure(s) out of " +
                    run.getRunCount() + " run(s)!");

            for( Failure f : run.getFailures() ) {
                System.out.println(f.toString());
            }

        }
    }


    public static class PrecisionTestsStrict extends ParameterizedCheckerTest {
		public PrecisionTestsStrict(File testFile) {
			super(testFile, PrecisionTests.checkerName, "enerj", "-Anomsgtext"); // , "-Ashowchecks");
		}
		@Parameters
		public static Collection<Object[]> data() {
			return testFiles("strict");
		}
	}

    public static class PrecisionTestsStrictStatic extends ParameterizedCheckerTest {
		public PrecisionTestsStrictStatic(File testFile) {
			super(testFile, PrecisionTests.checkerName, "enerj", "-Anomsgtext", "-Alint=mbstatic"); // , "-Ashowchecks");
		}
		@Parameters
		public static Collection<Object[]> data() {
			return testFiles("strict/static");
		}
	}

    public static class PrecisionTestsStrictDynamic extends ParameterizedCheckerTest {
		public PrecisionTestsStrictDynamic(File testFile) {
			super(testFile, PrecisionTests.checkerName, "enerj", "-Anomsgtext", "-Alint=mbdynamic"); // , "-Ashowchecks");
		}
		@Parameters
		public static Collection<Object[]> data() {
			return testFiles("strict/dynamic");
		}
	}


    public static class PrecisionTestsRelaxed extends ParameterizedCheckerTest {
		public PrecisionTestsRelaxed(File testFile) {
			super(testFile, PrecisionTests.checkerName, "enerj", "-Anomsgtext", "-Alint=strelaxed"); // , "-Ashowchecks");
		}
		@Parameters
		public static Collection<Object[]> data() {
			return testFiles("relaxed");
		}
	}
}