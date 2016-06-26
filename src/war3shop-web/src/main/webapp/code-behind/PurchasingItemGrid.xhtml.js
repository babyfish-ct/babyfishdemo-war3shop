/**
 * @author Tao Chen
 */
function PurchasingItemGrid() {

    //ioc: @UiField
    this.gridTooltip = null;
    
    //ioc: @UiField
    this.rawGrid = null;
}

PurchasingItemGrid.prototype.init = function(param) {
    var that = this;
    this.specification(param.specification);
    var columns = [
        {
            field: "id",
            title: "ID"
        },
      { 
        field: "product.id",
        title: "Product ID",
        template: function(purchasingItem) {
            return purchasingItem.product.id;
        }
      },
      {
        field: "product.name",
        title: "Product Name",
        template: function(purchasingItem) {
            return purchasingItem.product.name;
        }
      },
      {
        field: "purchasedUnitPrice",
        title: "Purchased Unit Price"
      },
      {
        field: "quantity",
        title: "Quantity"
      }
    ];
    this.rawGrid.kendoGridEx({
        mode: param.mode,
        autoBind: param.autoBind,
        dataSource: {
            transport: {
                read: { 
                    url: param.url || "purchasing/purchasing-items.spring",
                    dataType: "jsonp",
                    data: function() {
                        if (that._specification) {
                            return that._specification();
                        }
                    }
                },
                parameterMap: function(data, type) {
                    if (type == "read") {
                        return parameterMap(data, "this.product.description;this.product;");
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
        detailTemplate:
            "<div class='k-product-tabstrip'>" +
            " <ul class='k-product-header'><li>Product Basic Information</li></ul>" +
            " <div>" +
            "  <div style='float:left;'><img class='k-product-image'/></div>" +
            "  <div class='k-product-description' style='margin-left:110px;'></div>" +
            "  <div style='clear:both'></div>" +
            " </div>" +
            "</div>",
        detailInit: function(e) {
            e.detailCell.find(".k-product-image").setProductImage(e.data.product);
            if (e.data.product.description) {
                e.detailCell.find(".k-product-description").html(e.data.product.description);
            }
            var tabStrip = e.detailCell.find(".k-product-tabstrip");
            var header = tabStrip.find(".k-product-header");
            if (param.enableManufacturerDependencies) {
                $("<li></li>").text("Product Manufacturers").appendTo(header);
                createTemplate("ManufacturerGrid", {
                    mode: "readonly",
                    specification: function() {
                        return {
                            includedProductIds: e.data.product.id
                        };
                    }
                })
                .appendTo(tabStrip);
            }
            if (!header.is(":empty")) {
                tabStrip.kendoTabStrip({ animation:false }).data("kendoTabStrip").select(0);
            }
        },
        dataBound: function() {
            var kendoGridEx = that.rawGrid.data("kendoGridEx");
            $("tr[data-uid]", that.rawGrid).each(function() {
                var purchasingItem = kendoGridEx.dataItem(this);
                $(this).data("tooltip", productImageHtml(purchasingItem.product));
            });
        }
    });
    this.gridTooltip.kendoTooltipEx({
        filter: "tr[data-uid]:not(:has(td.k-hierarchy-cell:has(.k-minus)))",
        position: "left",
        animation: false,
        showAfter: 0,
        content: function(e) {
            return e.target.data("tooltip");
        }
    });
}

PurchasingItemGrid.prototype.specification = function(specification) {
    if (specification && typeof(specification) != "function") {
        throw new Error("The argument must be function when it is not null");
    }
    this._specification = specification;
};

PurchasingItemGrid.prototype.refresh = function() {
    this.rawGrid.data("kendoGridEx").dataSource.read();
};
