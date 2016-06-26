/**
 * @author Tao Chen
 */
// Ideally, CSS3 supports grayscale for image, like this:
// 
// img.grayscale {
//          -webkit-filter: grayscale(100%); /* For Chrome */
//          -moz-filter: grayscale(100%); /* Not work, hope for Firefox, but not implemented */
//          -ms-filter: grayscale(100%); /* Not work, Hope for IE, but not implemented */
//          -o-filter: grayscale(100%); /* For Opera */
//          filter: grayscale(100%); /* W3C */
//          filter: gray; /* For IE6-9 */
//          filter:url('grayscale.svg#grayscale'); /* For Firefox */
// }
//
// Specially, for Firefox, it needs another file "grayscale.svg", like this
//
// <svg xmlns="http://www.w3.org/2000/svg">  
//      <filter id="grayscale">  
//          <feColorMatrix 
//                  type="matrix" 
//                  values="
//                      0.3333 0.3333 0.3333 0      0
//                      0.3333 0.3333 0.3333 0      0
//                      0.3333 0.3333 0.3333 0      0 
//                      0      0      0      1      0"/>  
//      </filter>  
// </svg>
//
// Unfortunately, this powerful solution still can not run all browsers, such as
// IE10, IE11, Safri(version < 7). 
//
// So, I had to give up CSS3 and implement this work in server-side
// (Another choice is to create the gray image by javascript via canvas/Context2D API, 
// but I think do it by Java is more simple for browser cache mechanism). Please see 
//      "org.babyfishdemo.war3shop.web.ImageController" 
// to known more.
// Angry!!!
(function($) {
    $.fn.extend({
        grayscale: function(enable) {
            var src = this.attr("src");
            src = src.replace("&gray=true", "");
            if (enable) {
                src += "&gray=true";
            }
            this.attr("src", src);
        }
    });
})(jQuery);
