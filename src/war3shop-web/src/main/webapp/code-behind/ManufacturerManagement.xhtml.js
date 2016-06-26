/**
 * @author Tao Chen
 */
function ManufacturerManagement() {
    
    //ioc: @UiField
    this.manufacturerSpecification = null;
    
    //ioc: @UiField
    this.manufacturerGrid = null;
}

//ioc: @UiHandler
ManufacturerManagement.prototype.init = function() {
    var that = this;
    this.manufacturerGrid.data("controller").specification(function() {
        return that.manufacturerSpecification.data("controller").specification();
    });
};

//ioc: @UiHandler
ManufacturerManagement.prototype.manufacturerSpecification_specificationchanged = function() {
    this.manufacturerGrid.data("controller").refresh();
};
