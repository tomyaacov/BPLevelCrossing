import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.ResourceBProgram;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PathsFinder {

    StringBuilder toCsv = new StringBuilder("");

    public void allPaths(List<String> path, BProgramSyncSnapshot node, ExecutorService exSvc, int depth) throws InterruptedException {
        if (path.contains(Integer.toString(bpssHashCode(node))) || depth > 20){
            printPath(path);
            //System.out.println(path);
            return;
        }
        Set<BEvent> events = node.getBProgram().getEventSelectionStrategy().selectableEvents(node);
        path.add(Integer.toString(bpssHashCode(node)));
        for (BEvent e : events){
            BProgramSyncSnapshot nodeClone = BProgramSyncSnapshotCloner.clone(node);
            BProgramSyncSnapshot bpss = nodeClone.triggerEvent(e, exSvc, new ArrayList<>());
            ArrayList<String> newPath = new ArrayList(path);
            newPath.add(e.getName());
            allPaths(newPath, bpss, exSvc, depth + 1);
        }

    }

    public void printPath(List<String> path){
        toCsv.append(IntStream.range(0, path.size())
                .filter(n -> n % 2 == 1)
                .mapToObj(path::get)
                .collect(Collectors.joining(",")));
//        toCsv.append(IntStream.range(0, path.size())
//                .mapToObj(path::get)
//                .collect(Collectors.joining(",")));
        toCsv.append("\n");
    }

    public String run(BProgram bProgram, String output) throws InterruptedException, IOException {
        ExecutorService exSvc = ExecutorServiceMaker.makeWithName("1");
        BProgramSyncSnapshot start = bProgram.setup().start(exSvc);
        List<String> path = new ArrayList<String>();
        allPaths(new ArrayList<String>(), start, exSvc, 1);
        File targetFile = new File(output);
        targetFile.createNewFile();

        Writer targetFileWriter = new FileWriter(targetFile);
        targetFileWriter.write(toCsv.toString());
        targetFileWriter.close();
        exSvc.shutdown();
        return toCsv.toString();
    }

    public int bpssHashCode(BProgramSyncSnapshot bpss){
        return bpss.getStatements().hashCode();
    }

    public static void main(String[] args) throws Exception {
        SimpleEventSelectionStrategy ess = new SimpleEventSelectionStrategy();
        PathsFinder pf = new PathsFinder();
        String pnRuns = pf.run(new ResourceBProgram("lc_pn_check.js", ess), "lc_pn_check_paths_2.csv");
        System.out.println(pnRuns);

    }

}

