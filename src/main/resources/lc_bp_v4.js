importPackage(Packages.events);
const n = 1;
const x = [Approaching(0), Entering(0), Leaving(0), Approaching(1), Entering(1), Leaving(1), Raise(), Lower()];
const y = [Context(true, [true, true]), Context(true, [false, true]), Context(true, [true, false]), Context(true, [false, false])];
const z = [Context(false, [true, true]), Context(false, [false, true]), Context(false, [true, false]), Context(false, [false, false])];
let notContext = bp.EventSet("notContext", e => ! (e instanceof Context))
let contextEventSet = bp.EventSet("contextEventSet", e =>  (e instanceof Context))

for (var i = 0; i < n; i++){
    (function(i){
        bp.registerBThread("Trains keep comming to railway " + i + " and progress in Approaching, Entring, and Leaving sequence", function() {
            while (true){
                bp.sync({request: Approaching(i)});
                bp.sync({request: Entering(i)});//
                bp.sync({request: Leaving(i)});
            }
        });

        bp.registerBThread("The barrier cannot be raised when there is a train in railway " + i, function() {
            while (true){
                bp.sync({waitFor: Entering(i)});
                bp.sync({waitFor: Leaving(i), block: Raise()});//
            }
        });

        bp.registerBThread("Trains cannot enter railway " + i + " when the barier is down", function() {
            while (true){
                bp.sync({waitFor: Lower(), block: Entering(i)});//
                bp.sync({waitFor: Raise()});
            }
        });
        bp.registerBThread(i+"Lower the barrier when train is approaching and then raise it as soon as possible", function() {
            let e = undefined
            while(true) {
                bp.sync({waitFor: Approaching(i)});
                e = bp.sync({waitFor: contextEventSet});
                if (e.raised){
                    e = undefined
                    bp.sync({request: Lower(), waitFor:bp.all});
                }
                e = undefined
            }
        });
        

    })(i);
}

bp.registerBThread("Raise", function() {
    let e = undefined
    while(true) {
        e = bp.sync({waitFor: contextEventSet});
        if (!(e.raised) && !(e.trainInside.some(a => a))){
            e = undefined
            bp.sync({request: Raise(), waitFor:bp.all});
        }
        e = undefined
    }
});

bp.registerBThread("Interleave", function() {
    let raised = true
    let trainInside = [false, false]
    let e = undefined
    while(true) {
        e = bp.sync({waitFor: notContext});
        if (e instanceof Lower){
            raised = false
        }
        if (e instanceof Raise){
            raised = true
        }
        if (e instanceof Entering){//Approaching
            trainInside[e.i] = true
        }
        if (e instanceof Leaving){
            trainInside[e.i] = false
        }
        e = undefined
        bp.sync({request: Context(raised, trainInside), block: notContext})
    }
});

