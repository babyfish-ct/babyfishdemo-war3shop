/**
 * @author Tao Chen
 */
(function($) {
    /*
     * import kendo.ui.Widget;
     * import kendo.ui.Grid;
     * import kendo.ui.NumericTextBox;
     * import kendo.ui.Tooltip;
     */
    var 
        kendo = window.kendo,
        ui = kendo.ui,
        Widget = ui.Widget,
        Grid = ui.Grid,
        NumericTextBox = ui.NumericTextBox,
        Tooltip = ui.Tooltip;
    
    /*
     * Private members for all
     */
    var emptyFunction = function() {};
    
    /*
     * Global Event Handler
     */
    $(document).bind("mousedown focus", function(e) {
        if (dropDownPanel_openning != null) {
            var expectedEle1 = dropDownPanel_openning._root[0];
            var expectedEle2 = dropDownPanel_popupParent[0];
            for (var target = e.target; target != null; target = target.parentNode) {
                if (target == expectedEle1 || target == expectedEle2) {
                    return;
                }
            }
            for (var target = e.target; target != null; target = target.parentNode) {
                if ($(target).is(".k-popup")) {
                    return;
                }
            }
            dropDownPanel_openning.close();
            dropDownPanel_openning = null;
        }
    });
    $(document).bind("mouseup", function() {
        if (gridEx_startDargTrEle) {
            var gridEx = $(gridEx_startDargTrEle).closest("div[data-role='gridex']").data("kendoGridEx");
            gridEx_startDargTrEle = null;
            gridEx_clearRangeCss(gridEx);
        }
    });

    /*
     * Private members for DropDownPanel
     */
    var dropDownPanel_openning = null;
    
    var dropDownPanel_popupParent = 
        $("<div></div>")
        .attr("role", "DropDownPanelPopupParent")
        .css("left", "0px")
        .css("top", "0px")
        .css("width", "0px")
        .css("width", "0px")
        .css("overflow", "visible")
        .css("position", "absolute");
    
    var dropDownPanel_globalToppest = $(document.body);
    
    var dropDownPanel_getDataId = function(ele) {
        var type = ele.attr("data-id-type");
        if (!type) {
            return null;
        }
        var id = parseInt(ele.attr("data-id"));
        if (type == "number") {
            id = parseInt(id);
        }
        return id;
    };
    
    var dropDownPanel_setDataId = function(ele, id) {
        if (typeof(id) == "undefined" || id == null) {
            ele
            .removeAttr("data-id")
            .removeAttr("data-id-type");
        }
        ele
        .attr("data-id", id)
        .attr("data-id-type", typeof(id));
    };
    
    var dropDownPanel_createText = function(that, dataItem) {
        var span = $("<span></span>").attr("unselectable", "on");
        var tagTemplate = that.tagTemplate;
        if (tagTemplate) {
            if (typeof(tagTemplate) == "function") {
                tagTemplate = tagTemplate.call(this, dataItem);
            }
        } else {
            tagTemplate = dataItem[that.dataNameField];
        }
        if (typeof(tagTemplate) == "string") {
            span.html(tagTemplate);
        } else {
            span.append(tagTemplate);
        };
        if (that._singleTextParent) {
            span.css("display", "block");
            span.children().css("display", "block");
        }
        return span;
    };
    
    var dropDownPanel_createLi = function(that, dataItem) {
        var id = dataItem[that.dataIdField];
        var li = 
            $("<li></li>")
            .data("dataItem", dataItem)
            .attr("unselectable", "on")
            .addClass("k-button")
            .addClass("k-reset");
        dropDownPanel_setDataId(li, id);
        dropDownPanel_createText(that, dataItem).appendTo(li);
        $("<span></span>")
        .attr("class", "k-icon k-delete")
        .attr("unselectable", "on")
        .text("delete")
        .appendTo(li)
        .click(function() {
            that.unselectId(dataItem.id, true);
            that.close();
        });
        return li;
    };
    
    var dropDownPanel_tryShowPlaceHolder = function(that) {
        if (that._multipleList && that._multipleList.children("li[data-id]").length == 0 && !that._focused) {
            that._placeholderItem.show();
        }
    };
    
    var dropDownPanel_adjustPopup = function(that) {
        if (dropDownPanel_openning == that) {
            var offset = that._root.offset();
            var toppestOffset = dropDownPanel_globalToppest.offset();
            dropDownPanel_openning
            ._popup
            .css("left", offset.left - toppestOffset.left + dropDownPanel_globalToppest.scrollLeft())
            .css("top", offset.top - toppestOffset.top + dropDownPanel_globalToppest.scrollTop() + dropDownPanel_openning._root.outerHeight());
        }
    };
    
    var dropDownPanel_clearFocusState = function(that) {
        if (that._multipleList) {
            that._multipleList.children("li.k-state-focused[data-id]").removeClass("k-state-focused");
        }
    };
    
    /*
     * Public non-static members of DropDownPanel
     */
    var DropDownPanel = Widget.extend({
        options: {
            name: "DropDownPanel",
            placeholder: null,
            template: null,
            templateInit: null,
            tagTemplate: null,
            tagTemplateInit: null,
            dataIdField: null,
            dataNameField: null
        },
        init: function(element, options) {
            //super.init();
            Widget.fn.init.call(this, element, options);
            
            var that = this;
            var ele = $(element).addClass("k-dropdownpanel-element").hide();
            this._root = 
                $("<div></div>")
                .attr("class", "k-dropdownpanel k-widget k-header")
                .attr("unselectable", "on")
                .css("min-width", "50px")
                .bind("selectstart", function() {
                    return false;
                });
            var wrapper = $("<div></div>").attr("unselectable", "on");
            if (options.mode == "single") {
                this._root.addClass("k-dropdown");
                wrapper.attr("class", "k-dropdown-wrap k-state-default");
                this._singleTextParent = 
                    $("<span></span>")
                    .attr("unselectable", "on")
                    .addClass("k-input")
                    .css("text-align", "left")
                    .appendTo(wrapper);
                var button =
                    $("<span></span>")
                    .attr("unselectable", "on")
                    .addClass("k-select")
                    .appendTo(wrapper)
                    .append(
                            $("<span></span>")
                            .attr("unselectable", "on")
                            .attr("class", "k-icon k-i-arrow-s")
                            .text("select")
                    );
                wrapper.click(function() {
                    that.open();
                })
                .mouseover(function() {
                    wrapper.addClass("k-state-hover");
                })
                .mouseout(function(e) {
                    wrapper.removeClass("k-state-hover");
                });
                this._singleDeferredBehavior = function() {
                    wrapper[that._singleHover ? "addClass" : "removeClass"]("k-state-hover");
                }
            } else {
                this._root.addClass("k-multiselect");
                wrapper
                .attr("class", "k-multiselect-wrap k-floatwrap")
                .mousedown(function(e) {
                    if ($(e.target).is(".k-delete")) {
                        that.focus();
                    } else {
                        that.open();
                    }
                });
                this._multipleList = 
                    $("<ul></ul>")
                    .attr("unselectable", "on")
                    .addClass("k-reset")
                    .appendTo(wrapper);
                var placeholder = this.options.placeholder || ele.data("placeholder");
                this._placeholderItem = 
                    placeholder ? 
                    $("<li></li>").text(placeholder).appendTo(this._multipleList) : 
                    $();
                var input = 
                    $("<input/>")
                    .attr("type", "text")
                    .attr("maxlength", "0")
                    .addClass("k-input")
                    .css("width", "25px")
                    .keydown(function(e) {
                        var c = e.keyCode;
                        if (c == 8) {
                            var focusedLi = that._multipleList.children("li.k-state-focused[data-id]");
                            if (focusedLi.length != 0) {
                                that.unselectId(dropDownPanel_getDataId(focusedLi), true);
                            } else {
                                that.unselectId(dropDownPanel_getDataId(that._multipleList.children("li[data-id]:last")), true);
                            }
                            that.close();
                        } else if (c == 38) {
                            that.close();
                        } else if (c == 40) {
                            that.open();
                        } else if (c == 37 || c == 39) {
                            var focusedLi = that._multipleList.children("li.k-state-focused[data-id]");
                            var nextLi;
                            if (focusedLi.length == 0) {
                                nextLi = c == 37 ? that._multipleList.children("li[data-id]:last") : $();
                            } else {
                                if (c == 37) {
                                    nextLi = focusedLi.prev("li[data-id]");
                                    if (nextLi.length == 0) {
                                        nextLi = focusedLi;
                                    }
                                } else {
                                    nextLi = focusedLi.next("li[data-id]");
                                }
                            }
                            focusedLi.removeClass("k-state-focused");
                            nextLi.addClass("k-state-focused");
                        }
                    })
                    .focus(function() {
                        that._focused = true;
                    })
                    .blur(function() {
                        that._focused = false;
                        dropDownPanel_clearFocusState(that);
                        dropDownPanel_tryShowPlaceHolder(that);
                    });
                this._inputLi = $("<li></li>").append(input).appendTo(this._multipleList);
            }
            this._popup = 
                $("<div></div>")
                .attr("class", "k-dropdownpanel-popup k-block k-popup")
                .css("position", "absolute")
                .css("margin-bottom", "20px")
                .hide();
            this.
            _root
            .appendTo(ele.parent())
            .append(wrapper.append(ele));
            var template = this.options.template || "DropDownPanel Template";
            if (typeof(template) == "function") {
                template = template.call(this);
            }
            if (typeof(template) == "string") {
                this._popup.html(kendo.template(template));
            } else {
                this._popup.append(template);
            }
            if (typeof(this.options.templateInit) == "function") {
                this.options.templateInit.call(this);
            }
            this._openFx = kendo.fx(this._popup).expand("vertical").duration(200);
            this._closeFx = kendo.fx(this._popup).expand("vertical").duration(100);
            this.tagTemplate = this.options.tagTemplate;
            this.tagTemplateInit = this.options.tagTemplateInit;
            this.dataIdField = this.options.dataIdField || "id";
            this.dataNameField = this.options.dataNameField || "name";
            
            if (typeof(this.options.open) == "function") {
                this.element.bind("open", this.options.open);
            }
            if (typeof(this.options.close) == "function") {
                this.element.bind("open", this.options.close);
            }
        },
        open: function() {
            if (dropDownPanel_openning != this) {
                if (dropDownPanel_openning != null) {
                    dropDownPanel_openning.close();
                }
                dropDownPanel_openning = this;
                if (this._placeholderItem) {
                    this._placeholderItem.hide();
                }
                var zIndex = 0;
                $("div.k-window, div.k-overlay").each(function() {
                    var z = $(this).css("z-index");
                    z = parseInt(z);
                    if (!isNaN(z)) {
                        zIndex = zIndex > z ? zIndex : z;
                    }
                });
                this._popup.css("position", "absolute");
                for (var p = this.element.parentNode; p; p = p.parentNode) {
                    if ($(p).computedStyle()["position"] == "fixed") {
                        this._popup.css("position", "fixed");
                        break;
                    }
                }
                this._popup.css("z-index", zIndex + 1);
                dropDownPanel_adjustPopup(this);
                dropDownPanel_popupParent.children().detach();
                dropDownPanel_popupParent.append(this._popup).appendTo(dropDownPanel_globalToppest);
                this._closeFx.stop();
                this._openFx.play();
                dropDownPanel_clearFocusState(this);
                this.element.trigger("open");
            }
            this.focus();
        },
        close: function() {
            if (dropDownPanel_openning == this) {
                dropDownPanel_openning = null;
                dropDownPanel_tryShowPlaceHolder(this);
                this._openFx.stop();
                this._closeFx.reverse();
                dropDownPanel_clearFocusState(this);
                this.element.trigger("close");
            }
        },
        focus: function() {
            var that = this;
            if (that._inputLi) {
                deferred(function() {
                    that
                    ._inputLi
                    .children("input")[0]
                    //[0] get the DOM, it's the focus behavior of DOM, not focus event of jQuery
                    .focus();
                });
            }
        },
        value: function() {
            if (this._singleTextParent) {
                return dropDownPanel_getDataId(this._singleTextParent);
            }
            var value = [];
            this._multipleList.children("li[data-id]").each(function() {
                value[value.length] = dropDownPanel_getDataId($(this));
            });
            return value;
        },
        dataItemValue: function() {
            if (this._singleTextParent) {
                return this._singleTextParent.data("dataItem");
            }
            var value = [];
            this._multipleList.children("li[data-id]").each(function() {
                value[value.length] = $(this).data("dataItem");
            });
            return value;
        },
        selectDataItem: function(dataItem, triggerEvent) {
            var that = this;
            if (typeof(dataItem) == "undefined") {
                return;
            } 
            var id = dataItem[this.dataIdField];
            if (typeof(id) == "undfined") {
                return;
            }
            if (this._singleTextParent) {
                var existingId = dropDownPanel_getDataId(this._singleTextParent);
                if (existingId == id) {
                    return;
                }
                dropDownPanel_setDataId(this._singleTextParent, id);
                this._singleTextParent.data("dataItem", dataItem);
                var text = dropDownPanel_createText(this, dataItem);
                this._singleTextParent.empty().append(text);
            } else {
                this._placeholderItem.hide();
                var conflict = false;
                this._multipleList.children("li[data-id]").each(function() {
                    var existingId = dropDownPanel_getDataId($(this));
                    if (existingId == id) {
                        conflict = true;
                        return false; //break each
                    }
                });
                if (conflict) {
                    return;
                }
                dropDownPanel_createLi(this, dataItem).insertBefore(this._inputLi);
                dropDownPanel_adjustPopup(this);
            }
            if (triggerEvent) {
                $(this.element).trigger("change");
            }
        },
        unselectId: function(id, triggerEvent) {
            var that = this;
            if (this._singleTextParent) {
                var existingId = dropDownPanel_getDataId(this._singleTextParent);
                if (existingId == id) {
                    dropDownPanel_setDataId(this._singleTextParent, null);
                    this._singleTextParent.empty();
                }
            } else {
                this._multipleList.children("li[data-id]").each(function() {
                    var existingId = dropDownPanel_getDataId($(this));
                    if (existingId == id) {
                        $(this).remove();
                        dropDownPanel_tryShowPlaceHolder(that);
                        dropDownPanel_adjustPopup(that);
                        if (triggerEvent) {
                            $(that.element).trigger("change");
                        }
                        return false; //break each
                    }
                });
            }
        }
    });
    
    /*
     * Public non-static members of CheckBox
     */
    var CheckBox = Widget.extend({
        options: {
            name: "CheckBox"
        },
        init: function(element, options) {
            //super.init();
            Widget.fn.init.call(this, element, options);
        }
    });
    
    /*
     * Private members for GirdEx
     */
    
    var gridEx_startDargTrEle = null;
    
    var gridEx_clearRangeCss = function(that) {
        that
        .element
        .find("tr[data-uid].will-be-selected-tr-to-fix-chrome-bug")
        .removeClass("will-be-selected-tr-to-fix-chrome-bug");
        that.element.find(".k-grid--will-select").hide();
        that.element.find(".k-grid--will-unselect").hide();
    }
    var gridEx_setRangeCss = function(that, toEle, handler) {
        var begin = gridEx_startDargTrEle;
        if (!begin) {
            return;
        }
        gridEx_clearRangeCss(that);
        var end = toEle;
        if (begin != end) {
            var validated = false;
            for (var next = end.nextSibling; next != null; next = next.nextSibling) {
                if (next == begin) {
                    var tmp = begin;
                    begin = end;
                    end = tmp;
                    validated = true;
                    break;
                }
            }
            for (var prev = end.previousSibling; prev != null; prev = prev.previousSibling) {
                if (prev == begin) {
                    validated = true;
                }
            }
            if (!validated) {
                return;
            }
        }
        while (begin != null) {
            if ($(begin).is("[data-uid]")) {
                handler($(begin));
            }
            if (begin == end) {
                break;
            }
            begin = begin.nextSibling;
        }
    };
    
    var gridEx_filterColumns = function(options) {
        if (!options.disabledColumns) {
            return;
        }
        var disabledColumns = options.disabledColumns || [];
        var enabledColumns = options.enabledColumns instanceof Array ? options.enabledColumns : [];
        var columns = options.columns;
        var newColumns = [];
        for (var i = 0; i < columns.length; i++) {
            var name = columns[i].name;
            var enabled = true;
            if (name) {
                if (disabledColumns == "all") {
                    enabled = false;
                } else if (disabledColumns instanceof Array) {
                    for (var i = disabledColumns.length - 1; i >= 0; i--) {
                        if (disabledColumns[i] == name) {
                            enabled = false;
                            break;
                        }
                    }
                }
                for (var i = enabledColumns.length - 1; i >= 0; i--) {
                    if (enabledColumns[i] == name) {
                        enabled = true;
                        break;
                    }
                }
            }
            if (enabled) {
                newColumns[newColumns.length] = columns[i];
            }
        }
        options.columns = newColumns;
    };
    
    /*
     * Public non-static members of GridEx
     */
    var GridEx = Grid.extend({
        options: {
            name: "GridEx"
        },
        init: function(element, options) {
            var that = this;
            var mode = 0;
            if (options.mode == "readonly") {
                mode = 1;
            } else if (options.mode == "select") {
                mode = 2;
            } else if (options.mode == "multiselect") {
                mode = 3;
            }
            gridEx_filterColumns(options);
            this.mode = mode;
            if (!options.toolbar || mode != 0) {
                options.toolbar = [];
            }
            options.toolbar[options.toolbar.length] = { name: "-refresh", text: "Refresh" };
            if (mode == 3) {
                options.toolbar[options.toolbar.length] = { name: "-select-whole-page", text: "Select whole page" };
                options.toolbar[options.toolbar.length] = { name: "-unselect-whole-page", text: "Unselect whole page" };
            }
            if (options.detailTemplate) {
                options.toolbar[options.toolbar.length] = { name: "-expand-all", text: "Expand all" };
                options.toolbar[options.toolbar.length] = { name: "-collapse-all", text: "Collapse all" };
            }
            if (mode != 0) {
                // TODO: not template(My network is broken temporarily so that I can view the document of kendoUI).

                //Forbide reorder columns
                options.reorderable = false;
                
                // Add selected state column and remove command column
                var columns = [];
                if (mode == 3) {
                    columns[columns.length] = {
                        title: "",
                        width: "30px",
                        resizable: false
                    };
                }
                for (var i = 0; i < options.columns.length; i++) {
                    var column = options.columns[i];
                    if (typeof(column.command) == "undefined") {
                        columns[columns.length] = column;
                    }
                }
                options.columns = columns;
                
                // Replace the dataBound
                var originalDataBound = options.dataBound;
                options.dataBound = function() {
                    if (typeof(originalDataBound) == "function") {
                        originalDataBound.call();//TODO: parameter
                    }
                    that.element.find("tbody").bind("selectstart", function() {
                        return false;
                    });
                    if (mode == 2) { //single selection
                        $("td.k-hierarchy-cell", that.element).bind("mousedown mouseup", function(e) {
                            e.stopPropagation();
                        });
                        $("tr[data-uid]", that.element)
                        .mouseup(function() {
                            that.element.trigger("dataitemchanged", that.dataItem(this));
                        })
                        .mouseover(function(e) {
                            $(this).addClass("k-state-hover").addClass("will-be-selected-tr-to-fix-chrome-bug");
                        })
                        .mouseout(function(e) {
                            $(this).removeClass("k-state-hover").removeClass("will-be-selected-tr-to-fix-chrome-bug");
                        });
                    } else if (mode == 3) { //mutliple selection
                        var selectedIds = typeof(that._selectedIds) == "function" ? that._selectedIds() : [];
                        $("td.k-hierarchy-cell")
                        .mousedown(function(e) { e.stopPropagation(); })
                        .mouseover(function(e) { 
                            gridEx_clearRangeCss(that);
                            gridEx_startDargTrEle = null;
                            e.stopPropagation();  
                        })
                        .mouseup(function(e) {
                            gridEx_clearRangeCss(that);
                            gridEx_startDargTrEle = null;
                            e.stopPropagation(); 
                        });
                        $("tr[data-uid]", that.element).each(function() {
                            var tr = $(this);
                            tr
                            .children(typeof(options.detailTemplate) == "undefined" ? "td:first" : "td:eq(1)")
                            .append(
                                    $("<em></em>")
                                    .hide()
                                    .attr("disabled", "disabled")
                                    .attr("class", "k-grid--will-select")
                                    .css("width", "28px")
                                    .append(
                                            $("<span></span>")
                                            .attr("class", "k-icon k-si-plus")
                                    )
                            )
                            .append(
                                    $("<em></em>")
                                    .hide()
                                    .attr("disabled", "disabled")
                                    .attr("class", "k-grid--will-unselect")
                                    .css("width", "28px")
                                    .append(
                                            $("<span></span>")
                                            .attr("class", "k-icon k-si-minus")
                                    )
                            );
                            if (selectedIds.contains(that.dataItem(this).id)) {
                                tr.addClass("k-state-selected");
                            }
                            tr
                            .mousedown(function() {
                                gridEx_startDargTrEle = this;
                                gridEx_setRangeCss(that, this, function(row) {
                                    row.addClass("will-be-selected-tr-to-fix-chrome-bug");
                                    if (row.is(".k-state-selected")) {
                                        row.find(".k-grid--will-unselect").show();
                                    } else {
                                        row.find(".k-grid--will-select").show();
                                    }
                                });
                            })
                            .mouseover(function(e) {
                                if (e.which == 1) {
                                    gridEx_setRangeCss(that, this, function(row) {
                                        row.addClass("will-be-selected-tr-to-fix-chrome-bug");
                                        if (row.is(".k-state-selected")) {
                                            row.find(".k-grid--will-unselect").show();
                                        } else {
                                            row.find(".k-grid--will-select").show();
                                        }
                                    });
                                }
                            })
                            .mouseup(function(e) {
                                gridEx_setRangeCss(that, this, function(row) {
                                    if (row.is(".k-state-selected")) {
                                        row.removeClass("k-state-selected");
                                        that.element.trigger("dataitemunselected", that.dataItem(row));
                                    } else {
                                        row.addClass("k-state-selected");
                                        that.element.trigger("dataitemselected", that.dataItem(row));
                                    }
                                });
                                gridEx_startDargTrEle = null;
                            });
                        });
                    }
                };
            }
            //super.init();
            Grid.fn.init.call(this, element, options);
            $(".k-pager-input", element).find("input.k-textbox").keyup(function() {
                //TODO: mock keypress -\n
            });
            $(".k-grid--refresh", element).click(function() {
                that.dataSource.read();
            });
            if (mode != 0) {
                this.selectedIds(options.selectedIds);
                $(".k-grid--select-whole-page", element).click(function() {
                    var trs = that.element.find("tr[data-uid]:not(.k-state-selected)");
                    if (trs.length == 0) {
                        return;
                    }
                    that.element.hide(); //Fix the bug of chrom
                    try {
                        trs.each(function() {
                            $(this).addClass("k-state-selected");
                            that.element.trigger("dataitemselected", that.dataItem(this));
                        });
                    } finally {
                        that.element.show(); //Fix the bug of chrome
                    }
                });
                $(".k-grid--unselect-whole-page", element).click(function() {
                    var trs = that.element.find("tr[data-uid].k-state-selected");
                    if (trs.length == 0) {
                        return;
                    }
                    that.element.hide(); //Fix the bug of chrom
                    try {
                        trs.each(function() {
                            $(this).removeClass("k-state-selected");
                            that.element.trigger("dataitemunselected", that.dataItem(this));
                        });
                    } finally {
                        that.element.show(); //Fix the bug of chrome
                    }
                });
            }
            if (options.detailTemplate) {
                $(".k-grid--expand-all", element).click(function() {
                    that.expandRow(that.element.find(".k-master-row"));
                });
                $(".k-grid--collapse-all", element).click(function() {
                    that.collapseRow(that.element.find(".k-master-row"));
                });
            }
        },
        selectedIds: function(selectedIds) {
            if (selectedIds && typeof(selectedIds) != "function") {
                throw new Error("The argument must be function when it is not null");
            }
            this._selectedIds = selectedIds;
        }
    });
    
    var NumericTextBoxEx = NumericTextBox.extend({
        options: {
            name: "NumericTextBoxEx"
        },
        init: function(element, options) {
            var that = this;
            NumericTextBox.fn.init.call(this, element, options);
            $(element).closest(".k-numerictextbox").click(function(e) {
                var target = $(e.target);
                if (target.is(".k-i-arrow-n") || target.is(".k-i-arrow-s")) {
                    $(element).trigger("valuechange");
                }
            });
            $(element).bind("change keyup", function() {
                if (this.value == that.value()) {
                    if (!isNaN(parseInt(this.value))) {
                        $(this).trigger("valuechange");
                    }
                }
            });
        }
    });
    
    var TooltipEx = Tooltip.extend({
        options: {
            name: "TooltipEx"
        },
        init: function(element, options) {
            Tooltip.fn.init.call(this, element, options);
            this._recursive = options.recursive;
        },
        _show: function(target) {
            if (!this._recursive) {
                var owner = (target || this.element).closest("*[data-role='tooltipex']");
                if (!this.element.is(owner)) {
                    return;
                }
            }
            Tooltip.fn._show.call(this, target);
        }
    });
    
    /*
     * Add them into kendo UI
     */
    ui.plugin(DropDownPanel);
    ui.plugin(CheckBox);
    ui.plugin(GridEx);
    ui.plugin(NumericTextBoxEx);
    ui.plugin(TooltipEx);
    
    /*
     * Static memebers
     */
    ui.DropDownPanel.globalToppest = function(ele) {
        if (!ele) {
            dropDownPanel_globalToppest = $(document.body);
        } else if (ele instanceof HTMLElement) {
            dropDownPanel_globalToppest = $(ele);
        } else {
            dropDownPanel_globalToppest = ele;
        }
    }
    
})(jQuery);
