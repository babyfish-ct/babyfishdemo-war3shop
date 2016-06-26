/**
 * @author Tao Chen
 */
function RoleDialog() {
    //ioc: @UiHandler
    this.nameTextBox = null;
    
    //ioc: @UiHandler
    this.privilegeMultiSelect = null;
    
    //ioc: @UiHandler
    this.saveButton = null;
    
    //ioc: @UiHandler
    this.cancelButton = null;
}

RoleDialog.prototype.init = function(role) {
    this._roleId = role.id;
    this._roleVersion = role.version;
    var that = this;
    $("tr", this.root).each(function() {
        var input = $("input[name]", this);
        $("label", this).attr("for", input.attr("id"));
        $("span.k-invalid-msg", this).attr("data-for", input.attr("name"));
    });
    this.root.kendoValidator();
    this
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
            },
            error: function(e) {
                createErrorDialog(e);
            }
        }),
        dataBound: function() {
            if (typeof(role.id) != "undefined") {
                that.nameTextBox.val(role.name);
                that.privilegeMultiSelect.data("kendoMultiSelect").value(
                        role.privileges.select(function(e) {
                            return e.id;
                        })
                );
            }
        }
    });
    this
    .root
    .appendTo(document.body) //Self manager, should return false
    .kendoWindow({
        title: typeof(role.id) == "undefined" ? "Create Role": "Edit Role",
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
    this.saveButton.kendoButton();
    this.cancelButton.kendoButton();
};

//ioc: @UiHandler
RoleDialog.prototype.saveButton_click = function(e) {
    var that = this;
    if (this.root.data("kendoValidator").validate()) {
        jsonp({
            url: "authorization/update-role.spring",
            data: {
                id: this._roleId,
                version: this._roleVersion,
                name: this.nameTextBox.val(),
                privilegeIds: this.privilegeMultiSelect.data("kendoMultiSelect").value().join(",")
            },
            success: function(e) {
                that.root.trigger("save");
                that.destroy();
            },
            error: function(e) {
                createErrorDialog(e);
            }
        });
    }
};

//ioc: @UiHandler
RoleDialog.prototype.cancelButton_click = function(e) {
    this.destroy();
};

RoleDialog.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
};
