import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;


import formalmethodsintro.base.goal.GoalStructure;
import formalmethodsintro.base.goal.GoalStructure.Acc;
import formalmethodsintro.base.goal.GoalStructure.Alphabet;
import formalmethodsintro.base.goal.GoalStructure.InitialStateSet;
import formalmethodsintro.base.goal.GoalStructure.StateSet;
import formalmethodsintro.base.goal.GoalStructure.TransitionSet;
import formalmethodsintro.base.goal.GoalStructure.TransitionSet.Transition;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;

public class AutomatonIO {

    public static void write(Automaton aut, String file) throws Exception {
        GoalStructure gs = new GoalStructure();

        gs.setLabelOn("Transition");
        gs.setType("FiniteStateAutomaton");

        gs.setAlphabet(new Alphabet());
        gs.alphabet.setType("Propositional");

        gs.setStateSet(new StateSet());

        gs.acc = new Acc();
        gs.acc.setType("Buchi");

        gs.initialStateSet = new InitialStateSet();

        gs.transitionSet = new TransitionSet();
        gs.transitionSet.setComplete("false");

        gs.stateSet.state = new NoDuplicatesList<>();
        gs.alphabet.proposition = new NoDuplicatesList<>();
        gs.acc.stateID = new NoDuplicatesList<>();
        gs.transitionSet.transition = new NoDuplicatesList<>();

        Set<BEvent> symbols = new HashSet<>();

        for (Entry<BProgramSyncSnapshot, Map<BEvent, Set<BProgramSyncSnapshot>>> ent : aut.getTransitions().entrySet()) {

            for (Entry<BEvent, Set<BProgramSyncSnapshot>> tr : ent.getValue().entrySet()) {
                BEvent symbol = tr.getKey();

                gs.alphabet.proposition.add(symbol.getName());
                symbols.add(symbol);
            }
        }

        long tid = 1;
        for (Entry<BProgramSyncSnapshot, Map<BEvent, Set<BProgramSyncSnapshot>>> ent : aut.getTransitions().entrySet()) {
            BProgramSyncSnapshot source = ent.getKey();

            formalmethodsintro.base.goal.GoalStructure.StateSet.State stt = new formalmethodsintro.base.goal.GoalStructure.StateSet.State();
            stt.setSid(IdAssignner.getId(source));
            stt.setLabel(String.valueOf(source.hashCode()));

            gs.stateSet.state.add(stt);

            for (Entry<BEvent, Set<BProgramSyncSnapshot>> tr : ent.getValue().entrySet()) {

                BEvent symbol = tr.getKey();

                String label = symbol.getName();

//                for (L s : symbols) {
//                    if (!symbol.contains(s)) {
//                        label += "~" + s + " ";
//                    }
//                }

                for (BProgramSyncSnapshot destination : tr.getValue()) {
                    stt = new formalmethodsintro.base.goal.GoalStructure.StateSet.State();
                    stt.setSid(IdAssignner.getId(destination));
                    stt.setLabel(String.valueOf(destination.hashCode()));

                    gs.stateSet.state.add(stt);

                    // Transition
                    Transition tran = new Transition();
                    tran.setFrom(IdAssignner.getId(source));
                    tran.setTo(IdAssignner.getId(destination));
                    tran.label = label;
                    tran.tid = tid++;
                    gs.transitionSet.transition.add(tran);

                    // If this is an initial state, copy the transition
                    if (aut.getInitialStates().contains(source)) {
//                        Transition tran1 = new Transition();
//                        tran1.setFrom(0L);
//                        tran1.setTo(IdAssignner.getId(destination));
//                        tran1.label = label;
//                        tran1.tid = tid++;
//                        gs.transitionSet.transition.add(tran1);
                        gs.initialStateSet.stateID = IdAssignner.getId(source);
                    }

                }
            }
        }

//        for (BProgramSyncSnapshot s : aut.getAcceptingStates()) {
//            gs.acc.stateID.add(IdAssignner.getId(s));
//        }

        // Add a single initial state
//        formalmethodsintro.base.goal.GoalStructure.StateSet.State stt = new formalmethodsintro.base.goal.GoalStructure.StateSet.State();
//        stt = new formalmethodsintro.base.goal.GoalStructure.StateSet.State();
//        stt.setSid(0L);
//        stt.setLabel("initial");
//        gs.stateSet.state.add(stt);
//        gs.initialStateSet.stateID = 0L;

        JAXBContext jc = JAXBContext.newInstance("formalmethodsintro.base.goal");
        Marshaller marshaller = jc.createMarshaller();
        marshaller.marshal(gs, new File(file));
    }

//    public static MultiColorAutomaton<String, String> read(String file) throws Exception {
//
//        JAXBContext jc = JAXBContext.newInstance("formalmethodsintro.base.goal");
//        Unmarshaller unmarshaller = jc.createUnmarshaller();
//        GoalStructure gs = (GoalStructure) unmarshaller.unmarshal(new File(file));
//
//        MultiColorAutomaton<String, String> aut = new MultiColorAutomaton<>();
//
//        for (Transition t : gs.getTransitionSet().getTransition()) {
//            Set<String> symbol = new HashSet<>(Arrays.asList(t.label.split(" ")));
//
//            symbol = symbol.stream().filter(s -> !s.startsWith("~")).collect(Collectors.toSet());
//
//            String source = "" + t.getFrom();
//            String destination = "" + t.getTo();
//            aut.addTransition(source, symbol, destination);
//
//            if (gs.initialStateSet.getStateID() == t.getFrom()) {
//                aut.setInitial(source);
//            }
//
//            if (gs.acc.stateID != null && gs.acc.stateID.contains(t.getFrom())) {
//                aut.setAccepting(source, 0);
//            }
//
//            if (gs.initialStateSet.getStateID() == t.getTo()) {
//                aut.setInitial(destination);
//            }
//
//            if (gs.acc.stateID != null && gs.acc.stateID.contains(t.getTo())) {
//                aut.setAccepting(destination, 0);
//            }
//        }
//
//        return aut;
//    }

}

@SuppressWarnings("serial")
class NoDuplicatesList<E> extends LinkedList<E> {

    @Override
    public boolean add(E e) {
        if (this.contains(e)) {
            return false;
        } else {
            return super.add(e);
        }
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        Collection<E> copy = new LinkedList<>(collection);
        copy.removeAll(this);
        return super.addAll(copy);
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        Collection<E> copy = new LinkedList<>(collection);
        copy.removeAll(this);
        return super.addAll(index, copy);
    }

    @Override
    public void add(int index, E element) {
        if (!this.contains(element)) {
            super.add(index, element);
        }
    }
}

class IdAssignner {

    private static long lastId = 1;
    private static final Map<Object, Long> ID_MAP = new HashMap<>();

    static long getId(Object o) {
        if (!ID_MAP.containsKey(o)) {
            ID_MAP.put(o, lastId++);
        }
        return ID_MAP.get(o);
    }
}
