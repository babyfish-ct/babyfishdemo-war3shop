/**
 * @author Tao Chen
 */
function ManufacturerDialog() {
    
    //ioc: @UiField
    this.nameTextBox = null;
    
    //ioc: @UiField
    this.raceDropDownList = null;
    
    //ioc: @UiField
    this.emailTextBox = null;
    
    //ioc: @UiField
    this.phoneTextBox = null;
    
    //ioc: @UiField
    this.purchaserDropDown = null;
    
    //ioc: @UiField
    this.photoUpload = null;
    
    //ioc: @UiField
    this.descriptionEditor = null;
    
    //ioc: @UiField
    this.saveButton = null;
    
    //ioc: @UiField
    this.cancelButton = null;
}

//ioc: @UiHandler
ManufacturerDialog.prototype.init = function(param) {
    var manufacturer = param.manufacturer || {};
    prepareToUpload();
    
    $("tr", this.root).each(function() {
        var input = $("input,textarea", this);
        $("label", this).attr("for", input.attr("id"));
        $("span.k-invalid-msg", this).attr("data-for", input.attr("name"));
    });
    this.root.kendoValidator();
    this.raceDropDownList.kendoDropDownList();
    this.photoUpload.kendoUpload({
        async: {
            saveUrl: "upload/upload-image.spring?key=image",
            removeUrl: "upload/cancel-upload.spring?key=image",
            autoUpload: true
        },
        multiple: false
    });
    // Must specify the parentNode before create kendoEditor(I think it's a bug of kendoEditor).
    this.root.appendTo(document.body);
    this.descriptionEditor.kendoEditor({
        tools: [
            "bold",
            "italic",
            "underline",
            "strikethrough",
            "justifyLeft",
            "justifyCenter",
            "justifyRight",
            "justifyFull",
            "insertUnorderedList",
            "insertOrderedList",
            "indent",
            "outdent",
            "subscript",
            "superscript",
            "createTable",
            "addRowAbove",
            "addRowBelow",
            "addColumnLeft",
            "addColumnRight",
            "deleteRow",
            "deleteColumn",
            "formatting",
            "fontName",
            "fontSize",
            "foreColor",
            "backColor"
        ]
    });
    if (typeof(manufacturer.id) == "number") {
        this._manufacturerId = manufacturer.id;
        this._manufacturerVersion = manufacturer.version;
        this.nameTextBox.val(manufacturer.name);
        this.raceDropDownList.data("kendoDropDownList").value(manufacturer.race);
        this.emailTextBox.val(manufacturer.email);
        this.phoneTextBox.val(manufacturer.phone);
        if (manufacturer.description) {
            this.descriptionEditor.data("kendoEditor").value(manufacturer.description);
        }
        jsonp({
            url: "product/manufacturer.spring",
            data: {
                id: this._manufacturerId,
                queryPath: "this.purchasers;"
            },
            success: function(e) {
                for (var i = 0; i < e.purchasers.length; i++) {
                    that.purchaserDropDown.data("controller").selectDataItem(e.purchasers[i]);
                }
            },
            error: function(e) {
                createErrorDialog(e)
                .bind("close", function() {
                    that.destroy();
                });
            }
        });
    }
    this.saveButton.kendoButton();
    this.cancelButton.kendoButton();
    var that = this;
    this
    .root
    .kendoWindow({
        title: typeof(manufacturer.id) == "undefined" ? "Create Manufacture" : "Edit Manufacture",
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
};

//ioc: @UiHandler
ManufacturerDialog.prototype.saveButton_click = function(e) {
    if (this.root.data("kendoValidator").validate()) {
        var that = this;
        jsonp({
            url: "product/submit-manufacturer.spring",
            data: {
                id: this._manufacturerId,
                version: this._manufacturerVersion,
                active: true, 
                name: this.nameTextBox.val(),
                race: this.raceDropDownList.data("kendoDropDownList").value(),
                email: this.emailTextBox.val(),
                purchaserIds: this.purchaserDropDown.data("controller").value().join(","),
                phone: this.phoneTextBox.val(),
                description: this.descriptionEditor.data("kendoEditor").value()
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
ManufacturerDialog.prototype.cancelButton_click = function(e) {
    this.destroy();
};

ManufacturerDialog.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
};
