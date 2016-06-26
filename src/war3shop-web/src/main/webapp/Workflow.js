/**
 * @author Tao Chen
 */
function Workflow(flowDeclaration) {
    var data = {};
    for (var name in flowDeclaration) {
        if (name == "init") {
            var init = flowDeclaration[name];
            if (typeof(init) != "function") {
                throw new Error("The init must be a function");
            }
            init.call(this);
        } else {
            data[name] = { 
                    started: false, 
                    ended: false, 
                    action: flowDeclaration[name].action
            };
        }
    }
    for (var name in flowDeclaration) {
        if (name == "init") {
            continue;
        }
        var dependencies = flowDeclaration[name].dependencies;
        if (typeof(dependencies) == "string") {
            var dependencyName = data[dependencies];
            if (!dependencyName) {
                throw new Error(
                        "Can not resolve the dependencyName \"" +
                        dependencyName +
                        "\" of \"" +
                        name +
                        "\"");
            }
            data[name].dependencies = [ dependencyName ];
        } else if (dependencies instanceof Array) {
            var arr = [];
            for (var i = dependencies.length - 1; i >= 0; i--) {
                var dependencyName = dependencies[i];
                if (!dependencyName) {
                    throw new Error(
                            "Can not resolve the dependencyName \"" +
                            dependencyName +
                            "\" of \"" +
                            name +
                            "\"");
                }
                arr[i] = data[dependencies[i]];
            }
            data[name].dependencies = arr;
        } else {
            data[name].dependencies = [];
        }
    }
    this.data = data;
}

Workflow.prototype.startAllRoots = function() {
    for (var nodeName in this.data) {
        var node = this.data[nodeName];
        if (node.dependencies.length == 0) {
            this._startNode(node);
        }
    }
};

Workflow.prototype.start = function(name) {
    var node = this.data[name];
    if (!node) {
        throw new Error(
                "The workflow does not contains a node whose name is \"" +
                name +
                "\"");
    }
    this._startNode(node);
};

Workflow.prototype.finish = function(name) {
    var node = this.data[name];
    if (!node) {
        throw new Error(
                "The workflow does not contains a node whose name is \"" +
                name +
                "\"");
    }
    if (node.ended) {
        return;
    }
    node.started = true;
    node.ended = true;
    for (var otherName in this.data) {
        if (name != otherName) {
            var otherNode = this.data[otherName];
            if (!otherNode.started) {
                var dependencies = otherNode.dependencies;
                var allEnded = true;
                for (var i = dependencies.length - 1; i >= 0; i--) {
                    if (!dependencies[i].ended) {
                        allEnded = false;
                        break;
                    }
                }
                if (allEnded) {
                    this._startNode(otherNode);
                }
            }
        }
    }
};

Workflow.prototype._startNode = function(node) {
    if (!node.started) {
        node.started = true;
        if (node.action) {
            node.action.call(this);
        }
    }
}

