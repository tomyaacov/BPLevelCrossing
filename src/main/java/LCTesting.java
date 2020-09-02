import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.execution.BProgramRunner;
import il.ac.bgu.cs.bp.bpjs.execution.listeners.PrintBProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LCTesting {
    public static void main(final String[] args) throws Exception {
        SimpleEventSelectionStrategy ess = new SimpleEventSelectionStrategy(1);
        BProgram bProgram = new ResourceBProgram("lc_bp_v3.js", ess);
        Dfs vrf = new Dfs();
        vrf.setProgressListener(new PrintDfsListener());  // add a listener to print progress
        VerificationResult res = vrf.verify(bProgram);
        vrf.automaton.writeAutomaton("lc_bp_v3.xml");
        System.out.println(res.getScannedStatesCount());


    }

    public static void cleanHelperEvents(ArrayList<ArrayList<BEvent>> possiblePaths){
        List<String> helperEvents = Arrays.asList("ClosingRequest", "OpeningRequest", "KeepDown");
        possiblePaths.forEach(path -> path.removeIf(e -> helperEvents.contains(e.name)));
    }

    public static ArrayList<ArrayList<BEvent>> removeSubPath(ArrayList<ArrayList<BEvent>> possiblePaths){
        ArrayList<ArrayList<BEvent>> possiblePathsCopy = new ArrayList<>();
        ArrayList<BEvent> pathCopy;
        for(ArrayList<BEvent> path1:possiblePaths) {
            pathCopy = new ArrayList<>();
            for (BEvent event : path1) {
                pathCopy.add(new BEvent(event.getName()));
            }
            possiblePathsCopy.add(pathCopy);
        }

        for(ArrayList<BEvent> path1:possiblePaths){
            for(ArrayList<BEvent> path2:possiblePaths){
                if(path1.equals(path2)){
                    continue;
                }
                for (int i=0; i <= path1.size(); i++){
                    if (i == path1.size()){
                        possiblePathsCopy.remove(path1);
                        break;
                    }
                    if (i >= path2.size()) {
                        break;
                    }
                    if (!path1.get(i).equals(path2.get(i))){
                        break;
                    }
                }
            }
        }
        return possiblePathsCopy;
    }

    public static void printPossiblePaths(ArrayList<ArrayList<BEvent>> possiblePaths){
        for(ArrayList<BEvent> path:possiblePaths){
            for(BEvent e:path){
                System.out.print(e.getName() + ",");
            }
            System.out.println();
        }
    }


}
