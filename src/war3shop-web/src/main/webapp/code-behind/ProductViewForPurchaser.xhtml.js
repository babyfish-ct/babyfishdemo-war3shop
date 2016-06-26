/**
 * @author Tao Chen
 */
function ProductViewForPurchaser() {
    
    //ioc: @UiField
    this.productSpecification = null;
    
    //ioc: @UiField
    this.productGrid = null;
}

//ioc: @UiHandler
ProductViewForPurchaser.prototype.init = function() {
    var that = this;
    this.productGrid.data("controller").specification(function() {
        return that.productSpecification.data("controller").specification();
    });
};

//ioc: @UiHandler
ProductViewForPurchaser.prototype.productSpecification_specificationchanged = function() {
    this.productGrid.data("controller").refresh();
};
