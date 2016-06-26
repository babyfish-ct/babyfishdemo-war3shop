/**
 * @author Tao Chen
 */
function RoleManagement() {
    
    //ioc: @UiField
    this.roleGrid = null;
}

//ioc: @UiHandler
RoleManagement.prototype.init = function() {
    var that = this;
    this.roleGrid.kendoGrid({
        dataSource: {
            transport: {
                read: { 
                    url: "authorization/all-roles.spring",
                    dataType: "jsonp",
                    data: { queryPath: "this.privileges" }
                }
            },
            schema: {
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
            field: "name",
            title: "Name"
          },
          {
            field: "privilegeText",
            title: "Privileges",
            sortable: false,
            template: function(role) {
                return role.privileges.select(function(e) {
                        return e.name;
                    }).join(",");
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
        ],
        toolbar: [ 
            { name: "-create", text: "Create..." },
            { name: "-refresh", text: "Refresh" }
        ],
        //This is client-sort, need not server-sort, because it is not pageable.
        sortable: true, 
        reorderable: true,
        resizable: true,
        scrollable: false
    });
    $(".k-grid--create", this.roleGrid).click(function() {
        that.create_click();
    });
    $(".k-grid--refresh", this.roleGrid).click(function() {
        that.refresh_click();
    });
};

RoleManagement.prototype.create_click = function() {
    var that = this;
    createTemplate("RoleDialog").bind("save", function() {
        that.refresh();
    });
};

RoleManagement.prototype.refresh_click = function() {
    this.roleGrid.data("kendoGrid").dataSource.read();
};

RoleManagement.prototype.edit_click = function(e) {
    var role = this.roleGrid.data("kendoGrid").dataItem($(e.target).closest("tr"));
    var that = this;
    createTemplate("RoleDialog", role).bind("save", function() {
        that.refresh();
    });
};

RoleManagement.prototype.delete_click = function(e) {
    var role = this.roleGrid.data("kendoGrid").dataItem($(e.target).closest("tr"));
    var dialog = createTemplate("ConfirmDialog", {
        message: "Do you want to delete this administarator?"
    });
    var that = this;
    dialog.bind("ok", function() {
        jsonp({
            url: "authorization/delete-role.spring",
            data: { id: role.id },
            success: function(e) {
                that.refresh();
            },
            error: function(e) {
                createErrorDialog(e);
            }
        });
    })
};

RoleManagement.prototype.refresh = function() {
    this.roleGrid.data("kendoGrid").dataSource.read();
};
