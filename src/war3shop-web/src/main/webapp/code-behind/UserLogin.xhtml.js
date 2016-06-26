/**
 * @author Tao Chen
 */
function UserLogin() {
    
    //ioc: @UiField
    this.nameTextBox = null;
    
    //ioc: @UiField
    this.passwordTextBox = null;
    
    //ioc: @UiField
    this.captchaTextBox = null;
    
    //ioc: @UiField
    this.captchaImage = null;
    
    //ioc: @UiField
    this.loginButton = null;
}

//ioc: @UiHandler
UserLogin.prototype.init = function() {
    $("tr", this.root).each(function() {
        var input = $("input[name]", this);
        $("label", this).attr("for", input.attr("id"));
        $("span.k-invalid-msg", this).attr("data-for", input.attr("name"));
    });
    this.root.kendoValidator();
    this.loginButton.kendoButton();
};

//ioc: @UiHandler
UserLogin.prototype.loginButton_click = function(e) {
    if (this.root.data("kendoValidator").validate()) {
        var that = this;
        jsonp({
            url: "authorization/login.spring",
            data: {
                name: this.nameTextBox.val(),
                password: this.passwordTextBox.val(),
                captcha: this.captchaTextBox.val()
            },
            success: function(e) {
                that.root.trigger("loginsuccessed");
            },
            error: function(e) {
                createErrorDialog(e);
                that.captchaImage.data("controller").refresh();
            }
        });
    }
};
