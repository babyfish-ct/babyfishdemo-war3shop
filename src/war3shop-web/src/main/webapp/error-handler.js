/**
 * @author Tao Chen
 */
var createErrorDialog = function(e) {
    return createTemplate(
            "AlertDialog", 
            { 
                title: "Error", 
                message: e.exceptionMessage || e.errors || "Ajax requrest failed" 
            }
    );
};
