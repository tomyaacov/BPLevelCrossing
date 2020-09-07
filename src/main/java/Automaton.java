import events.ClosingRequest;
import events.KeepDown;
import events.Leaving;
import events.OpeningRequest;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import static java.util.Arrays.asList;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Set;

import org.svvrl.goal.cmd.Constant;
import org.svvrl.goal.cmd.Context;
import org.svvrl.goal.cmd.EquivalenceCommand;
import org.svvrl.goal.cmd.Expression;
import org.svvrl.goal.cmd.LoadCommand;
import org.svvrl.goal.cmd.Lval;
import org.svvrl.goal.cmd.TranslateCommand;
import org.svvrl.goal.core.aut.fsa.FSA;
import org.svvrl.goal.core.aut.opt.RefinedSimulation;
import org.svvrl.goal.core.aut.opt.RefinedSimulation2;
import org.svvrl.goal.core.aut.opt.SimulationRepository;
import org.svvrl.goal.core.comp.ComplementRepository;
import org.svvrl.goal.core.comp.piterman.PitermanConstruction;
import org.svvrl.goal.core.io.CodecRepository;
import org.svvrl.goal.core.io.FSACodec;

import org.svvrl.goal.cmd.EvaluationException;
import org.svvrl.goal.core.io.OldGFFCodec;

import java.util.*;
import java.util.stream.Collectors;

public class Automaton {
    private final Set<BProgramSyncSnapshot> initial;
    private final Map<Integer, Set<BProgramSyncSnapshot>> accepting;
    private Map<BProgramSyncSnapshot, Map<BEvent, Set<BProgramSyncSnapshot>>> transitions;
    public List<BEvent> helperEvents = Arrays.asList(new ClosingRequest(), new OpeningRequest(), new KeepDown());

    public Automaton() {
        transitions = new HashMap<>();
        initial = new HashSet<>();
        accepting = new HashMap<>();
    }

