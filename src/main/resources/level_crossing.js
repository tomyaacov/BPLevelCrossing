const n = 1;
const Enters = bp.EventSet("Enters", function(evt) {return evt.name.startsWith("Entering")});
const Approachings = bp.EventSet("Approachings", function(evt) {return evt.name.startsWith("Approaching")});
const Leavings = bp.EventSet("Leavings", function(evt) {return evt.name.startsWith("Leaving")});

for (var i = 0; i < n; i++){
    (function(i){
        bp.registerBThread("RailwayTraffic_" + i, function() {
            while (true){
                bp.sync({request: bp.Event("Approaching_" + i)});
                bp.sync({request: bp.Event("Entering_" + i)});
                bp.sync({request: bp.Event("Leaving_" + i)});
            }
        });

        bp.registerBThread("no entering when barrier not Lower" + i, function() {
            while (true){
                bp.sync({waitFor: bp.Event("Lower"), block: bp.Event("Entering_" + i)});
                bp.sync({waitFor: bp.Event("Raise")});
            }
        });

        bp.registerBThread("ClosingRequest" + i, function() {
            while (true){
                bp.sync({waitFor: bp.Event("Approaching_" + i)});
                bp.sync({request: bp.Event("Lower"), waitFor: bp.Event("Entering_" + i)});
            }
        });

        bp.registerBThread("BlockUpWhileIn_" + i, function() {
            while (true){
                bp.sync({waitFor: bp.Event("Entering_" + i)});
                bp.sync({block: bp.Event("Raise"), waitFor: bp.Event("Leaving_" + i)});
            }
        });


    })(i);
}

bp.registerBThread("Barriers", function() {
    while (true){
        bp.sync({waitFor: bp.Event("Lower")});
        bp.sync({request: bp.Event("Raise"), block:bp.Event("Lower")});//
    }
});

/*
After lower, raising is possible only if had leaving,
approaching, but no closing request.
meaning, that our barrier behavior depends on helper events
*/














// bp.registerBThread("no_entering_when_barrier_up", function() {
//     while (true){
//         bp.sync({waitFor: bp.Event("Lower"), block: Enters});
//         bp.sync({waitFor: bp.Event("Raise")});//
//     }
// });


// for (var i = 0; i < n; i++){
//     (function(i){
//         bp.registerBThread("RailwayTraffic_" + i, function() {
//             while (true){
//                 bp.sync({request: bp.Event("Approaching_" + i)});
//                 bp.sync({request: bp.Event("Entering_" + i)});
//                 bp.sync({request: bp.Event("Leaving_" + i)});
//             }
//         });
//         bp.registerBThread("Controller_" + i, function() {
//             while (true){
//                 bp.sync({waitFor: bp.Event("Approaching_" + i)});
//                 bp.sync({request: bp.Event("ClosingRequest")});
//                 bp.sync({request: bp.Event("OpeningRequest"), block: bp.Event("Raise")});
//             }
//         });
//     })(i);
// }
//
// bp.registerBThread("Barriers", function() {
//     while (true){
//         bp.sync({waitFor: bp.Event("ClosingRequest")});
//         bp.sync({request: bp.Event("Lower")});
//         bp.sync({request: bp.Event("Raise")});
//     }
// });
//
// bp.registerBThread("KeepLower", function() {
//     while (true){
//         bp.sync({waitFor: bp.Event("Lower")});
//         while (true){
//             bp.sync({waitFor: bp.Event("ClosingRequest")});
//             var e = bp.sync({request: bp.Event("KeepLower"), waitFor: bp.Event("Raise")});
//             if(e.name.equals("Raise")){break}
//         }
//     }
// });