/**
 * @author Tao Chen
 */
function OrderSpecification() {
    
    //ioc: @UiField
    this.deliveredDropDownList = null;
    
    //ioc: @UiField
    this.minActualMoneyTextBoxEx = null;
    
    //ioc: @UiField
    this.maxActualMoneyTextBoxEx = null;
    
    //ioc: @UiField
    this.minCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.maxCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.customerDependenciesTbody = null;
    
    //ioc: @UiField
    this.productDependenciesTbody = null;
    
    //ioc: @UiField
    this.deliverymanDependenciesTbody = null;
};

OrderSpecification.prototype.init = function(param) {
    var that = this;
    $("tr", this.root).each(function() {
        var input = $("input[id], select[id]", this);
        $("label", this).attr("for", input.attr("id"));
        $("td:eq(0)", this).css("text-align", "right");
    });
    $("input[id], select[id]", this.root).bind("change", function() {
        that._triggerChanged();
    });
    $("input[id]", this.root).bind("keyup", function() {
        that._triggerChanged();
    });
    this.deliveredDropDownList.kendoDropDownList();
    this
    .minActualMoneyTextBoxEx
    .bind("valuechange", function() {
        that._triggerChanged();
    })
    .kendoNumericTextBoxEx();
    this
    .maxActualMoneyTextBoxEx
    .bind("valuechange", function() {
        that._triggerChanged();
    })
    .kendoNumericTextBoxEx();
    this.minCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" });
    this.maxCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" });
    if (param.enableCustomerDependencies) {
        this.customerDependenciesTbody.data("controller").create();
        this.customerDependenciesTbody.bind("change", function() {
            that._triggerChanged();
        });
    }
    if (param.enableProductDependencies) {
        this.productDependenciesTbody.data("controller").create();
        this.productDependenciesTbody.bind("change", function() {
            that._triggerChanged();
        });
    }
    if (param.enableDeliverymanDependencies) {
        this.deliverymanDependenciesTbody.data("controller").create();
        this.deliverymanDependenciesTbody.bind("change", function() {
            that._triggerChanged();
        });
    }
};

OrderSpecification.prototype.specification = function() {
    var specification = {
        minActualMoney: this.minActualMoneyTextBoxEx.val(),
        maxActualMoney: this.maxActualMoneyTextBoxEx.val(),
        minCreationTime: kendo.toString(
                this.minCreationTimeDateTimePicker.data("kendoDateTimePicker").value(),
                "yyyy-MM-dd HH:mm"
        ),
        maxCreationTime: kendo.toString(
                this.maxCreationTimeDateTimePicker.data("kendoDateTimePicker").value(),
                "yyyy-MM-dd HH:mm"
        )
    };
    var deliveredValue = this.deliveredDropDownList.data("kendoDropDownList").value();
    if (deliveredValue == 1) {
        specification.delivered = true;
    } else if (deliveredValue == 2) {
        specification.delivered = false;
    }
    $.extend(
        specification,
        this.customerDependenciesTbody.data("controller").specification(),
        this.productDependenciesTbody.data("controller").specification(),
        this.deliverymanDependenciesTbody.data("controller").specification()
    );
    return specification;
};

OrderSpecification.prototype._triggerChanged = function() {
    var that = this;
    that.dirty = true;
    deferred(function() {
        if (that.dirty) {
            that.dirty = false;
            that.root.trigger("specificationchanged");
        }
    });
};
