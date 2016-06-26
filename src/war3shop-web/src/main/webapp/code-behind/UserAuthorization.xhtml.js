/**
 * @author Tao Chen
 */
function UserAuthorization() {
    //ioc: @UiField
    this.tabstrip = null;
}

//ioc: @UiHandler
UserAuthorization.prototype.init = function() {
    this.tabstrip.kendoTabStrip({
        animation:  {
            open: {
                effects: "fadeIn"
            }
        }
    });
    var that = this;
    this
    .root
    .appendTo(document.body) //Self manager, should return false
    .kendoWindow({
        title: "Authorization",
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
UserAuthorization.prototype.userLogin_loginsuccessed = function(e) {
    this.destroy();
    Page.instance().refreshSiteMap();
};

//ioc: @UiHandler
UserAuthorization.prototype.userRegister_registiersuccessed = function(e) {
    this.destroy();
    Page.instance().refreshSiteMap();
};

UserAuthorization.prototype.destroy = function() {
    this.root.data("kendoWindow").destroy();
}
