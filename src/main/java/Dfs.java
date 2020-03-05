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

public class Dfs {

    private static final AtomicInteger INSTANCE_COUNTER = new AtomicInteger();
    public static final long DEFAULT_MAX_TRACE = 1000L;
    public static final long DEFAULT_ITERATION_COUNT_GAP = 1000L;
    private static final Dfs.ProgressListener NULL_PROGRESS_LISTENER = new Dfs.ProgressListener() {
        public void started(Dfs vfr) {
        }

        public void iterationCount(long count, long statesHit, Dfs vfr) {
        }

        public void maxTraceLengthHit(ExecutionTrace aTrace, Dfs vfr) {
        }

        public void done(Dfs vfr) {
        }

        public boolean violationFound(Violation aViolation, Dfs vfr) {
            return false;
        }
    };
    private long visitedEdgeCount;
    private VisitedStateStore visited = new BThreadSnapshotVisitedStateStore();
    private long maxTraceLength = 1000L;
    private final List<Node> currentPath = new ArrayList();
    private Dfs.ProgressListener listener;
    private long iterationCountGap = 1000L;
    private BProgram currentBProgram;
    private boolean debugMode = false;
    private final Set<ExecutionTraceInspection> inspections = new HashSet();
    private ArrayExecutionTrace trace;
    public ArrayList<ArrayList<BEvent>> possiblePaths = new ArrayList();

    public Dfs() {
    }

    public VerificationResult verify(BProgram aBp) throws Exception {
        if (this.listener == null) {
            this.listener = NULL_PROGRESS_LISTENER;
        }

        this.currentBProgram = aBp;
        this.visitedEdgeCount = 0L;
        this.currentPath.clear();
        this.visited.clear();
        this.trace = new ArrayExecutionTrace(this.currentBProgram);
        if (this.inspections.isEmpty()) {
            this.inspections.addAll(ExecutionTraceInspections.DEFAULT_SET);
        }

        ExecutorService execSvc = ExecutorServiceMaker.makeWithName("DfsBProgramRunner-" + INSTANCE_COUNTER.incrementAndGet());
        long start = System.currentTimeMillis();
        this.listener.started(this);
        Violation vio = this.dfsUsingStack(new Node(this.currentBProgram, this.currentBProgram.setup().start(execSvc), (BEvent)null), execSvc);
        long end = System.currentTimeMillis();
        execSvc.shutdown();
        this.listener.done(this);
        return new VerificationResult(vio, end - start, this.visited.getVisitedStateCount(), this.visitedEdgeCount);
    }

    protected Violation dfsUsingStack(Node aStartNode, ExecutorService execSvc) throws Exception {
        long iterationCount = 0L;
        this.push(aStartNode);
        Violation v = this.inspectCurrentTrace();
        if (v != null) {
            return v;
        } else {
            while(!this.isPathEmpty()) {
                ++iterationCount;
                if (this.debugMode) {
                    this.printStatus(iterationCount, this.currentPath);
                }

                Node curNode = this.peek();
                if ((long)this.pathLength() == this.maxTraceLength) {
                    this.listener.maxTraceLengthHit(this.trace, this);
                    this.pop();
                } else {
                    try {
                        Node nextNode = this.getUnvisitedNextNode(curNode, execSvc);
                        if (nextNode == null) {
                            savePath();
                            if (this.isDebugMode()) {
                                System.out.println("-pop!-");
                            }

                            Node p = this.pop();
                            if (p.getEventIterator().hasNext()) {
                                throw new IllegalStateException("Still having some events to traverse: " + p.getEventIterator().next());
                            }
                        } else {
                            if (this.isDebugMode()) {
                                System.out.println("-visiting: " + nextNode);
                            }

                            this.push(nextNode);
                            v = this.inspectCurrentTrace();
                            if (v != null) {
                                return v;
                            }
                        }
                    } catch (Dfs.ViolatingPathFoundException var9) {
                        return var9.v;
                    }
                }

                if (iterationCount % this.iterationCountGap == 0L) {
                    this.listener.iterationCount(iterationCount, this.visited.getVisitedStateCount(), this);
                }
            }

            return null;
        }
    }

