/**
 * @author Tao Chen
 */
function CustomerManagement() {
    
    //ioc: @UiField
    this.customerSpecification = null;
    
    //ioc: @UiField
    this.customerList = null;
}

//ioc: @UiHandler
CustomerManagement.prototype.init = function() {
    var that = this;
    this.customerList.data("controller").specification(function() {
        return that.customerSpecification.data("controller").specification();
    });
};

//ioc: @UiHandler
CustomerManagement.prototype.customerSpecification_specificationchanged = function(e) {
    this.customerList.data("controller").refresh();
};
