/**
 * @author Tao Chen
 */
(function() {
    var regexp = /^[\$A-Za-z]([\$A-Za-z0-9])*(\.[\$A-Za-z]([\$A-Za-z0-9])*)*$/;
    window.definePackage = function(packageName) {
        if (!regexp.test(packageName)) {
            throw new Error("Invalid package name \"" + packageName + "\"");
        }
        var arr = packageName.split(".");
        var parent = window;
        for (var i = 0; i < arr.length; i++) {
            if (!parent[arr[i]]) {
                parent = parent[arr[i]] = {};
            } else {
                parent = parent[arr[i]];
            }
        }
        return parent;
    };
    
    definePackage("java.lang").Enum = function(ordinal, name, toString) {
        this._ordinal = ordinal;
        this._name = name;
        this._toString = toString || name;
    };
    
    var Enum = java.lang.Enum; 

    Enum.prototype.ordinal = function() {
        return this._ordinal;
    };

    Enum.prototype.name = function() {
        return this._name;
    };

    Enum.prototype.toString = function() {
        return this._toString;
    };

    /*
     * (1) Enum.define("youPackage.yourType", "A", "B");
     * (2) Enum.define(
     *        "youPackage.yourType", 
     *        { name: "A", toString: "The first value" }, 
     *        { name : "B", toString: "The second value"}
     *       );
     */
    Enum.define = function(typeName /* , ... */) {
        if (!regexp.test(typeName)) {
            throw new Error("Invalid type name \"" + typeName + "\"");
        }
        var type = function(ordinal, name, toString) {
            Enum.call(this, ordinal, name, toString);
        };
        for (var member in Enum.prototype) {
            type.prototype[member] = Enum.prototype[member];
        }
        var values = [];
        for (var i = 0; i < arguments.length - 1; i++) {
            var item = arguments[i + 1];
            if (typeof(value) == "string") {
                type[item] = values[i] = new type(i, item);
            } else {
                type[item.name] = values[i] = new type(i, item.name, item.toString);
            }
        }
        type.values = function() {
            return values;
        };
        /*
         * In Java, the method is "valueOf", not "of",
         * Here I change it to be "valOf" because chrome has a bug.
         * When the method name is "valueOf", although it can run successfully,
         * but the chrome will crash if you debug this method with the breakpoints
         * 
         * Need report it to google?
         */
        type.valOf = function(name) {
            for (var i = values.length - 1; i >= 0; i--) {
                if (values[i]._name == name) {
                    return values[i];
                }
            }
            throw new Error("No enum literal whoes name is \"" + name + "\"");
        };
        var lastIndex = typeName.lastIndexOf(".");
        var pkg = lastIndex == -1 ? window : definePackage(typeName.substring(0, lastIndex));
        var simpleName = lastIndex == -1 ? typeName : typeName.substring(lastIndex + 1);
        pkg[simpleName] = type;
    };
})();

java.lang.Enum.define(
        "org.babyfishdemo.war3shop.entities.ProductType",
        { name : "UNIT", toString: "Unit" },
        { name : "HERO", toString: "Hero" },
        { name : "ITEM", toString: "Item" }
);

java.lang.Enum.define(
        "org.babyfishdemo.war3shop.entities.Race",
        { name : "NEUTRAL", toString: "Neutral" },
        { name : "HUMAN", toString: "Human" },
        { name : "UNDEAD", toString: "Undead" },
        { name : "ORC", toString: "Orc" },
        { name : "NIGHTELF", toString: "Nightelf" }
);

java.lang.Enum.define(
        "org.babyfishdemo.war3shop.entities.PreferentialThresholdType",
        { name : "QUANTITY", toString: "Quantity" },
        { name : "MONEY", toString: "Money" }
);

java.lang.Enum.define(
        "org.babyfishdemo.war3shop.entities.PreferentialActionType",
        { name : "MULTIPLIED_BY_PERCENTAGE", toString: "Multiplied by percentage" },
        { name : "REDUCE_MONEY", toString: "Reduce money" },
        { name : "SEND_GIFT", toString: "Send gift" }
);
