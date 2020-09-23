package events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class Context extends BEvent {
    public boolean raised;
    public Object trainInside;

    public Context(boolean raised, Object trainInside) {
        super("Context");
        this.raised = raised;
        this.trainInside = trainInside;
    }

    

}
