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

    List<List<PathNode>> paths = new ArrayList<>();
    private static class PathNode{
        public final BProgramSyncSnapshot bpss;
        public final BEvent event;

        public boolean isCycleStart = false;

        public PathNode(BProgramSyncSnapshot bpss, BEvent event) {
            this.bpss = bpss;
            this.event = event;
        }

        public PathNode(PathNode other) {
            this.bpss = other.bpss;
                     this.event = other.event;
        }

        @Override
        public int hashCode() {
            return bpss.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof PathNode) {
                return bpss.equals(((PathNode)obj).bpss);
            }
            return false;
        }

        @Override
        public String toString() {
            return (isCycleStart? "{ " : "") + event.getName();
        }
    }

    public void allPaths(ArrayList<PathNode> path, BProgramSyncSnapshot node, ExecutorService exSvc, int depth) throws InterruptedException {
        int index = path.indexOf(new PathNode(node,null));
        if (index >= 0 || depth > 20000) {
            path.get(index).isCycleStart = true;
            paths.add(path);
            //System.out.println(path);
            return;
        }
        Set<BEvent> events = node.getBProgram().getEventSelectionStrategy().selectableEvents(node);
        //path.add(new PathNode(node.bpss, ));
        for (BEvent e : events) {
            BProgramSyncSnapshot nodeClone = BProgramSyncSnapshotCloner.clone(node);
            BProgramSyncSnapshot next = nodeClone.triggerEvent(e, exSvc, new ArrayList<>());
            ArrayList<PathNode> newPath = path.stream().map(n -> new PathNode(n)).collect(Collectors.toCollection(ArrayList::new));
            //ArrayList<PathNode> newPath = new ArrayList<>(path);
            newPath.add(new PathNode(node, e));
            allPaths(newPath, next, exSvc, depth + 1);
        }

    }

    public String printPath(List<PathNode> path){
        return path.stream()
                .map(n -> n.toString())
                .collect(Collectors.joining(" ")) + " }";
    }

    public String run(BProgram bProgram, String output) throws InterruptedException, IOException {
        ExecutorService exSvc = ExecutorServiceMaker.makeWithName("1");
        BProgramSyncSnapshot start = bProgram.setup().start(exSvc);
        allPaths(new ArrayList<PathNode>(), start, exSvc, 1);

        List<String> eventsToRemove = Arrays.asList("ClosingRequest", "OpeningRequest", "KeepDown");
        File targetFile = new File(output);
        targetFile.createNewFile();

        Writer targetFileWriter = new FileWriter(targetFile);
        List<String> allPaths = paths.stream().map(p -> printPath(p)).collect(Collectors.toList());
        allPaths = allPaths.stream().map(p -> p.replaceAll(eventsToRemove.stream().collect(Collectors.joining(" |")), "")).collect(Collectors.toList());
        Set<String> set = new LinkedHashSet<>();
        set.addAll(allPaths);
        allPaths.clear();
        allPaths.addAll(set);
        targetFileWriter.write(String.join(" | ", allPaths));
        targetFileWriter.close();
        exSvc.shutdown();
        return allPaths.toString();
    }



    public static void main(String[] args) throws Exception {
        SimpleEventSelectionStrategy ess = new SimpleEventSelectionStrategy();
        PathsFinder pf = new PathsFinder();
        String pnRuns = pf.run(new ResourceBProgram("lc_pn_check.js", ess), "lc_pn_check_paths_1.csv");
        System.out.println(pnRuns);

    }

}

