/**
 * @author Tao Chen
 */
function LazyContainer() {
};

LazyContainer.prototype.init = function(param) {
    if (param.create) {
        if (typeof(param.create) != "function") {
            throw new Error("The field \"create\" of options must be an function");
        }
    }
    if (!param.create && !param.templateName) {
        throw new Error("Neither \"create\" nor \"templateName\" is specified");
    }
    this._created = false;
    this._create = param.create;
    this._templateName = param.templateName;
    this._templateParam = param.templateParam;
};

LazyContainer.prototype.create = function() {
    if (!this._created) {
        this.root.append(
                this._create ? 
                this._create() :
                createTemplate(this._templateName, this._templateParam)
        );
        this._created = true;
    }
};
