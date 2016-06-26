/**
 * @author Tao Chen
 */
function ProductManagement() {
    
    //ioc: @UiField
    this.productSpecification = null;
    
    //ioc: @UiField
    this.productGrid = null;
}

//ioc: @UiHandler
ProductManagement.prototype.init = function() {
    var that = this;
    this.productGrid.data("controller").specification(function() {
        return that.productSpecification.data("controller").specification();
    });
};

//ioc: @UiHandler
ProductManagement.prototype.productSpecification_specificationchanged = function() {
    this.productGrid.data("controller").refresh();
};
