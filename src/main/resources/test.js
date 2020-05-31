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

        //Good for one train only
        // bp.registerBThread("No_entering_when_barrier_up_" + i, function() {
        //     while (true){
        //         bp.sync({waitFor: bp.Event("Lower"), block: [Enters, Leavings]});
        //         var e = bp.sync({waitFor: [bp.Event("Raise"), bp.Event("PrematureRaise")]});
        //         if (e.name.equals("PrematureRaise")){
        //             var e = bp.sync({waitFor: [bp.Event("Leaving_"+i), bp.Event("Entering_" + i)]});
        //             if (e.name.equals("Entering_" + i)){
        //                 bp.sync({waitFor: bp.Event("Leaving_"+i)});
        //             }
        //         }
        //     }
        // });

    })(i);
}
//

bp.registerBThread("No_entering_when_barrier_up", function() {
    var x = 0;
    var event;
    while (true){
        if(x < 1){
            event = bp.sync({waitFor: bp.Event("Lower"), block:[Enters, Leavings]});
            x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("Lower"), Approachings, Enters, Leavings]});
            if (event.name.equals("Lower") || event.name.startsWith("Approaching")){
                x += 1;
            }
            if (event.name.startsWith("Leaving")){
                x -= 1;
            }
        }
    }
});


bp.registerBThread("Barriers", function() {
    while (true){
        bp.sync({waitFor: Approachings});
        bp.sync({request: bp.Event("Lower")});
        //bp.sync({waitFor: Leavings});
        bp.sync({request: [bp.Event("Raise"), bp.Event("PrematureRaise")]});
    }
});

// bp.registerBThread("Premature_Raise", function() {
//     while (true){
//         bp.sync({waitFor: bp.Event("Lower")});
//         bp.sync({request: , waitFor: bp.Event("Raise")});
//     }
// });





// before!!!
// bp.registerBThread("No_entering_when_barrier_up", function() {
//     while (true){
//         bp.sync({waitFor: bp.Event("Lower"), block: Enters});
//         bp.sync({waitFor: bp.Event("Raise")});
//     }
// });


// for (var i = 0; i < n; i++){
//     (function(i){
//         bp.registerBThread("RailwayTraffic_" + i, function() {
//             while (true){
//                 bp.sync({request: bp.Event("Approaching_" + i)});
//                 bp.sync({request: [bp.Event("Entering_" + i), bp.Event("UnObservableEntering_" + i)], block: bp.Event("Raise")});//
//                 bp.sync({request: bp.Event("Leaving_" + i), block: bp.Event("Raise")});//
//             }
//         });
//

//     })(i);
// }
//
// bp.registerBThread("no_entering_when_barrier_up", function() {
//     while (true){
//         bp.sync({waitFor: bp.Event("Lower"), block: Enters});//
//         bp.sync({waitFor: bp.Event("Raise")});
//     }
// });
//
//
// bp.registerBThread("Barriers", function() {
//     while (true){
//         bp.sync({waitFor: Approachings});//
//         bp.sync({request: bp.Event("Lower")});
//         //bp.sync({waitFor: Leavings});
//         bp.sync({request: [bp.Event("Raise"), bp.Event("PrematureRaise")]});
//     }
// });