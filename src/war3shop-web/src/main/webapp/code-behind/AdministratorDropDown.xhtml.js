/**
 * @author Tao Chen
 */
function AdministratorDropDown() {
    
    //ioc: @UiField
    this.tagTooltip = null;
    
    //ioc: @UiField
    this.dropDownPanel = null;
};

//ioc: @UiField
AdministratorDropDown.prototype.init = function(param) {
    
    var that = this;
    var close = 
        param.mode == "single" ?
        $() :
        $("<button></button>")
        .text("Close")
        .addClass("k-button")
        .kendoButton()
        .css("float", "right")
        .click(function() {
            that.dropDownPanel.data("kendoDropDownPanel").close();
        });
    var specification = createTemplate("AdministratorSpecification", { fieldWidth: "150px" });
    var grid = createTemplate(
            "AdministratorGrid", 
            { 
                mode: param.mode == "single" ? "select" : "multiselect", 
                autoBind: false 
            }
    );
    
    specification.bind("specificationchanged", function() {
        grid.data("controller").refresh();
    });
    grid.data("controller").specification(function() {
        var spec = specification.data("controller").specification();
        if (param.implicitSpecification) {
            if (typeof(param.implicitSpecification) == "function") {
                $.extend(spec, param.implicitSpecification());
            } else {
                $.extend(spec, param.implicitSpecification);
            }
        }
        return spec;
    });
    grid.data("controller").selectedIds(function() {
        return that.dropDownPanel.data("kendoDropDownPanel").value();
    });
    
    grid
    .bind("dataitemselected", function(e, administrator) {
        that.dropDownPanel.data("kendoDropDownPanel").selectDataItem(administrator, true);
        e.stopPropagation();
    })
    .bind("dataitemunselected", function(e, administrator) {
        that.dropDownPanel.data("kendoDropDownPanel").unselectId(administrator.id, true);
        e.stopPropagation();
    })
    .bind("dataitemchanged", function(e, product) {
        that.dropDownPanel.data("kendoDropDownPanel").selectDataItem(product, true);
        that.dropDownPanel.data("kendoDropDownPanel").close();
        e.stopPropagation();
    });
    
    var dropDownTitle = param.dropDownTitle;
    if (!dropDownTitle) {
        dropDownTitle = "Please select several administrators";
    }
    
    this.dropDownPanel.kendoDropDownPanel({
        placeholder: param.placeholder,
        mode: param.mode,
        tagTemplate: function(administrator) {
            return (
                $("<span></span>")
                .addClass("tag-with-tooltip")
                .text(administrator.name)
                .data(
                        "tooltip", 
                        "<img style='height:120px' src='" + 
                        userImage(administrator) +
                        "'/>"
                )
            );
        },
        template: function() {
            return (
                $("<div></div>")
                .css("padding", "10px")
                .append(
                        $("<div></div")
                        .append(
                                $("<h2></h2>")
                                .css("display", "inline-block")
                                .text(dropDownTitle)
                        )
                        .append(close)
                )
                .append(
                        $("<div></div>")
                        .append(
                                $("<div></div>")
                                .css("float", "left")
                                .css("width", "330px")
                                .addClass("k-block")
                                .append(
                                        $("<div></div>")
                                        .addClass("k-header")
                                        .addClass("panel-header")
                                        .append(
                                                $("<span></span>")
                                                .addClass("panel-title")
                                                .text("Administrator Specification")
                                        )
                                )
                                .append(
                                        $("<div></div>")
                                        .addClass("panel-content")
                                        .append(specification)
                                )
                        )
                        .append(
                                $("<div></div>")
                                .css("margin-left", "350px")
                                .append(
                                        $("<div></div>")
                                        .css("display", "inline-block")
                                        .append(grid.css("width", "700px"))
                                )
                        )
                        .append($("<div></div>").css("clear", "both"))
                )
            );
        },
        open: function() {
            grid.data("controller").refresh();
        }
    });
    this.tagTooltip.kendoTooltip({
        filter: "span.tag-with-tooltip",
        position: param.tagPosition || "top",
        content: function(e) {
            return e.target.data("tooltip");
        }
    });
};

AdministratorDropDown.prototype.value = function() {
    return this.dropDownPanel.data("kendoDropDownPanel").value();
};

AdministratorDropDown.prototype.dataItemValue = function() {
    return this.dropDownPanel.data("kendoDropDownPanel").dataItemValue();
}

AdministratorDropDown.prototype.selectDataItem = function(dataItem) {
    this.dropDownPanel.data("kendoDropDownPanel").selectDataItem(dataItem);
};

AdministratorDropDown.prototype.unselectId = function(id) {
    this.dropDownPanel.data("kendoDropDownPanel").unselectId(id);
};
