import il.ac.bgu.cs.bp.bpjs.analysis.VerificationResult;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

import java.util.*;
import java.util.stream.Collectors;

public class ModelsCompareRunner {

    public static void main(final String[] args) throws Exception {
        SimpleEventSelectionStrategy ess = new SimpleEventSelectionStrategy();
        List<String> eventsToRemove = Arrays.asList("ClosingRequest()", "OpeningRequest()", "KeepDown()");
        PathsFinder pf = new PathsFinder();
        String pnRuns = pf.run(new ResourceBProgram("lc_pn_check.js", ess), "lc_pn_check_paths_1.csv");
        pf = new PathsFinder();
        String bpRuns = pf.run(new ResourceBProgram("lc_bp_v1.js", ess), "lc_bp_v1_paths_1.csv");
        for (String e : eventsToRemove){
            pnRuns = pnRuns.replace("," + e, "");
        }
        String[] pnRunsArr = pnRuns.split("\n",0);
        List<String> bpRunsArr = Arrays.asList(bpRuns.split("\n",0));
        //Create set from array elements
        LinkedHashSet<String> linkedHashSet = new LinkedHashSet<>( Arrays.asList(pnRunsArr) );

        //Get back the array without duplicates
        List<String> pnRunsArrUnique = new LinkedList<>();
        pnRunsArrUnique.addAll(0,linkedHashSet);

        List<String> onlyPNRuns = pnRunsArrUnique.stream().filter(r -> !bpRunsArr.contains(r)).collect(Collectors.toList());
        List<String> onlyBPRuns = bpRunsArr.stream().filter(r -> !pnRunsArrUnique.contains(r)).collect(Collectors.toList());
        List<String> bothRuns = new LinkedList<>();

        if (onlyBPRuns.size() == 0 && onlyPNRuns.size() == 0){
            System.out.println("Models are identical");
            System.out.println("Models possible runs:");
            System.out.println(bpRunsArr);
        } else {
            System.out.println("Models not equal");
            System.out.println("PN unique runs:");
            System.out.println(onlyPNRuns);
            System.out.println("BP unique runs:");
            System.out.println(onlyBPRuns);
        }
    }
}
