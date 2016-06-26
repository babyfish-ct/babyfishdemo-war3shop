/**
 * @author Tao Chen
 */
function AdministratorGrid() {
    
    //ioc: @UiField
    this.gridTooltip = null;
    
    //ioc: @UiField
    this.rawGrid = null;
}

//ioc: @UiHandler
AdministratorGrid.prototype.init = function(param) {
    var that = this;
    this.specification(param.specification);
    var detailTemplate = null;
    if (param.showDetail) {
        detailTemplate = function(administrator) {
            return (
                "<div>" +
                "<div style='float:left'>" +
                "<img style='width:100px' src='" + 
                userImage(administrator) +
                "'/>" +
                "</div>" +
                "<div style='margin-left:110px'>" +
                "<fieldset class='k-block'>" +
                "<legend>Roles</legend>" +
                administrator.roles.select(function(e) {
                    return (
                        "<div>" +
                        "<img src='images/role.png'/>" +
                        e.name +
                        "<div style='padding-left:50px;font-style:italic;'>" +
                        e.privileges.select(function(e) {
                            return "<div><img src='images/privilege.png'/>" + e.name + "</div>";
                        }).join("") +
                        "</div>" +
                        "</div>"
                    );
                }).join("") +
                "</fieldset>" +
                "<fieldset class='k-block'>" +
                "<legend>Privileges</legend>" +
                administrator.privileges.select(function(e) {
                    return "<div><img src='images/privilege.png'/>" + e.name + "</div>";
                }).join("") +
                "</fieldset>" +
                "</div>"
            );
        };
    }
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
                    url: "authorization/administrators.spring",
                    dataType: "jsonp",
                    data: function() {
                        if (typeof(that._specification) == "function") {
                            return that._specification();
                        }
                    }
                },
                parameterMap: function(data, type) {
                    if (type == "read") {
                        return parameterMap(
                                data, 
                                param.showDetail ? "this.roles.privileges;this.privileges;" : ""
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
        columns: [
          { 
            field: "id",
            title: "ID"
          },
          {
            field: "active",
            title: "Active"
          },
          { 
            field: "name",
            title: "Name"
          },
          {
            field: "email",
            title: "Email"
          },
          {
            field: "creationTime",
            title: "Creation Time",
            format: "{0: yyyy-MM-dd HH:mm}"
          },
          {
            field: "lastLoginTime",
            title: "Last Login Time",
            format: "{0: yyyy-MM-dd HH:mm}"
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
        detailTemplate: detailTemplate,
        pageable: {
            pageSize: 10,
            input: true
        },
        sortable: true,
        reorderable: true,
        resizable: true,
        scrollable: false,
        dataBound: function() {
            var kendoGridEx = that.rawGrid.data("kendoGridEx");
            $("tr[data-uid]", that.rawGrid).each(function() {
                var administrator = kendoGridEx.dataItem(this);
                $(this)
                .data(
                        "tooltip", 
                        "<img style='width:100px' src='" + 
                        userImage(administrator) + 
                        "'/>");
            });
            if (typeof(that._expectedExpandId) != "undefined") {
                $("tr[data-uid]", that.rawGrid).each(function() {
                    var administrator = kendoGridEx.dataItem(this);
                    if (that._expectedExpandId == administrator.id) {
                        kendoGridEx.expandRow(this);
                        return false; //break each
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

AdministratorGrid.prototype.specification = function(specification) {
    if (specification && typeof(specification) != "function") {
        throw new Error("The argument must function when it is not null");
    }
    this._specification = specification;
};

AdministratorGrid.prototype.selectedIds = function(selectedIds) {
    if (selectedIds && typeof(selectedIds) != "function") {
        throw new Error("The argument must be function");
    }
    this._selectedIds = selectedIds;
};

AdministratorGrid.prototype.refresh = function() {
    this.rawGrid.data("kendoGridEx").dataSource.read();
};

AdministratorGrid.prototype.create_click = function() {
    var that = this;
    var dialog = createTemplate("AdministratorDialog");
    dialog.bind("save", function() {
        that.refresh();
    });
};

AdministratorGrid.prototype.edit_click = function(e) {
    var administrator = this.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var that = this;
    var dialog = createTemplate("AdministratorDialog", { administrator : administrator });
    dialog.bind("save", function() {
        that._expectedExpandId = administrator.id;
        that.refresh();
    });
};

AdministratorGrid.prototype.delete_click = function(e) {
    var administrator = this.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var dialog = createTemplate("ConfirmDialog", {
        message: "Do you want to delete this administarator?"
    });
    var that = this;
    dialog.bind("ok", function() {
        jsonp({
            url: "authorization/delete-user.spring",
            data: { id: administrator.id },
            success: function(e) {
                that.refresh();
            },
            error: function(e) {
                createErrorDialog(e);
            }
        });
    });
};
