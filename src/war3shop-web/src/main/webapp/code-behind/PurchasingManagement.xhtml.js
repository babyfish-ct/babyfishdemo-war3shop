/**
 * @author Tao Chen
 */
function PurchasingManagement() {
    
    //ioc: @UiField
    this.purchasingSpecification = null;
    
    //ioc: @UiField
    this.purchasingGrid = null;
}

//ioc: @UiHandler
PurchasingManagement.prototype.init = function() {
    var that = this;
    this.purchasingGrid.data("controller").specification(function() {
        return that.purchasingSpecification.data("controller").specification();
    });
};

//ioc: @UiHandler
PurchasingManagement.prototype.purchasingSpecification_specificationchanged = function() {
    this.purchasingGrid.data("controller").refresh();
};
