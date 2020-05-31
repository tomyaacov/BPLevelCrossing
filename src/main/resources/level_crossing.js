const n = 1;
const Enters = bp.EventSet("Enters", function(evt) {return evt.name.startsWith("Entering")});
const Approachings = bp.EventSet("Approachings", function(evt) {return evt.name.startsWith("Approaching")});
const Leavings = bp.EventSet("Leavings", function(evt) {return evt.name.startsWith("Leaving")});

for (var i = 0; i < n; i++){
    (function(i){
        bp.registerBThread("RailwayTraffic_" + i, function() {
            while (true){
                bp.sync({request: bp.Event("Approaching_" + i)});
                bp.sync({request: bp.Event("Entering_" + i), block: bp.Event("Raise")});
                bp.sync({request: bp.Event("Leaving_" + i), block: bp.Event("Raise")});
            }
        });
    })(i);
}

bp.registerBThread("no_entering_when_barrier_up", function() {
    while (true){
        bp.sync({waitFor: bp.Event("Lower"), block: Enters});
        bp.sync({waitFor: bp.Event("Raise")});
    }
});

bp.registerBThread("Barriers", function() {
    while (true){
        bp.sync({waitFor: Approachings});
        bp.sync({request: bp.Event("Lower")});
        //bp.sync({waitFor: Leavings});
        bp.sync({request: bp.Event("Raise")});
    }
});


// //assert bthreads
// bp.registerBThread("no_lower_before_raise", function() {
//     while (true){
//         var evt = bp.sync({waitFor: [bp.Event("Lower"), bp.Event("Raise")]});
//         bp.ASSERT(!evt.name.equals("Raise"), "Raise before Lower!");
//         var evt = bp.sync({waitFor: [bp.Event("Lower"), bp.Event("Raise")]});
//         bp.ASSERT(!evt.name.equals("Lower"), "Lower after Lower!");
//     }
// });
//
// bp.registerBThread("approachings - leavings == 0", function() {
//     var i = 0;
//     while (true){
//         var evt = bp.sync({waitFor: [Approachings, Leavings, bp.Event("Raise")]});
//         if (Approachings.contains(evt)){
//             i++;
//         } else if (Leavings.contains(evt)){
//             i--;
//         } else {
//             bp.ASSERT(i==0, "Raise when approachings - leavings > 0");
//         }
//     }
// });

//how can we make sure we all possible options can still happen in the model
