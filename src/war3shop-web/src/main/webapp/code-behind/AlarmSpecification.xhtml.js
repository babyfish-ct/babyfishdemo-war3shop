/**
 * @author Tao Chen
 */
function AlarmSpecification() {
    
    //ioc: @UiField
    this.minCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.maxCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.acknowledgedSelect = null;
    
    //ioc: @UiField
    this.keywordTextBox = null;
}

//ioc: @UiHandler
AlarmSpecification.prototype.init = function(param) {
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
    this.minCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" });
    this.maxCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" });
    this.acknowledgedSelect.kendoDropDownList();
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

AlarmSpecification.prototype.specification = function() {
    var spec = {
        minCreationTime: kendo.toString(this.minCreationTimeDateTimePicker.data("kendoDateTimePicker").value(), "yyyy-MM-dd HH:mm"),
        maxCreationTime: kendo.toString(this.maxCreationTimeDateTimePicker.data("kendoDateTimePicker").value(), "yyyy-MM-dd HH:mm"),
        keyword: this.keywordTextBox.val()
    };
    var acknowledgedValue = this.acknowledgedSelect.data("kendoDropDownList").value();
    if (acknowledgedValue == "1") {
        spec.acknowledged = true;
    } else if (acknowledgedValue == "2") {
        spec.acknowledged = false;
    }
    return spec;
};

AlarmSpecification.prototype._triggerChanged = function() {
    var that = this;
    that._dirty = true;
    deferred(function() {
        if (that._dirty) {
            that._dirty = false;
            that.root.trigger("specificationchanged");
        }
    });
}
