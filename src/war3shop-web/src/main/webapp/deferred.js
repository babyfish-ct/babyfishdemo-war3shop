/**
 * @author Tao Chen
 */
/*
 * Like "com.google.gwt.user.client.DeferredCommand.addCommand(Command)" of GWT
 */
function deferred(handler) {
    if (typeof(handler) != "function") {
        throw new Error("The argument must be function");
    }
    deferred._handlers[deferred._handlers.length] = handler;
    if (deferred._handlers.length != 0) {
        setTimeout(function() {
            var snapshot = deferred._handlers;
            deferred._handlers = [];
            for (var i = 0; i < snapshot.length; i++) {
                snapshot[i]();
            }
        }, 0);
    }
}

deferred._oldHandlers = [];
deferred._handlers = [];
