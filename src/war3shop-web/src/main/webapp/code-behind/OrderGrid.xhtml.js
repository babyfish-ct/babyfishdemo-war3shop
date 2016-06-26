/**
 * @author Tao Chen
 */
function OrderGrid() {

    //ioc: @UiField
    this.gridTooltip = null;
    
    //ioc: @UiField
    this.rawGrid = null;
    
    this._specification = null;
}

OrderGrid.prototype.init = function(param) {
    var that = this;
    var columns = [
        {
            title: "Id",
            field: "id"
        },
        {
            title: "Creation Time",
            field: "creationTime"
        },
        {
            title: "Delivered Time",
            field: "deliveredTime"
        },
        {
            title: "Total Actual Money",
            field: "totalMoney.actualMoney",
            template: function(order) {
                return order.totalMoney.actualMoney;
            }
        },
        {
            title: "Total Expected Money",
            field: "totalMoney.expectedMoney",
            template: function(order) {
                return order.totalMoney.expectedMoney;
            }
        },
        {
            title: "Total reduced Money",
            field: "totalMoney.reducedMoney",
            template: function(order) {
                return order.totalMoney.reducedMoney;
            }
        },
        {
            title: "Total Gift Money",
            field: "totalMoney.giftMoney",
            template: function(order) {
                return order.totalMoney.giftMoney;
            }
        },
        {
            title: "Phone",
            field: "phone"
        },
    ];
    
    var additionalQueryPath = "";
    if (param.behavior == "manage" || param.behavior == "delivery") {
        columns.splice(
            columns.length - 2,
            0,
            {
                title: "Customer",
                field: "customer.name",
                template: function(order) {
                    return order.customer.name;
                }
            }
        );
        additionalQueryPath += "this.customer;";
    }
    if (param.behavior == "manage") {
        columns.splice(
            1,
            0,
            {
                title: "Deliveryman",
                field: "deliveryman.name",
                template: "<div class='k--grid-deliveryman'></div>"
            }
        );
        additionalQueryPath += "this.deliveryman;this.customer;";
    } else if (param.behavior == "delivery") {
        columns.push({
            command: [
                {
                name: "-delivery", 
                text: "Delivery",
                click: function(e) {
                    that.delivery_click(e);
                }
                }
            ]
    });
    } else {
        columns.push({
            command: [
                {
                name: "-change-address", 
                text: "Change Address",
                click: function(e) {
                    that.changeAddress_click(e);
                }
                }
            ]
    });
    }
    this.rawGrid.kendoGridEx({
        mode: param.mode,
        autoBind: param.autoBind,
        dataSource: {
            transport: {
                read: { 
                    url: param.url || "sale/assured-orders.spring",
                    dataType: "jsonp",
                    data: function() {
                        if (that._specification) {
                            return that._specification();
                        }
                    }
                },
                parameterMap: function(data, type) {
                    if (type == "read") {
                        return parameterMap(
                                data, 
                                "this.orderItems.product;" +
                                "this.orderItems.preferentialActions.giftProduct;" +
                                "this.giftItems.product;" +
                                additionalQueryPath
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
        columns: columns,
        pageable: {
            pageSize: 10,
            input: true
        },
        sortable: true,
        reorderable: true,
        resizable: true,
        scrollable: false,
        detailTemplate: "<div class='k-order-detail-container'></div>",
        detailInit: function(e) {
            var orderDetailContainer = e.detailCell.find(".k-order-detail-container");
            var addressDiv = $("<div></div>").css("padding-bottom", "20px");
            addressDiv.append($("<span></span>").text("Address: "));
            if (e.data) {
                addressDiv
                .append($("<input/>")
                .attr("type", "text")
                .attr("readonly", "readonly")
                .addClass("k-textbox")
                .css("width", "600px")
                .val(e.data.address));
            }
            var orderDetailPanel = createTemplate("OrderDetailPanel");
            orderDetailPanel.data("controller").setOrder(e.data);
            orderDetailContainer.append(addressDiv);
            orderDetailContainer.append(orderDetailPanel);
        },
        dataBound: function(e) {
            $("tr[data-uid]", that.rawGrid).each(function() {
                var order = that.rawGrid.data("kendoGridEx").dataItem(this);
                if (typeof(order) == "undefined") {
                    return;
                }
                if (order.deliveredTime) {
                    $(".k-grid--delivery", this).addClass("k-state-disabled");
                    $(".k-grid--change-address", this).addClass("k-state-disabled");
                }
                var deliverymanDiv = $(".k--grid-deliveryman", this);
                if (deliverymanDiv.length != 0) {
                    var deliverymanDropDown = createTemplate(
                            "AdministratorDropDown", {
                                mode: "single",
                                implicitSpecification: {
                                    includedPrivilegeNames: 'delivery-orders'
                                }
                            }
                    ).bind("change", function() {
                        var oldDataItemValue = deliverymanDropDown.data("old-data-item-value");
                        jsonp({
                            url: "sale/assign-deliveryman.spring",
                            data: {
                                orderId: order.id,
                                deliverymanId: deliverymanDropDown.data("controller").value()
                            },
                            success: function() {
                                deliverymanDropDown.data("old-data-item-value", deliverymanDropDown.data("controller").dataItemValue());
                            },
                            error: function(e) {
                                deliverymanDropDown.data("controller").selectDataItem(oldDataItemValue);
                                deliverymanDropDown.data("old-data-item-value", deliverymanDropDown.data("controller").dataItemValue());
                                createErrorDialog(e);
                            }
                        });
                    });
                    if (order.deliveryman) {
                        deliverymanDropDown.data("controller").selectDataItem(order.deliveryman);
                    }
                    deliverymanDiv.append(deliverymanDropDown);
                }
                var arr = [];
                for (var i = order.orderItems.length - 1; i >= 0; i--) {
                    var orderItem = order.orderItems[i];
                    arr[i] = { product: orderItem.product, quantity: orderItem.quantity };
                }
                for (var i = 0; i < order.giftItems.length; i++) {
                    var giftItem = order.giftItems[i];
                    var productId = giftItem.product.id;
                    var conflictIndex = arr.length - 1; 
                    while (conflictIndex >= 0) {
                        if (arr[conflictIndex].product.id == productId) {
                            arr[conflictIndex].quantity += giftItem.quantity;
                            break;
                        }
                        conflictIndex--;
                    }
                    if (conflictIndex == -1) {
                        arr.push({ product: giftItem.product, quantity: giftItem.quantity });
                    }
                }
                var tooltip = "<div style='width:";
                tooltip += (arr.length < 3 ? arr.length : 3) * 106 + "px";
                tooltip += ";'>";
                for (var i = 0; i < arr.length; i++) {
                    tooltip += "<div style='float:left;padding:1px;width:104px;height:144px'>";
                    tooltip += "<div class='k-widget' style='width:100px;height:140px'>";
                    tooltip += "<div style='height:120px;overflow:hidden;'>";
                    tooltip += "<img style='width:100px;height:120px;' src='" + productImage(arr[i].product) + "'/>";
                    tooltip += "</div>";
                    tooltip += "<div style='text-align:center'>";
                    tooltip += "x" + arr[i].quantity;
                    tooltip += "</div>";
                    tooltip += "</div>";
                    tooltip += "</div>";
                }
                tooltip += " <div style='clear:both;'></div>";
                tooltip += "</div>";
                if (param.behavior == "manage" || param.behavior == "delivery") {
                    var tooltip2 = 
                        "<div>" +
                        " <div class='k-widget' style='margin:5px'>" +
                        "  <div style='padding:5px'>Customer</div>" +
                        "   <img style='width:140px;height:160px;' src='" + userImage(order.customer) + "'/>" +
                        " </div>" +
                        " <div class='k-widget' style='margin:5px'>" +
                        "  <div style='padding:5px'>Products</div>" +
                        tooltip +
                        " </div>" +
                        "</div>";
                    tooltip = tooltip2;
                }
                $(this).data("tooltip", tooltip);
            });
        }
    });
    this.gridTooltip.kendoTooltipEx({
        filter: "tr[data-uid]:not(:has(.k-order-detail-container))",
        position: "left",
        animation: false,
        showAfter: 0,
        content: function(e) {
            return e.target.data("tooltip");
        }
    });
};

OrderGrid.prototype.specification = function(specification) {
    if (specification && typeof(specification) != "function") {
        throw new Error("The argument must be function when it is not null");
    }
    this._specification = specification;
};

OrderGrid.prototype.refresh = function() {
    this.rawGrid.data("kendoGridEx").dataSource.read();
};

OrderGrid.prototype.delivery_click = function(e) {
    if ($(e.target).is(".k-state-disabled")) {
        return;
    }
    var that = this;
    createTemplate("ConfirmDialog", { title: "Confirm", message: "Are you sure the delivery this order?" })
    .bind("ok", function() {
        var order = that.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
        jsonp({
            url: "sale/delivery-order.spring",
            data: { orderId: order.id },
            success: function() {
                that.refresh();
            },
            error: function(e) {
                createErrorDialog(e);
            }
        });
    });
};

OrderGrid.prototype.changeAddress_click = function(e) {
    if ($(e.target).is(".k-state-disabled")) {
        return;
    }
    var that = this;
    var order = that.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var orderAddressDialog = createTemplate(
            "OrderAddressDialog", 
            { 
                data: {
                    address: order.address,
                    phone: order.phone
                }
            }
    );
    orderAddressDialog.bind("ok", function() {
        jsonp({
            url: "sale/change-order-address.spring",
            data: { 
                orderId: order.id,
                address: orderAddressDialog.data("controller").data().address,
                phone: orderAddressDialog.data("controller").data().phone
            },
            success: function() {
                that.refresh();
            },
            error: function(e) {
                createErrorDialog(e);
            }
        });
    });
};
