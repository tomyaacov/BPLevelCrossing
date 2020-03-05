import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class Node {
    private final BProgramSyncSnapshot systemState;
    private final BProgram bp;
    private final Set<BEvent> selectableEvents;
    private final BEvent lastEvent;
    private final Iterator<BEvent> iterator;

    /** @deprecated */
    public static Node getInitialNode(BProgram bp, ExecutorService exSvc) throws Exception {
        BProgramSyncSnapshot seed = bp.setup().start(exSvc);
        return new Node(bp, seed, (BEvent)null);
    }

    protected Node(BProgram bp, BProgramSyncSnapshot systemState, BEvent e) {
        this.bp = bp;
        this.systemState = systemState;
        this.lastEvent = e;
        if (bp != null) {
            this.selectableEvents = bp.getEventSelectionStrategy().selectableEvents(systemState);
            ArrayList<BEvent> eventOrdered = new ArrayList(this.selectableEvents);
            Collections.shuffle(eventOrdered);
            this.iterator = eventOrdered.iterator();
        } else {
            this.selectableEvents = Collections.emptySet();
            this.iterator = this.selectableEvents.iterator();
        }

    }

    private String stateString() {
        StringBuilder str = new StringBuilder();
        this.systemState.getBThreadSnapshots().forEach((s) -> {
            str.append("\t").append(s.toString()).append(" {").append(s.getSyncStatement()).append("} \n");
        });
        return str.toString();
    }

    public String toString() {
        return (this.lastEvent != null ? "\n\tevent: " + this.lastEvent + "\n" : "") + this.stateString();
    }

    public Node getNextNode(BEvent e, ExecutorService exSvc) throws BPjsRuntimeException {
        try {
            return new Node(this.bp, BProgramSyncSnapshotCloner.clone(this.systemState).triggerEvent(e, exSvc, Collections.emptySet()), e);
        } catch (InterruptedException var4) {
            throw new BPjsRuntimeException("Thread interrupted during event invocaiton", var4);
        }
    }

    public Iterator<BEvent> getEventIterator() {
        return this.iterator;
    }

    public BEvent getLastEvent() {
        return this.lastEvent;
    }

    public BProgramSyncSnapshot getSystemState() {
        return this.systemState;
    }

    public Set<BEvent> getSelectableEvents() {
        return this.selectableEvents;
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hash(systemState);
        //result = prime * result + Objects.hash(lastEvent);
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof Node)) {
            return false;
        } else {
            Node other = (Node)obj;
            return !Objects.equals(this.lastEvent, other.getLastEvent()) ? false : Objects.equals(this.systemState, other.getSystemState());
        }
    }
}
