const n = 1;
const Enters = bp.EventSet("Enters", function(evt) {return evt.name.startsWith("Entering")});
const Approachings = bp.EventSet("Approachings", function(evt) {return evt.name.startsWith("Approaching")});
const Leavings = bp.EventSet("Leavings", function(evt) {return evt.name.startsWith("Leaving")});

var p_i = 0;
var p_1_x = 0;
var p_2_x = n;
var p_3_x = 0;
var p_4_x = 0;
var p_5_x = 0;
var p_6_x = n;
var p_7_x = 1;
var p_8_x = 0;
var p_9_x = 0;


bp.registerBThread("stateTitler", function(){
    while( true ) {
        bp.sync({waitFor:bp.all}, p_i+":"+p_1_x+":"+p_2_x+":"+p_3_x+":"+p_4_x+":"+p_5_x+":"+p_6_x+":"+p_7_x+":"+p_8_x+":"+p_9_x);
    }
});


for (var i = 0; i < n; i++){
    (function(i){
        bp.registerBThread("p_" + i + "_1", function() {
            while (true){
                bp.sync({waitFor: bp.Event("Approaching_" + i)});
                p_i += 1;
                bp.sync({waitFor: bp.Event("Leaving_" + i), block:bp.Event("Approaching_" + i)});
            }
        });
        bp.registerBThread("p_" + i + "_2", function() {
            while (true){
                bp.sync({waitFor: bp.Event("Approaching_" + i), block:[bp.Event("Entering_" + i), bp.Event("UnObservableEntering_" + i)]});
                bp.sync({waitFor: [bp.Event("Entering_" + i), bp.Event("UnObservableEntering_" + i)]});
                p_i += 1;
            }
        });
        bp.registerBThread("p_" + i + "_3", function() {
            while (true){
                bp.sync({waitFor: [bp.Event("Entering_" + i), bp.Event("UnObservableEntering_" + i)], block:bp.Event("Leaving_" + i)});
                bp.sync({waitFor: bp.Event("Leaving_" + i)});
                p_i = 0;
            }
        });
        bp.registerBThread("phelper_" + i, function() {
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
    var event;
    while (true){
        if(p_1_x < 1){
            bp.sync({waitFor: Approachings, block:bp.Event("ClosingRequest")});
            p_1_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest"), Approachings]});
            if (event.name.equals("ClosingRequest")){
                p_1_x -= 1;
            } else {
                p_1_x += 1;
            }
        }
    }
});

bp.registerBThread("p_2", function() {
    var event;
    while (true){
        if(p_2_x < 1){
            bp.sync({waitFor: bp.Event("OpeningRequest"), block:bp.Event("ClosingRequest")});
            p_2_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest"), bp.Event("OpeningRequest")]});
            if (event.name.equals("ClosingRequest")){
                p_2_x -= 1;
            } else {
                p_2_x += 1;
            }
        }
    }
});

bp.registerBThread("p_3", function() {
    var event;
    while (true){
        if(p_3_x < 1){
            bp.sync({waitFor: bp.Event("ClosingRequest"), block:bp.Event("OpeningRequest")});
            p_3_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest"), bp.Event("OpeningRequest")]});
            if (event.name.equals("OpeningRequest")){
                p_3_x -= 1;
            } else {
                p_3_x += 1;
            }
        }
    }
});

bp.registerBThread("p_4", function() {
    var event;
    while (true){
        if(p_4_x < 1){
            bp.sync({waitFor: Leavings, block:bp.Event("OpeningRequest")});
            p_4_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("OpeningRequest"), Leavings]});
            if (event.name.equals("OpeningRequest")){
                p_4_x -= 1;
            } else {
                p_4_x += 1;
            }
        }
    }
});

bp.registerBThread("p_5", function() {
    var event;
    while (true){
        if(p_5_x < 1){
            bp.sync({waitFor: bp.Event("ClosingRequest"), block:[bp.Event("Lower"), bp.Event("KeepDown")]});
            p_5_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest"), bp.Event("Lower"), bp.Event("KeepDown")]});
            if (event.name.equals("ClosingRequest")){
                p_5_x += 1;
            } else {
                p_5_x -= 1;
            }
        }
    }
});

bp.registerBThread("p_6", function() {
    var event;
    while (true){
        if(p_6_x >= n){
            event = bp.sync({waitFor: [bp.Event("ClosingRequest"), bp.Event("OpeningRequest"), bp.Event("Raise")]});
            if (event.name.equals("OpeningRequest")){
                p_6_x += 1;
            } else {
                if (event.name.equals("ClosingRequest")){
                    p_6_x -= 1;
                }
            }
        } else {
            if(p_6_x >= 1){
                event = bp.sync({waitFor: [bp.Event("ClosingRequest"), bp.Event("OpeningRequest")], block:bp.Event("Raise")});
                if (event.name.equals("OpeningRequest")){
                    p_6_x += 1;
                } else {
                    p_6_x -= 1;
                }
            } else {
                event = bp.sync({waitFor: bp.Event("OpeningRequest"), block:[bp.Event("Raise"), bp.Event("ClosingRequest")]});
                p_6_x += 1;
            }
        }
    }
});

bp.registerBThread("p_7", function() {
    var event;
    while (true){
        if(p_7_x < 1){
            bp.sync({waitFor: [bp.Event("Raise"), bp.Event("PrematureRaise")], block:bp.Event("Lower")});
            p_7_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("Raise"), bp.Event("Lower"), bp.Event("PrematureRaise")]});
            if (event.name.equals("Raise") || event.name.equals("PrematureRaise")){
                p_7_x += 1;
            } else {
                p_7_x -= 1;
            }
        }
    }
});

bp.registerBThread("p_8", function() {
    var event;
    while (true){
        if(p_8_x < 1){
            event = bp.sync({waitFor: bp.Event("Lower"), block:[bp.Event("Raise"), bp.Event("PrematureRaise"), bp.Event("KeepDown")]});
            if (event.name.equals("Lower")){
                p_8_x += 1;
            } else {
                p_8_x -= 1;
            }
        } else {
            event = bp.sync({waitFor: [bp.Event("Lower"), bp.Event("KeepDown"), bp.Event("Raise"), bp.Event("PrematureRaise")]});
            if (event.name.equals("Lower")){
                p_8_x += 1;
            } else {
                if (event.name.equals("Raise") || event.name.equals("PrematureRaise")){
                    p_8_x -= 1;
                }
            }
        }
    }
});

bp.registerBThread("p_9", function() {
    var event;
    while (true){
        if(p_9_x < 1){
            event = bp.sync({waitFor: [bp.Event("Lower"), bp.Event("KeepDown")], block:[Enters, Leavings]});
            p_9_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("Lower"), bp.Event("KeepDown"), Enters, Leavings]});
            if (event.name.equals("Lower") || event.name.equals("KeepDown")){
                p_9_x += 1;
            }
            if (event.name.startsWith("Leaving")){
                p_9_x -= 1;
            }
        }
    }
});

bp.registerBThread("phelper", function() {
    while (true){
        bp.sync({request: [bp.Event("ClosingRequest"),
                bp.Event("OpeningRequest"),
                bp.Event("Lower"),
                bp.Event("Raise"),
                bp.Event("KeepDown"),
                bp.Event("PrematureRaise")]});
    }
});