/**
 * @author Tao Chen
 */
function OrderDetailPanel() {
    
    //ioc: @UiField
    this.itemTooltip = null;
    
    //ioc: @UiField
    this.orderItemGrid = null;
    
    //ioc: @UiField
    this.giftItemGrid = null;
    
    this._thresholdType = null;
    
    this._actionType = null;
}

OrderDetailPanel.prototype.init = function(param) {
    var that = this;
    var orderItemGridQuantityTemplate = null;
    var defaultDataBind = function(e) {
        var kendoGrid = this.element.data("kendoGrid");
        $("tr[data-uid]", this.element).each(function(e) {
            var item = kendoGrid.dataItem(this);
            $(this).data("tooltip", productImageHtml((item.giftProduct || item.product)));
            if (!item.preferentialActions || item.preferentialActions.length == 0) {
                $(".k-hierarchy-cell>.k-icon", this).remove();
            }
        });
    };
    var orderItemGridDataBound = defaultDataBind;
    if (param.modifiable) {
        orderItemGridQuantityTemplate = "<input type='numeric' style='width:60px' class='k--grid-quantity'/>";
        orderItemGridDataBound = function(e) {
            defaultDataBind.call(this, e);
            var kendoGrid = this.element.data("kendoGrid");
            $("tr[data-uid]", this.element).each(function(e) {
                var input = $(".k--grid-quantity", this);
                var orderItem = kendoGrid.dataItem(this);
                input
                .data("orderItem", orderItem)
                .bind("valuechange", function(e) {
                    that.orderItemGrid_quantityChange(e);
                })
                .kendoNumericTextBoxEx()
                .data("kendoNumericTextBoxEx")
                .value(orderItem.quantity);
            });
        };
    }
    var columns = [
        {
            title: "Name",
            template: function(orderItem) {
                return orderItem.product.name;
            },
            field: "product.name" //For UI order
        },
        {
            field: "instantUnitPrice",
            title: "Unit Price",
        },
        {
            field: "quantity",
            title: "Quantity",
            template: orderItemGridQuantityTemplate
        },
        {
            title: "Total Price",
            template: function(orderItem) {
                return orderItem.instantUnitPrice * orderItem.quantity;
            }
        },
        {
            title: "Total Reduced Money",
            template: function(orderItem) {
                return orderItem.totalMoney.reducedMoney;
            }
        },
        {
            title: "Total Gift Money",
            template: function(orderItem) {
                return orderItem.totalMoney.giftMoney;
            }
        }
    ];
    if (param.modifiable) {
        columns.push({
            width: "100px",
            command: [
                {
                    name: "-delete",
                    text: "Delete",
                    click: function(e) {
                        that.orderItemGrid_delete(e);
                    }
                }
            ]
        });
    }
    var orderItemGridOptions = {
        sortable: true,
        reorderable: true,
        resizable: true,
        scrollable: false,
        height: param.orderItemGridHeight,
        toolbar: "Order Items",
        columns: columns,
        dataBound: orderItemGridDataBound,
        detailTemplate: "<div class=\"k--preferential-actions\"></div>",
        detailInit: function(e) {
            var preferentialActionsDiv = e.detailCell.find(".k--preferential-actions");
            var preferentialActions = e.data.preferentialActions;
            if (preferentialActions.length == 0) {
                return;
            }
            var ul = $("<ul></ul>").appendTo(preferentialActionsDiv);
            for (var i = 0; i < preferentialActions.length; i++) {
                var preferentialAction = preferentialActions[i];
                var thresholdType = org.babyfishdemo.war3shop.entities.PreferentialThresholdType.values()[preferentialAction.thresholdType].name();
                var actionType = org.babyfishdemo.war3shop.entities.PreferentialActionType.values()[preferentialAction.actionType].name();
                var li = $("<li></li>");
                var text, ruleHtml;
                if (actionType == "SEND_GIFT") {
                    text = "Gift money is " + preferentialAction.totalPreferential.giftMoney;
                } else {
                    text = "Reduced money is " + preferentialAction.totalPreferential.reducedMoney;
                }
                text +=", because the preferential rule is matched for " + preferentialAction.matchedCount + " times";
                if (thresholdType == "QUANTITY") {
                    ruleHtml = 
                        "[Rule: If quantity of this product reaches " + 
                        preferentialAction.thresholdQuantity + 
                        ", ";
                } else {
                    ruleHtml = 
                        "[Rule: If money of this product reaches " + 
                        preferentialAction.thresholdMoney + 
                        ", ";
                }
                if (actionType == "MULTIPLIED_BY_PERCENTAGE") {
                    ruleHtml += "*" + preferentialAction.percentageFactor + "%";
                } else if (actionType == "REDUCE_MONEY") {
                    ruleHtml += "-" + preferentialAction.reducedMoney;
                } else {
                    ruleHtml += 
                        "send " + 
                        preferentialAction.giftQuantity +
                        " <span class=\"k--tooltip k-button\">\"" +
                        preferentialAction.giftProduct.name + 
                        "\"</span>s";
                }
                ruleHtml += "]";
                var rule = $("<div style=\"padding-left:50px;font-style:italic;\"></div>").html(ruleHtml);
                if (preferentialAction.giftProduct) {
                    rule
                    .find(".k--tooltip")
                    .data("tooltip", productImageHtml(preferentialAction.giftProduct));
                }
                ul.append($("<li></li>").text(text).append(rule));
            }
        }
    };
    var giftItemGridOptions = {
        sortable: true,
        reorderable: true,
        resizable: true,
        scrollable: false,
        toolbar: "Gift Items",
        dataBound: defaultDataBind,
        columns: [
            {
                title: "Name",
                template: function(giftItem) {
                    return giftItem.product.name;
                }
            },
            {
                title: "Quantity",
                field: "quantity"
            },
            {
                title: "Instant Unit Price",
                field: "instantUnitPrice"
            }
        ]
    };
    this.orderItemGrid.kendoGrid(orderItemGridOptions);
    this.giftItemGrid.kendoGrid(giftItemGridOptions);
    this.itemTooltip.kendoTooltipEx({
        filter: "tr[data-uid], .k--tooltip",
        position: "left",
        animation: false,
        showAfter: 0,
        content: function(e) {
            return e.target.data("tooltip");
        }
    });
};

OrderDetailPanel.prototype.orderItemGrid_quantityChange = function(e) {
    var e = $.Event(
            "orderitemquantitychanged", 
            { 
                orderItem: $(e.target).data("orderItem"), 
                quantity: $(e.target).val() 
            }
    );
    this.root.trigger(e);
};

OrderDetailPanel.prototype.orderItemGrid_delete = function(e) {
    var e = $.Event(
            "orderitemdeleted",
            {
                orderItem: this.orderItemGrid.data("kendoGrid").dataItem($(e.target).closest("tr"))
            }
    );
    this.root.trigger(e);
};

OrderDetailPanel.prototype.setOrder = function(order) {
    if (!order) {
        order = {
            orderItems: [],
            giftItems: []
        };
    }
    this.orderItemGrid.data("kendoGrid").setDataSource(new kendo.data.DataSource({ data: order.orderItems }));
    this.giftItemGrid.data("kendoGrid").setDataSource(new kendo.data.DataSource({ data: order.giftItems }));
};