    public void removeHelperEvents(){
        boolean done = false;
        while(!done){
            done = removeSingleHelperEvent();
        }
        Set<BProgramSyncSnapshot> reachableStates = getReachableStates();
        accepting.put(0, reachableStates);
        transitions = transitions.entrySet()
                                .stream()
                                .filter(map -> reachableStates.contains(map.getKey()))
                                .collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue()));
    }

    public boolean removeSingleHelperEvent(){
        for (Map.Entry<BProgramSyncSnapshot, Map<BEvent, Set<BProgramSyncSnapshot>>> ent : transitions.entrySet()){
            for (Map.Entry<BEvent, Set<BProgramSyncSnapshot>> tr : ent.getValue().entrySet()){
                if(helperEvents.contains(tr.getKey())){
                    BProgramSyncSnapshot s = (BProgramSyncSnapshot)tr.getValue().toArray()[0];
                    transitions.get(ent.getKey()).remove(tr.getKey());
                    transitions.get(ent.getKey()).putAll(transitions.get(s));
                    return false;
                }
            }
        }
        return true;
    }

    public Set<BProgramSyncSnapshot> getReachableStates(){
        Set<BProgramSyncSnapshot> reachableStates = new HashSet<>();
        for (Map.Entry<BProgramSyncSnapshot, Map<BEvent, Set<BProgramSyncSnapshot>>> ent : transitions.entrySet()){
            for (Map.Entry<BEvent, Set<BProgramSyncSnapshot>> tr : ent.getValue().entrySet()){
                reachableStates.addAll(tr.getValue());
            }
        }
        reachableStates.addAll(initial);
        return reachableStates;
    }

    public void addState(BProgramSyncSnapshot s) {
        if (!transitions.containsKey(s)) {
            transitions.put(s, new HashMap<>());
        }
    }

    public void addTransition(BProgramSyncSnapshot source, BEvent symbol, BProgramSyncSnapshot destination) {
        if (!transitions.containsKey(source)) {
            addState(source);
        }

        if (!transitions.containsKey(destination)) {
            addState(destination);
        }

        Set<BProgramSyncSnapshot> set = transitions.get(source).get(symbol);
        if (set == null) {
            set = new HashSet<>();
            transitions.get(source).put(symbol, set);
        }
        set.add(destination);
    }

    public Set<BProgramSyncSnapshot> getAcceptingStates(int color) {
        Set<BProgramSyncSnapshot> acc = accepting.get(color);

        if (acc == null) {
            acc = new HashSet<>();
            accepting.put(color, acc);
        }

        return acc;
    }

    public Set<BProgramSyncSnapshot> getInitialStates() {
        return initial;
    }

    public Map<BProgramSyncSnapshot, Map<BEvent, Set<BProgramSyncSnapshot>>> getTransitions() {
        return transitions;
    }

    public Set<BProgramSyncSnapshot> nextState(BProgramSyncSnapshot source, BEvent symbol) {
        if (!transitions.containsKey(source)) {
            throw new IllegalArgumentException();
        } else {
            return transitions.get(source).get(symbol);
        }
    }

    public void setAccepting(BProgramSyncSnapshot s, int color) {
        Set<BProgramSyncSnapshot> acc = accepting.get(color);

        if (acc == null) {
            acc = new HashSet<>();
            accepting.put(color, acc);
        }

        addState(s);
        acc.add(s);
    }

    public void setInitial(BProgramSyncSnapshot s) {
        addState(s);
        initial.add(s);
    }

    public Set<Integer> getColors() {
        return accepting.keySet();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.initial);
        hash = 53 * hash + Objects.hashCode(this.accepting);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Automaton other = (Automaton) obj;
        if (!Objects.equals(this.initial, other.initial)) {
            return false;
        }
        if (!Objects.equals(this.accepting, other.accepting)) {
            return false;
        }
        return Objects.equals(this.transitions, other.transitions);
    }

    public void writeAutomaton(String fileName) throws Exception {
        removeHelperEvents();
        AutomatonIO.write(this, fileName);
    }


    public boolean isEquivalentTo(Automaton other) throws EvaluationException, Exception {
        AutomatonIO.write(other, "other.gff");
        AutomatonIO.write(this, "this.gff");

        Context context = new Context();

        Constant con1 = new Constant("this.gff");
        Constant con2 = new Constant("other.gff");

        Lval lval1 = new Lval("th", new Expression[]{});
        Lval lval2 = new Lval("ot", new Expression[]{});

        // CodecRepository..add(0, new GFFCodec());
        CodecRepository.add(0, new OldGFFCodec());

        SimulationRepository.addSimulation2("RefinedSimilarity", FSA.class, RefinedSimulation2.class);
        SimulationRepository.addSimulation("RefinedSimilarity", FSA.class, RefinedSimulation.class);

        ComplementRepository.add("Safra-Piterman Construction", PitermanConstruction.class);

        LoadCommand lc1 = new LoadCommand(asList(lval1, con1));
        lc1.eval(context);

        LoadCommand lc2 = new LoadCommand(asList(lval2, con2));
        lc2.eval(context);

        EquivalenceCommand ec = new EquivalenceCommand(asList(lval1, lval2));

        return (Boolean) ec.eval(context);

    }

    public boolean isEquivalentTo(String serializedAutomaton) {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>\r\n"
                + "<logic name=\"QPTL\">\r\n <name/>\r\n <description/>\r\n <formula>"
                + serializedAutomaton + "</formula>\r\n</logic>\r\n";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("formula.gff"))) {

            writer.write(xml);
            writer.close();

            AutomatonIO.write(this, "this.gff");

            Context context = new Context();

            Constant con1 = new Constant("this.gff");
            Constant con3 = new Constant("formula.gff");

            Lval lval1 = new Lval("th", new Expression[]{});
            Lval lval3 = new Lval("fo", new Expression[]{});

            //CodecRepository.add(0, new GFFCodec());
            CodecRepository.add(0, new FSACodec());


            SimulationRepository.addSimulation2("RefinedSimilarity", FSA.class, RefinedSimulation2.class);
            SimulationRepository.addSimulation("RefinedSimilarity", FSA.class, RefinedSimulation.class);

            ComplementRepository.add("Safra-Piterman Construction", PitermanConstruction.class);

            LoadCommand lc1 = new LoadCommand(asList(lval1, con1));
            lc1.eval(context);

            LoadCommand lc3 = new LoadCommand(asList(lval3, con3));
            lc3.eval(context);

            TranslateCommand tc = new TranslateCommand(asList(lval3));
            tc.eval(context);

            EquivalenceCommand ec = new EquivalenceCommand(asList(lval1, tc));

            return (Boolean) ec.eval(context);

        }
        catch (Exception e) {
            e.printStackTrace(System.err);
            return false;
        }
    }
}
