/**
 * @author Tao Chen
 */
function UserPassword() {
    
    //ioc: @UiField
    this.originalPasswordTextBox = null;
    
    //ioc: @UiField
    this.newPasswordTextBox = null;
    
    //ioc: @UiField
    this.newPasswordAgainTextBox = null;
    
    this._userVersion = null;
}

//ioc: @UiHandler
UserPassword.prototype.init = function() {
    var that = this;
    $("tr", this.root).each(function() {
        $("td:not([colspan]):eq(0)", this).css("text-align", "right");
        var input = $("input[id], select[id]", this);
        $("label", this).attr("for", input.attr("id"));
    });
    this.root.kendoValidator({
        rules: {
            sameNewPassword: function(input) {
                if (input.is(that.newPasswordAgainTextBox)) {
                    return input.val() == that.newPasswordTextBox.val();
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
        title: "Change password",
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
        },
        error: function(e) {
            createErrorDialog(e)
            .bind("close", function() {
                that.destroy();
            });
        }
    });
};

//ioc: @UiHandler
UserPassword.prototype.okButton_click = function(e) {
    if (!this.root.data("kendoValidator").validate()) {
        return;
    }
    var that = this;
    jsonp({
        url: "authorization/change-password.spring",
        data: {
            version: that._userVersion,
            originalPassword: that.originalPasswordTextBox.val(),
            newPassword: that.newPasswordTextBox.val(),
            newPasswordAgain: that.newPasswordAgainTextBox.val()
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
UserPassword.prototype.cancelButton_click = function(e) {
    this.destroy();
};

UserPassword.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
};
