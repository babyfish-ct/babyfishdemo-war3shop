/**
 * @author Tao Chen
 */
function SaleView() {
    
    //ioc: @UiField
    this.panelBar = null;
    
    //ioc: @UiField
    this.productListView = null;
    
    //ioc: @UiField
    this.productPager = null;
    
    //ioc: @UiField
    this.productSpecification = null;
    
    //ioc: @UiField
    this.orderedColumnList = null;
    
    //ioc: @UiField
    this.orderItemGrid = null;
    
    //ioc: @UiField
    this.viewDetailInformationButton = null;
    
    this._currentPageIndex = 1;
    
    this._intervalId = null;
    
    this._clonedDOMWrapper = null;
    
    this._clonedDOM = null;
    
    this._criteriaSlide = null;
    
    this._cartSlide = null;
    
    this._lastFxTime = 0;
    
    this._colCount = 1;   
    
    this._cartDialog = null;
    
    this._order = null;
};

//Callback of page
SaleView.prototype.isFullLayout = function() {
    return true;
}

//ioc: @UiHandler
SaleView.prototype.init = function() {
    var that = this;
    
    this.viewDetailInformationButton.kendoButton();
    
    var calcPageSize = function() {
        var colCount = Math.floor(that.productListView.innerWidth() / SaleView.CELL_WIDTH);
        var rowCount = Math.floor(that.productListView.innerHeight() / SaleView.CELL_HEIGHT);
        return (that._colCount = (colCount > 1 ? colCount : 1)) * (rowCount > 1 ? rowCount : 1);
    };
    this._resizeHandler = function() {
        var pageSize = calcPageSize();
        if (pageSize != dataSource.pageSize()) {
            that._currentPageIndex = 1;
            dataSource.pageSize(pageSize);
        } else {
            /*
             * It is necessary to re-layout,
             * because this event will be fired when the user
             * change the display suddenly 
             * but the pageSize is not changed.
             */
            $(".k-sale-list-view-item", that.productListView)
            .each(function(index) {
                var row = Math.floor(index / that._colCount);
                var col = index % that._colCount;
                $(this)
                .css("left", col * SaleView.CELL_WIDTH + "px")
                .css("top", row * SaleView.CELL_HEIGHT + "px")
            });
        }
    };
    $(window).bind("resize", this._resizeHandler);
    
    // Avoid the issue of kendo UI, 
    // let this template is added into the page 
    // and the layout is applied at first,
    // then create the splitter.
    deferred(function() {
        that.root.kendoSplitter({
            panes: [
                { collapsible: true, size: "380px" },
                { collapsible: false }
            ],
            resize: function() {
                deferred(function() {
                    that._resizeHandler();
                });
            }
        });
    });
    var dataSource = new kendo.data.DataSource({
        transport: {
            read: { 
                url: "sale/sales.spring",
                dataType: "jsonp",
                data: function() {
                    var psController = that.productSpecification.data("controller");
                    if (psController) {
                        return psController.specification();
                    }
                }
            },
            parameterMap: function(data, type) {
                if (type == "read") {
                    var productQueryPath = 
                        "this.description;" +
                        "this.manufacturers.description;" +
                        "this.inventory;";
                    $("li", that.orderedColumnList).each(function() {
                        var column = $("span[column]", this).attr("column");
                        var mode = $("a.k-state-selected", this).attr("mode");
                        if (mode) {
                            productQueryPath += "pre order by " + column + " " + mode + ";";
                        }
                    });
                    var preferentialQueryPath = "this.items.giftProduct";
                    return parameterMap(
                            data, 
                            {
                                productQueryPath: productQueryPath,
                                preferentialQueryPath: preferentialQueryPath
                            }
                    );
                }
            }
        },
        pageSize: 20,
        serverPaging: true,
        serverSorting: true,
        schema: {
            data: "entities",
            total: "totalRowCount",
            errors: "exceptionMessage"
        },
        error: function(e) {
            createErrorDialog(e);
        },
        error: function(e) {
            createErrorDialog(e);
        }
    });
    this.productListView.kendoListView({
        autoBind: false,
        dataSource: dataSource,
        template: function(e) {
            return (
                    "<div class='k-sale-list-view-item' product-id='" + 
                    e.product.id + 
                    "'></div>"
            );
        },
        dataBound: function() {
            var sales = this.dataSource.data();
            $(".k-sale-list-view-item", this.element)
            .each(function(index) {
                var row = Math.floor(index / that._colCount);
                var col = index % that._colCount;
                if (row % 2 != col % 2) {
                    $(this).addClass("k-alt");
                }
                var id = parseInt($(this).attr("product-id"));
                var sale = null;
                for (var i = sales.length - 1; i >= 0; i--) {
                    if (sales[i].product.id == id) {
                        sale = sales[i];
                        break;
                    }
                }
                $(this)
                .css("box-sizing", "border-box")
                .css("position", "absolute")
                .css("left", col * SaleView.CELL_WIDTH + "px")
                .css("top", row * SaleView.CELL_HEIGHT + "px")
                .css("width", SaleView.CELL_WIDTH)
                .css("height", SaleView.CELL_HEIGHT);
                createTemplate("SalePanel", { sale: sale })
                .appendTo(this)
                .bind("bought", function() {
                    that.refreshCart();
                });
            });
            if (that._order) {
                that.refreshBuyQuantity(that._order);
            }
        }
    });
    this
    .productPager
    .kendoPager({ 
        autoBind: false, 
        dataSource: dataSource,
        change: function() {
            var page = this.page();
            if (page < 1) {
                page = 1;
            }
            if (that._currentPageIndex != page) {
                that.pageturn(page > that._currentPageIndex);
                that._currentPageIndex = page;
            }
        }
    });
    deferred(function() {
        var pageSize = calcPageSize();
        if (pageSize != dataSource.pageSize()) {
            dataSource.pageSize(pageSize);
        } else {
            dataSource.read();
        }
    });
    
    $(this.orderedColumnList).kendoSortable({
        hint:function(element) {
            return ( 
                element
                .clone(false)
                .css("padding", "5px")
                .css("background-color", "#52aef7")
                .css("color", "#fff")
            );
        },
        placeholder:function(element) {
            return (
                element
                .clone()
                .css("background-color", "#dceffd")
                .css("color", "#52aef7")
                .text("Please drop here")
                .css("font-size", "20px")
            );
        },
        cursor: "url('/images/dragging.cur'), default",
        cursorOffset: {
            top: 0,
            left: 100
        },
        change: function() {
            that.productListView.data("kendoListView").dataSource.read();
        }
    });
    
    $("a", this.orderedColumnList).click(function(e) {
        var targetLink = $(e.target).closest("a");
        if (!targetLink.is(".k-state-selected")) {
            var li = targetLink.closest("li");
            var span = li.find("span[column]");
            var column = span.attr("column");
            var columnText = column.substring(0, 1).toUpperCase() + column.substring(1);
            $("a", li).removeClass("k-state-selected");
            var mode = targetLink.addClass("k-state-selected").attr("mode");
            if (mode) {
                span.css("text-decoration", "none").css("font-weight", "bolder");
            } else {
                span.css("text-decoration", "line-through").css("font-weight", "lighter");
            }
            that.productListView.data("kendoListView").dataSource.read();
        }
    });
    
    this.orderItemGrid.kendoGrid({
        sortable: true,
        reorderable: true,
        resizable: true,
        scrollable: false,
        columns: [
            {
                title: "Product Name",
                template: function(orderItem) {
                    return orderItem.product.name;
                },
                field: "product.name"
            },
            {
                width: "80px",
                title: "Quantity",
                template: function(orderItem) {
                    return "<input type='numeric' class='k--grid-quantity' style='width:70px'/>"
                }
            },
            {
                width: "60px",
                command: [
                    {
                        name: "-delete",
                        text: "Delete",
                        click: function(e) {
                            that.orderItemGrid_delete(e);
                        }
                    }
                ]
            }
        ],
        dataBound: function(e) {
            var kendoGrid = this;
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
        }
    });
    this.refreshCart();
    
    this.panelBar.kendoPanelBar({ expandMode: "multiple" });
};

