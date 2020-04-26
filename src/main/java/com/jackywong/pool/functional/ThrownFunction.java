package com.jackywong.pool.functional;

import java.util.Objects;

/**
 * Created by huangziqi on 2020/4/26
 */
@FunctionalInterface
public interface ThrownFunction<T,R> {
    R apply(T t) throws Exception;

    default <V> ThrownFunction<V, R> compose(ThrownFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> ThrownFunction<T, V> andThen(ThrownFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T> ThrownFunction<T, T> identity() {
        return t -> t;
    }
}
