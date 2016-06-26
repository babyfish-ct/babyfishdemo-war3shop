/**
 * @author Tao Chen
 */
function UserConfiguration() {
    
    //ioc: @UiFiled
    this.nameTextBox = null;
    
    //ioc: @UiFiled
    this.emailTextBox = null;
    
    //ioc: @UiField
    this.customerTbody = null;
    
    //ioc: @UiField
    this.phoneTextBox = null;
    
    //ioc: @UiField
    this.addressTextBox = null;
    
    //ioc: @UiField
    this.originalPhoto = null;
    
    //ioc: @UiFiled
    this.photoUpload = null;
    
    //ioc: @UiField
    this.okButton = null;
    
    //ioc: @UiField
    this.cancelButton = null;
}

//ioc: @UiHandler
UserConfiguration.prototype.init = function() {
    prepareToUpload();
    var that = this;
    $("tr", this.root).each(function() {
        $("td:not([colspan]):eq(0)", this).css("text-align", "right");
        var input = $("input[id], select[id]", this);
        $("label", this).attr("for", input.attr("id"));
    });
    this.root.kendoValidator();
    this.photoUpload.kendoUpload({
        async: {
            saveUrl: "upload/upload-image.spring?key=image",
            removeUrl: "upload/cancel-upload.spring?key=image",
            autoUpload: true
        },
        multiple: false,
        success: function() {
            that.originalPhoto.attr("src", uploadedImage());
        }
    });
    this.okButton.kendoButton();
    this.cancelButton.kendoButton();
    this
    .root
    .appendTo(document.body)
    .kendoWindow({
        title: "Account Settings",
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
    jsonp({
        url: "authorization/current-user.spring",
        success: function(user) {
            that._userVersion = user.version;
            that.nameTextBox.val(user.name);
            that.emailTextBox.val(user.email);
            if (user.isCustomer) {
                that.customerTbody.show();
                that.phoneTextBox.val(user.phone);
                that.addressTextBox.val(user.address);
            }
            that.root.data("kendoWindow").center();
        },
        error: function(e) {
            createErrorDialog(e)
            .bind("close", function() {
                that.destroy();
            });
        }
    });
    that.originalPhoto.attr("src", currentUserImage());
};

//ioc: @UiHandler
UserConfiguration.prototype.okButton_click = function(sender, e) {
    var that = this;
    jsonp({
        url: "authorization/configure-current-user.spring",
        data: {
            version: that._userVersion,
            name: that.nameTextBox.val(),
            email: that.emailTextBox.val(),
            phone: that.phoneTextBox.val(),
            address: that.addressTextBox.val()
        },
        success: function(e) {
            that.destroy();
        },
        error: function(e) {
            createErrorDialog(e);
        }
    });
};

//ioc: @UiHandler
UserConfiguration.prototype.cancelButton_click = function(sender, e) {
    this.destroy();
};

UserConfiguration.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
};
