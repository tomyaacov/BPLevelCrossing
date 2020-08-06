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





//
//        ess = new ModelComparingESS(4);
//        bProgram = new ResourceBProgram(Arrays.asList("test.js", "lc_pn_faults.js", "utils.js"),"joint model", ess);
//
//
//        vrf = new Dfs();
//        vrf.setDebugMode(false);
//        vrf.setProgressListener(new PrintDfsListener());  // add a listener to print progress
//        res = vrf.verify(bProgram);
//        System.out.println(res.getScannedStatesCount());
//
//        System.out.println(res.isViolationFound());  // true iff a counter example was found
//        if(res.isViolationFound()) {
//            res.getViolation().get().getCounterExampleTrace().getNodes().forEach(n -> System.out.println(n.getEvent()));      // an Optional<Violation>
//            //System.out.println(res.printBids());
//        }
//
//        SimpleEventSelectionStrategy ess = new SimpleEventSelectionStrategy(1);
//        BProgram bProgram = new ResourceBProgram("level_crossing_faults_1.js", ess);
//        BProgramRunner bProgramRunner = new BProgramRunner(bProgram);
//        bProgramRunner.addListener(new PrintBProgramRunnerListener());
//        bProgram.setWaitForExternalEvents(false);
//        bProgramRunner.run();


        //res.getViolation().ifPresent( v -> v.getCounterExampleTrace() );

        SimpleEventSelectionStrategy ess = new SimpleEventSelectionStrategy(1);
        BProgram bProgram = new ResourceBProgram("lc_pn_check_before.js", ess);
        Dfs vrf = new Dfs();
//        vrf.setDebugMode(true);
        vrf.setProgressListener(new PrintDfsListener());  // add a listener to print progress
        VerificationResult res = vrf.verify(bProgram);
        System.out.println(res.getScannedStatesCount());

        //level_crossing_faults_1.js
        //n=1,12 n=2,90 n=3,648 n=4,? n=5,?


//        SimpleEventSelectionStrategy ess = new SimpleEventSelectionStrategy(4);
//        BProgram bProgram = new ResourceBProgram("level_crossing_faults_1.js", ess);
//        BProgramRunner bProgramRunner = new BProgramRunner(bProgram);
//        bProgramRunner.addListener(new PrintBProgramRunnerListener());
//        bProgram.setWaitForExternalEvents(false);
//        bProgramRunner.run();
//        Dfs vrf = new Dfs();
//        vrf.setDebugMode(false);
//        vrf.setProgressListener(new PrintDfsListener());  // add a listener to print progress
//        VerificationResult res = vrf.verify(bProgram);
//        //printPossiblePaths(vrf.possiblePaths);
//        ArrayList<ArrayList<BEvent>> possiblePaths = removeSubPath(vrf.possiblePaths);
//        printPossiblePaths(possiblePaths);// this might take a while

//        ess = new SimpleEventSelectionStrategyFilter(new SimpleEventSelectionStrategy(2));
//        bProgram = new ResourceBProgram("lc_pn.js", ess);
//        vrf = new Dfs();
//        vrf.setDebugMode(false);
//        vrf.setProgressListener(new PrintDfsListener());  // add a listener to print progress
//        res = vrf.verify(bProgram);
//        cleanHelperEvents(vrf.possiblePaths);
//        possiblePaths = removeSubPath(vrf.possiblePaths);
//        printPossiblePaths(possiblePaths);// this might take a while


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
