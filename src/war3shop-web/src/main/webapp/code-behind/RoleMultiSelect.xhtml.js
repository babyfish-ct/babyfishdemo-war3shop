/**
 * @author Tao Chen
 */
function RoleMultiSelect() {
    
    //ioc: @UiField
    this.tagTooltip = null;
    
    //ioc: @UiField
    this.multiSelect = null;
};

//ioc: @UiHandler
RoleMultiSelect.prototype.init = function(param) {
    this
    .multiSelect
    .kendoMultiSelect({
        placeholder : param.placeholder,
        dataValueField: "id",
        dataTextField: "name",
        itemTemplate: function(e) {
            return e.itemTemplate;
        },
        tagTemplate: function(e) {
            return (
                "<span class='tag-with-tooltip'>" +
                e.name +
                "</span>" +
                "<div style='display:none'>" +
                "<div style='text-align:left;font-style:italic;'>" +
                e.tagTooltip +
                "</div>" +
                "</div>"
            );
        },
        dataSource:  new kendo.data.DataSource({
            transport: {
                read: {
                    url: "authorization/all-roles.spring",
                    dataType: "jsonp",
                    data: {
                        queryPath: "this.privileges"
                    }
                }
            },
            schema: {
                errors: "exceptionMessage"
            },
            error: function(e) {
                createErrorDialog(e);
            },
            requestEnd: function(e) {
                if (e.type == "read") {
                    var roles = e.response;
                    for (var i = roles.length - 1; i >= 0; i--) {
                        var role = roles[i];
                        role.itemTemplate = 
                            "<div style='font-weight:bold'>" + 
                            "<img src='images/role.png'/>" +
                            role.name + 
                            "</div>" +
                            "<div style='padding-left:30px;font-style:italic;'>" +
                            role.privileges.select(function(e) {
                                return "<div><img src='images/privilege.png'/>" + e.name + "</div>";
                            }).join("") +
                            "</div>";
                        role.tagTooltip =
                            "Privileges: " +
                            "<div style='padding-left:30px;'>" +
                            role.privileges.select(function(e) {
                                return "<div><img src='images/privilege.png'/>" + e.name + "</div>";
                            }).join("") +
                            "</div>";
                    }
                }
            }
        })
    });
    this
    .tagTooltip
    .kendoTooltip({
        filter: "span.tag-with-tooltip",
        position: "top",
        width: "300px",
        content: function(e) {
            return e.target.next("div").html();
        }
    });
};

RoleMultiSelect.prototype.bind = function(event, handler) {
    this.multiSelect.data("kendoMultiSelect").bind(event, handler);
};

RoleMultiSelect.prototype.value = function(value) {
    if (typeof(value) == "undefined") {
        return this.multiSelect.data("kendoMultiSelect").value();
    } else {
        this.multiSelect.data("kendoMultiSelect").value(value);
    }
}
