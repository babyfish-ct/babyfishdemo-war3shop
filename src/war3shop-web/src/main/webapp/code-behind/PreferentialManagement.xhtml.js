/**
 * @author Tao Chen
 */
function PreferentialManagement() {
    
    //ioc: @UiField
    this.preferentialSpecification = null;
    
    //ioc: @UiField
    this.preferentialGrid = null;
}

//ioc: @UiHandler
PreferentialManagement.prototype.init = function() {
    var that = this;
    this.preferentialGrid.data("controller").specification(function() {
        return that.preferentialSpecification.data("controller").specification();
    });
};

//ioc: @UiHandler
PreferentialManagement.prototype.preferentialSpecification_specificationchanged = function() {
    this.preferentialGrid.data("controller").refresh();
};
