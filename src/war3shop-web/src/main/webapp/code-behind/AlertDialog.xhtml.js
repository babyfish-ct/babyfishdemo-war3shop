/**
 * @author Tao Chen
 */
function AlertDialog() {
    //ioc: @UiField
    this.messageLabel = null;
    
    //ioc: @UiField
    this.okButton = null;
};

//ioc: @UiHandler
AlertDialog.prototype.init = function(param) {
    var that = this;
    this
    .messageLabel
    .css("min-width", param.minWidth || "200px")
    .css("max-width", param.maxWidth || "600px")
    .text(param.message);
    this
    .root
    .appendTo(document.body) //Self manager, should return false
    .kendoWindow({
        title: param.title || "Alert",
        actions: [ "Close" ],
        resizable: false,
        modal: true,
        pinned: true,
        close: function() {
            that.destroy();
        }
    })
    .data("kendoWindow")
    .center();
    this.okButton.kendoButton();
    if (typeof(param.close) == "function") {
        this.root.bind("close", param.close);
    }
}

//ioc: @UiHandler
AlertDialog.prototype.okButton_click = function(e) {
    this.destroy();
};

AlertDialog.prototype.destroy = function() {
    try {
        this.root.trigger("close");
    } finally {
        this.root.data("kendoWindow").destroy();
    }
}
