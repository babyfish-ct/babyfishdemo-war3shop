/**
 * @author Tao Chen
 */
function AdministratorDialog() {
    
    //ioc: @UiField
    this.nameTextBox = null;
    
    //ioc: @UiField
    this.activeCheckbox = null;
    
    //ioc: @UiField
    this.emailTextBox = null;
    
    //ioc: @UiField
    this.roleMultiSelect = null;
    
    //ioc: @UiField
    this.privilegeMultiSelect = null;
    
    //ioc: @UiField
    this.saveButton = null;
    
    //ioc: @UiField
    this.cancelButton = null;
};

AdministratorDialog.prototype.init = function(param) {
    var administrator = param.administrator || {};
    $("tr", this.root).each(function() {
        var input = $("input[name],select[name]", this);
        $("label", this).attr("for", input.attr("id"));
        $("span.k-invalid-msg", this).attr("data-for", input.attr("name"));
    });
    this._administratorId = administrator.id;
    this._administratorVersion = administrator.version;
    this.saveButton.kendoButton().data("kendoButton").enable(false);
    this.cancelButton.kendoButton()
    var that = this;
    new Workflow({
        queryPurchaseProductPrivilege: {
            action: function() {
                jsonp({
                    callBackThis: this,
                    url: "authorization/privilege-by-name.spring",
                    data: {
                        name: "purchase-products",
                        queryPath: "this.roles"
                    },
                    success: function(e) {
                        that._purchaseProductPrivilege = e;
                        this.finish("queryPurchaseProductPrivilege");
                    },
                    error: function(e) {
                        createErrorDialog(e)
                        .bind("close", function() {
                            that.destroy();
                        });
                    }
                });
            }
        },
        queryRoles: {
            action: function() {
                var wf = this;
                that.roleMultiSelect.data("controller").bind("dataBound", function() {
                    wf.finish("queryRoles");
                });
            }
        },
        queryPrivileges: {
            action: function() {
                var wf = this;
                that
                .privilegeMultiSelect
                .kendoMultiSelect({
                    dataValueField: "id",
                    dataTextField: "name",
                    dataSource: new kendo.data.DataSource({
                        transport: {
                            read: {
                                url: "authorization/all-privileges.spring",
                                dataType: "jsonp"
                            }
                        },
                        schema: {
                            errors: "exceptionMessage"
                        }
                    }),
                    error: function(e) {
                        createErrorDialog(e);
                    },
                    dataBound: function() {
                        wf.finish("queryPrivileges");
                    }
                });
            }
        },
        bindAdministarator: {
            dependencies: [ "queryPurchaseProductPrivilege", "queryRoles", "queryPrivileges" ],
            action: function() {
                if (typeof(administrator.id) == "undefined") {                  
                    that.nameTextBox.attr("required", "true");
                    that.activeCheckbox.attr("checked", true);
                    that.emailTextBox.attr("required", "true");
                    that.saveButton.data("kendoButton").enable(true);
                } else {
                    that.nameRow.hide();
                    that.activeCheckbox.attr("checked", administrator.active);
                    that.emailTextBox.val(administrator.email);
                    that.roleMultiSelect.data("controller").value(
                            administrator.roles.select(function(e) { 
                                return e.id; 
                            })
                    );
                    that.privilegeMultiSelect.data("kendoMultiSelect").value(
                            administrator.privileges.select(function(e) { 
                                return e.id; 
                            })
                    );
                    that.saveButton.data("kendoButton").enable(true);
                    that.root.data("kendoWindow").center();
                }
            }
        }
    }).startAllRoots();
    this.root.kendoValidator();
    this
    .root
    .appendTo(document.body)
    .kendoWindow({
        title: typeof(administrator.id) == "undefined" ? "Create Aministarator" : "Edit Administrator",
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
}

//ioc: @UiHandler
AdministratorDialog.prototype.saveButton_click = function(e) {
    var that = this;
    if (!this.root.data("kendoValidator").validate()) {
        return;
    }
    var submit = function() {
        that.saveButton.data("kendoButton").enable(false);
        jsonp({
            url: "authorization/submit-administrator.spring",
            data: {
                id: that._administratorId,
                version: that._administratorVersion,
                name: that.nameTextBox.val(),
                active: that.activeCheckbox.is(":checked"),
                email: that.emailTextBox.val(),
                roleIds: that.roleMultiSelect.data("controller").value().join(","),
                privilegeIds: that.privilegeMultiSelect.data("kendoMultiSelect").value().join(",")
            },
            success: function(e) {
                that.root.trigger("save");
                that.destroy();
            },
            error: function(e) {
                createErrorDialog(e);
            },
            complete: function() {
                that.saveButton.data("kendoButton").enable(true);
            }
        });
    };
    if (typeof(that._administratorId) == "undefined") {
        submit();
    } else {
        var hasPurchaseProdcutPrivilege = 
            this
            .privilegeMultiSelect
            .data("kendoMultiSelect")
            .value()
            .contains(this._purchaseProductPrivilege.id);
        var hasPurchaseProductRoles = 
            this
            .roleMultiSelect
            .data("controller")
            .value()
            .containsAny(
                    this
                    ._purchaseProductPrivilege
                    .roles
                    .select(function(e) {
                        return e.id;
                    })
            );
        if (hasPurchaseProdcutPrivilege || hasPurchaseProductRoles) {
            submit();
        } else {
            new Workflow({
                init: function() {
                    this.purchasedManufacturers = [];
                    this.purchasedProducts = [];
                    this.limit = 10;
                },
                purchasedManufacturers: {
                    action: function() {
                        jsonp({
                            callBackThis: this,
                            url: "product/manufacturers.spring",
                            data: {
                                includedPurchaserIds: that._administratorId,
                                pageIndex: 0,
                                pageSize: this.limit
                            },
                            success: function(e) {
                                this.purchasedManufacturers = e.entities;
                                this.finish("purchasedManufacturers");
                            },
                            error: function(e) {
                                createErrorDialog(e);
                            }
                        });
                    }
                },
                purchasedProducts: {
                    action: function() {
                        jsonp({
                            callBackThis: this,
                            url: "product/products.spring",
                            data: {
                                includedPurchaserIds: that._administratorId,
                                pageIndex: 0,
                                pageSize: this.limit
                            },
                            success: function(e) {
                                this.purchasedProducts = e.entities;
                                this.finish("purchasedProducts");
                            },
                            error: function(e) {
                                createErrorDialog(e);
                            }
                        });
                    }
                },
                waring: {
                    dependencies: [ "purchasedManufacturers", "purchasedProducts" ],
                    action: function() {
                        if (this.purchasedManufacturers.length == 0 && this.purchasedProducts.length == 0) {
                            submit();
                        } else {
                            var warning = "The original administrator is ";
                            if (this.purchasedManufacturers.length) {
                                warning += 
                                    "the purchaser of the manufacturers " +
                                    this
                                    .purchasedManufacturers
                                    .select(function(e) {
                                        return "\"" + e.name + "\"";
                                    })
                                    .join(", ");
                                if (this.purchasedManufacturers.length == this.limit) {
                                    warning + ", etc"
                                }
                            }
                            if (this.purchasedProducts.length) {
                                if (this.purchasedManufacturers.length) {
                                    warning += " and the ";   
                                }
                                warning += 
                                    "the purchaser of the products " +
                                    this
                                    .purchasedProducts
                                    .select(function(e) {
                                        return "\"" + e.name + "\"";
                                    })
                                    .join(", ");
                                if (this.purchasedProducts.length == this.limit) {
                                    warning + ", etc"
                                }
                            }
                            warning += 
                                ", now you want to the privilege \"purchase-products\" or " +
                                "the roles contains the privilege of it, " +
                                "that makes the purchasing relationship(s) will be delete automatically! " +
                                "Do you want to continue?";
                            var dialog = createTemplate(
                                    "ConfirmDialog", { 
                                        title: "Warning", 
                                        message: warning
                                    });
                            dialog.bind("ok", function() {
                                submit();
                            });
                        }
                    }
                }
            }).startAllRoots();
        }
    }
};

//ioc: @UiHandler
AdministratorDialog.prototype.cancelButton_click = function(e) {
    this.destroy();
};

AdministratorDialog.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
};
