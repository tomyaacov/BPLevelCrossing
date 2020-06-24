import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelsCompareRunner {

    public static void main(final String[] args) throws Exception {

        ModelComparingESS ess = new ModelComparingESS(1);
        BProgram bProgram = new ResourceBProgram(Arrays.asList("level_crossing_assistant.js", "lc_pn_check.js", "utils.js"),"joint model", ess);

        Dfs vrf = new Dfs();
        vrf.setDebugMode(false);
        vrf.setProgressListener(new PrintDfsListener());  // add a listener to print progress
        VerificationResult res = vrf.verify(bProgram);
        System.out.println(res.getScannedStatesCount());

        System.out.println(res.isViolationFound());  // true iff a counter example was found
        if(res.isViolationFound()) {
            res.getViolation().get().getCounterExampleTrace().getNodes().forEach(n -> System.out.println(n.getEvent()));      // an Optional<Violation>
        }
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
