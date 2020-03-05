import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;

import java.io.PrintStream;

public class PrintDfsListener implements Dfs.ProgressListener {

    private final PrintStream out;

    public PrintDfsListener(PrintStream out) {
        this.out = out;
    }

    public PrintDfsListener() {
        this(System.out);
    }

    public void started(Dfs v) {
        this.out.println("/v/ verification of '" + v.getCurrentBProgram().getName() + "' started");
    }

    public void iterationCount(long count, long statesHit, Dfs v) {
        this.out.println("/v/ " + v.getCurrentBProgram().getName() + ": iterations: " + count + " statesHit: " + statesHit);
    }

    public void maxTraceLengthHit(ExecutionTrace trace, Dfs v) {
        this.out.println("/v/ " + v.getCurrentBProgram().getName() + ": hit max trace length.");
    }

    public void done(Dfs v) {
        this.out.println("/v/ verification of " + v.getCurrentBProgram().getName() + " done");
    }

    public boolean violationFound(Violation aViolation, Dfs vfr) {
        this.out.println("/v/ Violation found: " + aViolation.decsribe());
        return false;
    }
}
