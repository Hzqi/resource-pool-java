package com.jackywong.pool.functional;

import java.util.Objects;

/**
 * Created by huangziqi on 2020/4/26
 */
public interface ThrownConsumer<T> {
    void accept(T t) throws Exception;

    default ThrownConsumer<T> andThen(ThrownConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }
}
