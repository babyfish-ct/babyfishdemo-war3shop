/**
 * @author Tao Chen
 */
function PreferentialItemGrid() {

    //ioc: @UiField
    this.gridTooltip = null;
    
    //ioc: @UiField
    this.rawGrid = null;
}

PreferentialItemGrid.prototype.init = function(param) {
    var that = this;
    var columns = [];
    if (param.thresholdType == 0) { //Quanity
        columns.push({
            field: "thresholdQuantity",
            title: "Threshold Quantity",
        });
    } else {
        columns.push({ //Money
            field: "thresholdMoney",
            title: "Threshold Money"
        });
    }
    if (param.actionType == 0) { //Multiplied by percentage
        columns.push({
            field: "percentageFactor",
            title: "Percentage Factor",
            template: function(preferentialItem) {
                return preferentialItem.percentageFactor + "%";
            }
        });
    } else if (param.actionType == 1) { //Reduce Money
        columns.push({
            field: "reducedMoney",
            title: "Reduced Money",
            template: function(preferentialItem) {
                return "-" + preferentialItem.reducedMoney;
            }
        });
    } else { //Send gift
        columns.push({
            field: "giftProduct",
            title: "Gift Product",
            template: function(preferentialItem) {
                return preferentialItem.giftProduct.name;
            }
        });
        columns.push({
            field: "giftQuantity",
            title: "Gift Quantity"
        });
    }
    this.rawGrid.kendoGridEx({
        mode: "readonly",
        autoBind: param.autoBind || true,
        dataSource: {
            transport: {
                read: { 
                    url: param.url || "product/preferential-items.spring",
                    dataType: "jsonp",
                    data: {
                        preferentialId: param.preferentialId
                    }
                },
                parameterMap: function(data, type) {
                    if (type == "read") {
                        return parameterMap(
                                data, 
                                "this.giftProduct;" +
                                "pre order by this.thresholdQuantity asc;" +
                                "pre order by this.thresholdMoney asc;"
                        );
                    }
                }
            },
            serverPaging: true,
            serverSorting: true,
            schema: {
                data: "entities",
                total: "totalRowCount",
                errors: "exceptionMessage"
            },
            error: function(e) {
                createErrorDialog(e);
            }
        },
        columns: param.columns || columns,
        pageable: {
            pageSize: 10,
            input: true
        },
        sortable: true,
        reorderable: true,
        resizable: true,
        scrollable: false,
        dataBound: function() {
            if (param.actionType == 2) {
                var kendoGridEx = that.rawGrid.data("kendoGridEx");
                $("tr[data-uid]", kendoGridEx.tbody).each(function() {
                    var preferentialItem = kendoGridEx.dataItem(this);
                    $(this).data("tooltip", productImageHtml(preferentialItem.giftProduct));
                });
            }
        }
    });
    if (param.actionType == 2) {
        this.gridTooltip.kendoTooltipEx({
            filter: "tr[data-uid]",
            position: "left",
            animation: false,
            showAfter: 0,
            content: function(e) {
                return e.target.data("tooltip") || "Hello";
            }
        });
    } else { //Very important to stop recursive tooltip of parent grid
        this.gridTooltip.attr("data-role", "tooltipex");
    }
};

PreferentialItemGrid.prototype.refresh = function() {
    this.rawGrid.data("kendoGridEx").dataSource.read();
};
