/**
 * @author Tao Chen
 */
function ElementDependenciesTbody() {
    
    //ioc: @UiHandler
    this.hasElementsTr = null;
    
    //ioc: @UiHandler
    this.hasElementsLabel = null;
    
    //ioc: @UiHandler
    this.hasElementSelect = null;
    
    //ioc: @UiHandler
    this.includedElementsTr = null;
    
    //ioc: @UiHandler
    this.includedElementsLabel = null;
    
    //ioc: @UiHandler
    this.includedElementsDropDownTd = null;
    
    //ioc: @UiHandler
    this.excludedElementsTr = null;
    
    //ioc: @UiHandler
    this.excludedElementsLabel = null;
    
    //ioc: @UiHandler
    this.excludedElementsDropDownTd = null;
}

ElementDependenciesTbody.prototype.init = function(param) {
    if (param.enableHasElements) {
        this.hasElementsTr.show();
    }
    this.hasElementsLabel.text(param.hasElementsText || "Has Elements");
    this.includedElementsLabel.text(param.includedElementsText || "Included Elements");
    this.excludedElementsLabel.text(param.excludedElementsText || "Excluded Elements");
    this._hasElementsField = param.hasElementsField || "hasElements";
    this._includedElementsField = param.includedElementsField || "includedElementIds";
    this._excludedElementsField = param.excludedElementsField || "excludedElementIds";
    this._dropDownTemplateName = param.dropDownTemplateName;
    this._includedDropDownTemplateParam = $.extend({}, param.dropDownTemplateParam, param.includedDropDownTemplateParam);
    this._excludedDropDownTemplateParam = $.extend({}, param.dropDownTemplateParam, param.excludedDropDownTemplateParam);
    
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
    
    if (!param.lazyCreate) {
        this.create();
    }
};

ElementDependenciesTbody.prototype.create = function() {
    if (this._created) {
        return;
    }
    this._created = true;
    var that = this;
    this.hasElementSelect.kendoDropDownList({
        change: function() {
            if (this.value() == "2") {
                that.includedElementsTr.hide();
                that.excludedElementsTr.hide();
            } else {
                that.includedElementsTr.show();
                that.excludedElementsTr.show();
            }
        }
    });
    this._includedElementsDropDown = 
        createTemplate(
                this._dropDownTemplateName, 
                this._includedDropDownTemplateParam
        )
        .css("width", "200px")
        .appendTo(this.includedElementsDropDownTd);
    this._excludedElementsDropDown = 
        createTemplate(
                this._dropDownTemplateName, 
                this._excludedDropDownTemplateParam
        )
        .css("width", "200px")
        .appendTo(this.excludedElementsDropDownTd);
    ElementDependenciesTbody._mutexDropDown(
            this._includedElementsDropDown, 
            this._excludedElementsDropDown);
    ElementDependenciesTbody._mutexDropDown(
            this._excludedElementsDropDown, 
            this._includedElementsDropDown);
    this.root.show();
};

ElementDependenciesTbody.prototype.specification = function() {
    var spec = {};
    if (!this._created) {
        return spec;
    }
    if (this.hasElementsTr.is(":visible")) {
        var has = this.hasElementSelect.data("kendoDropDownList").value();
        if (has == "1") {
            spec[this._hasElementsField] = true;
        } else if (has == "2") {
            spec[this._hasElementsField] = false;
            return spec;
        }
    }
    spec[this._includedElementsField] = 
        this
        ._includedElementsDropDown
        .data("controller")
        .value()
        .join(",");
    spec[this._excludedElementsField] = 
        this
        ._excludedElementsDropDown
        .data("controller")
        .value()
        .join(",");
    return spec;
};

ElementDependenciesTbody.prototype.isEmpty = function() {
    if (!this._created) {
        return true;
    }
    if (this.hasElementsTr.is(":visible")) {
        var has = this.hasElementSelect.data("kendoDropDownList").value();
        if (has != "0") {
            return false;
        }
    }
    return (
        this
        ._includedElementsDropDown
        .data("controller")
        .value()
        .length == 0 
        &&
        this
        ._excludedElementsDropDown
        .data("controller")
        .value()
        .length == 0
    );
};

ElementDependenciesTbody._mutexDropDown = function(changedDd, otherDd) {
    var changedC = changedDd.data("controller");
    var otherC = otherDd.data("controller");
    changedDd.bind("change", function() {
        var value = changedC.value();
        for (var i = value.length - 1; i >= 0; i--) {
            otherC.unselectId(value[i]);
        }
    });
};

