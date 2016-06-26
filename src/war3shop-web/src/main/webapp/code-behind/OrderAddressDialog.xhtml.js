/**
 * @author Tao Chen
 */
function OrderAddressDialog() {
    // ioc: @UiField
    this.addressTextBox = null;
    
    // ioc: @UiField
    this.phoneTextBox = null;
    
    // ioc: @UiField
    this.okButton = null;
    
    // ioc: @UiField
    this.cancelButton = null;
};

//ioc: @UiHandler
OrderAddressDialog.prototype.init = function(param) {
    var that = this;
    
    $("tr", this.root).each(function() {
        var input = $("input,textarea", this);
        $("label", this).attr("for", input.attr("id"));
        $("span.k-invalid-msg", this).attr("data-for", input.attr("name"));
    });
    
    this.root.kendoValidator();
    
    this
    .root
    .appendTo(document.body)
    .kendoWindow({
        title: "Confirm address and phone for this order",
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
    this.cancelButton.kendoButton();
    if (param.data) {
        that.addressTextBox.val(param.data.address);
        that.phoneTextBox.val(param.data.phone);
    } else {
        jsonp({
            url: "authorization/current-user.spring",
            success: function(user) {
                that.addressTextBox.val(user.address);
                that.phoneTextBox.val(user.phone);
            }
        });
    }
}

//ioc: @UiHandler
OrderAddressDialog.prototype.okButton_click = function(e) {
    if (!this.root.data("kendoValidator").validate()) {
        return;
    }
    var that = this;
    that.root.trigger("ok");
    that.destroy();
};

//ioc: @UiHandler
OrderAddressDialog.prototype.cancelButton_click = function(e) {
    this.root.trigger("cancel");
    this.destroy();
};

OrderAddressDialog.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
};

OrderAddressDialog.prototype.data = function() {
    return {
        address: this.addressTextBox.val(),
        phone: this.phoneTextBox.val()
    };
};
