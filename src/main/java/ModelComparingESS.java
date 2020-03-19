import il.ac.bgu.cs.bp.bpjs.model.SyncStatement;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgramSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.EventSelectionResult;
import il.ac.bgu.cs.bp.bpjs.model.eventselection.SimpleEventSelectionStrategy;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.ComposableEventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSet;
import il.ac.bgu.cs.bp.bpjs.model.eventsets.EventSets;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;
import org.mozilla.javascript.Context;


public class ModelComparingESS extends SimpleEventSelectionStrategy {
    public ModelComparingESS(long seed) {
        super(seed);
    }

    public ModelComparingESS() {
        super();
    }

    @Override
    public Set<BEvent> selectableEvents(BProgramSyncSnapshot bpss) {
        Set<SyncStatement> statements = bpss.getStatements();
        List<BEvent> externalEvents = bpss.getExternalEvents();
        if ( statements.isEmpty() ) {
            // Corner case, not sure this is even possible.
            return externalEvents.isEmpty() ? emptySet() : singleton(externalEvents.get(0));
        }

        EventSet blocked1 = ComposableEventSet.anyOf(statements.stream()
                .filter( stmt -> stmt!=null )
                .filter(stmt -> stmt.getBthread().getName().startsWith("p"))
                .map(SyncStatement::getBlock )
                .filter(r -> r != EventSets.none )
                .collect( toSet() ) );

        Set<BEvent> requested1 = statements.stream()
                .filter( stmt -> stmt!=null )
                .filter(stmt -> stmt.getBthread().getName().startsWith("p"))
                .flatMap( stmt -> stmt.getRequest().stream() )
                .collect( toSet() );

        EventSet blocked2 = ComposableEventSet.anyOf(statements.stream()
                .filter( stmt -> stmt!=null )
                .filter(stmt -> !stmt.getBthread().getName().startsWith("p"))
                .filter(stmt -> !stmt.getBthread().getName().startsWith("utils"))
                .map(SyncStatement::getBlock )
                .filter(r -> r != EventSets.none )
                .collect( toSet() ) );

        Set<BEvent> requested2 = statements.stream()
                .filter( stmt -> stmt!=null )
                .filter(stmt -> !stmt.getBthread().getName().startsWith("p"))
                .filter(stmt -> !stmt.getBthread().getName().startsWith("utils"))
                .flatMap( stmt -> stmt.getRequest().stream() )
                .collect( toSet() );

        List<String> helperEvents = Arrays.asList("ClosingRequest", "OpeningRequest", "KeepDown");

        Context.enter();
        Set<BEvent> helperRequestedAndNotBlocked1 = requested1.stream()
                .filter( req -> !blocked1.contains(req) )
                .filter(req -> (helperEvents.contains(req.name)))
                .collect( toSet() );

        if(helperRequestedAndNotBlocked1.size() > 0){
            return helperRequestedAndNotBlocked1;
        }

        Context.enter();
        Set<BEvent> requestedAndNotBlocked1 = requested1.stream()
                .filter( req -> !blocked1.contains(req) )
                .collect( toSet() );

        Context.enter();
        Set<BEvent> requestedAndNotBlocked2 = requested2.stream()
                .filter( req -> !blocked2.contains(req) )
                .collect( toSet() );

        if (!(requestedAndNotBlocked1.containsAll(requestedAndNotBlocked2) && requestedAndNotBlocked2.containsAll(requestedAndNotBlocked1))){
            Set<BEvent> a = new HashSet<BEvent>();
            a.add(new BEvent("Violation"));
            System.out.println("optional events in petri net model");
            System.out.println(requestedAndNotBlocked1);
            System.out.println("optional events in bp model");
            System.out.println(requestedAndNotBlocked2);
            return a;
        }


        EventSet blocked = ComposableEventSet.anyOf(statements.stream()
                .filter( stmt -> stmt!=null )
                .map(SyncStatement::getBlock )
                .filter(r -> r != EventSets.none )
                .collect( toSet() ) );

        Set<BEvent> requested = statements.stream()
                .filter( stmt -> stmt!=null )
                .filter(stmt -> !stmt.getBthread().getName().startsWith("utils"))
                .flatMap( stmt -> stmt.getRequest().stream() )
                .collect( toSet() );

        // Let's see what internal events are requested and not blocked (if any).
        try {
            Context.enter();
            Set<BEvent> requestedAndNotBlocked = requested.stream()
                    .filter( req -> !blocked.contains(req) )
                    .collect( toSet() );

            return requestedAndNotBlocked.isEmpty() ?
                    externalEvents.stream().filter( e->!blocked.contains(e) ) // No internal events requested, defer to externals.
                            .findFirst().map( e->singleton(e) ).orElse(emptySet())
                    : requestedAndNotBlocked;
        } finally {
            Context.exit();
        }
    }

    @Override
    public Optional<EventSelectionResult> select(BProgramSyncSnapshot bpss, Set<BEvent> selectableEvents) {
        if (selectableEvents.isEmpty()) {
            return Optional.empty();
        }
        BEvent chosen = new ArrayList<>(selectableEvents).get(rnd.nextInt(selectableEvents.size()));
        Set<BEvent> requested = bpss.getStatements().stream()
                .filter((SyncStatement stmt) -> stmt != null)
                .flatMap((SyncStatement stmt) -> stmt.getRequest().stream())
                .collect(Collectors.toSet());

        if (requested.contains(chosen)) {
            return Optional.of(new EventSelectionResult(chosen));
        } else {
            // that was an external event, need to find the first index
            return Optional.of(new EventSelectionResult(chosen, singleton(0)));
        }
    }
}
