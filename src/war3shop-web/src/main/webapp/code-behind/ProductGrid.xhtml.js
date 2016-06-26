/**
 * @author Tao Chen
 */
function ProductGrid() {

    //ioc: @UiField
    this.gridTooltip = null;
    
    //ioc: @UiField
    this.rawGrid = null;
}

ProductGrid.prototype.init = function(param) {
    var that = this;
    this.specification(param.specification);
    var columns = [
      { 
        field: "id",
        title: "ID"
      },
      {
        field: "active",
        title: "Active",
      },
      {
        field: "name",
        title: "Name"
      },
      {
        field: "type",
        title: "Type",
        template: function(product) {
            return org.babyfishdemo.war3shop.entities.ProductType.values()[product.type].toString();
        }
      },
      {
        field: "race",
        title: "Race",
        template: function(product) {
            return org.babyfishdemo.war3shop.entities.Race.values()[product.race].toString();
        }
      },
      { 
        field: "price",
        title: "Price"
      },
      {
        field: "creationTime",
        title: "Creation Time"
      },
      {
        command: [ 
            { 
                name: "-edit", 
                text: "Edit...",
                click: function(e) {
                    that.edit_click(e);
                }
            }, 
            { 
                name: "-delete", 
                text: "Delete",
                click: function(e) {
                    that.delete_click(e);
                }
            }
          ]
      }
    ];
    this.rawGrid.kendoGridEx({
        mode: param.mode,
        autoBind: param.autoBind,
        selectedIds: function() {
            if (typeof(that._selectedIds) == "function") {
                return that._selectedIds();
            }
            return [];
        },
        dataSource: {
            transport: {
                read: { 
                    url: param.url || "product/products.spring",
                    dataType: "jsonp",
                    data: function() {
                        if (that._specification) {
                            return that._specification();
                        }
                    }
                },
                parameterMap: function(data, type) {
                    if (type == "read") {
                        return parameterMap(data, "this.description;");
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
        toolbar: [ { name: "-create", text: "Create..." } ],
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
            " <ul class='k-product-header'><li>Basic Information</li></ul>" +
            " <div>" +
            "  <div style='float:left;'><img class='k-product-image'/></div>" +
            "  <div class='k-product-description' style='margin-left:110px;'></div>" +
            "  <div style='clear:both'></div>" +
            " </div>" +
            "</div>",
        detailInit: function(e) {
            e.detailCell.find(".k-product-image").setProductImage(e.data);
            if (e.data.description) {
                e.detailCell.find(".k-product-description").html(e.data.description);
            }
            var tabStrip = e.detailCell.find(".k-product-tabstrip");
            var header = tabStrip.find(".k-product-header");
            if (param.enableManufacturerDependencies) {
                $("<li></li>").text("Manufacturers").appendTo(header);
                createTemplate("LazyContainer", {
                    templateName: "ManufacturerGrid",
                    templateParam: {
                        mode: "readonly",
                        enablePurchaserDependencies: (param.enableManufacturerDependencies || {}).enablePurchaserDependencies,
                        specification: function() {
                            return {
                                includedProductIds: e.data.id
                            };
                        }
                    } 
                })
                .appendTo(tabStrip);
            }
            if (param.enablePurchaserDependencies) {
                $("<li></li>").text("Purchasers of this product and its manufacturers").appendTo(header);
                createTemplate("LazyContainer", {
                    templateName: "AdministratorGrid",
                    templateParam: {
                        mode: "readonly",
                        specification: function() {
                            return {
                                includedPurchasedProductIds: e.data.id
                            };
                        }
                    } 
                })
                .appendTo(tabStrip);
            }
            if (param.enablePreferentialDependencies) {
                $("<li></li>").text("Preferentials").appendTo(header);
                createTemplate("LazyContainer", {
                    templateName: "PreferentialGrid",
                    templateParam: {
                        mode: "readonly",
                        hideProductColumn: true,
                        specification: function() {
                            return {
                                includedProductIds: e.data.id
                            };
                        }
                    } 
                })
                .appendTo(tabStrip);
            }
            tabStrip
            .kendoTabStrip({ 
                animation:false,
                select: function(e) {
                    var controller = $(e.contentElement).data("controller");
                    if (controller instanceof LazyContainer) {
                        controller.create();
                    }
                }
            })
            .data("kendoTabStrip")
            .select(0);
        },
        dataBound: function() {
            var kendoGridEx = that.rawGrid.data("kendoGridEx");
            $("tr[data-uid]", that.rawGrid).each(function() {
                var product = kendoGridEx.dataItem(this);
                $(this).data("tooltip", productImageHtml(product));
            });
            if (typeof(that._expectedExpandId) != "undefined") {
                $("tr[data-uid]", that.rawGrid).each(function() {
                    var product = kendoGridEx.dataItem(this);
                    if (product.id == that._expectedExpandId) {
                        kendoGridEx.expandRow(this);
                        return false; //break the each
                    }
                });
                delete that._expectedExpandId;
            }
        }
    });
    $(".k-grid--create", this.rawGrid).click(function() {
        that.create_click();
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

ProductGrid.prototype.specification = function(specification) {
    if (specification && typeof(specification) != "function") {
        throw new Error("The argument must be function when it is not null");
    }
    this._specification = specification;
};

ProductGrid.prototype.selectedIds = function(selectedIds) {
    if (selectedIds && typeof(selectedIds) != "function") {
        throw new Error("The argument must be function");
    }
    this._selectedIds = selectedIds;
};

ProductGrid.prototype.create_click = function() {
    var dialog = createTemplate("ProductDialog");
    var that = this;
    dialog.bind("save", function() {
        that.refresh();
    });
};

ProductGrid.prototype.edit_click = function(e) {
    var product = this.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var dialog = createTemplate("ProductDialog", { product: product });
    var that = this;
    dialog.bind("save", function() {
        that._expectedExpandId = product.id;
        that.refresh();
    });
};

ProductGrid.prototype.delete_click = function(e) {
    var product = this.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var dialog = createTemplate("ConfirmDialog", {
        message: "Do you want to delete this product?"
    });
    var that = this;
    dialog.bind("ok", function() {
        jsonp({
            url: "product/delete-product.spring",
            data: { id: product.id },
            success: function(e) {
                that.refresh();
            },
            error: function(e) {
                createErrorDialog(e);
            }
        });
    });
};

ProductGrid.prototype.refresh = function() {
    this.rawGrid.data("kendoGridEx").dataSource.read();
};
