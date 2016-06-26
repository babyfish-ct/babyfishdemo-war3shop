/**
 * @author Tao Chen
 */
function PreferentialSpecification() {
    
    //ioc: @UIField
    this.repeatableDropDownList = null;
    
    //ioc: @UiField
    this.minDatePicker = null;
    
    //ioc: @UiField
    this.maxDatePicker = null;
    
    //ioc: @UiField
    this.thresholdTypeMultiSelect = null;
    
    //ioc: @UiField
    this.actionTypeMultiSelect = null;
    
    //ioc: @UiField
    this.productDependenciesTbody = null;
    
    //ioc: @UiField
    this.giftProductDependenciesTbody = null;
    
    //ioc: @UiField
    this.activeDropDownList = null;
}

PreferentialSpecification.prototype.init = function(param) {
    var that = this;
    $("tr", this.root).each(function() {
        var input = $("input[id], select[id]", this);
        $("label", this).attr("for", input.attr("id"));
        $("td:eq(0)", this).css("text-align", "right");
    });
    if (typeof(param.fieldWidth) != "undefined") {
        $("tr", this.root).each(function() {
            $("td:eq(1)", this).find("input, select").css("width", param.fieldWidth);
        });
    }
    $("input[id], select[id]", this.root).bind("change", function() {
        that._triggerChanged();
    });
    $("input[id]", this.root).bind("keyup", function() {
        that._triggerChanged();
    });
    this.minDatePicker.kendoDatePicker({ format: "yyyy-MM-dd" })
    this.maxDatePicker.kendoDatePicker({ format: "yyyy-MM-dd" });
    this.thresholdTypeMultiSelect.kendoMultiSelect();
    this.activeDropDownList.kendoDropDownList();
    this.actionTypeMultiSelect.kendoMultiSelect();
    if (param.enableProductDependencies) {
        this
        .productDependenciesTbody
        .bind("change", function() {
            that._triggerChanged();
        })
        .data("controller")
        .create();
    }
    if (param.enableGiftProductDependencies) {
        this
        .giftProductDependenciesTbody
        .bind("change", function() {
            that._triggerChanged();
        })
        .data("controller")
        .create();
    }
}

PreferentialSpecification.prototype.specification = function() {
    var specification = {
        minDate: kendo.toString(
                this.minDatePicker.data("kendoDatePicker").value(), 
                "yyyy-MM-dd"),
        maxDate: kendo.toString(
                this.maxDatePicker.data("kendoDatePicker").value(), 
                "yyyy-MM-dd"),
        thresholdTypes: 
                "collection<org.babyfishdemo.war3shop.entities.PreferentialThresholdType>:" +
                this.thresholdTypeMultiSelect.data("kendoMultiSelect").value(),
        actionTypes: 
                "collection<org.babyfishdemo.war3shop.entities.PreferentialActionType>:" +
                this.actionTypeMultiSelect.data("kendoMultiSelect").value()
    };
    var activeValue = this.activeDropDownList.data("kendoDropDownList").value();
    if (activeValue == 1) {
        specification.active = true;
    } else if (activeValue == 2) {
        specification.active = false;
    }
    $.extend(
            specification,
            this.productDependenciesTbody.data("controller").specification(),
            this.giftProductDependenciesTbody.data("controller").specification()
    );
    return specification;
};

PreferentialSpecification.prototype._triggerChanged = function() {
    var that = this;
    that.dirty = true;
    deferred(function() {
        if (that.dirty) {
            that.dirty = false;
            that.root.trigger("specificationchanged");
        }
    });
};