    protected Node getUnvisitedNextNode(Node src, ExecutorService execSvc) throws Dfs.ViolatingPathFoundException {
        while(src.getEventIterator().hasNext()) {
            BEvent nextEvent = (BEvent)src.getEventIterator().next();

            try {
                Node possibleNextNode = src.getNextNode(nextEvent, execSvc);
                ++this.visitedEdgeCount;
                BProgramSyncSnapshot pns = possibleNextNode.getSystemState();
                if (!this.visited.isVisited(pns)) {
                    return possibleNextNode;
                }

                boolean cycleFound = false;

                for(int idx = 0; idx < this.currentPath.size() && !cycleFound; ++idx) {
                    Node nd = (Node)this.currentPath.get(idx);
                    if (pns.equals(nd.getSystemState())) {
                        cycleFound = true;
                        this.trace.cycleTo(nextEvent, idx);
                        Set<Violation> res = (Set)this.inspections.stream().map((i) -> {
                            return i.inspectTrace(this.trace);
                        }).filter((o) -> {
                            return o.isPresent();
                        }).map(Optional::get).collect(Collectors.toSet());
                        Iterator var10 = res.iterator();

                        while(var10.hasNext()) {
                            Violation v = (Violation)var10.next();
                            if (!this.listener.violationFound(v, this)) {
                                throw new Dfs.ViolatingPathFoundException(v);
                            }
                        }
                    }
                }

                if (!cycleFound) {
                    this.trace.advance(nextEvent, pns);
                    Set<Violation> res = (Set)this.inspections.stream().map((i) -> {
                        return i.inspectTrace(this.trace);
                    }).filter((o) -> {
                        return o.isPresent();
                    }).map(Optional::get).collect(Collectors.toSet());
                    if (res.size() > 0) {
                        Iterator var15 = res.iterator();

                        while(var15.hasNext()) {
                            Violation v = (Violation)var15.next();
                            if (!this.listener.violationFound(v, this)) {
                                throw new Dfs.ViolatingPathFoundException(v);
                            }
                        }
                    }

                    this.trace.pop();
                }
            } catch (BPjsRuntimeException var12) {
                this.trace.advance(nextEvent, (BProgramSyncSnapshot)null);
                Violation jsev = new JsErrorViolation(this.trace, var12);
                if (!this.listener.violationFound(jsev, this)) {
                    throw new Dfs.ViolatingPathFoundException(jsev);
                }

                this.trace.pop();
            }
        }

        return null;
    }

    public void setMaxTraceLength(long maxTraceLength) {
        this.maxTraceLength = maxTraceLength;
    }

    public long getMaxTraceLength() {
        return this.maxTraceLength;
    }

    public void setVisitedStateStore(VisitedStateStore aVisitedStateStore) {
        this.visited = aVisitedStateStore;
    }

    public VisitedStateStore getVisitedStateStore() {
        return this.visited;
    }

    public void setProgressListener(Dfs.ProgressListener pl) {
        this.listener = pl != null ? pl : NULL_PROGRESS_LISTENER;
    }

    public void setIterationCountGap(long iterationCountGap) {
        this.iterationCountGap = iterationCountGap;
    }

    public long getIterationCountGap() {
        return this.iterationCountGap;
    }

    public BProgram getCurrentBProgram() {
        return this.currentBProgram;
    }

    void printStatus(long iteration, List<Node> path) {
        System.out.println("Iteration " + iteration);
        System.out.println("  visited: " + this.visited.getVisitedStateCount());
        path.forEach((n) -> {
            System.out.println("  " + n.getLastEvent());
        });
    }

    private Violation inspectCurrentTrace() {
        Set<Violation> res = (Set)this.inspections.stream().map((vx) -> {
            return vx.inspectTrace(this.trace);
        }).filter((o) -> {
            return o.isPresent();
        }).map(Optional::get).collect(Collectors.toSet());
        if (res.size() > 0) {
            Iterator var2 = res.iterator();

            while(var2.hasNext()) {
                Violation v = (Violation)var2.next();
                if (!this.listener.violationFound(v, this)) {
                    return v;
                }
            }

            if (this.isDebugMode()) {
                System.out.println("-pop! (violation)-");
            }

            this.pop();
        }

        return null;
    }

    private void push(Node n) {
        this.visited.store(n.getSystemState());
        this.currentPath.add(n);
        if (this.trace.getStateCount() == 0) {
            this.trace.push(n.getSystemState());
        } else {
            this.trace.advance(n.getLastEvent(), n.getSystemState());
        }

    }

    private Node pop() {
        Node popped = (Node)this.currentPath.remove(this.currentPath.size() - 1);
        this.trace.pop();
        return popped;
    }

    private int pathLength() {
        return this.currentPath.size();
    }

    private boolean isPathEmpty() {
        return this.pathLength() == 0;
    }

    private Node peek() {
        return this.isPathEmpty() ? null : (Node)this.currentPath.get(this.currentPath.size() - 1);
    }

    public boolean isDebugMode() {
        return this.debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public void addInspection(ExecutionTraceInspection ins) {
        this.inspections.add(ins);
    }

    public Set<ExecutionTraceInspection> getInspections() {
        return this.inspections;
    }

    public boolean removeInspection(ExecutionTraceInspection ins) {
        return this.inspections.remove(ins);
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

    public interface ProgressListener {
        void started(Dfs var1);

        void iterationCount(long var1, long var3, Dfs var5);

        void maxTraceLengthHit(ExecutionTrace var1, Dfs var2);

        boolean violationFound(Violation var1, Dfs var2);

        void done(Dfs var1);
    }

}
