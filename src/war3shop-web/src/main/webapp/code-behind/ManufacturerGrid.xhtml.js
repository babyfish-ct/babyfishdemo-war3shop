/**
 * @author Tao Chen
 */
function ManufacturerGrid() {
    
    //ioc: @UiField
    this.gridTooltip = null;
    
    //ioc: @UiField
    this.rawGrid = null;
}

//@ioc: @UiHandler
ManufacturerGrid.prototype.init = function(param) {
    var that = this;
    this.specification(param.specification);
    this.selectedIds(param.selectedIds);
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
                    url: param.url || "product/manufacturers.spring",
                    dataType: "jsonp",
                    data: function() {
                        if (typeof(that._specification) == "function") {
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
            },
            requestEnd: function(e) {
                if (e.type == "read") {
                    if (e.response.expectedPageIndex != e.response.actualPageIndex) {
                        that.rawGrid.data("kendoGridEx").pager.page(e.response.actualPageIndex + 1);
                    }
                }
            }
        },
        columns: [
            { 
            field: "id",
            title: "ID"
          },
          { 
            field: "name",
            title: "Name"
          },
          {
            field: "race",
            title: "Race",
            template: function(manufacturer) {
                return org.babyfishdemo.war3shop.entities.Race.values()[manufacturer.race].toString();
            }
          },
          {
            field: "email",
            title: "Email"
          },
            {
            field: "phone",
            title: "Phone"
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
        ],
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
            "<div class='k-manufacturer-tabstrip'>" +
            " <ul class='k-manufacturer-header'><li>Basic Information</li></ul>" +
            " <div>" +
            "  <div style='float:left;'><img class='k-manufacturer-image' style='width:100px'/></div>" +
            "  <div class='k-manufacturer-description' style='margin-left:110px;'></div>" +
            "  <div style='clear:both'></div>" +
            " </div>" +
            "</div>",
        detailInit: function(e) {
            e.detailCell.find(".k-manufacturer-image").attr(
                    "src", 
                    manufacturerImage(e.data)
            );
            if (e.data.description) {
                e.detailCell.find(".k-manufacturer-description").html(e.data.description);
            }
            var tabStrip = e.detailCell.find(".k-manufacturer-tabstrip");
            var header = tabStrip.find(".k-manufacturer-header");
            if (param.enableProductDependencies) {
                $("<li></li>").text("Products").appendTo(header);
                createTemplate("LazyContainer", {
                    templateName: "ProductGrid",
                    templateParam: {
                        mode: "readonly",
                        specification: function() {
                            return {
                                includedManufacturerIds: e.data.id
                            };
                        }
                    } 
                })
                .appendTo(tabStrip);
            }
            if (param.enablePurchaserDependencies) {
                $("<li></li>").text("Purchasers of this manufacturer").appendTo(header);
                createTemplate("LazyContainer", {
                    templateName: "AdministratorGrid",
                    templateParam: {
                        mode: "readonly",
                        specification: function() {
                            return {
                                includedPurchasedManufacturerIds: e.data.id
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
            .data("kendoTabStrip").select(0);
        },
        dataBound: function() {
            var kendoGridEx = that.rawGrid.data("kendoGridEx");
            $("tr[data-uid]", that.rawGrid).each(function() {
                var manufacturer = kendoGridEx.dataItem(this);
                $(this).data(
                        "tooltip", 
                        "<img style='width:100px' src='" + 
                        manufacturerImage(manufacturer) +
                        "'/>"
                );
            });
            if (typeof(that._expectedExpandId) != "undefined") {
                $("tr[data-uid]", that.rawGrid).each(function() {
                    var manufacturer = kendoGridEx.dataItem(this);
                    if (manufacturer.id == that._expectedExpandId) {
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
};

ManufacturerGrid.prototype.specification = function(specification) {
    if (specification && typeof(specification) != "function") {
        throw new Error("The argument must be function");
    }
    this._specification = specification;
};

ManufacturerGrid.prototype.selectedIds = function(selectedIds) {
    if (selectedIds && typeof(selectedIds) != "function") {
        throw new Error("The argument must be function");
    }
    this._selectedIds = selectedIds;
};

ManufacturerGrid.prototype.create_click = function() {
    var dialog = createTemplate("ManufacturerDialog");
    var that = this;
    dialog.bind("save", function() {
        that.refresh();
    });
};

ManufacturerGrid.prototype.edit_click = function(e) {
    var manufacturer = this.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var dialog = createTemplate("ManufacturerDialog", { manufacturer: manufacturer });
    var that = this;
    dialog.bind("save", function() {
        that._expectedExpandId = manufacturer.id;
        that.refresh();
    });
};

ManufacturerGrid.prototype.delete_click = function(e) {
    var manufacturer = this.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var dialog = createTemplate("ConfirmDialog", {
        message: "Do you want to delete this manufacturer?"
    });
    var that = this;
    dialog.bind("ok", function() {
        jsonp({
            url: "product/delete-manufacturer.spring",
            data: { id: manufacturer.id },
            success: function(e) {
                that.refresh();
            },
            error: function(e) {
                createErrorDialog(e);
            }
        });
    });
};

ManufacturerGrid.prototype.refresh = function() {
    this.rawGrid.data("kendoGridEx").dataSource.read();
};
