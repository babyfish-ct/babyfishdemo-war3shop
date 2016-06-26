/**
 * @author Tao Chen
 */
(function($) {
    $.fn.extend({
        computedStyle: function() {
            return $.computedStyle(this[0]);
        }
    });
    $.extend({
        computedStyle: function(ele) {
            if (ele instanceof HTMLElement) {
                if (window.getComputedStyle) {
                    return window.getComputedStyle(ele);
                } else { // For low version IE
                    return ele.currentStyle;
                }
            } else if (ele) {
                return $.computedStyle(ele[0]);
            }
            return {};
        }
    });
})(jQuery);
