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
                bp.sync({request: bp.Event("Leaving_" + i), block: bp.Event("Raise")});//
            }
        });
    })(i);
}

bp.registerBThread("no_entering_when_barrier_up", function() {
    while (true){
        bp.sync({waitFor: bp.Event("Lower"), block: Enters});//
        bp.sync({waitFor: [bp.Event("Raise"), bp.Event("PrematureRaise")]});
    }
});

// bp.registerBThread("no_leaving_when_barrier_up", function() {
//     while (true){
//         bp.sync({waitFor: bp.Event("Lower"), block: Leavings});//
//         bp.sync({waitFor: [bp.Event("Raise"), bp.Event("PrematureRaise")]});
//     }
// });


bp.registerBThread("Barriers", function() {
    while (true){
        bp.sync({waitFor: Approachings});//
        bp.sync({request: bp.Event("Lower")});
        //bp.sync({waitFor: Leavings});
        bp.sync({request: [bp.Event("Raise"), bp.Event("PrematureRaise")]});
    }
});


