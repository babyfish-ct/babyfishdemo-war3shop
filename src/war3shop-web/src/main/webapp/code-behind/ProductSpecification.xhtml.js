/**
 * @author Tao Chen
 */
function ProductSpecification() {
    
    //ioc: @UiField
    this.specificationTable = null;
    
    //ioc: @UiField
    this.likeNameTextBox = null;
    
    //ioc: @UiField
    this.minPriceTextBox = null;
    
    //ioc: @UiField
    this.maxPriceTextBox = null;
    
    //ioc: @UiField
    this.typeMultiSelect = null;
    
    //ioc: @UiField
    this.raceMultiSelect = null;
    
    //ioc: @UiField
    this.creationTimeRangeTbody = null;
    
    //ioc: @UiField
    this.minCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.maxCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.inventoryRangeTbody = null;
    
    //ioc: @UiField
    this.minInventoryTextBox = null;
    
    //ioc: @UiField
    this.maxInventoryTextBox = null;

    //ioc: @UiField
    this.manufacturerDependenciesTbody = null;
    
    //ioc: @UiField
    this.purchaserDependenciesTbody = null;
    
    //ioc: @UiField
    this.activeTbody = null;
    
    //ioc: @UiField
    this.activeDropDownList = null;
}

ProductSpecification.prototype.init = function(param) {
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
    this.minPriceTextBox.kendoNumericTextBoxEx();
    this.maxPriceTextBox.kendoNumericTextBoxEx();
    this.typeMultiSelect.kendoMultiSelect();
    this.raceMultiSelect.kendoMultiSelect();
    if (param.enableActiveChoices) {
        this.activeDropDownList.kendoDropDownList();
        this.activeTbody.show();
    }
    if (param.enableCreationTimeRange) {
        this.minCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" })
        this.maxCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" });
        this.creationTimeRangeTbody.show();
    }
    if (param.enableInventoryRange) {
        this.minInventoryTextBox.kendoNumericTextBoxEx();
        this.maxInventoryTextBox.kendoNumericTextBoxEx();
        this.inventoryRangeTbody.show();
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
    if (param.enablePurchaserDependencies) {
        this
        .purchaserDependenciesTbody
        .bind("change", function() {
            that._triggerChanged();
        })
        .data("controller")
        .create();
    }
}

ProductSpecification.prototype.specification = function() {
    var specification = {
        likeName: this.likeNameTextBox.val(),
        minPrice: this.minPriceTextBox.val(),
        maxPrice: this.maxPriceTextBox.val(),
        types: 
            "collection<org.babyfishdemo.war3shop.entities.ProductType>:" +
            this
            .typeMultiSelect
            .data("kendoMultiSelect")
            .value()
            .join(","),
        races:
            "collection<org.babyfishdemo.war3shop.entities.Race>:" +
            this
            .raceMultiSelect
            .data("kendoMultiSelect")
            .value()
            .join(",")
    };
    if (this.activeTbody.is(":visible")) {
        var value = this.activeDropDownList.data("kendoDropDownList").value();
        if (value == 1) {
            specification.active = true;
        } else if (value == 2) {
            specification.active = false;
        }
    }
    if (this.creationTimeRangeTbody.is(":visible")) {
        specification.minCreationTime = kendo.toString(
                this.minCreationTimeDateTimePicker.data("kendoDateTimePicker").value(), 
                "yyyy-MM-dd HH:mm");
        specification.maxCreationTime = kendo.toString(
                this.maxCreationTimeDateTimePicker.data("kendoDateTimePicker").value(), 
                "yyyy-MM-dd HH:mm");
    }
    if (this.inventoryRangeTbody.is(":visible")) {
        specification.minInventory = this.minInventoryTextBox.val();
        specification.maxInventory = this.maxInventoryTextBox.val();
    }
    $.extend(
            specification,
            this.manufacturerDependenciesTbody.data("controller").specification(),
            this.purchaserDependenciesTbody.data("controller").specification()
    );
    return specification;
};

ProductSpecification.prototype.isEmpty = function() {
    if (this.likeNameTextBox.val().length) {
        return false;
    }
    if (this.minPriceTextBox.val().length) {
        return false;
    }
    if (this.maxPriceTextBox.val().length) {
        return false;
    }
    if (this.typeMultiSelect.data("kendoMultiSelect").value().length != 0) {
        return false;
    }
    if (this.raceMultiSelect.data("kendoMultiSelect").value().length != 0) {
        return false;
    }
    if (this.creationTimeRangeTbody.is(":visible")) {
        if (this.minCreationTimeDateTimePicker.value()) {
            return false;
        }
        if (this.maxCreationTimeDateTimePicker.value()) {
            return false;
        }
    }
    if (this.inventoryRangeTbody.is(":visible")) {
        if (this.minInventoryTextBox.val().length) {
            return false;
        }
        if (this.maxInventoryTextBox.val().length) {
            return false;
        }
    }
    if (!this.manufacturerDependenciesTbody.data("controller").isEmpty()) {
        return false;
    }
    if (!this.purchaserDependenciesTbody.data("controller").isEmpty()) {
        return false;
    }
    return true;
};

ProductSpecification.prototype._triggerChanged = function() {
    var that = this;
    that.dirty = true;
    deferred(function() {
        if (that.dirty) {
            that.dirty = false;
            that.root.trigger("specificationchanged");
        }
    });
};
