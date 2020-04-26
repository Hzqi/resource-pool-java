package com.jackywong.pool.functional;

/**
 * Created by huangziqi on 2020/4/26
 */
@FunctionalInterface
public interface ThrownSupplier<T> {
    T get() throws Exception;
}
