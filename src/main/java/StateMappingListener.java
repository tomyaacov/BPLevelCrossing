import il.ac.bgu.cs.bp.bpjs.analysis.DfsBProgramVerifier;
import il.ac.bgu.cs.bp.bpjs.analysis.DfsTraversalNode;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTrace;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspection;
import il.ac.bgu.cs.bp.bpjs.analysis.ExecutionTraceInspections;
import il.ac.bgu.cs.bp.bpjs.analysis.violations.Violation;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 *
 * @author michael
 */
public class StateMappingListener implements ExecutionTraceInspection, DfsBProgramVerifier.ProgressListener {
    public int count=0;
    private final PrintStream out;
    private final BProgram bprog;
    private boolean showHash = false;
    private Set<Integer> visitedHashes = new TreeSet<>();
    public List<String> statesList = new ArrayList<>();
    public List<Map<String, String>> stateEventsList = new ArrayList<>();
    public String initialState = null;
    public String toCsv = "";



    public StateMappingListener(BProgram aBProgram, PrintStream out) {
        this.out = out;
        bprog = aBProgram;
    }

    //////////////////////////////
    // Trace Inspection Methods
    //////////////////////////////

    @Override
    public String title() {
        return "StateMapping";
    }

    @Override
    public Optional<Violation> inspectTrace(ExecutionTrace et) {
        count++;
        Optional<Violation> inspection = ExecutionTraceInspections.FAILED_ASSERTIONS.inspectTrace(et);
        //printTrace(et);
//        if ( et.getStateCount() == 1 ) {
//            if ( ! visitedHashes.isEmpty() ) {
//                System.err.println("** Visiting a trace of size 1 twice.");
//            }
//            out.println( "start -> " + bpssNodeId(et.getLastState()) + " [color=blue]");
//            visitedHashes.add(et.getLastState().hashCode());
//
//        } else {
//
//            BProgramSyncSnapshot source, destination;
//
//            if ( et.isCyclic() ) {
//
//            } else {
//                source = et.getNodes().get
//            }
//
//        }

        if ( et.isCyclic() ) {
            out.println( bpssNodeId(et.getLastState()) + " -> "
                    + bpssNodeId(et.getFinalCycle().get(0).getState())
                    + "[label=\"" + eventToStr(et.getLastEvent()) + "\"] // cycle");

            printTrace(et);
        }
//        else {
//            out.println(newNode(et.getLastState(), !inspection.isPresent()));
//            if ( et.getStateCount() > 1 ) {
//                out.println( bpssNodeId(et.getNodes().get(et.getStateCount()-2).getState()) + " -> "
//                        + bpssNodeId(et.getLastState())
//                        + "[label=\"" + eventToStr(et.getLastEvent()) + "\"] // new");
//            } else {
//                out.println( "start -> " + bpssNodeId(et.getLastState()) + " [color=blue]");
//            }
//        }

        // We prune on failed assertions.
        return inspection;
    }

    //////////////////////////////
    // Progress Listener Methods
    //////////////////////////////
    @Override
    public void started(DfsBProgramVerifier dbpv) {
        //out.println("digraph " + GVUtils.sanitize(bprog.getName()) + " {");
        out.println("label=\"" + bprog.getName() + "\"");
        out.println("start [shape=none fontcolor=blue label=\"start\"]");
    }

    @Override
    public void iterationCount(long iteration, long statesFound, DfsBProgramVerifier dbpv) {
        System.err.println(" - " + iteration + "/" + statesFound);
    }

    @Override
    public boolean violationFound(Violation vltn, DfsBProgramVerifier dbpv) {
        return true;
    }

    @Override
    public void done(DfsBProgramVerifier dbpv) {
        out.println("}");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("/tmp/paths.csv"));
            writer.write(toCsv);

