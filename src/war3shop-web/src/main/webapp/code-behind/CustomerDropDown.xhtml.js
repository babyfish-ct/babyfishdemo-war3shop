/**
 * @author Tao Chen
 */
function CustomerDropDown() {
    
    //ioc: @UiField
    this.tagTooltip = null;
    
    //ioc: @UiField
    this.dropDownPanel = null;
};

//ioc: @UiField
CustomerDropDown.prototype.init = function(param) {
    
    var that = this;
    var close = 
        $("<button></button>")
        .text("Close")
        .addClass("k-button")
        .kendoButton()
        .css("float", "right")
        .click(function() {
            that.dropDownPanel.data("kendoDropDownPanel").close();
        });
    var customerSpecification = createTemplate("CustomerSpecification");
    var customerList = createTemplate("CustomerList", { mode: "multiselect" });
    customerSpecification.bind("specificationchanged", function() {
        customerList.data("controller").refresh();
    });
    customerList.data("controller").specification(function() {
        var spec = customerSpecification.data("controller").specification();
        if (param.implicitSpecification) {
            if (typeof(param.implicitSpecification) == "function") {
                $.extend(spec, param.implicitSpecification());
            } else {
                $.extend(spec, param.implicitSpecification);
            }
        }
        return spec;
    });
    
    customerList
    .bind("customerselected", function(e, customer) {
        that.dropDownPanel.data("kendoDropDownPanel").selectDataItem(customer, true);
        e.stopPropagation();
    })
    .bind("customerunselected", function(e, customer) {
        that.dropDownPanel.data("kendoDropDownPanel").unselectId(customer.id, true);
        e.stopPropagation();
    });
    
    var dropDownTitle = param.dropDownTitle;
    if (!dropDownTitle) {
        dropDownTitle = "Please select several customers";
    }
    
    this.dropDownPanel.kendoDropDownPanel({
        placeholder: param.placeholder,
        mode: param.mode,
        tagTemplate: function(customer) {
            return (
                $("<span></span>")
                .addClass("tag-with-tooltip")
                .text(customer.name)
                .data(
                        "tooltip", 
                        "<img style='height:140px' src='" + 
                        userImage(customer) +
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
                                .css("width", "350px")
                                .addClass("k-block")
                                .append(
                                        $("<div></div>")
                                        .addClass("k-header")
                                        .addClass("panel-header")
                                        .append(
                                                $("<span></span>")
                                                .addClass("panel-title")
                                                .text("Customer Specification")
                                        )
                                )
                                .append(
                                        $("<div></div>")
                                        .addClass("panel-content")
                                        .append(customerSpecification)
                                )
                        )
                        .append(
                                $("<div></div>")
                                .css("margin-left", "370px")
                                .append(
                                        $("<div></div>")
                                        .css("display", "inline-block")
                                        .append(customerList)
                                )
                        )
                        .append($("<div></div>").css("clear", "both"))
                )
            );
        },
        open: function() {
            customerList.data("controller").reselect(that.dropDownPanel.data("kendoDropDownPanel").value());
            customerList.data("controller").refresh();
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

CustomerDropDown.prototype.value = function() {
    return this.dropDownPanel.data("kendoDropDownPanel").value();
};

CustomerDropDown.prototype.dataItemValue = function() {
    return this.dropDownPanel.data("kendoDropDownPanel").dataItemValue();
};

CustomerDropDown.prototype.selectDataItem = function(dataItem) {
    this.dropDownPanel.data("kendoDropDownPanel").selectDataItem(dataItem);
};

CustomerDropDown.prototype.unselectId = function(id) {
    this.dropDownPanel.data("kendoDropDownPanel").unselectId(id);
};
