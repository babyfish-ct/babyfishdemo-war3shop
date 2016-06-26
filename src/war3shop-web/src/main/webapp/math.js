/**
 * @author Tao Chen
 */
function selfParseInt(text) {
    var n = parseInt(text);
    if (isNaN(n)) {
        return;
    }
    return n;
}