            writer.close();
        } catch (IOException e){

        }

        out.println(toCsv);
    }

    private static final String INVALID_NODE_STYLE = " color=red fontcolor=red shape=hexagon ";
    private static final String HOT_NODE_STYLE = " penwidth=2 color=\"#888800\" ";

    private String newNode( BProgramSyncSnapshot bpss, boolean isValid ) {
        String bpssNodeTitle = bpssNodeTitle(bpss);
        //statesList.add(bpssNodeId(bpss));
        //stateEventsList.add(new HashMap<>());
        System.err.println( bpssNodeTitle + ":" );
        for ( BThreadSyncSnapshot btss : bpss.getBThreadSnapshots() ) {
            System.err.println("  " + btss.getName() + "@" + btss.getContinuationProgramState().getProgramCounter());
            System.err.println("   : " + btss.getSyncStatement());
        }
        return bpssNodeId(bpss) + "[label=\"" + bpssNodeTitle + "\""
                + (isValid? (bpss.isHot()?HOT_NODE_STYLE:"") :INVALID_NODE_STYLE) + "]";
    }

    private String bpssNodeTitle( BProgramSyncSnapshot bpss ) {
        Optional<BThreadSyncSnapshot> obt = bpss.getBThreadSnapshots().stream().filter( s -> s.getName().equals("stateTitler")).findAny();
        String stateTitle = obt.map( bt -> bt.getSyncStatement().getData().toString() + "\\n" ).orElse("");
        if ( !obt.isPresent() || showHash ) {
            stateTitle = stateTitle + Integer.toHexString(bpss.hashCode());
        }
        return stateTitle;
    }

    private String bpssNodeId( BProgramSyncSnapshot bpss ) {
        return "bpss" + Integer.toHexString(bpss.hashCode());
    }

    public void printTrace( ExecutionTrace et ) {
        System.err.println("Trace");
        et.getNodes().forEach( nd->{
            System.err.println( nd.getEvent().map( e->e.getName()).orElse("-") );
            toCsv += nd.getEvent().map( e->e.getName()).orElse("-");
            toCsv += ",";
        });
        toCsv += "\n";
        System.err.println("/Trace");
        List<ExecutionTrace.Entry> nodes = et.getNodes();
        String lastState = null;
        String lastEvent = null;
        String currentState = null;
        String currentEvent = null;
        for (ExecutionTrace.Entry ent : nodes){

            currentState = bpssNodeTitle(ent.getState()).replace("\\n", "");
            currentEvent = ent.getEvent().map( e->e.getName()).orElse("-");


            if (!statesList.contains(currentState)){
                statesList.add(currentState);
                stateEventsList.add(new HashMap<>());
            }
            if (!(lastState==null) && !(lastEvent.equals("-"))){
                int id = statesList.indexOf(lastState);
                stateEventsList.get(id).putIfAbsent(lastEvent, currentState);
            }
            lastState = currentState;
            lastEvent = currentEvent;

        }
    }


    private String eventToStr( BEvent evt ) {
        final StringBuilder outb = new StringBuilder(evt.getName());
        evt.getDataField().ifPresent( d -> outb.append("\\n").append(d.toString()));
        return outb.toString();
    }

    @Override
    public void maxTraceLengthHit(ExecutionTrace aTrace, DfsBProgramVerifier vfr) {}

    public void createFsmFile(){

    }

    public void generateFsmFile(String fileName) throws IOException {
        StringBuilder content = new StringBuilder();
        content.append(statesList.size()).append("\n\n");
        for (int i =0; i < statesList.size(); i++){
            String s = statesList.get(i);
            Map<String, String> m = stateEventsList.get(i);
            content.append(s).append("\t").append("0\t").append(m.size()).append("\n");
            m.forEach((k, v) -> {
                content.append(k).append("\t").append(v).append("\t").append("c").append("\t");
                if (k.contains("UnObservable") || k.contains("Premature")){
                    content.append("u");
                }
                content.append("o\n");
            });
            content.append("\n");
        }
        FileWriter myWriter = new FileWriter(fileName);
        myWriter.write(content.toString());
        myWriter.close();
    }

}
