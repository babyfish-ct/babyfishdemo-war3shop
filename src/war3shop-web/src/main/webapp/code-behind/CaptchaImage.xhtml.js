/**
 * @author Tao Chen
 */
function CaptchaImage() { 
    //ioc: @UiField
    this.image = null;
    
    //ioc: @UiField
    this.refereshButton = null;
}

CaptchaImage.prototype.init = function(param) {
    if (!param.url) {
        throw new Error("param.url is missing");
    }
    var that = this;
    var time = new Date().getTime();
    this.image.attr("src", param.url + "?" + time);
    this.refreshButton.kendoButton({
        spriteCssClass: "k-icon k-i-refresh",
        click: function() {
            that.refresh();
        }
    });
};

 CaptchaImage.prototype.refresh = function() {
     var src = this.image.attr("src");
     var qstIndex = src.indexOf("?");
     if (qstIndex != -1) {
         src = src.substring(0, qstIndex);
     }
     var time = new Date().getTime();
     src += "?" + time;
     this.image.attr("src", src);
 };
