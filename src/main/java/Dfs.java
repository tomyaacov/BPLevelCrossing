import il.ac.bgu.cs.bp.bpjs.analysis.*;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.JsErrorViolation;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.internal.ExecutorServiceMaker;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toSet;

public class Dfs {

    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    public final static long DEFAULT_MAX_TRACE = 10000;
    public final static long DEFAULT_ITERATION_COUNT_GAP = 10000;
    private static final ProgressListener NULL_PROGRESS_LISTENER = new ProgressListener() {
        @Override public void started(Dfs vfr) {}
        @Override public void iterationCount(long count, long statesHit, Dfs vfr) {}
        @Override public void maxTraceLengthHit(ExecutionTrace aTrace, Dfs vfr) {}
        @Override public void done(Dfs vfr) {}

        @Override
        public boolean violationFound(Violation aViolation, Dfs vfr) {
            return false;
        }
    };
    private long visitedEdgeCount;
    private VisitedStateStore visited = new BThreadSnapshotVisitedStateStore();
    private long maxTraceLength = DEFAULT_MAX_TRACE;
    private final List<Node> currentPath = new ArrayList<>();
    private ProgressListener listener;
    private long iterationCountGap = DEFAULT_ITERATION_COUNT_GAP;
    private BProgram currentBProgram;
    private boolean debugMode = false;
    private final Set<ExecutionTraceInspection> inspections = new HashSet<>();
    private ArrayExecutionTrace trace;
    public ArrayList<ArrayList<BEvent>> possiblePaths = new ArrayList();

    public Dfs() {
    }

    public VerificationResult verify(BProgram aBp) throws Exception {
        if ( listener == null ) {
            listener = NULL_PROGRESS_LISTENER;
        }
        currentBProgram = aBp;
        visitedEdgeCount = 0;
        currentPath.clear();
        visited.clear();
        trace = new ArrayExecutionTrace(currentBProgram);

        // in case no verifications were specified, use the defauls set.
        if ( inspections.isEmpty() ) {
            inspections.addAll( ExecutionTraceInspections.DEFAULT_SET );
        }

        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("DfsBProgramRunner-" + INSTANCE_COUNTER.incrementAndGet());
        long start = System.currentTimeMillis();
        listener.started(this);
        Violation vio = dfsUsingStack(new Node(currentBProgram, currentBProgram.setup().start(execSvc), null), execSvc);
        long end = System.currentTimeMillis();
        execSvc.shutdown();
        listener.done(this);
        return new VerificationResult(vio, end - start, visited.getVisitedStateCount(), visitedEdgeCount);
    }

    protected Violation dfsUsingStack(Node aStartNode, ExecutorService execSvc) throws Exception {
        long iterationCount = 0;

        push(aStartNode);
        Violation v = inspectCurrentTrace();
        if ( v != null ) return v;

        while (!isPathEmpty()) {
            iterationCount++;

            if (debugMode) {
                printStatus(iterationCount, currentPath);
            }

            Node curNode = peek();

            if (pathLength() == maxTraceLength) {
                // fold stack;
                listener.maxTraceLengthHit(trace, this);
                pop();

            } else {
                try {
                    Node nextNode = getUnvisitedNextNode(curNode, execSvc);
                    if (nextNode == null) {
                        this.savePath();
                        // fold stack, retry next iteration;
                        if (isDebugMode()) {
                            System.out.println("-pop!-");
                        }
                        Node p = pop();
                        if ( p.getEventIterator().hasNext() ) {
                            throw new IllegalStateException("Still having some events to traverse: " + p.getEventIterator().next() );
                        }

                    } else {
                        // go deeper
                        if (isDebugMode()) {
                            System.out.println("-visiting: " + nextNode);
                        }
                        push(nextNode);
                        v = inspectCurrentTrace();
                        if ( v != null ) return v;
                    }
                } catch (ViolatingPathFoundException vcfe ) {
                    return vcfe.v;
                }
            }

            if ( iterationCount % iterationCountGap == 0 ) {
                listener.iterationCount(iterationCount, visited.getVisitedStateCount(), this);
            }
        }

        return null;
    }

