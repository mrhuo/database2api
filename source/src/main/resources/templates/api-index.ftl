<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>database2api api index</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        ul, li, dl, ol {
            list-style: none;
        }

        a {
            text-decoration: none;
            color: rgb(81, 130, 187);
            transition: all 0.3s;
        }

        a:hover {
            color: darkblue;
        }

        html, body {
            width: 100%;
            height: 100%;
            overflow: hidden;
        }

        span.tag {
            font-size: 10px;
            background: darkorange;
            color: white;
            border-radius: 4px;
            margin-left: 8px;
            padding: 2px 4px;
        }

        table {
            width: 100%;
        }

        table, td, th {
            border-collapse: collapse;
        }

        table thead th {
            background-color: rgb(81, 130, 187);
            color: #fff;
            border-bottom-width: 0;
        }

        /* Column Style */
        table td {
            color: #000;
        }

        /* Heading and Column Style */
        table tr, table th {
            border-width: 1px;
            border-style: solid;
            border-color: rgb(81, 130, 187);
        }

        /* Padding and font style */
        table td, table th {
            padding: 5px 10px;
            font-size: 12px;
            font-weight: bold;
        }

        .container {
            display: flex;
            flex-direction: row;
            width: 100%;
            height: 100%;
        }

        .container .sidebar {
            width: 16%;
            min-width: 200px;
            background: #e5e5e5;
            overflow-y: auto;
            padding: 16px;
            display: flex;
            flex-direction: column;
        }

        .container .sidebar h1 {
            font-size: 24px;
            font-weight: bold;
            text-align: center;
            display: block;
            margin: 0 10px 10px 10px;
        }

        .container .sidebar .api-list {
            flex: 1;
            overflow-y: auto;
        }

        .container .sidebar .api-list .api-item {
            display: block;
            font-size: 14px;
            line-height: 28px;
        }

        .container .sidebar .api-list .api-item:before {
            content: '▶';
            padding-right: 5px;
        }

        .container .sidebar .api-copyright {
            font-size: 12px;
            color: gray;
            display: block;
            text-align: center;
        }

        .container .content {
            flex: 1;
            overflow-y: auto;
            padding: 16px;
            position: relative;
        }

        .container .content table {
            margin-top: 10px;
            margin-bottom: 25px;
        }

        .container .content .test-api-button {
            font-size: 12px;
            background: rgb(81, 130, 187);
            color: white;
            padding: 4px 12px;
            border-radius: 4px;
            border-width: 0;
            cursor: pointer;
        }

        .container .content .test-api-button:hover {
            background: rgba(81, 130, 187, 0.8);
        }

        .container .content .mask {
            display: none;
            position: fixed;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
        }

        .container .content .mask:before {
            content: ' ';
            display: block;
            position: absolute;
            left: 0;
            top: 0;
            opacity: 0.6;
            width: 100%;
            height: 100%;
            background: #000000;
            z-index: 101;
        }

        .test-api-container {
            width: 94vw;
            height: 94vh;
            background: white;
            border-radius: 10px;
            display: flex;
            opacity: 1;
            left: 3vw;
            top: 3vh;
            position: absolute;
            z-index: 102;
            padding: 10px;
            flex-direction: column;
            overflow: hidden;
        }

        .test-api-container header {
            display: flex;
            flex-direction: row;
            text-align: center;
            padding: 0 10px 10px 10px;
            border-bottom: 1px solid #dddddd;
            margin-bottom: 10px;
        }

        .test-api-container header b {
            flex: 1;
            font-size: 16px;
            padding-top: 10px;
        }

        .test-api-container header .dialog-close-btn {
            font-size: 18px;
            user-select: none;
            -webkit-user-select: none;
            cursor: pointer;
            padding: 4px 8px;
        }

        .test-api-container header .dialog-close-btn:hover {
            background: #f0f0f0;
        }

        .test-api-container main {
            flex: 1;
            display: flex;
            max-height: calc(94vh - 55px);
        }

        .test-api-container ul {
            width: 300px;
            overflow-y: auto;
            max-height: 100%;
        }

        .test-api-container ul li {
            background: #f2f2f2;
            margin-bottom: 12px;
            border-radius: 6px;
            padding: 8px;
        }

        .test-api-container ul li:hover {
            background: #e2e2e2;
            cursor: pointer;
        }

        .test-api-container ul li b {
            font-weight: bold;
            display: block;
            font-size: 14px;
            color: #333;
        }

        .test-api-container ul li p {
            display: block;
            font-size: 12px;
            color: #999;
            margin-top: 5px;
        }

        .test-api-container .test-api-content {
            flex: 1;
            padding: 10px;
            margin-left: 5px;
        }

        .fieldset-group {
            border-color: #eee;
            font-size: 14px;
            padding: 10px;
            border-radius: 4px;
        }

        .fieldset-group + .fieldset-group {
            margin-top: 10px;
        }

        .fieldset-group legend {
            margin-left: 15px;
            padding: 0 10px;
        }
    </style>
