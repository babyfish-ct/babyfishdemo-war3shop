/**
 * @author Tao Chen
 */
function ProductDropDown() {
    
    //ioc: @UiField
    this.tagTooltip = null;
    
    //ioc: @UiField
    this.dropDownPanel = null;
};

//ioc: @UiField
ProductDropDown.prototype.init = function(param) {
    
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
    var specification = createTemplate("ProductSpecification", { 
        fieldWidth: "150px",
        enableActiveChoices: param.enableActiveChoices,
        enableInventoryRange: param.enableInventoryRange
    });
    var grid = createTemplate(
            "ProductGrid", { 
                url: param.url, 
                mode: mode == "single" ? "select" : "multiselect",
                autoBind: false
            }
        );
    
    specification.bind("specificationchanged", function() {
        grid.data("controller").refresh();
    });
    grid.data("controller").specification(function() {
        var spec = specification.data("controller").specification();
        var implicitSpec = param.implicitSpecification;
        if (implicitSpec) {
            if (typeof(implicitSpec) == "function") {
                implicitSpec = implicitSpec();
            }
            $.extend(spec, implicitSpec);
        }
        return spec;
    });
    grid.data("controller").selectedIds(function() {
        return that.dropDownPanel.data("kendoDropDownPanel").value();
    });
    
    grid
    .bind("dataitemselected", function(e, product) {
        that.dropDownPanel.data("kendoDropDownPanel").selectDataItem(product, true);
        e.stopPropagation();
    })
    .bind("dataitemunselected", function(e, product) {
        that.dropDownPanel.data("kendoDropDownPanel").unselectId(product.id, true);
        e.stopPropagation();
    })
    .bind("dataitemchanged", function(e, product) {
        that.dropDownPanel.data("kendoDropDownPanel").selectDataItem(product, true);
        that.dropDownPanel.data("kendoDropDownPanel").close();
        e.stopPropagation();
    });
    
    this.dropDownPanel.kendoDropDownPanel({
        mode: mode,
        placeholder: param.placeholder,
        tagTemplate: function(product) {
            return (
                $("<span></span>")
                .addClass("tag-with-tooltip")
                .text(product.name)
                .data("tooltip", productImageHtml(product, true))
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
                                .text("Please select several products")
                        )
                        .append(close)
                )
                .append(
                        $("<div></div>")
                        .append(
                                $("<div></div>")
                                .css("float", "left")
                                .css("width", "240px")
                                .addClass("k-block")
                                .append(
                                        $("<div></div>")
                                        .addClass("k-header")
                                        .addClass("panel-header")
                                        .append(
                                                $("<span></span>")
                                                .addClass("panel-title")
                                                .text("Product Specification")
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
                                .css("margin-left", "260px")
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

ProductDropDown.prototype.value = function() {
    return this.dropDownPanel.data("kendoDropDownPanel").value();
};

ProductDropDown.prototype.dataItemValue = function() {
    return this.dropDownPanel.data("kendoDropDownPanel").dataItemValue();
};

ProductDropDown.prototype.selectDataItem = function(dataItem) {
    this.dropDownPanel.data("kendoDropDownPanel").selectDataItem(dataItem);
};

ProductDropDown.prototype.unselectId = function(id) {
    this.dropDownPanel.data("kendoDropDownPanel").unselectId(id);
};

ProductDropDown.prototype.coreWidget = function(width) {
    return this.root.find(".k-widget");
};
