const n = 1;
const Enters = bp.EventSet("Enters", function(evt) {return evt.name.startsWith("Entering")});
const Approachings = bp.EventSet("Approachings", function(evt) {return evt.name.startsWith("Approaching")});
const Leavings = bp.EventSet("Leavings", function(evt) {return evt.name.startsWith("Leaving")});


for (var i = 0; i < n; i++){
    (function(i){
        bp.registerBThread("RailwayTraffic_" + i, function() {
            while (true){
                bp.sync({request: bp.Event("Approaching_" + i)});
                bp.sync({request: [bp.Event("Entering_" + i), bp.Event("UnObservableEntering_" + i)], block: bp.Event("Raise")});
                bp.sync({request: bp.Event("Leaving_" + i), block: bp.Event("Raise")});
            }
        });

    })(i);
}

bp.registerBThread("Barriers", function() {
    while (true){
        bp.sync({waitFor: Approachings});
        bp.sync({request: bp.Event("Lower")});
        //bp.sync({waitFor: Leavings});
        bp.sync({request: [bp.Event("Raise"), bp.Event("PrematureRaise")]});
    }
});

bp.registerBThread("No_entering_when_barrier_up", function() {
    while (true){
        bp.sync({waitFor: bp.Event("Lower"), block: Enters});
        bp.sync({waitFor: [bp.Event("Raise"), bp.Event("PrematureRaise")]});
    }
});

bp.registerBThread("stateTitler", function(){
    var a = 0;
    var b = 0;
    var c = 0;
    while( true ) {
        var le = bp.sync({waitFor:bp.all}, a+":"+b+":"+c);
        if ( le.name.includes("Approaching") || le.name.includes("Entering") ||  le.name.includes("Leaving")) a = (a+1)%3;
        if ( (le.name.includes("Approaching") && b===0) || (le.name.includes("Lower") && b===1) ||  (le.name.includes("Raise") && b===2)) b = (b+1)%3;
        if ( (le.name.includes("Lower") && c===0) || (le.name.includes("Raise") && c===1)) c = (c+1)%2;
    }
});

