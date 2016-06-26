/**
 * @author Tao Chen
 */
/**
 * Be careful, this js file must be included before the js files of kendo UI!
 * 
 * Kendo UI uses local variable "ajax" to cache "$.ajax" in its initalization functions,
 * So the $.ajax must be modified before kendo UI cache it.
 * 
 * @author Chen Tao(babyfish.ct@gmail.com)
 */
(function($) {
    
    /*
     * The AOP must only be applied once even if this file has been included more than once
     */
    if ($.$__alibabaFastJsonAOPApplied) {
        return;
    }
    $.$__alibabaFastJsonAOPApplied = true;
    
    /*
     * AOP logic to resolve the circular reference of fast json.
     */
    var originalAjax = $.ajax;
    $.ajax = function(options) {
        var originalSuccess = options.success;
        if (typeof(originalSuccess) == "function") {
            options.success = function(data) {
                resolveCircularReferences(data);
                originalSuccess.apply(this, arguments);
            };
        }
        originalAjax.apply(this, arguments);
    };
    
    /*
     * It is very important to resolve the circular references after parse the fastjson.
     * 
     * Circular references is really a cool functionality of Alibaba Fastjson,
     * this is the most important reason why I choose alibaba-fastJson!
     * It looks like the java serialization, section5-SOAP or GWT serialization, 
     * it fully supports the graph-style programming like Java, .NET and high level scripts, 
     * not like other tree-only-style protocols such as document-SOAP and generic json.
     * 
     * Though alibaba-fastjson supports "DisableCircularReferenceDetect" in sever-side,
     * it is not good choice because the tree-structure may contain duplicated object,
     * so don't use it in server-side and resolve the circular references in client-side
     * 
     * -------------------------------------------------------------------------------------
     * Unfortunately, KendoUI does not support the object graph with circular references,
     * because the kendo.data.ObservableObject and kendo.data.ObservableArray is not smart 
     * enough so that the stack-overflow error will be raised when the kendo UI widgets accept
     * the json data with circular references, so server-side has to guarantee it never generate 
     * the object graph with circular references like this
     * {
     *      "name": "parentObject",
     *      "children": [
     *          { "name": "childObject1", "parent": { "$ref": "$" } },
     *          { "name": "childObject2", "parent": { "$ref": "$" } },
     *          { "name": "childObject3", "parent": { "$ref": "$" } }
     *      ]
     * }
     *  
     * But, this client-side circular reference resolving is still necessary because 
     * this structure is still allowed and need to be resolved, this structure is
     * very useful, especially when the duplicated objects contain lob properties
     * [
     *      { "name": "childObject1", "parent": { "name": "parentObject", "otherField": ....Huge data.... } },
     *      { "name": "childObject2", "parent": { "$ref": "$[0].parent" } },
     *      { "name": "childObject3", "parent": { "$ref": "$[0].parent" } }
     * ]
     * 
     * As a demo, this function is still designed to resolve the fastjson with any complexity of circular 
     * references. You can still use this javascript function in the other web applications that fully 
     * supports circular references in client-side.
     * -------------------------------------------------------------------------------------
     */
    var resolveCircularReferences = function(obj) {
        /*
         * In Alibaba fastjson, the circular reference is a object with 
         * a property "$ref" whose value may start with "$", like this:
         * 
         * { $ref: "$.products[5].purchasers[4]" }
         * 
         * The "$" means the root of the object graph.
         */
        var $ = obj;
        
        var process = function(o, owner, parent) {
            if (typeof(o) != "object" || o.$__alibabaFastJsonAOPApplied) {
                return o;
            }
            // Avoid dead recursion
            o.$__alibabaFastJsonAOPApplied = true;
            
            var ref = o.$ref;
            if (typeof(ref) == "string") {
                if (ref == "@") {
                    return owner;
                }
                if (ref == "..") {
                    return parent;
                }
                if (ref.charAt(0) == "$") {
                    /*
                     * The ref is a expression path.
                     * 
                     * <<<==== A suggestion for alibaba-fastJson ====>>>
                     *    If change the recursive method
                     *      "String com.alibaba.fastjson.serializer.SerialContext.getPath();"
                     *    to be
                     *      "void com.alibaba.fastjson.serializer.SerialContext.writePathTo(
                     *              com.alibaba.fastjson.serializer.SerializeWriter targetWriter
                     *      );"
                     *  , the path calculation can become faster.
                     * <<<==== A suggestion for alibaba-fastJson ====>>>
                     */
                    return eval(ref);
                }
            }
            if (o instanceof Array) {
                for (var i = o.length - 1; i >= 0; i--) {
                    o[i] = process(o[i], o, owner);
                }
            } else {
                for (var k in o) {
                    var v = o[k];
                    var k2 = process(k, o, owner);
                    if (k !== k2) {
                        delete o[k];
                    }
                    o[k2] = process(v, o, owner);
                }
            }
            return o;
        };
        
        /*
         * Start the resolve work from root object. 
         */
        process($);
    };
    
})(jQuery);
