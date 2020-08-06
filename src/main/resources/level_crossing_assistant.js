const n = 2;
const Enters = bp.EventSet("Enters", function(evt) {return evt.name.startsWith("Entering")});
const Approachings = bp.EventSet("Approachings", function(evt) {return evt.name.startsWith("Approaching")});
const Leavings = bp.EventSet("Leavings", function(evt) {return evt.name.startsWith("Leaving")});
const ClosingRequests = bp.EventSet("ClosingRequests", function(evt) {return evt.name.startsWith("ClosingRequest")});

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
                bp.sync({request: bp.Event("ClosingRequest_" + i), block: bp.Event("Entering_" + i)});
                bp.sync({request: bp.Event("Lower"), waitFor: bp.Event("Entering_" + i)});
            }
        });
        bp.registerBThread("BlockRaise" + i, function() {
            while (true){
                bp.sync({waitFor: bp.Event("ClosingRequest_" + i)});
                bp.sync({block: bp.Event("Raise"), waitFor: bp.Event("Leaving_" + i)});
            }
        });
        // bp.registerBThread("UnobservableEntering_" + i, function() {
        //     while (true){
        //         bp.sync({waitFor: bp.Event("Approaching_" + i)});
        //         bp.sync({request: bp.Event("UnobservableEntering_" + i)});
        //     }
        // });
    })(i);
}

bp.registerBThread("Barriers", function() {
    while (true){
        bp.sync({waitFor: bp.Event("Lower")});
        bp.sync({request: bp.Event("Raise"), block:bp.Event("Lower")});
    }
});

// bp.registerBThread("PrematureRaise", function() {
//     while (true){
//         bp.sync({waitFor: bp.Event("Lower")});
//         bp.sync({request: bp.Event("PrematureRaise")});
//     }
// });


// bp.registerBThread("B_" + i, function() {
//     while (true){
//         bp.sync({waitFor: bp.Event("Lower")});
//         var e = bp.sync({waitFor: [bp.Event("Approaching_" + i), bp.Event("Raise")]});
//         if (e.equals(bp.Event("Raise"))){
//             continue;
//         }
//         bp.sync({request: bp.Event("Raise"), waitFor:bp.Event("ClosingRequest_" + i)});//
//     }
// });

// bp.registerBThread("BlockUpWhileIn_" + i, function() {
//     while (true){
//         bp.sync({waitFor: bp.Event("Approaching_" + i)});
//         bp.sync({block: bp.Event("Raise"), waitFor: bp.Event("Leaving_" + i)});
//     }
// });
