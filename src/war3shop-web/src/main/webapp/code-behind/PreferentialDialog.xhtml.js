/**
 * @author Tao Chen
 */
function PreferentialDialog() {
    
    //ioc: @UiField
    this.productDropDown = null;
    
    //ioc: @UiField
    this.activeCheckbox = null;
    
    //ioc: @UiField
    this.productInvalidMsg = null;
    
    //ioc: @UiField
    this.startDatePicker = null;
    
    //ioc: @UiField
    this.endDatePicker = null;
    
    //ioc: @UiField
    this.thresholdTypeDropDownList = null;
    
    //ioc: @UiField
    this.actionTypeDropDownList = null;
    
    //ioc: @UiField
    this.preferentialItemGrid = null;
    
    //ioc: @UiField
    this.itemsInvalidMsg = null;
    
    //ioc: @UiField
    this.saveButton = null;
    
    //ioc: @UiField
    this.cancelButton = null;
}

//ioc: @UiHandler
PreferentialDialog.prototype.init = function(param) {
    var preferential = param.preferential || {};
    prepareToUpload();
    var that = this;
    $("tr", this.root).each(function() {
        var input = $("input,textarea,select", this);
        $("label", this).attr("for", input.attr("id"));
        $("span.k-invalid-msg:not(k-manual-msg)", this).attr("data-for", input.attr("name"));
        $("td:eq(0)", this).css("text-align", "right");
        $("td:eq(1)", this).css("text-align", "left");
    });
    var refreshItemGridColumns = function() {
        var kendoGrid = that.preferentialItemGrid.data("kendoGrid");
        var thresholdValue = that.thresholdTypeDropDownList.data("kendoDropDownList").value();
        if (thresholdValue == "0") {
            kendoGrid.showColumn("thresholdQuantity");
            kendoGrid.hideColumn("thresholdMoney");
        } else {
            kendoGrid.hideColumn("thresholdQuantity");
            kendoGrid.showColumn("thresholdMoney");
        }
        var actionValue = that.actionTypeDropDownList.data("kendoDropDownList").value();
        if (actionValue == "0") {
            kendoGrid.showColumn("percentageFactor");
            kendoGrid.hideColumn("reducedMoney");
            kendoGrid.hideColumn("giftProduct");
            kendoGrid.hideColumn("giftQuantity");
        } else if (actionValue == "1") {
            kendoGrid.hideColumn("percentageFactor");
            kendoGrid.showColumn("reducedMoney");
            kendoGrid.hideColumn("giftProduct");
            kendoGrid.hideColumn("giftQuantity");
        } else {
            kendoGrid.hideColumn("percentageFactor");
            kendoGrid.hideColumn("reducedMoney");
            kendoGrid.showColumn("giftProduct");
            kendoGrid.showColumn("giftQuantity");
        }
        that.itemsInvalidMsg.hide();
    };
    this.productDropDown.data("controller").coreWidget().css("width", "200px").bind("change", function() {
        that.productInvalidMsg.hide();
    });
    this.startDatePicker.kendoDatePicker({ format: "yyyy-MM-dd" });
    this.endDatePicker.kendoDatePicker({ format: "yyyy-MM-dd" });
    this.thresholdTypeDropDownList.kendoDropDownList({
        change: refreshItemGridColumns
    });
    this.actionTypeDropDownList.kendoDropDownList({
        change: refreshItemGridColumns
    })
    this.preferentialItemGrid.kendoGrid({
        dataSource: {
            schema: {
                model: {
                    fields: {
                        thresholdQuantity: { type: "number" },
                        thresholdMoney: { type: "number" },
                        percentageFactor: { type: "number" },
                        reducedMoney: { type: "number" },
                        giftProduct: { type: "string" },
                        giftQuantity: { type: "number" }
                    }
                }
            }
        },
        height: "200px",
        columns: [
            {
                field: "thresholdQuantity",
                title: "Threshold Quantity",
                width: "140px",
                template: "<input type='number' class='k-grid--threshold-quantity' style='width:120px' min='1'/>"
            },
            {
                field: "thresholdMoney",
                title: "Threshold Money",
                width: "140px",
                template: "<input type='number' class='k-grid--threshold-money' style='width:120px' min='100'/>"
            },
            {
                field: "percentageFactor",
                title: "Percentage Factor",
                width: "100px",
                template: "<input type='number' class='k-grid--percentage-factor' style='width:70px' min='60' max='99'/>%"
            },
            {
                field: "reducedMoney",
                title: "Reduced Money",
                width: "130px",
                template: "-<input type='number' class='k-grid--reduced-money' style='width:100px' min='1'/>"
            },
            {
                field: "giftProduct",
                title: "Gift Product",
                width: "220px",
                template: "<div class='k-grid--gift-wrapper' style='width:200px'></div>"
            },
            {
                field: "giftQuantity",
                title: "giftQuantity",
                width: "100px",
                template: "<input type='number' class='k-grid--gift-quantity' style='width:80px' min='1'/>"
            },
            {
                command: [
                    {
                        name: "-delete",
                        text: "Delete",
                        click: function(e) {
                            if (!$(e.target).is(".k-state-disabled")) {
                                var kendoGrid = that.preferentialItemGrid.data("kendoGrid");
                                var purchasingItem = kendoGrid.dataItem($(e.target).closest("tr"));
                                kendoGrid.dataSource.remove(purchasingItem);
                                kendoGrid.refresh();
                                that._validateGrid();
                            }
                        }
                    }
                ]
            }
        ],
        toolbar: [ { name: "-new-item", text: "New Item" } ],
        dataBound: function() {
            var kendoGrid = this;
            $("tr[data-uid]", this.tbody).each(function() {
                var invenceItem = kendoGrid.dataItem(this);
                var giftProductDropDown = createTemplate("ProductDropDown", {
                    mode: "single",
                    tagPosition: "left",
                    enableActiveChoices: true
                });
                giftProductDropDown.data("controller").coreWidget().css("width", "200px");
                giftProductDropDown
                .bind("change", function() {
                    invenceItem.giftProduct = giftProductDropDown.data("controller").dataItemValue();
                    that._validateGrid();
                })
                .appendTo($(".k-grid--gift-wrapper", this))
                .data("controller")
                .selectDataItem(invenceItem.giftProduct);
                
                $(".k-grid--gift-quantity", this).kendoNumericTextBox({
                    change: function() {
                        invenceItem.giftQuantity = this.value();
                        that._validateGrid();
                    }
                })
                .data("kendoNumericTextBox")
                .value(invenceItem.giftQuantity);
                
                $(".k-grid--threshold-quantity", this).kendoNumericTextBox({
                    change: function() {
                        invenceItem.thresholdQuantity = this.value();
                        that._validateGrid();
                    }
                })
                .data("kendoNumericTextBox")
                .value(invenceItem.thresholdQuantity);
                
                $(".k-grid--threshold-money", this).kendoNumericTextBox({
                    change: function() {
                        invenceItem.thresholdMoney = this.value();
                        that._validateGrid();
                    }
                })
                .data("kendoNumericTextBox")
                .value(invenceItem.thresholdMoney);
                
                $(".k-grid--percentage-factor", this).kendoNumericTextBox({
                    change: function() {
                        invenceItem.percentageFactor = this.value();
                        that._validateGrid();
                    }
                })
                .data("kendoNumericTextBox")
                .value(invenceItem.percentageFactor);
                
                $(".k-grid--reduced-money", this).kendoNumericTextBox({
                    change: function() {
                        invenceItem.reducedMoney = this.value();
                        that._validateGrid();
                    }
                })
                .data("kendoNumericTextBox")
                .value(invenceItem.reducedMoney);
            });
        }
    });
    $(".k-grid--new-item", this.root).click(function() {
        var dataSource = that.preferentialItemGrid.data("kendoGrid").dataSource;
        var actionType = that.actionTypeDropDownList.data("kendoDropDownList").value();
        if (actionType == "2") {
            var data = dataSource.data();
            if (data.length != 0) {
                dataSource.add({ giftProduct: data[data.length - 1].giftProduct });
                return;
            }
        }
        dataSource.add({});
    });
    
    this.saveButton.kendoButton();
    this.cancelButton.kendoButton();
    
    var open = function() {
        refreshItemGridColumns();
        that
        .root
        .appendTo(document.body)
        .kendoWindow({
            title: typeof(preferential.id) == "undefined" ? "Create Preferential" : "Edit Preferential",
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
    
    this.root.kendoValidator();
    if (typeof(preferential.id) == "undefined") {
        that.activeCheckbox.attr("checked", true);
        open();
    } else {
        jsonp({
            url: "product/preferential.spring",
            data: { 
                id: preferential.id,
                queryPath: 
                    "this.product;" +
                    "this.items.giftProduct;" +
                    "pre order by this.items.thresholdQuantity;" +
                    "pre order by this.items.thresholdMoney;"
            },
            success: function(e) {
                that._preferentialId = e.id;
                that._preferentialVersion = e.version;
                that.productDropDown.data("controller").selectDataItem(e.product);
                that.activeCheckbox.attr("checked", e.active);
                that.startDatePicker.data("kendoDatePicker").value(kendo.toString(e.startDate, "yyyy-MM-dd"));
                that.endDatePicker.data("kendoDatePicker").value(kendo.toString(e.endDate, "yyyy-MM-dd"));
                that.thresholdTypeDropDownList.data("kendoDropDownList").value("" + e.thresholdType);
                that.actionTypeDropDownList.data("kendoDropDownList").value("" + e.actionType);
                deferred(function() { //Use deffered to fix bug of kendo ui
                    for (var i = 0; i < e.items.length; i++) {
                        var item = e.items[i];                      
                        that.preferentialItemGrid.data("kendoGrid").dataSource.add(item);
                    }
                });
                open();
            },
            error: function(e) {
                createErrorDialog(e);
            }
        });
    }
};

//ioc: @UiHandler
PreferentialDialog.prototype.saveButton_click = function(e) {
    var that = this;
    var manualValidated = true;
    var productId = this.productDropDown.data("controller").value();
    var active = this.activeCheckbox.is(":checked");
    if (typeof(productId) != "number") {
        manualValidated = false;
        this.productInvalidMsg.show();
    }
    manualValidated &= this._validateGrid();
    if (this.root.data("kendoValidator").validate() && manualValidated) {
        var startDate = kendo.toString(
                this.startDatePicker.data("kendoDatePicker").value(), 
                "yyyy-MM-dd"
        );
        var endDate = kendo.toString(
                this.endDatePicker.data("kendoDatePicker").value(), 
                "yyyy-MM-dd"
        );
        var thresholdType = this.thresholdTypeDropDownList.data("kendoDropDownList").value();
        var actionType = this.actionTypeDropDownList.data("kendoDropDownList").value();
        var data = this.preferentialItemGrid.data("kendoGrid").dataSource.data();
        var preferential = {
            product: {
                id: productId  
            }, 
            active: active,
            startDate: startDate,
            endDate: endDate,
            thresholdType: org.babyfishdemo.war3shop.entities.PreferentialThresholdType.values()[thresholdType].name(),
            actionType: org.babyfishdemo.war3shop.entities.PreferentialActionType.values()[actionType].name(),
            items: []
        };
        if (typeof(this._preferentialId) == "number") {
            preferential.id = this._preferentialId;
            preferential.version = this._preferentialVersion;
        }
        for (var i = 0; i < data.length; i++) {
            var dataItem = data[i];
            var preferentialItem = {};
            if (typeof(dataItem.id) == "number") {
                preferentialItem.id = dataItem.id;
            }
            if (thresholdType == "0") {
                preferentialItem.thresholdQuantity = dataItem.thresholdQuantity;
            } else {
                preferentialItem.thresholdMoney = dataItem.thresholdMoney;
            }
            if (actionType == "0") {
                preferentialItem.percentageFactor = dataItem.percentageFactor;
            } else if (actionType == "1") {
                preferentialItem.reducedMoney = dataItem.reducedMoney;
            } else {
                preferentialItem.giftProduct = {
                    id: dataItem.giftProduct.id
                },
                preferentialItem.giftQuantity = dataItem.giftQuantity;
            }
            preferential.items.push(preferentialItem);
        }
        jsonp({
            url: "product/submit-preferential.spring",
            data: { json: JSON.stringify(preferential) },
            success: function() {
                that.root.trigger("save");
                that.destroy();
            },
            error: function(e) {
                createErrorDialog(e);
            }
        });
    }
};

//ioc: @UiHandler
PreferentialDialog.prototype.cancelButton_click = function(e) {
    this.destroy();
};

PreferentialDialog.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
};

PreferentialDialog.prototype._validateGrid = function() {
    var thresholdType = this.thresholdTypeDropDownList.data("kendoDropDownList").value();
    var actionType = this.actionTypeDropDownList.data("kendoDropDownList").value();
    var data = this.preferentialItemGrid.data("kendoGrid").dataSource.data();
    if (data.length == 0) {
        this.itemsInvalidMsg.text("No preferential items").show();
        return false;
    }
    for (var i = 0; i < data.length; i++) {
        var preferentialItem = data[i];
        if (thresholdType == "0") {
            if (preferentialItem.thresholdQuantity == 0) {
                this.itemsInvalidMsg.text("Some items miss threshold quantity").show();
                return false;
            }
        } else {
            if (preferentialItem.thresholdMoney == 0) {
                this.itemsInvalidMsg.text("Some items miss threshold money").show();
                return false;
            }
        }
        if (actionType == "0") {
            if (preferentialItem.percentageFactor == 0) {
                this.itemsInvalidMsg.text("Some items miss percentage factor").show();
                return false;
            }
        } else if (actionType == "1") {
            if (preferentialItem.reducedMoney == 0) {
                this.itemsInvalidMsg.text("Some items miss reduced money").show();
                return false;
            }
        } else {
            if (!preferentialItem.giftProduct) {
                this.itemsInvalidMsg.text("Some items miss gift product").show();
                return false;
            }
            if (preferentialItem.giftQuantity == 0) {
                this.itemsInvalidMsg.text("Some items miss gift quantity").show();
                return false;
            }
        }
    }
    this.itemsInvalidMsg.hide();
    return true;
}