    protected Node getUnvisitedNextNode(Node src, ExecutorService execSvc)
            throws ViolatingPathFoundException{
        while (src.getEventIterator().hasNext()) {
            final BEvent nextEvent = src.getEventIterator().next();
            try {
                Node possibleNextNode = src.getNextNode(nextEvent, execSvc);
                visitedEdgeCount++;

                BProgramSyncSnapshot pns = possibleNextNode.getSystemState();
                if (visited.isVisited(pns) ) {
                    boolean cycleFound = false;
                    for ( int idx=0; idx<currentPath.size() && !cycleFound; idx++) {
                        // Was this a cycle?
                        Node nd = currentPath.get(idx);
                        if ( pns.equals(nd.getSystemState()) ) {
                            // found an actual cycle
                            cycleFound = true;
                            trace.cycleTo(nextEvent, idx);
                            Set<Violation> res = inspections.stream().map(i->i.inspectTrace(trace))
                                    .filter(o->o.isPresent()).map(Optional::get).collect(toSet());

                            for ( Violation v : res ) {
                                if ( ! listener.violationFound(v, this)) {
                                    throw new ViolatingPathFoundException(v);
                                }
                            }
                        }
                    }
                    if ( ! cycleFound ) {
                        // revisiting a state from a different path. Quickly inspect the path and contnue.
                        trace.advance(nextEvent, pns);
                        Set<Violation> res = inspections.stream().map(i->i.inspectTrace(trace))
                                .filter(o->o.isPresent()).map(Optional::get).collect(toSet());
                        if ( res.size() > 0  ) {
                            for ( Violation v : res ) {
                                if ( ! listener.violationFound(v, this) ) {
                                    throw new ViolatingPathFoundException(v);
                                }
                            }
                        }
                        trace.pop();
                    }
                } else {
                    // advance to this newly discovered node
                    return possibleNextNode;
                }
            } catch ( BPjsRuntimeException bprte ) {
                trace.advance(nextEvent, null);
                Violation jsev = new JsErrorViolation(trace, bprte);
                if ( ! listener.violationFound(jsev, this) ) {
                    throw new ViolatingPathFoundException(jsev);
                }
                trace.pop();
            }
        }
        return null;
    }

    public void setMaxTraceLength(long maxTraceLength) {
        this.maxTraceLength = maxTraceLength;
    }

    public long getMaxTraceLength() {
        return maxTraceLength;
    }

    public void setVisitedStateStore(VisitedStateStore aVisitedStateStore) {
        visited = aVisitedStateStore;
    }

    public VisitedStateStore getVisitedStateStore() {
        return visited;
    }

    public void setProgressListener(ProgressListener pl) {
        listener = (pl != null) ? pl : NULL_PROGRESS_LISTENER;
    }

    public void setIterationCountGap(long iterationCountGap) {
        this.iterationCountGap = iterationCountGap;
    }

    public long getIterationCountGap() {
        return iterationCountGap;
    }

    public BProgram getCurrentBProgram() {
        return currentBProgram;
    }

    void printStatus(long iteration, List<Node> path) {
        System.out.println("Iteration " + iteration);
        System.out.println("  visited: " + visited.getVisitedStateCount());
        path.forEach(n -> System.out.println("  " + n.getLastEvent()));
    }

    private Violation inspectCurrentTrace() {
        Set<Violation> res = inspections.stream()
                .map(v->v.inspectTrace(trace))
                .filter(o->o.isPresent()).map(Optional::get)
                .collect(toSet());
        if ( res.size() > 0  ) {
            for ( Violation v : res ) {
                if ( ! listener.violationFound(v, this) ) {
                    return v;
                }
            }
            if (isDebugMode()) {
                System.out.println("-pop! (violation)-");
            }
            pop();
        }
        return null;
    }

    private void push(Node n) {
        visited.store(n.getSystemState());
        currentPath.add(n);
        if ( trace.getStateCount() == 0 ) {
            trace.push( n.getSystemState() );
        } else {
            trace.advance(n.getLastEvent(), n.getSystemState());
        }
    }

    private Node pop() {
        Node popped = currentPath.remove(currentPath.size() - 1);
        trace.pop();
        return popped;
    }

    private int pathLength() {
        return currentPath.size();
    }

    private boolean isPathEmpty() {
        return pathLength() == 0;
    }

    private Node peek() {
        return isPathEmpty() ? null : currentPath.get(currentPath.size() - 1);
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void addInspection( ExecutionTraceInspection ins ) {
        inspections.add(ins);
    }

    public Set<ExecutionTraceInspection> getInspections() {
        return inspections;
    }

    public boolean removeInspection( ExecutionTraceInspection ins ) {
        return inspections.remove(ins);
    }

    public void savePath(){
        ArrayList<BEvent> current = currentPath.stream().map(n -> n.getLastEvent()).collect(Collectors.toCollection(ArrayList::new));
        current.remove(0);
        possiblePaths.add(current);
    }


    private static class ViolatingPathFoundException extends Exception {
        final Violation v;

        public ViolatingPathFoundException(Violation v) {
            this.v = v;
        }
    }


    public static interface ProgressListener {

        void started(Dfs vfr);

        void iterationCount(long count, long statesHit, Dfs vfr);

        void maxTraceLengthHit(ExecutionTrace aTrace, Dfs vfr);

        boolean violationFound( Violation aViolation, Dfs vfr );

        void done(Dfs vfr);
    }

}