</head>
<body>
<div class="container">
    <div class="sidebar">
        <h1>database2api</h1>
        <div class="api-list">
            <#list tables as table>
                <a class="api-item" href="#${table.tableName}">${table.tableName}</a>
            </#list>
        </div>
        <div class="api-copyright">
            generated by <a href="https://github.com/mrhuo/database2api" target="_blank">database2api</a>
        </div>
    </div>
    <div class="content">
        <#list tables as table>
            <a name="${table.tableName}"></a>
            <h3><b>${table.tableName}</b>表结构</h3>
            <table border="1">
                <thead>
                <tr>
                    <th width="260">列名</th>
                    <th width="100">类型</th>
                    <th width="100">长度</th>
                    <th width="100">是否为空</th>
                    <th>备注</th>
                </tr>
                </thead>
                <tbody>
                <#list table.columns as column>
                    <tr>
                        <td>
                            ${column.name}
                            <#if column.autoIncrement>
                                <span class="tag">自增</span>
                            </#if>
                            <#if column.pk>
                                <span class="tag">主键</span>
                            </#if>
                        </td>
                        <td>${column.typeName}</td>
                        <td>${column.size}</td>
                        <td>${column.nullable?c}</td>
                        <td>${column.comment!''}</td>
                    </tr>
                </#list>
                </tbody>
            </table>
            <h3>
                <b>${table.tableName}</b>公开API
<#--                <button data-table="${table.tableName}" data-columns='${Json.toJson(table.columns)}'-->
<#--                        class="test-api-button">测试API-->
<#--                </button>-->
            </h3>
            <table border="1">
                <thead>
                <tr>
                    <th width="260">API</th>
                    <th width="100">请求方式</th>
                    <th>备注</th>
                </tr>
                </thead>
                <tbody>
                <tr>
                    <td>/api/${table.tableName}</td>
                    <td>POST</td>
                    <td>增加数据</td>
                </tr>
                <tr>
                    <td>/api/${table.tableName}/{id}</td>
                    <td>DELETE</td>
                    <td>删除数据</td>
                </tr>
                <tr>
                    <td>/api/${table.tableName}</td>
                    <td>PUT</td>
                    <td>更新数据</td>
                </tr>
                <tr>
                    <td>/api/${table.tableName}/paged</td>
                    <td>GET</td>
                    <td>分页获取数据，支持参数 columns，page，limit，orderBy，sort</td>
                </tr>
                <tr>
                    <td>/api/${table.tableName}/all</td>
                    <td>GET</td>
                    <td>获取所有数据，支持参数 columns，orderBy，sort</td>
                </tr>
                <tr>
                    <td>/api/${table.tableName}/{id}</td>
                    <td>GET</td>
                    <td>获取单条数据</td>
                </tr>
                </tbody>
            </table>
        </#list>

        <div class="mask"></div>
    </div>
