package com.jackywong.pool;

import com.jackywong.pool.functional.ThrownBiFunction;
import com.jackywong.pool.functional.ThrownConsumer;
import com.jackywong.pool.functional.Tuple;

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by huangziqi on 2020/4/26
 */
public class LocalPool<T> {
    //Count of open entries (both idle and in use).
    protected Integer inUse;
    //Idle entries.
    protected List<Entry<T>> entries;
    private Lock _lock;

    public LocalPool(Integer inUse, List<Entry<T>> entries) {
        this.inUse = inUse;
        this.entries = entries;
        this._lock = new ReentrantLock();
    }

    public void lockWithModify(ThrownBiFunction<Integer, List<Entry<T>>, Tuple<Integer, List<Entry<T>>>> function) throws Exception {
        _lock.lock();
        try {
            Tuple<Integer, List<Entry<T>>> tuple = function.apply(inUse, entries);
            this.inUse = tuple.getA();
            this.entries = tuple.getB();
        } finally {
            _lock.unlock();
        }
    }
}
