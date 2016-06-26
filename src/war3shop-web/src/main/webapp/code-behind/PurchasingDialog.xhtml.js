/**
 * @author Tao Chen
 */
function PurchasingDialog() {
    
    //ioc: @UiField
    this.purchasingItemGrid = null;
    
    //ioc: @UiField
    this.saveButton = null;
    
    //ioc: @UiField
    this.cancelButton = null;
}

//ioc: @UiHandler
PurchasingDialog.prototype.init = function(param) {
    var that = this;
    var refereshTotalPurchasedPrice = function() {
        var totalPurchasedPrice = 0;
        var kendoGrid = that.purchasingItemGrid.data("kendoGrid");
        $("tr[data-uid]", kendoGrid.tbody).each(function() {
            var purchasingItem = kendoGrid.dataItem(this);
            if (!isNaN(purchasingItem.purchasedUnitPrice) && !isNaN(purchasingItem.quantity)) {
                totalPurchasedPrice += purchasingItem.purchasedUnitPrice * purchasingItem.quantity;
            }
        });
        $("input.k-grid--total-purchased-price", that.purchasingItemGrid).val(totalPurchasedPrice);
    };
    this.purchasingItemGrid.kendoGrid({
        dataSource: {
            schema: {
                model: {
                    fields: {
                        productId: { type: "number" },
                        productName: { type: "number" },
                        unitPrice: { type: "number" },
                        inventory: { type: "number" },
                        purchasedUnitPrice: { type: "number" },
                        quantity: { type: "number" }
                    }
                }
            }
        },
        scrollable: false,
        columns: [
            { 
                title: "Product", 
                width: "200px",
                template: function() {
                    return "<div class='k-grid--product'></div>";
                }
            },
            { 
                field: "unitPrice",
                title: "Unit Price", 
                width: "100px"
            },
            { 
                field: "inventory",
                title: "Existing Inventory", 
                width: "100px"
            },
            { 
                title: "Purchased Price", 
                width: "150px",
                template: function() {
                    return "<div class='k-grid--purchased-unit-price'></div>";
                }
            },
            { 
                title: "Quantity", 
                width: "100px",
                template: function() {
                    return "<div class='k-grid--quantity'></div>";
                }
            },
            {
                command: [
                    {
                        name: "-delete",
                        text: "Delete",
                        click: function(e) {
                            if (!$(e.target).is(".k-state-disabled")) {
                                var kendoGrid = that.purchasingItemGrid.data("kendoGrid");
                                var purchasingItem = kendoGrid.dataItem($(e.target).closest("tr"));
                                kendoGrid.dataSource.remove(purchasingItem);
                                kendoGrid.refresh();
                            }
                        }
                    }
                ]
            }
        ],
        toolbar: 
            "<div style='padding:10px;'>" +
            "<div style='float:right;font-size:10px;font-style:italic;'>" +
            "(Notes: Row will be deleted automatically when quantity is set to be 0!)" +
            "</div>" +
            "<span style='font-size:16px;font-weight:bold'>Total purchased price: </span>" +
            "<input class='k-grid--total-purchased-price k-textbox' readonly='readonly' style='width:150px' value='0'/>" +
            "</div>",
        dataBound: function() {
            var kendoGrid = this;
            $("tr[data-uid]", kendoGrid.tbody).each(function() {
                var tr = this;
                var purchasingItem = kendoGrid.dataItem(this);
                var productDropDown =
                    createTemplate("ProductDropDown", {
                        url: "purchasing/my-purchased-products.spring",
                        mode: "single",
                        tagPosition: "left",
                        implicitSpecification: { active: true }
                    })
                    .appendTo($(".k-grid--product", this))
                    .change(function() {
                        var value = productDropDown.data("controller").dataItemValue();
                        var addEmptyRow = typeof((purchasingItem.product || {}).id) == "undefined"; 
                        purchasingItem.product = value;
                        purchasingItem.unitPrice = value.price;
                        
                        // Hibernate does NOT support "lazy" for @OneToOne(mappedBy = "...")
                        purchasingItem.inventory = value.inventory ? value.inventory.quantity : 0;
                        
                        $("tr[data-uid]", kendoGrid.tbody).each(function(index) {
                            if (this != tr) {
                                var otherItem = kendoGrid.dataItem(this);
                                if (purchasingItem.product.id == otherItem.product.id) {
                                    if (typeof(otherItem.purchasedUnitPrice) == "number") {
                                        purchasingItem.purchasedUnitPrice = otherItem.purchasedUnitPrice;
                                    }
                                    purchasingItem.quantity += otherItem.quantity;
                                    kendoGrid.dataSource.remove(otherItem);
                                    return false; //break each
                                }
                            }
                        });
                        if (addEmptyRow) {
                            kendoGrid.dataSource.add({ quantity: 1 });
                        }
                        kendoGrid.refresh();
                    });
                var purchasedUnitPriceNumericTextBox =
                    $("<input/>")
                    .attr("type", "number")
                    .attr("min", "0")
                    .attr("max", "99999")
                    .width("140px")
                    .appendTo($(".k-grid--purchased-unit-price", this))
                    .kendoNumericTextBoxEx()
                    .bind("change keyup", function() {
                        // Use $.fn.val() that can be accessed immediately after the keyup event, 
                        // not kendoNumbericTextBox.value() that can only be accessed after blur event.
                        purchasingItem.purchasedUnitPrice = selfParseInt(purchasedUnitPriceNumericTextBox.val());
                        refereshTotalPurchasedPrice();
                    });
                var quantityNumericTextBox =
                    $("<input/>")
                    .attr("type", "number")
                    .attr("min", "0")
                    .attr("max", "999")
                    .width("90px")
                    .appendTo($(".k-grid--quantity", this))
                    .kendoNumericTextBoxEx({ decimals: 0 })
                    .bind("valuechange", function() {
                        // Use $.fn.val() that can be accessed immediately after the keyup event, 
                        // not kendoNumbericTextBox.value() that can only be accessed blur event.
                        purchasingItem.quantity = selfParseInt(quantityNumericTextBox.val());
                        if (typeof(purchasingItem.quantity) == "number" && purchasingItem.quantity == 0) {
                            kendoGrid.dataSource.remove(purchasingItem);
                            kendoGrid.refresh();
                        } else {
                            refereshTotalPurchasedPrice();
                        }
                    });
                productDropDown.data("controller").coreWidget().width("190px");
                if (typeof((purchasingItem.product || {}).id) == "number") {
                    productDropDown.data("controller").selectDataItem(purchasingItem.product);
                    purchasedUnitPriceNumericTextBox.data("kendoNumericTextBoxEx").value(purchasingItem.purchasedUnitPrice);
                    quantityNumericTextBox.data("kendoNumericTextBoxEx").value(purchasingItem.quantity);
                } else {
                    purchasedUnitPriceNumericTextBox.data("kendoNumericTextBoxEx").enable(false);
                    quantityNumericTextBox.data("kendoNumericTextBoxEx").enable(false);
                    $(".k-grid--delete", this).addClass("k-state-disabled");
                }
                refereshTotalPurchasedPrice();
            });
        }
    })
    .data("kendoGrid")
    .dataSource
    .add({ quantity: 1 });
    
    this.saveButton.kendoButton();
    this.cancelButton.kendoButton();
    this
    .root
    .appendTo(document.body)
    .kendoWindow({
        title: "Add Purchasing",
        actions: [ "Close" ],
        resizable: false,
        modal: true,
        pinned: true,
        close: function() {
            that.destroy();
        }
    })
    .data("kendoWindow")
    .center();
};

