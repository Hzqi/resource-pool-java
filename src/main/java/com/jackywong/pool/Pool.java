package com.jackywong.pool;

import com.jackywong.pool.functional.ThrownConsumer;
import com.jackywong.pool.functional.ThrownFunction;
import com.jackywong.pool.functional.ThrownSupplier;
import com.jackywong.pool.functional.Tuple;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by huangziqi on 2020/4/26
 *
 * 参考Haskell的resource-pool实现的pool
 */
public class Pool<T> {
    //创建资源的方法
    protected ThrownSupplier<T> createFunc;
    //关闭资源的方法
    protected ThrownConsumer<T> destoryFunc;
    //子池数
    protected Integer numStripes;
    //闲置的资源持续时间, 默认0.5秒
    protected Long idleTime = 500L;
    //最大资源
    protected Integer maxResource;
    //子池
    protected List<LocalPool<T>> localPools;
    //监听线程
    private Thread reaper;

    private Pool() {
    }

    private Pool(ThrownSupplier<T> createFunc, ThrownConsumer<T> destoryFunc, Integer numStripes, Long idleTime, Integer maxResource, List<LocalPool<T>> localPools) {
        this.createFunc = createFunc;
        this.destoryFunc = destoryFunc;
        this.numStripes = numStripes;
        this.idleTime = idleTime;
        this.maxResource = maxResource;
        this.localPools = localPools;
        createAndStartReaper();
    }

    @Override
    public String toString() {
        return "Pool{" +
                "numStripes=" + numStripes +
                ", idleTime=" + idleTime +
                ", maxResource=" + maxResource +
                '}';
    }

    public static <T> Pool<T> createPool(ThrownSupplier<T> createFunc, ThrownConsumer<T> destoryFunc, Integer subPools, Long idleTime, Integer maxResource) throws Exception {
        if (subPools < 1)
            throw new Exception("invalid stripe count " + subPools);
        if (idleTime < 500)
            throw new Exception("invalid idle time " + idleTime);
        if (maxResource < 1)
            throw new Exception("invalid maximum resource count " + maxResource);
        //建立子池
        List<LocalPool<T>> localPools = new LinkedList<>();
        for (int i = 0; i < subPools; i++) {
            Integer inUse = 0;
            List<Entry<T>> entries = new LinkedList<>();
            LocalPool<T> localPool = new LocalPool<>(inUse,entries);
            localPools.add(localPool);
        }
        Pool<T> pool = new Pool<>(createFunc,destoryFunc, subPools, idleTime, maxResource, localPools);
        return pool;
    }

    public static <T> Pool<T> createPool(ThrownSupplier<T> createFunc, ThrownConsumer<T> destoryFunc, Integer subPools, Long idleTime) throws Exception {
        return createPool(createFunc,destoryFunc,subPools,idleTime,1);
    }

    public static <T> Pool<T> createPool(ThrownSupplier<T> createFunc, ThrownConsumer<T> destoryFunc, Integer subPools) throws Exception {
        return createPool(createFunc,destoryFunc,subPools,500L);
    }

    public static <T> Pool<T> createPool(ThrownSupplier<T> createFunc, ThrownConsumer<T> destoryFunc) throws Exception {
        return createPool(createFunc, destoryFunc, 1);
    }

    //创建不断循环检查池子的线程
    protected void createAndStartReaper() {
        this.reaper = new Thread(() -> {
            try {
                // 不断检查
                while (true) {
                    Thread.sleep(100);
                    long now = System.currentTimeMillis();
                    for (LocalPool<T> localPool : localPools) {
                        localPool.lockWithModify((inUse, entries) -> {
                            //将列表分成两个，过期和未过期
                            Map<Boolean, List<Entry<T>>> staleAndFresh = entries.stream()
                                    .collect(Collectors.partitioningBy(i -> isStale(i.lastUse, now)));
                            //关闭已经过期的资源
                            List<Entry<T>> stales = staleAndFresh.get(true);
                            for (Entry<T> entry : stales) {
                                destoryFunc.accept(entry.entry);
                            }
                            return Tuple.of(inUse - stales.size(), staleAndFresh.get(false));
                        });
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        });
        this.reaper.start();
    }

    //检查是否过期
    protected boolean isStale(long lastUse, long now) {
        return (now - lastUse) > idleTime;
    }

    //利用资源池里的资源干活
    public <R> R withResource(ThrownFunction<T,R> func) throws Exception {
        ResourceAndPool<T> resourceAndPool = takeResource();
        T resource = resourceAndPool.getResource();
        LocalPool<T> local = resourceAndPool.getLocalPool();
        try {
            R result = func.apply(resource);
            return result;
        } finally {
            //报错就直接抛出
            //最后无论成不成功都把资源放回去
            putResource(local, resource);
        }
    }

    //从资源池里获取资源
    public ResourceAndPool<T> takeResource() throws Exception {
        LocalPool<T> local = getLocalPool();
        Box<T> resourceBox = new Box<>(null);
        Box<Boolean> flag = new Box<>(false);

        while (!flag.getValue()) {
            local.lockWithModify((inUse, entries) -> {
                if (!entries.isEmpty()) {
                    //资源池里有东西，直接返回
                    resourceBox.setValue(entries.get(0).entry);
                    flag.setValue(true);
                    return Tuple.of(inUse, entries.subList(1,entries.size()));
                } else {
                    //资源池里没有，尝试创建一个
                    //先看看是否能创建
                    if(inUse.equals(maxResource)) {
                        return Tuple.of(inUse, entries);
                    } else {
                        resourceBox.setValue(createFunc.get());
                        flag.setValue(true);
                        return Tuple.of(inUse + 1, entries);
                    }
                }
            });
            if (!flag.getValue()) {
                Thread.sleep(500); //这里等待0.5秒，循环清理默认是0.5秒执行一次
            }
        }
        return new ResourceAndPool<>(resourceBox.getValue(), local);
    }

    //内部的，创建子池
    protected LocalPool<T> getLocalPool() {
        long myThreadId = Thread.currentThread().getId();
        int index = Math.toIntExact(myThreadId % numStripes);
        return localPools.get(index);
    }

    //资源放回资源池
    public void putResource(LocalPool<T> localPool, T resource) throws Exception {
        long now = System.currentTimeMillis();
        Entry<T> entry = new Entry<>(resource, now);
        localPool.lockWithModify((inUse,entries) -> {
            entries.add(entry);
            return Tuple.of(inUse, entries);
        });
    }

    public void close() throws Exception {
        this.reaper.interrupt();
        for (LocalPool<T> localPool : localPools) {
            localPool.lockWithModify((inUse,entries)  -> {
                for (Entry<T> entry : entries) {
                    destoryFunc.accept(entry.entry);
                }
                return Tuple.of(0, new LinkedList<>());
            });
        }
    }
}
