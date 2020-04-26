# resource-pool-java
这是一个仿照Hashkell上resource-pool非常简单的资源池，很简单，但是看起来应该有用。

可以根据自己的需要自己定义「资源」，http连接池、数据库连接池、线程池（开玩笑:)）等。

## Usage

### 创建池 createPool
```java
Pool<Connection> pool = Pool.createPool(
        //创建资源的函数
        () -> DriverManager.getConnection("jdbc:postgresql://localhost:5432/mytest","huangziqi","yang520"),
        //关闭资源的函数
        connection -> connection.close(),
        //子池数量（一个资源池可以包含多个子池，默认为1）
        3, 
        //闲置资源维持实现，单位毫秒，默认500ms
        1000L, 
        //最大资源数（每个子池的最大资源数，如果子池是3，
        //该参数是10，那么对于整个pool来说，最大资源数是30）
        10);
```

### 直接使用 withResource
```java
Object value = pool.withResource(conn -> {
    //do something you want
    //这里返回的东西会直接返回到上面最外层
    return conn.getClientInfo();
});
```

### 手动使用 takeResource putResource
手动使用的话，需要注意，取了资源出来，用完要放回去
```java
ResourceAndPool<Connection> resAndPool = pool.takeResource();
Connection conn = resAndPool.getResource();
//do something with 'conn'
//...
//返回资源池
pool.putResource(conn, resAndPool.getLocalPool());
```