## database2api

#### 什么是 **database2api**？

顾名思义，就是快速、直接的将数据库转化为 API 接口，方便多种应用场景。而且提供了静态文件托管功能，非常实用。

#### **database2api**是使用什么开发的？

使用 [Kotlin](https://kotlinlang.org/) + [Ktor](https://ktor.io/) 开发，使用 **Netty** 作为内置服务器，请求速度极快。

#### 支持哪些数据库？

常见的关系数据库如：Sqlite、Oracle、MySQL、PostgreSQL、IBM DB2、Microsoft SQL Server、Sybase、Informix、MariaDb 等数据库都支持。

#### 为什么要创造 **database2api**？

经常有朋友问我，以前有个网站做了很久了，需要更新版本，但是苦于代码耦合太严重，开发新版本工作量太大。

自从有了 **database2api**，直接将数据库连接交于 **database2api**，自动生成全站的 API 接口，再开发独立的 web 界面，新版本的开发问题就迎刃而解了。

#### 什么是扩展 API？

就是写一个JS文件，作为API扩展接口，执行数据库访问，完成API请求的功能。

开启方式，在配置文件里设置 `EXT_API_ENABLED=true`，并在 `data` 目录下创建 `ext` 目录，创建文件 `get_hello`，内容如下：

```js
function main() {
    var name = context.query.name || "no name";
    return "hello " + name;
}
```

规定函数名 `main`，重新启动 **database2api** 后可看到控制台提示：

```text
2024-07-14 17:26:58.380 [main] INFO  com.mrhuo.plugins.RoutingKt - Database2Api.scriptApiRoute: 创建扩展API[GET:/api/ext/hello]成功
```

访问该API[http://127.0.0.1:8080/api/ext/hello?name=mrhuo]时，返回结果如下：

```json
{
  "code": 0,
  "msg": "OK",
  "data": "hello mrhuo"
}
```

**注意**：扩展API因为用到了脚本引擎来解释执行脚本代码，性能不是太好，如非必要，请勿过度依赖此功能。

扩展API中目前支持 `db`, `context` 两个对象。
- `db` 对象主要用于数据库查询，提供 `db.query(sql)`, `db.queryOne(sql)`, `db.exec(sql)` 这三个方法
- `context` 对象主要用于当前请求参数的获取，提供 `context.uri`, `context.method`, `context.headers`, `context.query`, `context.body` 五个对象

#### 已测试数据库

- [x] Sqlite
- [x] MySQL
- [x] PostgreSQL
- [x] Microsoft SQL Server
- [x] MariaDb
- [ ] Oracle
- [ ] IBM DB2
- [ ] Sybase
- [ ] Informix

#### 如何开始使用？

###### 目录结构预览

```text
│  database2api.jar  <-- 主程序
└─ data
     └─ web          <-- 静态文件目录
     └─ setting.ini  <-- 配置文件
```

###### 配置文件预览(./data/setting.ini)

```text
## DEFAULT CONFIG FOR database2api
# API 默认端口
API_PORT=8080
# 生成API的前缀，如设置 api/v1 后，则API变为：http://localhost:{PORT}/api/v1/xxxxxx
API_PREFIX=api
# 是否启用 API 文档，地址 http://localhost:{PORT}
API_INDEX_ENABLED=true
# 是否启用接口授权访问功能
API_AUTH_ENABLED=false
# 接口授权访问，支持：Basic, JWT,
API_AUTH_TYPE=JWT
# 接口允许访问的用户名密码列表
API_AUTH_USERS=admin:123456,user:1234
# 数据库默认链接地址
DB_URL=jdbc:sqlite://G:/database2api-test/sqlite/fqb.db
# 数据库用户名
DB_USER=
# 数据库密码
DB_PWD=
# 生成API的数据表名称，为空则所有的表都生成API，多个使用英文逗号分割
INCLUDE_TABLES=
# 需要忽略的数据表名称，如果不为空，则指定的表名被过滤，多个使用英文逗号分割
IGNORED_TABLES=
# 是否启用静态网站，启用后，则创建web目录，放入静态资源即可访问
STATIC_WEB_ENABLED=true
# 是否开启扩展API，允许用户使用JS代码使用自定义SQL查询数据库
EXT_API_ENABLED=true
```

###### 数据库连接字符串模板

1. Sqlite
```text
DB_URL=jdbc:sqlite://G:/db.db
```

2. MySQL
```text
DB_URL=jdbc:mysql://127.0.0.1:3306/db?useSSL=false&serverTimezone=UTC&charset=utf8mb
```

3. PostgreSQL
```text
DB_URL=jdbc:postgresql://127.0.0.1:5432/db
```

4. Microsoft SQL Server
```text
DB_URL=jdbc:sqlserver://;serverName=rm-abc.sqlserver.rds.aliyuncs.com;port=1433;databaseName=db_cms
```

5. MariaDb

```text
jdbc:mariadb://127.0.0.1:3306/mysql?useSSL=false&serverTimezone=UTC&charset=utf8mb4
```

6. Oracle
```text
TODO
```

7. IBM DB2
```text
TODO
```

8. Sybase
```text
TODO
```

9. Informix
```text
TODO
```

###### 运行方式

```shell
java -jar database2api.jar
```

###### 启动日志类似

```text
2024-07-11 23:43:14.367 [main] DEBUG cn.hutool.log.LogFactory - Use [Slf4j] Logger As Default.
2024-07-11 23:43:14.369 [main] INFO  com.mrhuo.Database2Api - Database2Api: 开始初始化
2024-07-11 23:43:14.382 [main] INFO  com.mrhuo.Database2Api - Database2Api: 开始初始化 API 配置
2024-07-11 23:43:14.431 [main] DEBUG cn.hutool.setting.SettingLoader - Load setting file [D:\work\java\database2api\data\setting.ini]
2024-07-11 23:43:14.444 [main] INFO  com.mrhuo.Database2Api - Database2Api: 静态网站主页[http://127.0.0.1:8080/web/index.html]
2024-07-11 23:43:14.444 [main] INFO  com.mrhuo.Database2Api - Database2Api: 开始初始化数据库
2024-07-11 23:43:14.444 [main] INFO  com.mrhuo.Database2Api - Database2Api: 使用链接字符串[jdbc:sqlite://G:/database2api-test/sqlite/fqb.db]
2024-07-11 23:43:15.236 [main] INFO  com.mrhuo.Database2Api - Database2Api: 获取到所有数据表的表结构
2024-07-11 23:43:15.236 [main] INFO  com.mrhuo.Database2Api - Database2Api: 已保存到文件[D:\work\java\database2api\data\tables.json]
2024-07-11 23:43:15.236 [main] INFO  com.mrhuo.Database2Api - Database2Api: 初始化全部成功
2024-07-11 23:43:15.383 [main] INFO  ktor.application - Autoreload is disabled because the development mode is off.
2024-07-11 23:43:16.241 [main] INFO  ktor.application - Application started in 0.928 seconds.
2024-07-11 23:43:16.242 [main] INFO  ktor.application - Application started: io.ktor.server.application.Application@299266e2
2024-07-11 23:43:16.633 [DefaultDispatcher-worker-1] INFO  ktor.application - Responding at http://127.0.0.1:8080
```

启动成功后目录结构变为：

```text
│  database2api.jar
└─ data
     │  setting.ini
     │  tables.json      <-- 这是数据库中所有的表名称，下次启动时不会从数据库重新获取，直接使用此文件。如数据库已更新，则删除此文件
     │  table_names.json <-- 这是数据库中所有表结构，下次启动时不会从数据库重新获取，直接使用此文件。如数据库已更新，则删除此文件
     └─ web
         └─ index.html   <-- 这是静态网页默认首页
```

此时，访问链接 [http://127.0.0.1:8080/](http://127.0.0.1:8080/) （端口号在配置文件中定义）

![screenshots/image1.png](screenshots/image1.png)

测试获取所有数据接口：http://127.0.0.1:8080/api/DEVICE/all

![screenshots/image2.png](screenshots/image2.png)

测试分页显示数据接口：http://127.0.0.1:8080/api/DEVICE/paged

![screenshots/image3.png](screenshots/image3.png)

可以看到，仅仅是配置了数据库链接，就自动生成一个完整的可用的API接口。

#### 接口加密访问

现已支持 Basic、JWT 对API授权，配置如下：

```text
# 是否启用接口授权访问功能
API_AUTH_ENABLED=false
# 接口授权访问，支持：Basic, JWT,
API_AUTH_TYPE=JWT
# 接口允许访问的用户名密码列表
API_AUTH_USERS=admin:123456,user:1234
```

###### Basic 授权

- 需要配置 `API_AUTH_ENABLED=true` 开启API授权
- 需要配置 `API_AUTH_TYPE=Basic` （注意大小写）
- 需要配置 `API_AUTH_USERS=user:pass,user1:pass1`，设置允许访问的用户密码对

> Basic 授权失败演示

![授权失败](screenshots/auth-basic-failed.png)

> Basic 授权成功演示

![授权成功](screenshots/auth-basic-success.png)

###### JWT 授权

- 需要配置 `API_AUTH_ENABLED=true` 开启API授权
- 需要配置 `API_AUTH_TYPE=JWT` （注意大小写）
- 需要配置 `API_AUTH_USERS=user:pass,user1:pass1`，设置允许访问的用户密码对

注意，JWT授权，单独提供了一个用户登录接口，路劲为 `/api/api-user-login`，前面的 `api` 前缀，由配置 `API_PREFIX` 来设置

> JWT 验证失败演示

![JWT 验证失败](screenshots/auth-jwt-failed.png)

> JWT 验证成功演示

![JWT 验证成功](screenshots/auth-jwt-success.png)

> JWT 用户登录成功演示

![用户登录成功](screenshots/auth-jwt-login.png)

> JWT 用户登录失败演示

![用户登录失败](screenshots/auth-jwt-login-failed.png)

#### 扩展开发，TODO

- [x] 接口授权访问，已支持：Basic，JWT
- [ ] 数据表表名安全隐患
- [ ] 自动生成 UI 管理后台

#### 如何联系我？

```text
admin@mrhuo.com
```

#### 开源地址

```text
https://github.com/mrhuo/database2api
```

#### 版权提示

MIT
