/**
 * @author Tao Chen
 */
function AdministratorSpecification() {
    
    //ioc: @UiField
    this.likeNameTextBox = null;
    
    //ioc: @UiField
    this.likeEmailTextBox = null;
    
    //ioc: @UiField
    this.minCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.maxCreationTimeDateTimePicker = null;
    
    //ioc: @UiField
    this.permissionDependenciesTbody = null;
    
    //ioc: @UiField
    this.includedRoleTd = null;
    
    //ioc: @UiField
    this.excludedRoleTd = null;
    
    //ioc: @UiField
    this.includedPrivilegeTd = null;
    
    //ioc: @UiField
    this.excludedPrivilegeTd = null;
    
    //ioc: @UiField
    this.activeTbody = null;
    
    //ioc: @UiField
    this.activeDropDownList = null;
}

//ioc: @UiHandler
AdministratorSpecification.prototype.init = function(param) {
    $("tr", this.root).each(function() {
        var input = $("input[name],select[name]", this);
        $("label", this).attr("for", input.attr("id"));
        $("td:eq(0)", this).css("text-align", "right");
    });
    var that = this;
    $("tr", this.root).each(function() {
        var label = $("label", this);
        if (label.length) {
            var target = $("*[id]", this);
            if (target.length) {
                label.attr("for", target.attr("id"));
            }
        }
    });
    this.activeDropDownList.kendoDropDownList();
    this.minCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" });
    this.maxCreationTimeDateTimePicker.kendoDateTimePicker({ format: "yyyy-MM-dd HH:mm" });
    if (param.enableActiveChoices) {
        this.activeTbody.show();
    }
    if (param.enablePermissionDependencies) {
        this.permissionDependenciesTbody.show();
        this.includedRoleMultiSelect = 
            createTemplate("RoleMultiSelect", { placeholder: "Included Roles..." })
            .appendTo(this.includedRoleTd);
        this.excludedRoleMultiSelect = 
            createTemplate("RoleMultiSelect", { placeholder: "Excluded Roles..." })
            .appendTo(this.excludedRoleTd);
        var privilegeDataSource = new kendo.data.DataSource({
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
        }); 
        (
            this.includedPrivilegeMultiSelect = 
            $("<select></select>")
            .attr("multiple", "multiple")
            .css("width", "200px")
            .appendTo(this.includedPrivilegeTd)
        )
        .kendoMultiSelect({
            dataValueField: "id",
            dataTextField: "name",
            dataSource: privilegeDataSource,
            autoBind: false,
            placeholder: "Included Privileges"
        });
        (
            this.excludedPrivilegeMultiSelect =
            $("<select></select>")
            .attr("multiple", "multiple")
            .css("width", "200px")
            .appendTo(this.excludedPrivilegeTd)
        )
        .kendoMultiSelect({
            dataValueField: "id",
            dataTextField: "name",
            dataSource: privilegeDataSource,
            autoBind: false,
            placeholder: "Excluded Privileges"
        });
        privilegeDataSource.read();
        AdministratorSpecification._mutexRoleMultiSelect(
                this.includedRoleMultiSelect,
                this.excludedRoleMultiSelect);
        AdministratorSpecification._mutexRoleMultiSelect(
                this.excludedRoleMultiSelect,
                this.includedRoleMultiSelect);
        AdministratorSpecification._mutexMultiSelect(
                this.includedPrivilegeMultiSelect,
                this.excludedPrivilegeMultiSelect);
        AdministratorSpecification._mutexMultiSelect(
                this.excludedPrivilegeMultiSelect,
                this.includedPrivilegeMultiSelect);
    }
    /*
     * It is important to use deffered operation, 
     * please see the _mutexRoleMultiSelect and _mutexMultiSelect
     */
    $("input, select", this.root).bind("change", function() {
        that._triggerChanged();
    });
    $("input", this.root).bind("keyup", function() {
        that._triggerChanged();
    });
};

AdministratorSpecification.prototype.specification = function() {
    var spec = {
        likeName: this.likeNameTextBox.val(),
        likeEmail: this.likeEmailTextBox.val(),
        minCreationTime: kendo.toString(this.minCreationTimeDateTimePicker.data("kendoDateTimePicker").value(), "yyyy-MM-dd HH:mm"),
        maxCreationTime: kendo.toString(this.maxCreationTimeDateTimePicker.data("kendoDateTimePicker").value(), "yyyy-MM-dd HH:mm")
    };
    if (this.activeTbody.is(":visible")) {
        var value = this.activeDropDownList.data("kendoDropDownList").value();
        if (value == 1) {
            spec.active = true;
        } else if (value == 2) {
            spec.active = false;
        }
    }
    if (this.includedRoleMultiSelect) {
        spec.includedRoleIds = this.includedRoleMultiSelect.data("controller").value().join(",");
    }
    if (this.excludedRoleMultiSelect) {
        spec.excludedRoleIds = this.excludedRoleMultiSelect.data("controller").value().join(",");
    }
    if (this.includedPrivilegeMultiSelect) {
        spec.includedPrivilegeIds = this.includedPrivilegeMultiSelect.data("kendoMultiSelect").value().join(",");
    }
    if (this.excludedPrivilegeMultiSelect) {
        spec.excludedPrivilegeIds = this.excludedPrivilegeMultiSelect.data("kendoMultiSelect").value().join(",");
    }
    return spec;
};

AdministratorSpecification._mutexRoleMultiSelect = function(changedRmsw, otherRmsw) {
    var changedController = changedRmsw.data("controller");
    var otherController = otherRmsw.data("controller");
    changedRmsw.bind("change", function() {
        otherController.value(otherController.value().minus(changedController.value()));
    });
};

AdministratorSpecification._mutexMultiSelect = function(changedMs, otherMs) {
    var changedKms = changedMs.data("kendoMultiSelect");
    var otherKms = otherMs.data("kendoMultiSelect");
    changedMs.bind("change", function() {
        otherKms.value(otherKms.value().minus(changedKms.value()));
    });
};

AdministratorSpecification.prototype._triggerChanged = function() {
    var that = this;
    that._dirty = true;
    deferred(function() {
        if (that._dirty) {
            that._dirty = false;
            that.root.trigger("specificationchanged");
        }
    });
}
