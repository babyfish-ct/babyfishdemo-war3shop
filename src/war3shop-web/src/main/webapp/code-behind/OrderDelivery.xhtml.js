/**
 * @author Tao Chen
 */
function OrderDelivery() {
    
    //ioc: @UiField
    this.orderSpecification = null;
    
    //ioc: @UiField
    this.orderGrid = null;
};

OrderDelivery.prototype.init = function(param) {
    var that = this;
    this.orderGrid.data("controller").specification(function() {
        return that.orderSpecification.data("controller").specification();
    });
};

//ioc: @UiHandler
OrderDelivery.prototype.orderSpecification_specificationchanged = function(e) {
    this.orderGrid.data("controller").refresh();
};
