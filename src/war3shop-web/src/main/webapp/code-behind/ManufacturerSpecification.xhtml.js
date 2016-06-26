/**
 * @author Tao Chen
 */
function ManufacturerSpecification() {
    
    //ioc: @UiField
    this.likeNameTextBox = null;
    
    //ioc: @UiField
    this.raceMultiSelect = null;
    
    //ioc: @UiField
    this.likeEmailTextBox = null;
    
    //ioc: @UiField
    this.likePhoneTextBox = null;
    
    //ioc: @UiField
    this.productDependenciesTbody = null;
    
    //ioc: @UiField
    this.purchaserDependenciesTbody = null;
    
    //ioc: @UiField
    this.includedPurchaserTd = null;
    
    //ioc: @UiField
    this.excludedPurchaserTd = null;
}

ManufacturerSpecification.prototype.init = function(param) {
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
    this.raceMultiSelect.kendoMultiSelect();
    $("input[id], select[id]", this.root).bind("change", function() {
        that._triggerChanged();
    });
    $("input[id]", this.root).bind("keyup", function() {
        that._triggerChanged();
    });
    if (param.enableProductDependencies) {
        this
        .productDependenciesTbody
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
};

ManufacturerSpecification.prototype.specification = function() {
    var specification = {
        likeName: this.likeNameTextBox.val(),
        likeEmail: this.likeEmailTextBox.val(),
        likePhone: this.likePhoneTextBox.val(),
        races: 
            "collection<org.babyfishdemo.war3shop.entities.Race>:" + 
            this.raceMultiSelect.data("kendoMultiSelect").value().join(",")
    };
    $.extend(
            specification, 
            this.productDependenciesTbody.data("controller").specification(),
            this.purchaserDependenciesTbody.data("controller").specification()
    );
    return specification;
};

ManufacturerSpecification.prototype._triggerChanged = function() {
    var that = this;
    that._dirty = true;
    deferred(function() {
        if (that._dirty) {
            that._dirty = false;
            that.root.trigger("specificationchanged");
        }
    });
};
