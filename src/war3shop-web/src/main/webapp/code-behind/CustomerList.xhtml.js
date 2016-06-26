/**
 * @author Tao Chen
 */
function CustomerList() {
    
    //ioc: @UiField
    this.gridTooltip = null;
    
    //ioc: @UiField
    this.listView = null;
    
    //ico: @UiField
    this.pager = null;
    
    this._specification = null;
    
    this._selectedMap = {}; //key: id, value: true
    
    this._startDargElement = null;
    
    this._isMultiSelectMode = false;
}

//ioc: @UiHandler
CustomerList.clinit = function() {
    $(document.body).mouseup(function() {
        var currentList = CustomerList._currentList;
        if (currentList) {
            delete CustomerList._currentList;
            currentList._applySelection();
        }
    });
};

//ioc: @UiHandler
CustomerList.prototype.init = function(param) {
    var that = this;
    var rows = param.rows || 2;
    var cols = param.cols || 5;
    var dataSource = new kendo.data.DataSource({
        transport: {
            read: { 
                url: "authorization/customers.spring",
                dataType: "jsonp",
                data: function() {
                    if (typeof(that._specification) == "function") {
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
        pageSize: rows * cols,
        schema: {
            data: "entities",
            total: "totalRowCount",
            errors: "exceptionMessage"
        },
        error: function(e) {
            createErrorDialog(e);
        }
    });
    this._isMultiSelectMode = param.mode == "multiselect";
    this.pager.kendoPager({
        dataSource: dataSource 
    });
    this.listView.width(cols * 140 + "px").kendoListView({
        dataSource: dataSource,
        template: 
            "<div class='k--customer-cell' style='float:left;'>" +
            " <img class='k--customer-image' style='width:140px;height:160px'/>" +
            " <div style='text-align:center;width:136px;height:34px;padding:2px;overflow:hidden;'>" +
            "  <div class='k--customer-name' style='font-weight:bold;padding:1px;'></div>" +
            "  (id:<span class='k--customer-id'></span>, active:<span class='k--customer-active'></span>)" +
            " </div>" +
            "</div>",
        dataBound: function() {
            var customers = this.dataSource.data();
            $(".k--customer-cell", this.element).each(function(index) {
                var customer = customers[index];
                var tooltip = 
                    "<table>" +
                    " <tr>" +
                    "  <td style='text-align:right;width:110px;padding-right:20px;'>Name:</td>" +
                    "  <td style='text-align:left;'>" + customer.name + "</td>" +
                    " </tr>" +
                    " <tr>" +
                    "  <td style='text-align:right;width:110px;padding-right:20px;'>Active:</td>" +
                    "  <td style='text-align:left;'>" + customer.active + "</td>" +
                    " </tr>" +
                    " <tr>" +
                    "  <td style='text-align:right;width:110px;padding-right:20px;'>Email:</td>" +
                    "  <td style='text-align:left;'>" + customer.email + "</td>" +
                    " </tr>" +
                    " <tr>" +
                    "  <td style='text-align:right;width:110px;padding-right:20px;'>Creation Time:</td>" +
                    "  <td style='text-align:left;'>" + customer.creationTime + "</td>" +
                    " </tr>" +
                    " <tr>" +
                    "  <td style='text-align:right;width:110px;padding-right:20px;'>Phone:</td>" +
                    "  <td style='text-align:left;'>" + customer.phone + "</td>" +
                    " </tr>" +
                    " <tr>" +
                    "  <td style='text-align:right;width:110px;padding-right:20px;'>Address:</td>" +
                    "  <td style='text-align:left;'>" + customer.address + "</td>" +
                    "</tr>" +
                    "</table>";
                var cell = $(this).data("customer", customer).data("tooltip", tooltip);
                $(".k--customer-image", this).attr("src", userImage(customer));
                $(".k--customer-id", this).text(customer.id);
                $(".k--customer-name", this).text(customer.name);
                if (param.mode == "edit") {
                    $(".k--customer-active", this).append(
                            $("<input/>")
                            .attr("type", "checkbox")
                            .attr("checked", customer.active)
                            .change(function() {
                                var checkbox = $(this);
                                jsonp({
                                    url: "authorization/active-customer.spring",
                                    data: {
                                        id: customer.id,
                                        version: customer.version,
                                        active: checkbox.is(":checked")
                                    },
                                    success: function() {
                                        that.refresh();
                                    },
                                    error: function(e) {
                                        checkbox.attr("checked", !checkbox.is(":checked"));
                                        createErrorDialog(e);
                                    }
                                });
                            })
                    );
                } else {
                    $(".k--customer-active", this).text(customer.active);
                }
                that._refreshCell(cell);
                cell
                .bind("mouseenter", function(e) {
                    if (param.mode != "multiselect" || e.which == 0) {
                        cell.addClass("k-state-selected");
                        $(".k--customer-image", cell).css("opacity", "0.7");
                    }
                })
                .bind("mouseleave", function(e) {
                    if (param.mode != "multiselect" || e.which == 0) {
                        cell.removeClass("k-state-selected");
                        $(".k--customer-image", cell).css("opacity", "1.0");
                    }
                });
                if (param.mode == "multiselect") {
                    cell
                    .bind("mousedown", function() {
                        that._startDargElement = this;
                        CustomerList._currentList = that;
                        that._selecting(this);
                        that._refreshCell();
                        return false;
                    })
                    .bind("mousemove", function(e) {
                        if (e.which == 1 && that._startDargElement) {
                            that._selecting(this);
                            that._refreshCell();
                        }
                        return false;
                    });
                }
            });
        }
    });
    this.gridTooltip.kendoTooltipEx({
        filter: ".k--customer-cell",
        position: "top",
        animation: false,
        showAfter: 0,
        content: function(e) {
            return e.target.data("tooltip");
        }
    });
};

CustomerList.prototype.specification = function(specification) {
    this._specification = specification;
};

CustomerList.prototype.refresh = function() {
    this.listView.data("kendoListView").dataSource.read();
};

CustomerList.prototype.reselect = function(ids) {
    var map = {};
    for (var i = ids.length - 1; i >= 0; i--) {
        map[ids[i]] = true;
    }
    this._selectedMap = map;
    var that = this;
    $(".k--customer-cell", this.element).each(function() {
        that._refreshCell($(this));
    });
};

CustomerList.prototype._refreshCell = function(cell) {
    if (!cell) {
        var that = this;
        $(".k--customer-cell", this.listView.element).each(function() {
            that._refreshCell($(this));
        });
        return;
    }
    var customerId = cell.data("customer").id;
    var image = $(".k--customer-image", cell);
    if (cell[0].__selecting) {
        if (this._isMultiSelectMode) {
            if (this._selectedMap[customerId]) {
                image.grayscale(true);
            } else {
                image.grayscale(false);
            }
        }
        cell.addClass("k-state-selected");
        $(".k--customer-image", cell).css("opacity", "0.7");
    } else {
        if (this._isMultiSelectMode) {
            if (this._selectedMap[customerId]) {
                image.grayscale(false);
            } else {
                image.grayscale(true);
            }
        }
        cell.removeClass("k-state-selected");
        $(".k--customer-image", cell).css("opacity", "1.0");
    }
};

CustomerList.prototype._selecting = function(currentElement) {
    var that = this;
    var count = 0;
    if (that._startDargElement) {
        $(".k--customer-cell", this.listView.element).each(function() {
            if (that._startDargElement == this) {
                count++;
            }
            if (currentElement == this) {
                count++;
            }
            this.__selecting = count == 1;
        });
        that._startDargElement.__selecting = true;
        currentElement.__selecting = true;
    }
};

CustomerList.prototype._applySelection = function() {
    var that = this;
    if (that._startDargElement) {
        that._startDargElement = null;
        try {
            $(".k--customer-cell", that.listView).each(function() {
                if (this.__selecting) {
                    delete this.__selecting;
                    var cell = $(this);
                    var customer = cell.data("customer");
                    var eventName;
                    if (that._selectedMap[customer.id]) {
                        delete that._selectedMap[customer.id];
                        eventName = "customerunselected";
                    } else {
                        that._selectedMap[customer.id] = true;
                        eventName = "customerselected";
                    }
                    that.root.trigger(eventName, customer);
                }
            });
        } finally {
            that._refreshCell();
            var cell = $(this);
            cell.addClass("k-state-selected");
            $(".k--customer-image", cell).css("opacity", "0.7");
        }
    }
};
