importPackage(Packages.events);

// //assert bthreads
bp.registerBThread("utils_model_compare", function() {
    bp.sync({request: bp.Event("Violation")});
    bp.ASSERT(false, "Violation!");
});

bp.registerBThread("utils_logger", function() {
    var set = HashSet();
    while (true){
        u = bp.sync({request: Update(), block:NotUpdate});
        if (u.requestedAndNotBlockedSystem2.length == 0){
            set = HashSet();
        }
        set.addAll(u.requestedAndNotBlockedSystem2);
        bp.sync({waitFor: bp.all()});
    }

});