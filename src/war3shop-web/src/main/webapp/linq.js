/**
 * @author Tao Chen
 */
Array.prototype.where = function(predicate) {
    if (typeof(predicate) != "function") {
        throw new Error("The argument must be a function");
    }
    var arr = [];
    var index = 0;
    var len = this.length;
    for (var i = 0; i < len; i++) {
        if (predicate(this[i])) {
            arr[index++] = this[i];
        }
    }
    return arr;
};

Array.prototype.select = function(transformer) {
    if (typeof(transformer) != "function") {
        throw new Error("The argument must be a function");
    }
    var arr = [];
    for (var i = this.length - 1; i >= 0; i--) {
        arr[i] = transformer(this[i]);
    }
    return arr;
};

Array.prototype.contains = function(e) {
    for (var i = this.length - 1; i >= 0; i--) {
        if (this[i] == e) {
            return true;
        }
    }
    return false;
};

Array.prototype.containsAll = function(c) {
    for (var i = c.length - 1; i >= 0; i--) {
        if (!this.contains(c[i])) {
            return false;
        }
    }
    return true;
};

Array.prototype.containsAny = function(c) {
    for (var i = c.length - 1; i >= 0; i--) {
        if (this.contains(c[i])) {
            return true;
        }
    }
    return false;
};

Array.prototype.minus = function(other) {
    if (typeof(other) == "undefined") {
        return this;
    }
    return this.where(function(e) {
        return !other.contains(e);
    });
};
