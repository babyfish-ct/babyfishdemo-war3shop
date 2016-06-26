/**
 * @author Tao Chen
 */
(function($) {
    $.fn.extend({
        setProductImage: function(product, restrictHeight) {
            if (restrictHeight) {
                this.height(product.type == 2 ? "64px" : "120px");
            } else {
                this.width(product.type == 2 ? "64px" : "100px");
            }
            this.attr("src", productImage(product));
        }
    });
    window.productImageHtml = function(product, restrictHeight) {
        if (restrictHeight) {
            return (
                    "<img style='height:'" +
                    (product.type == 2 ? "64;" : "120") +
                    "px;' src='" +
                    productImage(product) +
                    "'/>"
            );
        }
        return (
                "<img style='width:'" +
                (product.type == 2 ? "64;" : "100") +
                "px;' src='" +
                productImage(product) +
                "'/>"
        );
    };
    window.userImage = function(user) {
        return statelessImage("user", user)
    };
    window.productImage = function(product) {
        return statelessImage("product", product)
    };  
    window.manufacturerImage = function(manufacturer) {
        return statelessImage("manufacturer", manufacturer)
    };
    window.uploadedImage = function() {
        return statefulImage("image/uploaded-image.spring");
    };
    window.currentUserImage = function() {
        return statefulImage("image/current-user-image.spring");
    };
    var statelessImage = function(entityName, entity) {
        return "image/" + entityName + "-image.spring?id=" + entity.id + "&version=" + entity.version;
    };
    var statefulImage = function(url) {
        var time = new Date().getTime();
        return url + "?time=" + time;
    };
})(jQuery);
