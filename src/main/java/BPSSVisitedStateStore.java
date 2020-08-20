import il.ac.bgu.cs.bp.bpjs.analysis.VisitedStateStore;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;

import java.util.HashSet;
import java.util.Set;

public class BPSSVisitedStateStore implements VisitedStateStore {

    private final Set<BProgramSyncSnapshot> visited = new HashSet();

    @Override
    public void store(BProgramSyncSnapshot bProgramSyncSnapshot) {
        this.visited.add(bProgramSyncSnapshot);
    }

    @Override
    public boolean isVisited(BProgramSyncSnapshot bProgramSyncSnapshot) {
        return this.visited.contains(bProgramSyncSnapshot);
    }

    @Override
    public void clear() {
        this.visited.clear();
    }

    @Override
    public long getVisitedStateCount() {
        return (long)this.visited.size();
    }
}