//ioc: @UiHandler
SaleView.prototype.productSpecification_specificationchanged = function() {
    this.refreshListView();
};

//ioc: @UiHandler
SaleView.prototype.viewDetailInformationButton_click = function() {
    if (!this._cartDialog) {
        var that = this;
        this._cartDialog = 
            createTemplate("CartDialog")
            .bind("closing", function() {
                that._cartDialog = null;
            })
            .bind("orderitemquantitychanged", function(e) {
                that._changeOrderItemQuantity(e.orderItem, e.quantity);
            })
            .bind("orderitemdeleted", function(e) {
                that._deleteOrderItem(e.orderItem);
            })
            .bind("ordercreated", function() {
                that.refreshListView();
                that.refreshCart();
                createTemplate(
                        "AlertDialog", 
                        {
                            title: "Conguratulations", 
                            message: "The order is created successfully" 
                        }
                );
            });
        this._cartDialog.data("controller").setOrder(this._order);
    }
};

SaleView.prototype.refreshListView = function() {
    this.productPager.data("kendoPager").page(this._currentPageIndex = 1);
    this.productListView.data("kendoListView").dataSource.read();
};

SaleView.prototype.orderItemGrid_quantityChange = function(e) {
    var that = this;
    var orderItem = $(e.target).data("orderItem");
    var quantity = $(e.target).val();
    if (quantity != orderItem.quantity) {
        deferred(function() {
            that._changeOrderItemQuantity(orderItem, quantity);
        });
    }
};

