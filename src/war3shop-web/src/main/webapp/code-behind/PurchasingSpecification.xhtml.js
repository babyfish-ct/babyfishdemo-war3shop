/**
 * @author Tao Chen
 */
function PurchasingSpecification() {
    
    //ioc: @UiField
    this.minCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.maxCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.minTotalPriceNumericTextBox = null;
    
    //ioc: @UiField
    this.maxTotalPriceNumericTextBox = null;
    
    //ioc: @UiField
    this.productDependenciesTbody = null;
    
    //ioc: @UiField
    this.manufacturerDependenciesTbody = null;
}

PurchasingSpecification.prototype.init = function(param) {
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
    this.minCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" })
    this.maxCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" });
    this.minTotalPriceNumericTextBox.kendoNumericTextBoxEx();
    this.maxTotalPriceNumericTextBox.kendoNumericTextBoxEx();
    if (param.enableProductDependencies) {
        this
        .productDependenciesTbody
        .bind("change", function() {
            that._triggerChanged();
        })
        .data("controller")
        .create();
    }
    if (param.enableManufacturerDependencies) {
        this
        .manufacturerDependenciesTbody
        .bind("change", function() {
            that._triggerChanged();
        })
        .data("controller")
        .create();
    }
}

PurchasingSpecification.prototype.specification = function() {
    var specification = {
        minCreationTime: kendo.toString(
                this.minCreationTimeDateTimePicker.data("kendoDateTimePicker").value(), 
                "yyyy-MM-dd HH:mm"),
        maxCreationTime: kendo.toString(
                this.maxCreationTimeDateTimePicker.data("kendoDateTimePicker").value(), 
                "yyyy-MM-dd HH:mm"),
        minTotalPurchasedPrice: this.minTotalPriceNumericTextBox.val(),
        maxTotalPurchasedPrice: this.maxTotalPriceNumericTextBox.val()
    };
    $.extend(
            specification,
            this.productDependenciesTbody.data("controller").specification(),
            this.manufacturerDependenciesTbody.data("controller").specification()       
    );
    return specification;
};

PurchasingSpecification.prototype._triggerChanged = function() {
    var that = this;
    that.dirty = true;
    deferred(function() {
        if (that.dirty) {
            that.dirty = false;
            that.root.trigger("specificationchanged");
        }
    });
};
