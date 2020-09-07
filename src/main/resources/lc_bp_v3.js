importPackage(Packages.events);
const n = 2;
const x = [Approaching(0), Entering(0), Leaving(0), Approaching(1), Entering(1), Leaving(1), Raise(), Lower()];

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

        bp.registerBThread("Lower the barrier when train" + i + " is approaching and then raise it as soon as possible", function() {
            bp.sync({waitFor: Approaching(i)});
            bp.sync({request: Lower()});
            while(true){
                let e=""
                bp.sync({waitFor: Leaving(i)});
                e = bp.sync({request: Raise(), waitFor: Approaching(i)}).name;
                if (e.startsWith("Raise")){
                    bp.sync({waitFor: Approaching(i)})
                } else {
                    e = bp.sync({request: Raise(), waitFor: Entering(i)}).name
                }
                if (!e.startsWith("Entering")){
                    bp.sync({request: Lower()});
                }
            }
        });

    })(i);
}



