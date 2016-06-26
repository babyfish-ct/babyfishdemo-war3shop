/**
 * @author Tao Chen
 */
function parameterMap(map, queryPath) {
    if (typeof(map.page) != "undefined") {
        //The page index of Kendo UI starts from 1 
        //but the page index of server-side starts from 0
        map.pageIndex = map.page - 1;
        delete map.page;
    }
    if (typeof(queryPath) == "string") {
        // The queryPaths for one entity type.
        queryPath = queryPath.trim();
        if (queryPath.length != 0) {
            map.queryPath = queryPath;
            if (queryPath.charAt(queryPath.length - 1) != ";") {
                map.queryPath += ";";
            }
        }
    } else if (typeof(queryPath) == "object") {
        // The queryPaths for several entity types.
        for (var key in queryPath) {
            var subQueryPath = queryPath[key];
            if (typeof(subQueryPath) == "string") {
                subQueryPath = subQueryPath.trim();
                if (subQueryPath.length != 0) {
                    map[key] = subQueryPath;
                    if (subQueryPath.charAt(subQueryPath.length - 1) != ";") {
                        map[key] += ";";
                    }
                }
            }
        }
    } else {
        map.queryPath = "";
    }
    if (typeof(map.sort) != "undefined") {
        var arr = map.sort;
        for (var i = 0; i < arr.length; i++) {
            map.queryPath += "pre order by ";
            map.queryPath += arr[i].field;
            map.queryPath += " ";
            map.queryPath += arr[i].dir;
            map.queryPath += ";"
        }
        delete map.sort;
    }
    delete map.top;
    delete map.skip;
    delete map.take;
    return map;
}
