package events;

import il.ac.bgu.cs.bp.bpjs.model.BEvent;

@SuppressWarnings("serial")
public class Entering extends BEvent{

    public int i;

    public Entering(int i){
        super("Entering"+i);
        this.i = i;
    }

    public Entering(){
        super("Entering");
        this.i = -1;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!getClass().isInstance(obj)) {
            return false;
        }
        if (i == -1) {
            return true;
        }
        Entering other = (Entering) obj;
        if (other.i == -1) {
            return true;
        }
        return i == other.i;
    }

//    @Override
//    public int compareTo(BEvent e) {
//        return this.getName().compareTo(e.getName());
//    }

    @Override
    public int hashCode() {
        String s = "Entering";
        return s.hashCode();
    }
}
