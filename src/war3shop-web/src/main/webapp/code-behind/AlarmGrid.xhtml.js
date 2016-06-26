/**
 * @author Tao Chen
 */
function AlarmGrid() {
    //ioc: @UiField
    this.rawGrid = null;
}

//ioc: @UiHandler
AlarmGrid.prototype.init = function(param) {
    var that = this;
    this.specification(param.specification);
    this.rawGrid.kendoGridEx({
        mode: param.mode,
        autoBind: param.autoBind,
        dataSource: {
            transport: {
                read: { 
                    url: "alarm/my-alarms.spring",
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
            field: "creationTime",
            title: "Creation Time",
            format: "{0: yyyy-MM-dd HH:mm}"
          },
          {
            field: "message",
            title: "Message"
          },
          {
            command: [ 
                { 
                    name: "-acknowledge", 
                    text: "Acknowledge",
                    click: function(e) {
                        that.acknowledge_click(e);
                    }
                }, 
                { 
                    name: "-unacknowledge", 
                    text: "Unacknowledge",
                    click: function(e) {
                        that.unacknowledge_click(e);
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
                var alarm = kendoGridEx.dataItem(this);
                if (alarm.acknowledged) {
                    $(".k-grid--acknowledge", this).hide();
                } else {
                    $(".k-grid--unacknowledge", this).hide();
                }
            });
        }
    });
};

AlarmGrid.prototype.specification = function(specification) {
    if (specification && typeof(specification) != "function") {
        throw new Error("The argument must function when it is not null");
    }
    this._specification = specification;
};

AlarmGrid.prototype.refresh = function() {
    this.rawGrid.data("kendoGridEx").dataSource.read();
};

AlarmGrid.prototype.unacknowledge_click = function(e) {
    var order = this.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var that = this;
    jsonp({
        url: "alarm/unacknowledge-alarm.spring",
        data: { alarmId: order.id },
        success: function(e) {
            that.refresh();
        },
        error: function(e) {
            createErrorDialog(e);
        }
    });
};

AlarmGrid.prototype.acknowledge_click = function(e) {
    var order = this.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var that = this;
    jsonp({
        url: "alarm/acknowledge-alarm.spring",
        data: { alarmId: order.id },
        success: function(e) {
            that.refresh();
        },
        error: function(e) {
            createErrorDialog(e);
        }
    });
};

AlarmGrid.prototype.delete_click = function(e) {
    var order = this.rawGrid.data("kendoGridEx").dataItem($(e.target).closest("tr"));
    var that = this;
    jsonp({
        url: "alarm/delete-alarm.spring",
        data: { alarmId: order.id },
        success: function(e) {
            that.refresh();
        },
        error: function(e) {
            createErrorDialog(e);
        }
    });
};
