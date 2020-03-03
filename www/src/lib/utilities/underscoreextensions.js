/**
 * Created by bberman on 1/17/17.
 */
const optimizeCb = function(func, context, argCount) {
    if (context === void 0) return func;
    switch (argCount == null ? 3 : argCount) {
        case 1: return function(value) {
            return func.call(context, value);
        };
        case 2: return function(value, other) {
            return func.call(context, value, other);
        };
        case 3: return function(value, index, collection) {
            return func.call(context, value, index, collection);
        };
        case 4: return function(accumulator, value, index, collection) {
            return func.call(context, accumulator, value, index, collection);
        };
    }
    return function() {
        return func.apply(context, arguments);
    };
};

const cb = function(value, context, argCount) {
    if (value == null) return _.identity;
    if (_.isFunction(value)) return optimizeCb(value, context, argCount);
    if (_.isObject(value)) return _.matcher(value);
    return _.property(value);
};
_.iteratee = function(value, context) {
    return cb(value, context, Infinity);
};


_.mixin({
    mapObject: function (obj, iteratee, context) {
        iteratee = cb(iteratee, context);
        var keys = _.keys(obj),
            length = keys.length,
            results = {},
            currentKey;
        for (var index = 0; index < length; index++) {
            currentKey = keys[index];
            results[currentKey] = iteratee(obj[currentKey], currentKey, obj);
        }
        return results;
    }
});