</div>
<#--<script type="text/html" id="testApiDialogTemplate">-->
<#--    <div class="test-api-container">-->
<#--        <header>-->
<#--            <b>database2api 接口测试工具</b>-->
<#--            <span class="dialog-close-btn">×</span>-->
<#--        </header>-->
<#--        <main>-->
<#--            <ul>-->
<#--                <li data-method="POST" data-url="/api/{{tableName}}">-->
<#--                    <b>POST /api/{{tableName}}</b>-->
<#--                    <p>增加数据</p>-->
<#--                </li>-->
<#--                <li data-method="DELETE" data-url="/api/{{tableName}}/{id}">-->
<#--                    <b>DELETE /api/{{tableName}}/{id}</b>-->
<#--                    <p>删除数据</p>-->
<#--                </li>-->
<#--                <li data-method="PUT" data-url="/api/{{tableName}}">-->
<#--                    <b>PUT /api/{{tableName}}</b>-->
<#--                    <p>更新数据</p>-->
<#--                </li>-->
<#--                <li data-method="GET" data-url="/api/{{tableName}}/paged">-->
<#--                    <b>GET /api/{{tableName}}/paged</b>-->
<#--                    <p>分页获取数据，支持参数 columns，page，limit，orderBy，sort</p>-->
<#--                </li>-->
<#--                <li data-method="GET" data-url="/api/{{tableName}}/all">-->
<#--                    <b>GET /api/{{tableName}}/all</b>-->
<#--                    <p>获取所有数据，支持参数 columns，orderBy，sort</p>-->
<#--                </li>-->
<#--                <li data-method="GET" data-url="/api/{{tableName}}/{id}">-->
<#--                    <b>GET /api/{{tableName}}/{id}</b>-->
<#--                    <p>获取单条数据</p>-->
<#--                </li>-->
<#--            </ul>-->
<#--            <div class="test-api-content" style="display: none">-->
<#--                <fieldset class="fieldset-group">-->
<#--                    <legend>请求 URL</legend>-->
<#--                    <div class="test-api-req-url"></div>-->
<#--                </fieldset>-->
<#--                <fieldset class="fieldset-group input">-->
<#--                    <legend>输入数据</legend>-->
<#--                    <div class="test-api-form"></div>-->
<#--                </fieldset>-->
<#--                <fieldset class="fieldset-group result">-->
<#--                    <legend>输出结果</legend>-->
<#--                    <div class="test-api-result"></div>-->
<#--                </fieldset>-->
<#--            </div>-->
<#--        </main>-->
<#--    </div>-->
<#--</script>-->
<#--<script type="text/html" id="testApiFormTemplate">-->
<#--    <div class="form">-->

<#--    </div>-->
<#--    <div>-->
<#--        <button>请求 API</button>-->
<#--    </div>-->
<#--</script>-->
<script>
    // let API = (function () {
    //     let get = function () {
    //     };
    //     let post = function () {
    //     };
    //     let put = function () {
    //     };
    //     let del = function () {
    //     };
    //     return {
    //         get,
    //         post,
    //         put,
    //         delete: del
    //     };
    // })();
    //
    // function buildInputForm(method, url, columns) {
    //     let formTemplate = document.querySelector('#testApiFormTemplate').innerHTML;
    //     return formTemplate;
    // }
    //
    // function showTestApiDialog(tableName, columns) {
    //     let mask = document.querySelector('.mask');
    //     let dialogTemplate = document.querySelector('#testApiDialogTemplate').innerHTML;
    //     dialogTemplate = dialogTemplate.replaceAll("{{tableName}}", tableName);
    //     mask.innerHTML = dialogTemplate;
    //     mask.style.display = "block";
    //     let dialogCloseBtn = mask.querySelector('.dialog-close-btn');
    //     dialogCloseBtn.onclick = function () {
    //         mask.style.display = "none";
    //         mask.innerHTML = "";
    //     }
    //
    //     let lis = mask.querySelectorAll("li");
    //     lis.forEach((li, index) => {
    //         li.onclick = function () {
    //             console.log(index);
    //             let method = this.dataset.method;
    //             let url = this.dataset.url;
    //             let testApiContent = mask.querySelector('.test-api-content');
    //             testApiContent.style.display = "block";
    //
    //             let testApiReqUrl = mask.querySelector('.test-api-req-url');
    //             testApiReqUrl.innerHTML = '<span class="tag">' + method + '</span> ' + url;
    //
    //             let testApiInputForm = mask.querySelector('.fieldset-group.input');
    //             testApiInputForm.querySelector('.test-api-form').innerHTML = buildInputForm(method, url, columns);
    //
    //             let testApiResult = mask.querySelector('.fieldset-group.result');
    //             // testApiResult.querySelector('.test-api-result').innerHTML
    //         };
    //     });
    // }
    //
    // document.querySelectorAll('.test-api-button').forEach(it => {
    //     let tableName = it.dataset.table;
    //     let columns = it.dataset.columns;
    //     it.onclick = function () {
    //         showTestApiDialog(tableName, JSON.parse(columns));
    //     };
    // });
</script>
</body>
</html>