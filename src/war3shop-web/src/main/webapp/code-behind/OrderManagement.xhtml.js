/**
 * @author Tao Chen
 */
function OrderManagement() {
    
    //ioc: @UiField
    this.orderSpecification = null;
    
    //ioc: @UiField
    this.orderGrid = null;
};

OrderManagement.prototype.init = function(param) {
    var that = this;
    this.orderGrid.data("controller").specification(function() {
        return that.orderSpecification.data("controller").specification();
    });
};

//ioc: @UiHandler
OrderManagement.prototype.orderSpecification_specificationchanged = function(e) {
    this.orderGrid.data("controller").refresh();
};
