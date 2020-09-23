importPackage(Packages.events);
const n = 1;
const x = [Approaching(0), Entering(0), Leaving(0), Approaching(1), Entering(1), Leaving(1), Raise(), Lower()];
const y = [Context(true, [true, true]), Context(true, [false, true]), Context(true, [true, false]), Context(true, [false, false])];
const z = [Context(false, [true, true]), Context(false, [false, true]), Context(false, [true, false]), Context(false, [false, false])];
const notContext = bp.EventSet("notContext", e => ! (e instanceof Context))
const contextEventSet = bp.EventSet("contextEventSet", e =>  (e instanceof Context))

for (var i = 0; i < n; i++){
    (function(i){
        bp.registerBThread("Trains keep comming to railway " + i + " and progress in Approaching, Entring, and Leaving sequence", function() {
            while (true){
                bp.sync({request: Approaching(i)});
                bp.sync({request: Entering(i)});//
                bp.sync({request: Leaving(i)});
            }
        });

        bp.registerBThread("Trains cannot enter railway " + i + " when the barier is down", function() {
            while (true){
                bp.sync({waitFor: Lower(), block: Entering(i)});//
                bp.sync({waitFor: Raise()});
            }
        });

        bp.registerBThread(i+"Lower the barrier when train is approaching and then raise it as soon as possible", function() {
            while(true) {
                bp.sync({waitFor: Approaching(i)});
                bp.sync({request: Lower(), waitFor:Entering(i)});
            }
        });
        

    })(i);
}

bp.registerBThread("The barrier can be raised when there is no train in railway and the barrier is down", function() {
    let e = undefined
    while(true) {
        e = bp.sync({waitFor: contextEventSet});
        if (!(e.raised) && !(e.trainInside.some(a => a))){
            e = undefined
            bp.sync({request: Raise(), waitFor: notContext});
        }
        e = undefined
    }
});

bp.registerBThread("interleave lower raise", function() {
    while (true){
        bp.sync({waitFor: Lower(), block: Raise()});//
        bp.sync({waitFor: Raise(), block: Lower()});
    }  
});

bp.registerBThread("Interleave", function() {
    let ctx = Context(true, [false, false])
    while(true) {
        bp.sync({request: ctx, block: notContext})
        e = bp.sync({waitFor: notContext});
        if (e instanceof Lower){
            ctx.raised = false
        }
        if (e instanceof Raise){
            ctx.raised = true
        }
        if (e instanceof Entering){//Approaching
            ctx.trainInside[e.i] = true
        }
        if (e instanceof Leaving){
            ctx.trainInside[e.i] = false
        }
    }
});