//ioc: @UiHandler
PurchasingDialog.prototype.saveButton_click = function(e) {
    var that = this;
    var data = this.purchasingItemGrid.data("kendoGrid").dataSource.data();
    if (!data.length) {
        createTemplate("AlertDialog", { title: "Error", message: "No purchasing itmes" });
        return;
    }
    var purchasingItems = [];
    for (var i = 0; i < data.length; i++) {
        var dataItem = data[i];
        if (typeof((dataItem.product || {}).id) != "number") {
            continue;
        }
        if (isNaN(dataItem.purchasedUnitPrice)) {
            createTemplate("AlertDialog", { title: "Error", message: "Miss purchased price for some purchasing items" });
            return;
        }
        var purchasingItem = {
            product: {
                id: dataItem.product.id
            },
            purchasedUnitPrice: dataItem.purchasedUnitPrice,
            quantity: dataItem.quantity
        };
        purchasingItems.push(purchasingItem);
    }
    jsonp({
        url: "purchasing/create-purchasing.spring",
        data: { json: JSON.stringify(purchasingItems) },
        success: function() {
            that.root.trigger("save");
            that.destroy();
        },
        error: function(e) {
            createErrorDialog(e);
        }
    });
};

//ioc: @UiHandler
PurchasingDialog.prototype.cancelButton_click = function(e) {
    this.destroy();
};

PurchasingDialog.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
};
