/**
 * @author Tao Chen
 */
function ManufacturerDropDown() {
    
    //ioc: @UiField
    this.tagTooltip = null;
    
    //ioc: @UiField
    this.dropDownPanel = null;
};

//ioc: @UiField
ManufacturerDropDown.prototype.init = function(param) {
    
    var that = this;
    var mode = param.mode == "single" ? "single" : "multiple";
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
    var specification = createTemplate("ManufacturerSpecification", { fieldWidth: "150px" });
    var grid = createTemplate(
            "ManufacturerGrid", { 
                url: param.url,
                mode: mode== "single" ? "select" : "multiselect", 
                autoBind: false 
            });
    
    specification.bind("specificationchanged", function() {
        grid.data("controller").refresh();
    });
    grid.data("controller").specification(function() {
        return specification.data("controller").specification();
    });
    grid.data("controller").selectedIds(function() {
        return that.dropDownPanel.data("kendoDropDownPanel").value();
    });
    
    grid
    .bind("dataitemselected", function(e, manufacturer) {
        that.dropDownPanel.data("kendoDropDownPanel").selectDataItem(manufacturer, true);
        e.stopPropagation();
    })
    .bind("dataitemunselected", function(e, manufacturer) {
        that.dropDownPanel.data("kendoDropDownPanel").unselectId(manufacturer.id, true);
        e.stopPropagation();
    })
    .bind("dataitemchanged", function(e, product) {
        that.dropDownPanel.data("kendoDropDownPanel").selectDataItem(product, true);
        that.dropDownPanel.data("kendoDropDownPanel").close();
        e.stopPropagation();
    });
    
    this.dropDownPanel.kendoDropDownPanel({
        placeholder: param.placeholder,
        tagTemplate: function(manufacturer) {
            return (
                $("<span></span>")
                .addClass("tag-with-tooltip")
                .text(manufacturer.name)
                .data(
                        "tooltip", 
                        "<img style='height:120px' src='" + 
                        manufacturerImage(manufacturer) +
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
                                .text("Please select several manufacturers")
                        )
                        .append(close)
                )
                .append(
                        $("<div></div>")
                        .append(
                                $("<div></div>")
                                .css("float", "left")
                                .css("width", "230px")
                                .addClass("k-block")
                                .append(
                                        $("<div></div>")
                                        .addClass("k-header")
                                        .addClass("panel-header")
                                        .append(
                                                $("<span></span>")
                                                .addClass("panel-title")
                                                .text("Manufacturer Specification")
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
                                .css("margin-left", "250px")
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

ManufacturerDropDown.prototype.value = function() {
    return this.dropDownPanel.data("kendoDropDownPanel").value();
};

ManufacturerDropDown.prototype.selectDataItem = function(dataItem) {
    this.dropDownPanel.data("kendoDropDownPanel").selectDataItem(dataItem);
};

ManufacturerDropDown.prototype.unselectId = function(id) {
    this.dropDownPanel.data("kendoDropDownPanel").unselectId(id);
};
