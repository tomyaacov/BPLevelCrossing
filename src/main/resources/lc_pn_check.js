const n = 1;
const Enters = bp.EventSet("Enters", function(evt) {return evt.name.startsWith("Entering")});
const Approachings = bp.EventSet("Approachings", function(evt) {return evt.name.startsWith("Approaching")});
const Leavings = bp.EventSet("Leavings", function(evt) {return evt.name.startsWith("Leaving")});
const ClosingRequests = bp.EventSet("ClosingRequests", function(evt) {return evt.name.startsWith("ClosingRequest_0")});

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
                bp.sync({waitFor: bp.Event("Approaching_" + i), block:bp.Event("Entering_" + i)});
                bp.sync({waitFor: bp.Event("Entering_" + i)});
            }
        });
        bp.registerBThread("p_" + i + "_3", function() {
            while (true){
                bp.sync({waitFor: bp.Event("Entering_" + i), block:bp.Event("Leaving_" + i)});
                bp.sync({waitFor: bp.Event("Leaving_" + i)});
            }
        });
        bp.registerBThread("phelper_" + i, function() {
            while (true){
                bp.sync({request: [bp.Event("Approaching_" + i),
                        bp.Event("Entering_" + i),
                        bp.Event("Leaving_" + i)]});
            }
        });
    })(i);
}

bp.registerBThread("p_1", function() {
    let p_1_x = 0;
    let event;
    while (true){
        if(p_1_x < 1){
            bp.sync({waitFor: Approachings, block:bp.Event("ClosingRequest_0")});
            p_1_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest_0"), Approachings]}).name;
            if (event.equals("ClosingRequest_0")){
                p_1_x -= 1;
            } else {
                p_1_x += 1;
            }
        }
    }
});

bp.registerBThread("p_2", function() {
    let p_2_x = n;
    let event;
    while (true){
        if(p_2_x < 1){
            bp.sync({waitFor: bp.Event("OpeningRequest"), block:bp.Event("ClosingRequest_0")});
            p_2_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest_0"), bp.Event("OpeningRequest")]}).name;
            if (event.equals("ClosingRequest_0")){
                p_2_x -= 1;
            } else {
                p_2_x += 1;
            }
        }
    }
});

bp.registerBThread("p_3", function() {
    let p_3_x = 0;
    let event;
    while (true){
        if(p_3_x < 1){
            bp.sync({waitFor: bp.Event("ClosingRequest_0"), block:bp.Event("OpeningRequest")}).name;
            p_3_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest_0"), bp.Event("OpeningRequest")]});
            if (event.equals("OpeningRequest")){
                p_3_x -= 1;
            } else {
                p_3_x += 1;
            }
        }
    }
});

bp.registerBThread("p_4", function() {
    let p_4_x = 0;
    let event;
    while (true){
        if(p_4_x < 1){
            bp.sync({waitFor: Leavings, block:bp.Event("OpeningRequest")});
            p_4_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("OpeningRequest"), Leavings]}).name;
            if (event.equals("OpeningRequest")){
                p_4_x -= 1;
            } else {
                p_4_x += 1;
            }
        }
    }
});

bp.registerBThread("p_5", function() {
    let p_5_x = 0;
    let event;
    while (true){
        if(p_5_x < 1){
            bp.sync({waitFor: bp.Event("ClosingRequest_0"), block:[bp.Event("Lower"), bp.Event("KeepDown")]});
            p_5_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("ClosingRequest_0"), bp.Event("Lower"), bp.Event("KeepDown")]}).name;
            if (event.equals("ClosingRequest_0")){
                p_5_x += 1;
            } else {
                p_5_x -= 1;
            }
        }
    }
});

bp.registerBThread("p_6", function() {
    let p_6_x = n;
    let event;
    while (true){
        if(p_6_x >= n){
            event = bp.sync({waitFor: [bp.Event("ClosingRequest_0"), bp.Event("OpeningRequest"), bp.Event("Raise")]}).name;
            if (event.equals("OpeningRequest")){
                p_6_x += 1;
            } else {
                if (event.equals("ClosingRequest_0")){
                    p_6_x -= 1;
                }
            }
        } else {
            if(p_6_x >= 1){
                event = bp.sync({waitFor: [bp.Event("ClosingRequest_0"), bp.Event("OpeningRequest")], block:bp.Event("Raise")}).name;
                if (event.equals("OpeningRequest")){
                    p_6_x += 1;
                } else {
                    p_6_x -= 1;
                }
            } else {
                event = bp.sync({waitFor: bp.Event("OpeningRequest"), block:[bp.Event("Raise"), bp.Event("ClosingRequest_0")]}).name;
                p_6_x += 1;
            }
        }
    }
});

bp.registerBThread("p_7", function() {
    let p_7_x = 1;
    let event;
    while (true){
        if(p_7_x < 1){
            bp.sync({waitFor: [bp.Event("Raise"), bp.Event("PrematureRaise")], block:bp.Event("Lower")});
            p_7_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("Raise"), bp.Event("Lower"), bp.Event("PrematureRaise")]}).name;
            if (event.equals("Raise") || event.equals("PrematureRaise")){
                p_7_x += 1;
            } else {
                p_7_x -= 1;
            }
        }
    }
});

bp.registerBThread("p_8", function() {
    let p_8_x = 0;
    let event;
    while (true){
        if(p_8_x < 1){
            event = bp.sync({waitFor: bp.Event("Lower"), block:[bp.Event("Raise"), bp.Event("PrematureRaise"), bp.Event("KeepDown")]}).name;
            if (event.equals("Lower")){
                p_8_x += 1;
            } else {
                p_8_x -= 1;
            }
        } else {
            event = bp.sync({waitFor: [bp.Event("Lower"), bp.Event("KeepDown"), bp.Event("Raise"), bp.Event("PrematureRaise")]}).name;
            if (event.equals("Lower")){
                p_8_x += 1;
            } else {
                if (event.equals("Raise") || event.equals("PrematureRaise")){
                    p_8_x -= 1;
                }
            }
        }
    }
});

bp.registerBThread("p_9", function() {
    let p_9_x = 0;
    let event;
    while (true){
        if(p_9_x < 1){
            event = bp.sync({waitFor: [bp.Event("Lower"), bp.Event("KeepDown")], block:[Enters, Leavings]}).name;
            p_9_x += 1;
        } else {
            event = bp.sync({waitFor: [bp.Event("Lower"), bp.Event("KeepDown"), Enters, Leavings]}).name;
            if (event.equals("Lower") || event.equals("KeepDown")){
                p_9_x += 1;
            }
            if (event.startsWith("Leaving")){
                p_9_x -= 1;
            }
        }
    }
});

bp.registerBThread("phelper", function() {
    while (true){
        bp.sync({request: [bp.Event("ClosingRequest_0"),
                bp.Event("OpeningRequest"),
                bp.Event("Lower"),
                bp.Event("Raise"),
                bp.Event("KeepDown")]});
    }
});