/**
 * @author Tao Chen
 */
function Page() {
    
    //ioc: @UiField
    this.menu = null;
    
    //ioc: @UiField
    this.contentPanel = null;
    
    this._currentContent = null;
    
    if (Page._instance != null) {
        throw new Error("Page is singleton class");
    }
    Page._instance = this;
};

//ioc: @UiHandler
Page.prototype.init = function() {
    kendo.ui.DropDownPanel.globalToppest(this.contentPanel);
    this.refreshSiteMap(true);
    var theme = $.cookie(Page.THEME_COOKIE_NAME);
    if (theme) {
        var arr = Page.THEMES;
        for (var i = arr.length - 1; i >= 0; i--) {
            if (arr[i] == theme) {
                $("#kendoStyleLink").attr("href", "kendo-ui/styles/kendo." + theme + ".min.css");
                break;
            }
        }
    }
};

//ioc: @UiHandler
Page.prototype.fullscreenButton_click = function(e) {
    var doc = document;
    if (doc.fullscreenElement || doc.webkitFullscreenElement || doc.mozFullScreenElement || doc.msFullscreenElement) {
        (doc.exitFullscreen || doc.webkitCancelFullScreen || doc.mozCancelFullScreen || doc.msExitFullscreen || function(){}).call(doc);
    } else {
        var de = doc.documentElement;
        (de.requestFullscreen || de.webkitRequestFullscreen || de.mozRequestFullScreen || de.msRequestFullscreen || function(){}).call(de);
    }
};

//ioc: special @UiHandler of site-map.xml
Page.prototype.menu_authorization = function(e) {
    createTemplate("UserAuthorization");
};

//ioc: special @UiHandler of site-map.xml
Page.prototype.menu_logout = function(e) {
    var that = this;
    jsonp({
        url: "authorization/logout.spring",
        success: function(e) {
            that.refreshSiteMap();
        }
    });
};

//ioc: special @UiHandler of site-map.xml
Page.prototype.menu_configuration = function(e) {
    createTemplate("UserConfiguration");
};

//ioc: special @UiHandler of site-map.xml
Page.prototype.menu_password = function(e) {
    createTemplate("UserPassword");
};

//ioc: special @UiHandler of site-map.xml
Page.prototype.menu_sysMailConfiguration = function(e) {
    createTemplate("SysMailConfiguration");
};

//ioc: special @UiHandler of site-map.xml
Page.prototype.menu_theme = function(e) {
    var theme = $(e.item).attr("node-id");
    $("#kendoStyleLink").attr("href", "kendo-ui/styles/kendo." + theme + ".min.css");
    $.cookie(Page.THEME_COOKIE_NAME, theme, { expires: 7, path: '/' });
};

Page.instance = function() {
    return Page._instance;
}

Page._instance = null;

Page.THEMES = [
    "highcontrast", 
    "black", 
    "moonlight", 
    "metroblack", 
    "blueopal", 
    "default", 
    "bootstrap", 
    "silver", 
    "uniform",
    "metro",
    "flat"
];

Page.THEME_COOKIE_NAME = "org_babyfishdemo_war3shop_theme";

Page.prototype.refreshSiteMap = function(initializing) {
    var that = this;
    $.ajax({
        url: "authorization/sitemapnodes.spring",
        contentType: "application/x-www-form-urlencoded",
        dataType: "jsonp",
        success: function(data) {
            if (data.exceptionClass) {
                createErrorDialog(data);
            } else {
                if (initializing) {
                    that.set("SaleView");
                } else {
                    that.clear();
                }
                var kendoMenu = that.menu.data("kendoMenu");
                if (kendoMenu) {
                    kendoMenu.destroy();
                }
                that.menu.empty();
                if (data.length > 5) {
                    var management = { text: "Management", childNodes: [] };
                    var myData = { text: "MyData", childNodes: [] };
                    var newArr = [];
                    for (var i = 0; i < data.length; i++) {
                        if (data[i].text.indexOf("Manage ") == 0) {
                            management.childNodes.push(data[i]);
                        } else if (data[i].text.indexOf("My ") == 0) {
                            myData.childNodes.push(data[i]);
                        }
                    }
                    if (management.childNodes.length > 1 || myData.childNodes.length > 1) {
                        var newData = [];
                        if (management.childNodes.length > 1) {
                            newArr.push(management);
                            for (var i = management.childNodes.length - 1; i >= 0; i--) {
                                management.childNodes[i].merged = true;
                            }
                        }
                        if (myData.childNodes.length > 1) {
                            newArr.push(myData);
                            for (var i = myData.childNodes.length - 1; i >= 0; i--) {
                                myData.childNodes[i].merged = true;
                            }
                        }
                        for (var i = 0; i < data.length; i++) {
                            if (!data[i].merged) {
                                newArr.push(data[i]);
                            }
                        }
                        data = newArr;
                    }
                }
                for (var i = 0; i < data.length; i++) {
                    that.menu.append(Page._createLiForSiteMapNode(data[i]));
                }
                that.menu.kendoMenu({
                    select: function(e) {
                        var template = $(e.item).attr("template");
                        var click = $(e.item).attr("click");
                        if (template) {
                            that.set(template);
                        } else if (click) {
                            Page.instance()[click](e);
                        }
                    }
                });
            }
        },
        error: function() {
            alert("Invalid");
        }
    });
};

Page.prototype.clear = function() {
    var currentContent = this._currentContent;
    if (currentContent) {
        try {
            var controller = currentContent.data("controller");
            if (controller && controller.destroy) {
                controller.destroy();
            }
        } finally {
            this._currentContent = null;
            this.contentPanel.empty();
            
            /* -----------------------------------
             * KendoUIGarbageCollection
             * -----------------------------------
             * KendoUI widget often create some <div/> elements with some special css class(Eg ".k-popup") and style 
             * "position:absolute;display:none;" and attach them to the document.body(not under the current kendo UI widget) 
             * and try to show them if some events happen, it will not remove them except you call the destroy method of 
             * every Kendo UI widget. 
             * If this application is MPA created by some classic Server Page technogies such as PHP, JSP or ASP.NET MVC, 
             * when the page changed, all the garbage elements will be removed automatically, 
             * but this demo is a pure-javascript SPA UI,  the page never changes except the user click the refresh of browser. 
             */
            $("body>:not(.root-of-kendo-ui-garbage-collection)").remove();
        }
    }
};

Page.prototype.set = function(templateName) {
    this.clear();
    var content = createTemplate(templateName);
    var isFullLayout = content.data("controller").isFullLayout;
    if (isFullLayout && isFullLayout.call(content.data("controller"))) {
        this.contentPanel.css("overflow", "hidden");
    } else {
        this.contentPanel.css("overflow", "auto");
    }
    this.contentPanel.append(this._currentContent = content);
}

Page._createLiForSiteMapNode = function(siteMapNode) {
    var li = 
        $("<li></li>")
        .addClass("navigation-menu-item")
        .attr("node-id", siteMapNode.nodeId)
        .attr("template", siteMapNode.template)
        .attr("click", siteMapNode.click)
        .text(siteMapNode.text);
    var children = siteMapNode.childNodes;
    var childrenCount = children.length;
    if (childrenCount != 0) {
        var ul = $("<ul></ul>");
        for (var i = 0; i < childrenCount; i++) {
            Page._createLiForSiteMapNode(children[i]).appendTo(ul);
        }
        li.append(ul);
    }
    return li;
}
