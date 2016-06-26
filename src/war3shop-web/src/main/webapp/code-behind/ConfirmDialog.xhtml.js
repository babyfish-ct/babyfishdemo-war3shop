/**
 * @author Tao Chen
 */
function ConfirmDialog() {
    //ioc: @UiField
    this.messageLabel = null;
    
    //ioc: @UiField
    this.okButton = null;
    
    //ioc: @UiField
    this.cancelButton = null;
};

//ioc: @UiHandler
ConfirmDialog.prototype.init = function(param) {
    var that = this;
    this.messageLabel.text(param.message);
    this
    .root
    .appendTo(document.body)
    .kendoWindow({
        title: param.title ? param.title : "Please confirm",
        actions: [ "Close" ],
        resizable: false,
        modal: true,
        pinned: true,
        close: function() {
            that.root.trigger("cancel");
            that.destroy();
        }
    })
    .data("kendoWindow")
    .center();
    this.okButton.kendoButton();
    this.cancelButton.kendoButton();
    if (typeof(param.ok) == "function") {
        this.root.bind("ok", param.ok);
    }
    if (typeof(param.cancel) == "function") {
        this.root.bind("cancel", param.cancel);
    }
}

//ioc: @UiHandler
ConfirmDialog.prototype.okButton_click = function(e) {
    this.root.trigger("ok", e);
    this.destroy();
};

//ioc: @UiHandler
ConfirmDialog.prototype.cancelButton_click = function(e) {
    this.root.trigger("cancel", e);
    this.destroy();
};

ConfirmDialog.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
}
