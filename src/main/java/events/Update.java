package events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

import java.util.Set;

public class Update extends BEvent {

    public Set<BEvent> requestedAndNotBlockedSystem2;

    public Update(){
        super("Update");
    }

}
