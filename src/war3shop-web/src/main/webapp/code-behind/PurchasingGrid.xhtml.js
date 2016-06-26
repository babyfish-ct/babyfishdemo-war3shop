/**
 * @author Tao Chen
 */
function PurchasingGrid() {
    
    //ioc: @UiField
    this.rawGrid = null;
}

PurchasingGrid.prototype.init = function(param) {
    var that = this;
    this.specification(param.specification);
    var columns = [
      { 
        field: "id",
        title: "ID"
      },
      {
        field: "creationTime",
        title: "Creation Time"
      },
      {
        field: "totalPurchasedPrice",
        title: "Total Purchased Price"
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
                    url: param.url || "purchasing/my-purchasings.spring",
                    dataType: "jsonp",
                    data: function() {
                        if (that._specification) {
                            return that._specification();
                        }
                    }
                },
                parameterMap: function(data, type) {
                    if (type == "read") {
                        return parameterMap(data);
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
        detailTemplate: " ",
        detailInit: function(e) {
            var purchasingItemGrid = createTemplate("PurchasingItemGrid", {
                enableManufacturerDependencies: true,
                specification: function() {
                    return {
                        purchasingId: e.data.id
                    };
                }
            });
            e.detailCell.empty().append(purchasingItemGrid);
        }
    });
    $(".k-grid--create", this.rawGrid).click(function() {
        that.create_click();
    });
}

PurchasingGrid.prototype.specification = function(specification) {
    if (specification && typeof(specification) != "function") {
        throw new Error("The argument must be function when it is not null");
    }
    this._specification = specification;
};

PurchasingGrid.prototype.create_click = function() {
    var dialog = createTemplate("PurchasingDialog");
    var that = this;
    dialog.bind("save", function() {
        that.refresh();
    });
};

PurchasingGrid.prototype.refresh = function() {
    this.rawGrid.data("kendoGridEx").dataSource.read();
};
