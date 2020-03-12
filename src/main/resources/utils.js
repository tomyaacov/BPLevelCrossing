// //assert bthreads
bp.registerBThread("utils_model_compare", function() {
    bp.sync({request: bp.Event("Violation")});
    bp.ASSERT(false, "Violation!");
});