const n = 1;
const Enters = bp.EventSet("Enters", function(evt) {return evt.name.startsWith("Entering")});
const Approachings = bp.EventSet("Approachings", function(evt) {return evt.name.startsWith("Approaching")});
const Leavings = bp.EventSet("Leavings", function(evt) {return evt.name.startsWith("Leaving")});

for (var i = 0; i < n; i++){
    (function(i){
        bp.registerBThread("p_" + i + "_1", function() {
            while (true){
                bp.sync({waitFor: bp.Event("Approaching_" + i)});
                bp.sync({waitFor: bp.Event("Leaving_" + i), block:bp.Event("Approaching_" + i)});
            }
        });
        bp.registerBThread("p_" + i + "_2", function() {
            while (true){
                bp.sync({waitFor: bp.Event("Approaching_" + i), block:[bp.Event("Entering_" + i), bp.Event("UnObservableEntering_" + i)]});
                bp.sync({waitFor: [bp.Event("Entering_" + i), bp.Event("UnObservableEntering_" + i)]});
            }
        });
        bp.registerBThread("p_" + i + "_3", function() {
            while (true){
                bp.sync({waitFor: [bp.Event("Entering_" + i), bp.Event("UnObservableEntering_" + i)], block:bp.Event("Leaving_" + i)});
                bp.sync({waitFor: bp.Event("Leaving_" + i)});
            }
        });
        bp.registerBThread("helper_" + i, function() {
            while (true){
                bp.sync({request: [bp.Event("Approaching_" + i),
                        bp.Event("Entering_" + i),
                        bp.Event("Leaving_" + i),
                        bp.Event("UnObservableEntering_" + i)]});
            }
        });
    })(i);
}

bp.registerBThread("p_1", function() {
    var x = 0;
    var event;
    while (true){
        if(x < 1){
            bp.sync({waitFor: Approachings, block:bp.Event("ClosingRequest")});
            x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest"), Approachings]});
            if (event.name.equals("ClosingRequest")){
                x -= 1;
            } else {
                x += 1;
            }
        }
    }
});

bp.registerBThread("p_2", function() {
    var x = n;
    var event;
    while (true){
        if(x < 1){
            bp.sync({waitFor: bp.Event("OpeningRequest"), block:bp.Event("ClosingRequest")});
            x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest"), bp.Event("OpeningRequest")]});
            if (event.name.equals("ClosingRequest")){
                x -= 1;
            } else {
                x += 1;
            }
        }
    }
});

bp.registerBThread("p_3", function() {
    var x = 0;
    var event;
    while (true){
        if(x < 1){
            bp.sync({waitFor: bp.Event("ClosingRequest"), block:bp.Event("OpeningRequest")});
            x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest"), bp.Event("OpeningRequest")]});
            if (event.name.equals("OpeningRequest")){
                x -= 1;
            } else {
                x += 1;
            }
        }
    }
});

bp.registerBThread("p_4", function() {
    var x = 0;
    var event;
    while (true){
        if(x < 1){
            bp.sync({waitFor: Leavings, block:bp.Event("OpeningRequest")});
            x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("OpeningRequest"), Leavings]});
            if (event.name.equals("OpeningRequest")){
                x -= 1;
            } else {
                x += 1;
            }
        }
    }
});

bp.registerBThread("p_5", function() {
    var x = 0;
    var event;
    while (true){
        if(x < 1){
            bp.sync({waitFor: bp.Event("ClosingRequest"), block:[bp.Event("Lower"), bp.Event("KeepDown")]});
            x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest"), bp.Event("Lower"), bp.Event("KeepDown")]});
            if (event.name.equals("ClosingRequest")){
                x += 1;
            } else {
                x -= 1;
            }
        }
    }
});

bp.registerBThread("p_6", function() {
    var x = n;
    var event;
    while (true){
        if(x >= n){
            event = bp.sync({waitFor: [bp.Event("ClosingRequest"), bp.Event("OpeningRequest"), bp.Event("Raise")]});
            if (event.name.equals("OpeningRequest")){
                x += 1;
            } else {
                if (event.name.equals("ClosingRequest")){
                    x -= 1;
                }
            }
        } else {
            if(x >= 1){
                event = bp.sync({waitFor: [bp.Event("ClosingRequest"), bp.Event("OpeningRequest")], block:bp.Event("Raise")});
                if (event.name.equals("OpeningRequest")){
                    x += 1;
                } else {
                    x -= 1;
                }
            } else {
                event = bp.sync({waitFor: bp.Event("OpeningRequest"), block:[bp.Event("Raise"), bp.Event("ClosingRequest")]});
                    x += 1;
            }
        }
    }
});

bp.registerBThread("p_7", function() {
    var x = 1;
    var event;
    while (true){
        if(x < 1){
            bp.sync({waitFor: [bp.Event("Raise"), bp.Event("PrematureRaise")], block:bp.Event("Lower")});
            x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("Raise"), bp.Event("Lower"), bp.Event("PrematureRaise")]});
            if (event.name.equals("Raise") || event.name.equals("PrematureRaise")){
                x += 1;
            } else {
                x -= 1;
            }
        }
    }
});

bp.registerBThread("p_8", function() {
    var x = 0;
    var event;
    while (true){
        if(x < 1){
            event = bp.sync({waitFor: bp.Event("Lower"), block:[bp.Event("Raise"), bp.Event("PrematureRaise"), bp.Event("KeepDown")]});
            if (event.name.equals("Lower")){
                x += 1;
            } else {
                x -= 1;
            }
        } else {
            event = bp.sync({waitFor: [bp.Event("Lower"), bp.Event("KeepDown"), bp.Event("Raise"), bp.Event("PrematureRaise")]});
            if (event.name.equals("Lower")){
                x += 1;
            } else {
                if (event.name.equals("Raise") || event.name.equals("PrematureRaise")){
                    x -= 1;
                }
            }
        }
    }
});

bp.registerBThread("p_9", function() {
    var x = 0;
    var event;
    while (true){
        if(x < 1){
            event = bp.sync({waitFor: [bp.Event("Lower"), bp.Event("KeepDown")], block:[Enters, Leavings]});
            x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("Lower"), bp.Event("KeepDown"), Enters, Leavings]});
            if (event.name.equals("Lower") || event.name.equals("KeepDown")){
                x += 1;
            }
            if (event.name.startsWith("Leaving")){
                x -= 1;
            }
        }
    }
});

bp.registerBThread("helper", function() {
    while (true){
        bp.sync({request: [bp.Event("ClosingRequest"),
                bp.Event("OpeningRequest"),
                bp.Event("Lower"),
                bp.Event("Raise"),
                bp.Event("KeepDown"),
                bp.Event("PrematureRaise")]});
    }
});