import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

public class BPRunner {
    public static void main(final String[] args) throws Exception {
        SimpleEventSelectionStrategy ess = new SimpleEventSelectionStrategy(4);
        BProgram bProgram = new ResourceBProgram("lc_bp_v4.js", ess);
        BProgramRunner bProgramRunner = new BProgramRunner(bProgram);
        bProgramRunner.addListener(new PrintBProgramRunnerListener());
        bProgram.setWaitForExternalEvents(false);
        bProgramRunner.run();
    }
}
