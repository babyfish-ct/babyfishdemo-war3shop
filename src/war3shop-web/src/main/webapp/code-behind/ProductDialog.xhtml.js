/**
 * @author Tao Chen
 */
function ProductDialog() {
    
    //ioc: @UiField
    this.nameTextBox = null;
    
    //ioc: @UiField
    this.activeCheckbox = null;
    
    //ioc: @UiField
    this.typeDropDownList = null;
    
    //ioc: @UiField
    this.raceDropDownList = null;
    
    //ioc: @UiField
    this.priceTextBox = null;
    
    //ioc: @UiField
    this.manufacturerDropDown = null;
    
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
ProductDialog.prototype.init = function(param) {
    var product = param.product || {};
    prepareToUpload();
    var that = this;
    $("tr", this.root).each(function() {
        var input = $("input,textarea", this);
        $("label", this).attr("for", input.attr("id"));
        $("span.k-invalid-msg", this).attr("data-for", input.attr("name"));
    });
    this.root.kendoValidator();
    this.typeDropDownList.kendoDropDownList();
    this.raceDropDownList.kendoDropDownList();
    this.priceTextBox.kendoNumericTextBox();
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
    if (typeof(product.id) == "undefined") {
        this.activeCheckbox.attr("checked", true);
    } else {
        this._productId = product.id;
        this._productVersion = product.version;
        this.nameTextBox.val(product.name);
        this.activeCheckbox.attr("checked", product.active);
        this.typeDropDownList.data("kendoDropDownList").value(product.type);
        this.raceDropDownList.data("kendoDropDownList").value(product.race);
        this.priceTextBox.data("kendoNumericTextBox").value(product.price);
        if (product.description) {
            this.descriptionEditor.data("kendoEditor").value(product.description);
        }
        jsonp({
            url: "product/product.spring",
            data: {
                id: this._productId,
                queryPath: "this.manufacturers;this.purchasers;"
            },
            success: function(e) {
                for (var i = 0; i < e.manufacturers.length; i++) {
                    that.manufacturerDropDown.data("controller").selectDataItem(e.manufacturers[i]);
                }
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
    this
    .root
    .kendoWindow({
        title: typeof(product.id) == "undefined" ? "Create Product" : "Edit Product",
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
ProductDialog.prototype.saveButton_click = function(e) {
    if (this.root.data("kendoValidator").validate()) {
        var that = this;
        jsonp({
            url: "product/submit-product.spring",
            data: {
                id: this._productId,
                version: this._productVersion,
                name: this.nameTextBox.val(),
                active: this.activeCheckbox.is(":checked"),
                type: this.typeDropDownList.data("kendoDropDownList").value(),
                race: this.raceDropDownList.data("kendoDropDownList").value(),
                price: this.priceTextBox.data("kendoNumericTextBox").value(),
                manufacturerIds: this.manufacturerDropDown.data("controller").value().join(","),
                purchaserIds: this.purchaserDropDown.data("controller").value().join(","),
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
ProductDialog.prototype.cancelButton_click = function(e) {
    this.destroy();
};

ProductDialog.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
};
