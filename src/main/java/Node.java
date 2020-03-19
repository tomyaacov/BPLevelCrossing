import il.ac.bgu.cs.bp.bpjs.bprogramio.BProgramSyncSnapshotCloner;
import il.ac.bgu.cs.bp.bpjs.exceptions.BPjsRuntimeException;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;

import java.util.*;
import java.util.concurrent.ExecutorService;

public class Node {
    public static Node getInitialNode(BProgram bp, ExecutorService exSvc) throws Exception {
        BProgramSyncSnapshot seed = bp.setup().start(exSvc);

        return new Node(bp, seed, null);
    }

    private final BProgramSyncSnapshot systemState;
    private final BProgram bp;
    private final Set<BEvent> selectableEvents;
    private final BEvent lastEvent;
    private final Iterator<BEvent> iterator;

    protected Node(BProgram bp, BProgramSyncSnapshot systemState, BEvent e) {
        this.bp = bp;
        this.systemState = systemState;
        this.lastEvent = e;

        if (bp != null) {
            selectableEvents = bp.getEventSelectionStrategy().selectableEvents(systemState);
            ArrayList<BEvent> eventOrdered = new ArrayList<>(selectableEvents);
            Collections.shuffle(eventOrdered);
            iterator = eventOrdered.iterator();
        } else {
            selectableEvents = Collections.<BEvent>emptySet();
            iterator = selectableEvents.iterator();
        }

    }

    private String stateString() {

        StringBuilder str = new StringBuilder();
        systemState.getBThreadSnapshots().forEach(
                s -> str.append("\t").append(s.toString()).append(" {").append(s.getSyncStatement()).append("} \n"));

        return str.toString();
    }

    @Override
    public String toString() {
        return ((lastEvent != null) ? "\n\tevent: " + lastEvent + "\n" : "") + stateString();
    }

    public Node getNextNode(BEvent e, ExecutorService exSvc) throws BPjsRuntimeException {
        try {
            return new Node(bp, BProgramSyncSnapshotCloner.clone(systemState).triggerEvent(e, exSvc, Collections.emptySet()), e);
        } catch ( InterruptedException ie ) {
            throw new BPjsRuntimeException("Thread interrupted during event invocaiton", ie);
        }
    }


    public Iterator<BEvent> getEventIterator() {
        return iterator;
    }

    public BEvent getLastEvent() {
        return lastEvent;
    }

    public BProgramSyncSnapshot getSystemState() {
        return systemState;
    }

    public Set<BEvent> getSelectableEvents() {
        return selectableEvents;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hash(systemState);
        //result = prime * result + Objects.hash(lastEvent);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Node)) {
            return false;
        }

        Node other = (Node) obj;
        if (!Objects.equals(lastEvent, other.getLastEvent())) {
            return false;
        }

        return Objects.equals(systemState, other.getSystemState());
    }
}
