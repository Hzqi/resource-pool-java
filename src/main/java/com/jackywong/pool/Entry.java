package com.jackywong.pool;

import java.time.LocalDateTime;

/**
 * Created by huangziqi on 2020/4/26
 *
 * -- | A single resource pool entry.
 * data Entry a = Entry {
 *       entry :: a
 *     , lastUse :: UTCTime
 *     -- ^ Time of last return.
 *     }
 */
public class Entry<T> {
    protected T entry;
    protected Long lastUse;

    public Entry(T entry, Long lastUse) {
        this.entry = entry;
        this.lastUse = lastUse;
    }

    public T getEntry() {
        return entry;
    }

    public void setEntry(T entry) {
        this.entry = entry;
    }

    public Long getLastUse() {
        return lastUse;
    }

    public void setLastUse(Long lastUse) {
        this.lastUse = lastUse;
    }
}
