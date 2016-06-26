/**
 * @author Tao Chen
 */
function AlarmManagement() {
    
    //ioc: @UiField
    this.alarmSpecification = null;
    
    //ioc: @UiField
    this.alarmGrid = null;
}

//ioc: @UiHandler
AlarmManagement.prototype.init = function() {
    var that = this;
    this.alarmGrid.data("controller").specification(function() {
        return that.alarmSpecification.data("controller").specification();
    });
};

//ioc: @UiHandler
AlarmManagement.prototype.alarmSpecification_specificationchanged = function(e) {
    this.alarmGrid.data("controller").refresh();
};
