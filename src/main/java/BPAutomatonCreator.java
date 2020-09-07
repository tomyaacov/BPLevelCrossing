import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;


public class BPAutomatonCreator {
    public static void main(final String[] args) throws Exception {
        SimpleEventSelectionStrategy ess = new SimpleEventSelectionStrategy(1);
        BProgram bProgram = new ResourceBProgram("lc_pn_check.js", ess);
        Dfs vrf = new Dfs();
        vrf.setProgressListener(new PrintDfsListener());  // add a listener to print progress
        VerificationResult res = vrf.verify(bProgram);
        vrf.automaton.writeAutomaton("lc_pn_check_2.xml");
        System.out.println(res.getScannedStatesCount());


    }
}
