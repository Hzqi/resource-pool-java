package com.jackywong.pool.functional;

import java.util.Objects;
import java.util.function.BiFunction;

/**
 * Created by huangziqi on 2020/4/26
 */
@FunctionalInterface
public interface ThrownBiFunction<T, U, R> {

    R apply(T t, U u) throws Exception;

    default <V> ThrownBiFunction<T, U, V> andThen(ThrownFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t, U u) -> after.apply(apply(t, u));
    }
}
