/**
 * @author Tao Chen
 */
function PreferentialGrid() {
    
    //ioc: @UiField
    this.gridTooltip = null;

    //ioc: @UiField
    this.rawGrid = null;
}

PreferentialGrid.prototype.init = function(param) {
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
      param.hideProductColumn ?
      null :
      {
        field: "product.name",
        title: "Product",
        template: function(preferential) {
            return preferential.product.name;
        }
      },
      {
        field: "product.price",
        title: "price",
        template: function(preferential) {
            return preferential.product.price;
        }
      },
      {
        field: "startDate",
        title: "Start Date",
        format: "{0: yyyy-MM-dd}"
      },
      {
        field: "endDate",
        title: "End Date",
        format: "{0: yyyy-MM-dd}"
      },
      {
        field: "thresholdType",
        title: "Threshold Type",
        template: function(preferential) {
            return org.babyfishdemo.war3shop.entities.PreferentialThresholdType.values()[preferential.thresholdType].toString();
        }
      },
      {
        field: "actionType",
        title: "Action Type",
        template: function(preferential) {
            return org.babyfishdemo.war3shop.entities.PreferentialActionType.values()[preferential.actionType].toString();
        }
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
                    url: param.url || "product/preferentials.spring",
                    dataType: "jsonp",
                    data: function() {
                        if (that._specification) {
                            return that._specification();
                        }
                    }
                },
                parameterMap: function(data, type) {
                    if (type == "read") {
                        return parameterMap(data, "this.product;");
                    }
                },
                requestEnd: function(e) {
                    if (e.type == "read") {
                        alert("requestEnd: " + e.response);
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
        columns: (param.columns || columns).where(function(e) {
            return e != null;
        }),
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
            param.hideProductColumn 
            ?
            "<div class='k-preferential-detail'></div>" 
            :
            "<div>" +
            " <div style='float:left;'><img class='k-product-image'/></div>" +
            " <div style='margin-left:110px;'>" +
            "  <div class='k-preferential-detail' style='display:inline-block;width:100%'></div>" +
            " </div>" +
            " <div style='clear:both;'></div>" +
            "</div>",
        detailInit: function(e) {
            if (!param.hideProductColumn) {
                e.detailCell.find(".k-product-image").setProductImage(e.data.product);
            }
            createTemplate(
                    "PreferentialItemGrid", 
                    {
                        preferentialId: e.data.id,
                        thresholdType: e.data.thresholdType,
                        actionType: e.data.actionType
                    }
            )
            .appendTo($(".k-preferential-detail", e.detailCell));
        },
        dataBound: function() {
            var kendoGridEx = that.rawGrid.data("kendoGridEx");
            $("tr[data-uid]", that.rawGrid).each(function() {
                var preferential = kendoGridEx.dataItem(this);
                $(this).data("tooltip", productImageHtml(preferential.product));
            });
            if (typeof(that._expectedExpandId) != "undefined") {
                $("tr[data-uid]", that.rawGrid).each(function() {
                    var preferential = kendoGridEx.dataItem(this);
                    if (preferential.id == that._expectedExpandId) {
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
    
    if (param.hideProductColumn) { //Very important to stop recursive tooltip of parent grid 
        this.gridTooltip.attr("data-role", "tooltipex");
    } else {
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
};

PreferentialGrid.prototype.specification = function(specification) {
    if (specification && typeof(specification) != "function") {
        throw new Error("The argument must be function when it is not null");
    }
    this._specification = specification;
};

PreferentialGrid.prototype.selectedIds = function(selectedIds) {
    if (selectedIds && typeof(selectedIds) != "function") {
        throw new Error("The argument must be function");
    }
    this._selectedIds = selectedIds;
};

PreferentialGrid.prototype.create_click = function() {
    var dialog = createTemplate("PreferentialDialog");
    var that = this;
    dialog.bind("save", function() {
        that.refresh();
    });
};

PreferentialGrid.prototype.edit_click = function(e) {
    var preferential = this.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var dialog = createTemplate("PreferentialDialog", { preferential: preferential });
    var that = this;
    dialog.bind("save", function() {
        that._expectedExpandId = preferential.id;
        that.refresh();
    });
};

PreferentialGrid.prototype.delete_click = function(e) {
    var preferential = this.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var dialog = createTemplate("ConfirmDialog", {
        message: "Do you want to delete this preferential?"
    });
    var that = this;
    dialog.bind("ok", function() {
        jsonp({
            url: "product/delete-preferential.spring",
            data: { id: preferential.id },
            success: function(e) {
                that.refresh();
            },
            error: function(e) {
                createErrorDialog(e);
            }
        });
    });
};

PreferentialGrid.prototype.refresh = function() {
    this.rawGrid.data("kendoGridEx").dataSource.read();
};
