/**
 * @author Tao Chen
 */
function OrderView() {
    
    //ioc: @UiField
    this.orderSpecification = null;
    
    //ioc: @UiField
    this.orderGrid = null;
};

OrderView.prototype.init = function(param) {
    var that = this;
    this.orderGrid.data("controller").specification(function() {
        return that.orderSpecification.data("controller").specification();
    });
};

//ioc: @UiHandler
OrderView.prototype.orderSpecification_specificationchanged = function(e) {
    this.orderGrid.data("controller").refresh();
};
