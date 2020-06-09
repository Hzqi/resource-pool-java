package com.jackywong.pool.functional;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by huangziqi on 2020/4/24
 * 一个「尝试」的类型，包含报错信息的
 */
public class Try<T> {
    private T value;
    private Exception ex;
    private boolean success;

    private Try() {}

    /**
     * 一个用户处理是否成功的方法
     * @param supplier
     * @param <T>
     * @return
     */
    public static <T> Try<T> of(Supplier<T> supplier) {
        try {
            T result = supplier.get();
            return success(result);
        } catch (Exception ex) {
            return failure(ex);
        }
    }

    /**
     * 成功实例
     * @param value
     * @param <T>
     * @return
     */
    public static <T> Try<T> success(T value) {
        Try<T> t = new Try<>();
        t.value = value;
        t.success = true;
        return t;
    }

    /**
     * 失败的实例
     * @param ex
     * @param <T>
     * @return
     */
    public static <T> Try<T> failure(Exception ex) {
        Try<T> t = new Try<>();
        t.ex = ex;
        t.success = false;
        return t;
    }

    /**
     * 是否成功
     * @return
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 是否失败
     * @return
     */
    public boolean isFailure() {
        return !isSuccess();
    }

    /**
     * 直接获取内容，注意，若是失败的实例会马上报错
     * @return
     */
    public T get() {
        if(isSuccess()) {
            return value;
        } else {
            throw new RuntimeException(ex);
        }
    }

    public Exception getEx() {
        if(isSuccess()) {
            throw new RuntimeException("this doesn't throw a excetpion.");
        } else {
            return ex;
        }
    }

    /**
     * 获取内容，若是失败就使用other
     * @param other
     * @param <R>
     * @return
     */
    public <R extends T> T getOrElse(R other) {
        if (isSuccess()) {
            return value;
        } else {
            return other;
        }
    }

    /**
     * 获取内容，若是失败就调用other
     * @param other
     * @param <R>
     * @return
     */
    public <R extends T> T getOrElse(Supplier<R> other) {
        if (isSuccess()) {
            return value;
        } else {
            return other.get();
        }
    }

    /**
     * map映射内容
     * @param func
     * @param <U>
     * @return
     */
    public <U> Try<U> map(ThrownFunction<T, U> func) {
        if (isSuccess()) {
            try {
                U u = func.apply(value);
                return Try.success(u);
            } catch (Exception ex) {
                return Try.failure(ex);
            }
        } else {
            return Try.failure(ex);
        }
    }

    /**
     * 获取内容，若是失败处理异常后返回
     * @param function
     * @param <R>
     * @return
     */
    public <R extends T> T getWithException(Function<Exception, R> function) {
        if (isSuccess()) {
            return value;
        } else {
            return function.apply(ex);
        }
    }

    /**
     * 类似catch，最后去处理异常的
     * @param func
     */
    public void withException(Consumer<Exception> func) {
        if (isFailure()) {
            func.accept(this.ex);
        }
    }

    /**
     * 获取内容，若是失败就抛出异常
     * @return
     * @throws Exception
     */
    public T getOrThrow() throws Exception {
        if (isSuccess()) {
            return value;
        } else {
            throw ex;
        }
    }
}
