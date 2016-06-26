/**
 * @author Tao Chen
 */
function jsonp(ajaxData) {
    var handleSuccess = function(ajaxData, e) {
        try {
            if (ajaxData.success) {
                ajaxData.success.call(ajaxData.callBackThis || null, e);
            }
        } finally {
            if (ajaxData.complete) {
                ajaxData.complete.call(ajaxData.callBackThis || null, e);
            }
        }
    };
    var handleError = function(ajaxData, e) {
        if (e.message == null) {
            e.message = "Unknown error message, only know the exception class is " + e.exceptionClass;
        }
        try {
            if (ajaxData.error) {
                ajaxData.error.call(ajaxData.callBackThis || null, e);
            }
        } finally {
            if (ajaxData.complete) {
                ajaxData.complete(ajaxData.callBackThis || null, e);
            }
        }
    };
    var requestData = ajaxData.data;
    if (typeof(requestData) == "function") {
        requestData = requestData();
    }
    var async = typeof(ajaxData.async) == "boolean" ? ajaxData.async : true;
    jQuery.ajax({
        url: ajaxData.url,
        async: async,
        type: "POST",
        contentType: "application/x-www-form-urlencoded",
        dataType: "jsonp",
        data: requestData,
        success: function(responseData) {
            if (responseData.exceptionClass) {
                handleError(ajaxData, responseData);
            } else {
                handleSuccess(ajaxData, responseData);
            }
        },
        error: function(xhr, textStatus, errorThrown) {
            handleError(
                    ajaxData, 
                    {
                        exceptionClass: "org.babyfishdemo.javascript.HTTPError",
                        message: "Failed: HTTP status: " + xhr.status + ", text status: " + textStatus,
                        status : xhr.status
                    }
            );
        }
    });
}

function prepareToUpload() {
    jQuery.ajax({
        url: "upload/prepare-to-upload.spring",
        type: "POST",
        contentType: "application/x-www-form-urlencoded"
    });
}
