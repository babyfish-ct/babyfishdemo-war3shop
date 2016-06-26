/**
 * @author Tao Chen
 */
function SysMailConfiguration() {
    
    //ioc: @UiField
    this.smtpTbody = null;
    
    //ioc: @UiField
    this.protocolTextBox = null;
    
    //ioc: @UiField
    this.enableSSLCheckbox = null;
    
    //ioc: @UiField
    this.hostTextBox = null;
    
    //ioc: @UiField
    this.portTextBox = null;
    
    //ioc: @UiField
    this.userTextBox = null;
    
    //ioc: @UiField
    this.passwordCheckbox = null;
    
    //ioc: @UiField
    this.passwordTbody = null;
    
    //ioc: @UiField
    this.passwordTextBox = null;
    
    //ioc: @UiField
    this.passwordAgainTextBox = null;
}

SysMailConfiguration.prototype.init = function() {
    var that = this;
    $("tr", this.root).each(function() {
        $("td:not([colspan]):eq(0)", this).css("text-align", "right");
        var input = $("input[id], select[id]", this);
        $("label", this).attr("for", input.attr("id"));
    });
    this.portTextBox.kendoNumericTextBox();
    this.passwordCheckbox.change(function() {
        if ($(this).is(":checked")) {
            that.passwordTbody.show();
        } else {
            that.passwordTbody.hide();
        }
    });
    this.smtpTbody.kendoValidator();
    this.passwordTbody.kendoValidator({
        rules: {
            sameNewPassword: function(input) {
                if (input.is(that.passwordAgainTextBox)) {
                    return input.val() == that.passwordTextBox.val();
                }
                return true;
            }
        }
    });
    this.okButton.kendoButton();
    this.cancelButton.kendoButton();
    this
    .root
    .appendTo(document.body)
    .kendoWindow({
        title: "System email settings",
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
        url: "configuration/sys-mail.spring",
        success: function(map) {
            that.protocolTextBox.val(map["sys.email.protocol"]);
            that.enableSSLCheckbox.attr("checked", map["sys.email.ssl"] == "true");
            that.hostTextBox.val(map["sys.email.host"]);
            that.portTextBox.data("kendoNumericTextBox").value(map["sys.email.port"] || (map["sys.email.ssl"] == "true" ? 465 : 25));
            that.userTextBox.val(map["sys.email.user"]);
        },
        error: function(e) {
            createErrorDialog(e)
            .bind("close", function() {
                that.destroy();
            });
        }
    });
};

SysMailConfiguration.prototype.okButton_click = function() {
    if (!this.smtpTbody.data("kendoValidator").validate()) {
        return;
    }
    var changePassword = this.passwordTbody.is(":visible"); 
    if (changePassword && !this.passwordTbody.data("kendoValidator").validate()) {
        return;
    }
    var that = this;
    var ssl = this.enableSSLCheckbox.is(":checked");
    var data = {
        protocol: this.   protocolTextBox.val(),
        ssl:  ssl ? "true" : "false",
        host: this.hostTextBox.val(),
        port: this.portTextBox.data("kendoNumericTextBox").value() || (ssl ? 465 : 25),
        user: this.userTextBox.val()
    };
    if (changePassword) {
        data.password = this.passwordTextBox.val();
    }
    jsonp({
        url: "configuration/change-sys-mail.spring",
        data: data,
        success: function(map) {
            that.destroy();
        },
        error: function(e) {
            createErrorDialog(e)
            .bind("close", function() {
                that.destroy();
            });
        }
    });
};

SysMailConfiguration.prototype.cancelButton_click = function() {
    this.destroy();
};

SysMailConfiguration.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
};
