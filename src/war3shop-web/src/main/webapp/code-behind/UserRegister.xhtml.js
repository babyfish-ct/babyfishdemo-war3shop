/**
 * @author Tao Chen
 */
function UserRegister() {
    
    //ioc: @UiField
    this.nameTextBox = null;
    
    //ioc: @UiField
    this.nameExistenceLabel = null;
    
    //ioc: @UiField
    this.passwordTextBox = null;
    
    //ioc: @UiField
    this.passwordAgainTextBox = null;
    
    //ioc: @UiField
    this.emailTextBox = null;
    
    //ioc: @UiField
    this.phoneTextBox = null;
    
    //ioc: @UiField
    this.addressTextBox = null;
    
    //ioc: @UiField
    this.captchaTextBox = null;
    
    //ioc: @UiField
    this.captchaImage = null;
    
    //ioc: @UiField
    this.photoUpload = null;
    
    //ioc: @UiField
    this.registerButton = null;
}

//ioc: @UiHandler
UserRegister.prototype.init = function() {
    
    prepareToUpload();
    
    $("tr", this.root).each(function() {
        var input = $("input[name]", this);
        $("label", this).attr("for", input.attr("id"));
        $("span.k-invalid-msg", this).attr("data-for", input.attr("name"));
    });
    var that = this;
    this.root.kendoValidator({
        rules: {
            samePassword: function(input) {
                if (input.is(that.passwordAgainTextBox)) {
                    return input.val() == that.passwordTextBox.val();
                }
                return true;
            }
        }
    });
    this.photoUpload.kendoUpload({
        async: {
            saveUrl: "upload/upload-image.spring?key=image",
            removeUrl: "upload/cancel-upload.spring?key=image",
            autoUpload: true
        },
        multiple: false
    });
    this.registerButton.kendoButton();
};

//ioc: @UiHandler
UserRegister.prototype.nameTextBox_change = function(e) {
    if (this.root.data("kendoValidator").validateInput(this.nameTextBox)) {
        var that = this;
        $.ajax({
            url: "authorization/is-registerable.spring",
            contentType: "application/x-www-form-urlencoded",
            dataType: "jsonp",
            data: {
                name: this.nameTextBox.val()
            },
            success: function(data) {
                if (data === false) {
                    that.nameExistenceLabel.show();
                } else {
                    that.nameExistenceLabel.hide();
                }
            },
            error: function() {
                that.nameExistenceLabel.hide();
            }
        });
    } else {
        this.nameExistenceLabel.hide();
    }
};

//ioc: @UiHandler
UserRegister.prototype.registerButton_click = function(e) {
    if (this.nameExistenceLabel.css("display") != "none" || !this.root.data("kendoValidator").validate()) {
        return;
    }
    var that = this;
    jsonp({
        url: "authorization/register.spring",
        data: {
            name: this.nameTextBox.val(),
            password: this.passwordTextBox.val(),
            passwordAgain: this.passwordAgainTextBox.val(),
            email: this.emailTextBox.val(),
            phone: this.phoneTextBox.val(),
            address: this.addressTextBox.val(),
            captcha: this.captchaTextBox.val()
        },
        success: function(e) {
            that.root.trigger("registiersuccessed");
        },
        error: function(e) {
            createErrorDialog(e);
            that.captchaImage.data("controller").refresh();
        }
    });
};
