package com.jackywong.pool;

/**
 * Created by huangziqi on 2020/4/26
 */
public class ResourceAndPool<T> {
    private T resource;
    private LocalPool<T> localPool;

    public ResourceAndPool(T resource, LocalPool<T> localPool) {
        this.resource = resource;
        this.localPool = localPool;
    }

    public T getResource() {
        return resource;
    }

    public void setResource(T resource) {
        this.resource = resource;
    }

    public LocalPool<T> getLocalPool() {
        return localPool;
    }

    public void setLocalPool(LocalPool<T> localPool) {
        this.localPool = localPool;
    }
}
