/**
 * @author Tao Chen
 */
function CartDialog() {
    
    //ioc: @UiField
    this.orderDetailPanel = null;
    
    //ioc: @UiField
    this.sumbitButton = null;
    
    //ioc: @UiField
    this.totalExpectedMoneyTd = null;
    
    //ioc: @UiField
    this.totalReducedMoneyTd = null;
    
    //ioc: @UiField
    this.totalGiftMoneyTd = null;
    
    //ioc: @UiField
    this.totalActualMoneyTd = null;
}

CartDialog.prototype.init = function(param) {
    var that = this;
    $("tr", this.root).each(function() {
        var input = $("input[name]", this);
        $("label", this).attr("for", input.attr("id"));
        $("span.k-invalid-msg", this).attr("data-for", input.attr("name"));
    });
    this.sumbitButton.kendoButton();
    this
    .root
    .appendTo(document.body)
    .kendoWindow({
        title: "The detail information of my cart",
        actions: [ "Minimize", "Maximize", "Close" ],
        resizable: true,
        modal: false,
        width: "800px",
        height: "400px",
        minWidth: "700px",
        minHeight: "300px",
        close: function() {
            that.destroy();
        }
    })
    .data("kendoWindow")
    .center();
    this.sumbitButton.data("kendoButton").enable(false);
};

CartDialog.prototype.setOrder = function(order) {
    if (order) {
        this.orderDetailPanel.data("controller").setOrder(order);
        if (order && order.orderItems.length > 0) {
            this.totalExpectedMoneyTd.text(
                    (order.totalMoney.expectedMoney + order.totalMoney.giftMoney) +
                    "(" +
                    order.totalMoney.expectedMoney +
                    " + " +
                    order.totalMoney.giftMoney +
                    ")"
            );
            this.totalReducedMoneyTd.text(order.totalMoney.reducedMoney);
            this.totalGiftMoneyTd.text(order.totalMoney.giftMoney);
            this.totalActualMoneyTd.text(order.totalMoney.actualMoney);
            this.sumbitButton.data("kendoButton").enable(true);
        } else {
            this.totalExpectedMoneyTd.text("0");
            this.totalReducedMoneyTd.text("0");
            this.totalGiftMoneyTd.text("0");
            this.totalActualMoneyTd.text("0");
            this.sumbitButton.data("kendoButton").enable(false);
        }
    }
};

CartDialog.prototype.destroy = function() {
    this.root.trigger("closing");
    this.root.data("kendoWindow").destroy();
};

//ioc: @UiHandler
CartDialog.prototype.submitButton_click = function(e) {
    var that = this;
    createTemplate("OrderAddressDialog")
    .bind("ok", function(e) {
        jsonp({
            url: "sale/create-order.spring",
            data: $(e.target).data("controller").data(),
            success: function() {
                that.root.trigger("ordercreated");
                that.destroy();
            },
            error: function(e) {
                createErrorDialog(e);
            }
        });
    });
};
