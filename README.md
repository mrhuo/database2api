## database2api

#### 什么是 **database2api**？

顾名思义，就是快速、直接的将数据库转化为 API 接口，方便多种应用场景。而且提供了静态文件托管功能，非常实用。

#### **database2api**是使用什么开发的？

使用 [Ktor](https://ktor.io/) + [hutool](https://hutool.cn/) 开发，使用 **Netty** 作为内置服务器，请求速度极快。

#### 支持哪些数据库？

常见的关系数据库如：Sqlite、Oracle、MySQL、PostgreSQL、IBM DB2、Microsoft SQL Server、Sybase、Informix 等数据库都支持。

#### 为什么要创造 **database2api**？

经常有朋友问我，以前有个网站做了很久了，需要更新版本，但是苦于代码耦合太严重，开发新版本工作量太大。

自从有了 **database2api**，直接将数据库连接交于 **database2api**，自动生成全站的 API 接口，再开发独立的 web 界面，新版本的开发问题就迎刃而解了。

#### 已测试数据库

- [x] Sqlite
- [x] MySQL
- [x] PostgreSQL
- [x] Microsoft SQL Server
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

#### 如何联系我？

```text
admin@mrhuo.com
```

#### 版权提示

