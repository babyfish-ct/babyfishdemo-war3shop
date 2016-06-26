/**
 * @author Tao Chen
 */
function SalePanel() {
    
    //ioc: @UiField
    this.productDiv = null;
    
    //ioc: @UiField
    this.photoImg = null;
    
    //ioc: @UiField
    this.nameSpan = null;
    
    //ioc: @UiField
    this.typeSpan = null;
    
    //ioc: @UiField
    this.raceSpan = null;
    
    //ioc: @UiField
    this.priceSpan = null;
    
    //ioc: @UiField
    this.quantitySpan = null;
    
    //ioc: @UiField
    this.buyQuantitySpan = null;
    
    //ioc: @UiField
    this.descriptionButton = null;
    
    //ioc: @UiField
    this.buyButton = null;
    
    //ioc: @UiField
    this.preferentialDiv = null;
    
    //ioc: @UiField
    this.manufacturerDiv = null;
    
    //ioc: @UiField
    this.descriptionDiv = null;
    
    //ioc: @UiField
    this.descriptionTextDiv = null;
    
    //ioc: @UiField
    this.buttonTooltipDiv = null;
    
    //ioc: @UiField
    this.preferentialBackButton = null;
    
    //ioc: @UiField
    this.manufacturerBackButton = null;
    
    //ioc: @UiField
    this.descriptionBackButton = null;
    
    //ioc: @UiField
    this.preferentialStartDateSpan = null;
    
    //ioc: @UiField
    this.preferentialEndDateSpan = null;
    
    //ioc: @UiField
    this.preferentialThresholdTypeSpan = null;
    
    //ioc: @UiField
    this.preferentialActionTypeSpan = null;
    
    //ioc: @UiField
    this.gridTooltipDiv = null;
    
    //ioc: @UiField
    this.preferentialItemGridDiv = null;
    
    //ioc: @UiField
    this.manufacturerListView = null;
    
    this._preferentialFlipFx = null;
    
    this._descriptionFlipFx = null;
    
    this._productId = null;
}

//ioc: @UiHandler
SalePanel.prototype.init = function(param) {
    var sale = param.sale;
    if (sale == null) {
        throw new Error("param.sale must be specified.");
    }
    this._productId = sale.product.id;
    this.photoImg.setProductImage(sale.product);
    this.nameSpan.text(sale.product.name);
    this.priceSpan.text(sale.product.price);
    this.typeSpan.text(org.babyfishdemo.war3shop.entities.ProductType.values()[sale.product.type].toString());
    this.raceSpan.text(org.babyfishdemo.war3shop.entities.Race.values()[sale.product.race].toString());
    this.quantitySpan.text(sale.product.inventory != null ? sale.product.inventory.quantity : "0");
    this.buyQuantitySpan.text("0");
    if (sale.preferential && sale.preferential.items) {
        var thresholdType = org.babyfishdemo.war3shop.entities.PreferentialThresholdType.values()[sale.preferential.thresholdType];
        var actionType = org.babyfishdemo.war3shop.entities.PreferentialActionType.values()[sale.preferential.actionType];
        this.preferentialButton.kendoButton();
        this.preferentialStartDateSpan.text(kendo.toString(sale.preferential.startDate, "yyyy-MM-dd"));
        this.preferentialEndDateSpan.text(kendo.toString(sale.preferential.endDate, "yyyy-MM-dd"));
        this.preferentialThresholdTypeSpan.text(thresholdType.toString());
        this.preferentialActionTypeSpan.text(actionType.toString());
        var columns = [];
        var dataBound = null;
        if (thresholdType == org.babyfishdemo.war3shop.entities.PreferentialThresholdType.QUANTITY) {
            columns.push({
                field: "thresholdQuantity",
                title: "Quantity>="
            });
        } else {
            columns.push({
                field: "thresholdMoney",
                title: "Moeny>="
            });
        }
        if (actionType == org.babyfishdemo.war3shop.entities.PreferentialActionType.MULTIPLIED_BY_PERCENTAGE) {
            columns.push({
                field: "percentageFactor",
                title: "Percentage",
                template: function(item) {
                    return item.percentageFactor + "%";
                }
            });
        } else if (actionType == org.babyfishdemo.war3shop.entities.PreferentialActionType.REDUCE_MONEY) {
            columns.push({
                field: "reducedMoney",
                title: "Reduced",
                template: function(item) {
                    return "-" + item.reducedMoney;
                }
            });
        } else {
            columns.push({
                field: "giftProduct.name",
                title: "Gift",
                template: function(item) {
                    return item.giftProduct.name;
                }
            });
            columns.push({
                field: "giftQuantity",
                title: "Gift Quantity"
            });
            dataBound = function(e) {
                var kendoGrid = e.sender;
                $("tr[data-uid]", kendoGrid.element).each(function() {
                    var item = kendoGrid.dataItem(this);
                    $(this).data("tooltip", productImageHtml(item.giftProduct));
                });
            };
            this.gridTooltipDiv.kendoTooltipEx({
                filter: "tr[data-uid]",
                position: "left",
                animation: false,
                showAfter: 0,
                content: function(e) {
                    return e.target.data("tooltip");
                }
            });
        }
        this.preferentialItemGridDiv.kendoGrid({
            dataSource: {
                data: sale.preferential.items
            },
            scrollable: false,
            columns: columns,
            dataBound: dataBound
        });
    } else {
        this.preferentialButton.hide();
    }
    if (sale.product && sale.product.manufacturers) {
        var manufacturers = sale.product.manufacturers;
        for (var i = 0; i < manufacturers.length; i++) {
            var manufacturer = manufacturers[i];
            $("<div></div>")
            .css("margin-bottom", "5px")
            .addClass("k-widget")
            .append(
                $("<div></div>")
                .css("padding", "5px")
                .css("margin", "5px")
                .css("font-weight", "bold")
                .addClass("k-widget")
                .text(manufacturer.name)
            )
            .append(
                $("<div></div>")
                .append(
                    $("<img/>")
                    .css("float", "left")
                    .width("100px")
                    .attr("src", manufacturerImage(manufacturer))
                )
                .append(
                    $("<div></div>")
                    .css("margin-left", "100px")
                    .append(
                        $("<div></div>")
                        .css("display", "inline-block")
                        .html(manufacturer.description)
                    )
                )
                .append($("<div></div>").css("clear", "both"))
            )
            .appendTo(this.manufacturerListView);
        }
    }
    this.manufacturerButton.kendoButton();
    this.descriptionButton.kendoButton();
    this.buyButton.kendoButton();
    this.descriptionTextDiv.html(sale.product.description);
    this.preferentialBackButton.kendoButton();
    this.manufacturerBackButton.kendoButton();
    this.descriptionBackButton.kendoButton();
    this.buttonTooltipDiv.kendoTooltipEx({
        filter: "button[tooltip]",
        position: "top",
        animation: false,
        showAfter: 0,
        content: function(e) {
            return e.target.attr("tooltip");
        }
    });
    this._preferentialFlipFx =
        kendo
        .fx(this.root)
        .flipHorizontal(this.productDiv, this.preferentialDiv)
        .duration(500);
    this._manufacturerFlipFx =
        kendo
        .fx(this.root)
        .flipHorizontal(this.productDiv, this.manufacturerDiv)
        .duration(500);
    this._descriptionFlipFx = 
        kendo
        .fx(this.root)
        .flipHorizontal(this.productDiv, this.descriptionDiv)
        .duration(500);
    this.setBuyQuantityByOrder(null);
};