SaleView.prototype.orderItemGrid_delete = function(e) {
    var orderItem = this.orderItemGrid.data("kendoGrid").dataItem($(e.target).closest("tr"));
    this._deleteOrderItem(orderItem);
};

SaleView.prototype._changeOrderItemQuantity = function(orderItem, quantity) {
    var that = this;
    jsonp({
        url: "sale/set-product-quantity-of-cart.spring",
        data:  {
            productId: orderItem.product.id,
            quantity: quantity
        },
        success: function() {
            that.refreshCart();
        },
        error: function() {
            //TODO:
        }
    });
};

SaleView.prototype._deleteOrderItem = function(orderItem) {
    var that = this;
    jsonp({
        url: "sale/remove-product-from-cart.spring",
        data: {
            productId: orderItem.product.id
        },
        success: function() {
            that.refreshCart();
        },
        error: function() {
            //TODO:
        }
    });
};

SaleView.prototype.refreshCart = function() {
    var that = this;
    jsonp({
        url: "sale/temporary-order.spring",
        data: { 
            queryPath: 
                "this.orderItems.product;" +
                "this.orderItems.preferentialActions.giftProduct;" +
                "this.giftItems.product;" +
                "pre order by this.orderItems.id asc;"
        },
        success: function(order) {
            that._order = order;
            if (order) {
                that
                .orderItemGrid.data("kendoGrid")
                .setDataSource(new kendo.data.DataSource({ data: order.orderItems }));
                if (that._cartDialog) {
                    that._cartDialog.data("controller").setOrder(that._order);
                }
                that.refreshBuyQuantity(order);
            }
        },
        error: function(e) {
            //TODO:
        }
    });
};

SaleView.prototype.refreshBuyQuantity = function(order) {
    $(".sale-panel-class", this.productListView).each(function() {
        $(this).data("controller").setBuyQuantityByOrder(order);
    });
};

SaleView.prototype.pageturn = function(forward) {
    this.clearFx();
    var total = this.productListView.width();
    this._clonedDOMWrapper = 
        $("<div></div>")
        .css("position", "absolute")
        .css("overflow", "hidden")
        .css("left", this.productListView.css("left"))
        .css("top", this.productListView.css("top"))
        .css("right", this.productListView.css("right"))
        .css("bottom", this.productListView.css("bottom"))
        .css("opacity", "0.4");
    this._clonedDOM =
        this.productListView.clone(false)
        .css("top", "0px")
        .css("bottom", "0px")
        .css("width", total + "px")
        .css("left", "0px");
    this.productListView.parent().append(this._clonedDOMWrapper.append(this._clonedDOM));
    this._lastFxTime = new Date().getTime();
    var that = this;
    this._intervalId = setInterval(
            function() {
                var left = SaleView._pixel(that._clonedDOM.css("left"));
                if (Math.abs(left) >= total) {
                    that.clearFx();
                } else {
                    var currentFxTime = new Date().getTime();
                    var step = total * (currentFxTime - that._lastFxTime) / 500;
                    that._lastFxTime = currentFxTime;
                    if (forward) {
                        left -= step;
                    } else {
                        left += step;
                    }
                    that._clonedDOM.css("left", left);
                }
            },
            20
    );
};

SaleView.prototype.clearFx = function() {
    if (this._intervalId) {
        this._clonedDOMWrapper.remove();
        clearInterval(this._intervalId);
        this._intervalId = null;
        this._clonedDOMWrapper = null;
        this._clonedDOM = null;
    }
};

SaleView._pixel = function(value) {
    if (value.length > 2 && value.substring(value.length - 2, value.length) == "px") {
        value = value.substring(0, value.length - 2);
    }
    return parseInt(value);
};

SaleView.prototype.destroy = function() {
    if (this._cartDialog) {
        this._cartDialog.data("controller").destroy();
    }
    $(window).unbind("resize", this._resizeHandler);
    this.clearFx();
};

SaleView.CELL_WIDTH = 300;

SaleView.CELL_HEIGHT = 200;
