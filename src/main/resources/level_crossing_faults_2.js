const n = 2;
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
    var d = 0;
    while( true ) {
        var le = bp.sync({waitFor:bp.all}, a+":"+b+":"+c+":"+d);
        if ( le.name.includes("Approaching_0") || le.name.includes("Entering_0") ||  le.name.includes("Leaving_0")) a = (a+1)%3;
        if ( le.name.includes("Approaching_1") || le.name.includes("Entering_1") ||  le.name.includes("Leaving_1")) b = (b+1)%3;
        if ( (le.name.includes("Approaching") && c===0) || (le.name.includes("Lower") && c===1) ||  (le.name.includes("Raise") && c===2)) c = (c+1)%3;
        if ( (le.name.includes("Lower") && d===0) || (le.name.includes("Raise") && d===1)) d = (d+1)%2;
    }
});

