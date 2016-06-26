/**
 * @author Tao Chen
 */
if (!Array.prototype.where) {
    throw new Error("\"linq.js\" must be included before \"kendo-linq.js\"");
}
kendo.data.ObservableArray.prototype.where = Array.prototype.where;
kendo.data.ObservableArray.prototype.select = Array.prototype.select;
kendo.data.ObservableArray.prototype.contains = Array.prototype.contains;
kendo.data.ObservableArray.prototype.minus = Array.prototype.minus;