//ioc: @UiHandler
SalePanel.prototype.buyButton_click = function(e) {
    var that = this;
    jsonp({
        url: "sale/add-product-into-cart.spring",
        data: { productId: this._productId },
        success: function() {
            that.root.trigger("bought");
        },
        error: function(e) {
            createErrorDialog(e);
        }
    });
};

//ioc: @UiHandler
SalePanel.prototype.preferentialButton_click = function(e) {
    this._stopFlipFx();
    this._preferentialFlipFx.play();
};

//ioc: @UiHandler
SalePanel.prototype.manufacturerButton_click = function(e) {
    this._stopFlipFx();
    this._manufacturerFlipFx.play();
}

//ioc: @UiHandler
SalePanel.prototype.descriptionButton_click = function(e) {
    this._stopFlipFx();
    this._descriptionFlipFx.play();
};

//ioc: @UiHandler
SalePanel.prototype.preferentialBackButton_click = function(e) {
    this._stopFlipFx();
    this._preferentialFlipFx.reverse();
};

//ioc: @UiHandler
SalePanel.prototype.manufacturerBackButton_click = function(e) {
    this._stopFlipFx();
    this._manufacturerFlipFx.reverse();
};

//ioc: @UiHandler
SalePanel.prototype.descriptionBackButton_click = function(e) {
    this._stopFlipFx();
    this._descriptionFlipFx.reverse();
};

SalePanel.prototype.setBuyQuantityByOrder = function(order) {
    var buyQuantity = 0;
    if (order && order.orderItems != null) {
        for (var i = order.orderItems.length - 1; i >= 0; i--) {
            var orderItem = order.orderItems[i];
            if (orderItem.product.id == this._productId) {
                buyQuantity = orderItem.quantity;
                break;
            }
        }
    }
    this.buyQuantitySpan.text(buyQuantity);
    var quantity = parseInt(this.quantitySpan.text());
    this.buyButton.data("kendoButton").enable(quantity > buyQuantity);
};

SalePanel.prototype._stopFlipFx = function() {
    this._preferentialFlipFx.stop();
    this._manufacturerFlipFx.stop();
    this._descriptionFlipFx.stop();
}
