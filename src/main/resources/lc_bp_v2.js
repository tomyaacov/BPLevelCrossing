importPackage(Packages.events);
const n = 1;

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
                bp.sync({waitFor: Lower(), block: Entering(i)});
                bp.sync({waitFor: Raise()});
            }
        });

    })(i);
}

bp.registerBThread("", function() {
    while (true){
        bp.sync({request: Lower()});
        bp.sync({request: Raise()});
    }
});


