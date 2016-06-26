/**
 * @author Tao Chen
 */
function CustomerSpecification() {
    
    //ioc: @UiField
    this.likeNameTextBox = null;
    
    //ioc: @UiField
    this.likeEmailTextBox = null;
    
    //ioc: @UiField
    this.activeTbody = null;
    
    //ioc: @UiField
    this.activeDropDownList = null;
    
    //ioc: @UiField
    this.minCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.maxCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.likePhoneTextBox = null;
    
    //ioc: @UiField
    this.likeAddressTextBox = null;
}

//ioc: @UiHandler
CustomerSpecification.prototype.init = function(param) {
    $("tr", this.root).each(function() {
        var input = $("input[name],select[name]", this);
        $("label", this).attr("for", input.attr("id"));
        $("td:eq(0)", this).css("text-align", "right");
    });
    var that = this;
    $("tr", this.root).each(function() {
        var label = $("label", this);
        if (label.length) {
            var target = $("*[id]", this);
            if (target.length) {
                label.attr("for", target.attr("id"));
            }
        }
    });
    this.activeDropDownList.kendoDropDownList();
    this.minCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" });
    this.maxCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" });
    if (param.enableActiveChoices) {
        this.activeTbody.show();
    }
    /*
     * It is important to use deffered operation, 
     * please see the _mutexRoleMultiSelect and _mutexMultiSelect
     */
    $("input, select", this.root).bind("change", function() {
        that._triggerChanged();
    });
    $("input", this.root).bind("keyup", function() {
        that._triggerChanged();
    });
};

CustomerSpecification.prototype.specification = function() {
    var spec = {
        likeName: this.likeNameTextBox.val(),
        likeEmail: this.likeEmailTextBox.val(),
        minCreationTime: kendo.toString(this.minCreationTimeDateTimePicker.data("kendoDateTimePicker").value(), "yyyy-MM-dd HH:mm"),
        maxCreationTime: kendo.toString(this.maxCreationTimeDateTimePicker.data("kendoDateTimePicker").value(), "yyyy-MM-dd HH:mm"),
        likePhone: this.likePhoneTextBox.val(),
        likeAddress: this.likeAddressTextBox.val()
    };
    if (this.activeTbody.is(":visible")) {
        var value = this.activeDropDownList.data("kendoDropDownList").value();
        if (value == 1) {
            spec.active = true;
        } else if (value == 2) {
            spec.active = false;
        }
    }
    return spec;
};

CustomerSpecification.prototype._triggerChanged = function() {
    var that = this;
    that._dirty = true;
    deferred(function() {
        if (that._dirty) {
            that._dirty = false;
            that.root.trigger("specificationchanged");
        }
    });
}
