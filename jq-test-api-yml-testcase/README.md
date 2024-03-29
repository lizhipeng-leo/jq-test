#   介绍
    1、通用的接口测试框架，测试的host和账号密码相关通过maven导入具体的module
    2、测试用例使用yml编写
    3、测试报告使用Allure
    4、运行mvn test 
        可选参数：
            -Dspring.profiles.active={环境} 
            -Dtest.file.name={className}
            -Dtest.name={methodName}       

#   测试用例编写

##  定义

####    Suite

    整个测试套件，包含所有的测试
    
####    class

    一个yml文件为一个testClass
|字段名称|描述|数据类型
|---|---|---|
|name|class名称|String
|dataProvider|测试数据|List\<Map>
|invocationCount|运行次数|int
|beforeSuite|所有测试运行前运行|TestMethod
|beforeClass|此class下所有测试运行前运行|TestMethod
|beforeMethod|此class每个测试前运行|TestMethod
|testMethod|测试方法|List\<TestMethod>
|afterSuite|所有测试运行后运行|TestMethod
|afterClass|此class下所有测试运行后运行|TestMethod
|afterMethod|此class每个测试后运行|TestMethod

####    testMethod

    一个class下可以有多个testMethod
    
|字段名称|描述|数据类型
|---|---|---|
|name|测试名称|String
|dataProvider|测试数据|List\<Map>
|invocationCount|运行次数|int
|step|测试步骤|testStep
    
####    testStep

    一个testMethod下可以有多个testStep
|字段名称|描述|数据类型
|---|---|---|
|name|步骤名称|String
|byName|通过step名称复制step|String
|dataProvider|测试数据|List\<Map>
|host|主机地址（配置文件中默认配置，可修改）|String
|url|请求地址|String
|variables|url参数|Map
|method|请求方式|String
|headers|请求头|Map
|form|form表单|Map
|file|所要上传的文件|key:(默认file)form表单的key path:文件路径
|body|请求体|String
|assertion|判断|Assertion
|save|保存参数|SaveParam

###### example

      - name: 查询客户Id
        url: /api/v1/customer/oldNewV2
        variables:
          param:
          current: 1
          size: 50
        headers:
          Authorization: ${token}
        method: GET
        assertion:
          - key: $.code
            value: 10001
        save:
          - value: $.data..customerId
            name: customerId

####    Assertion

|字段名称|描述|数据类型
|---|---|---|
|type|判断的数据类型(默认为响应体)|DataType
|valueType|值的类型（可不填，当值的类型不明确时填写）|ValueType
|assertionType|判断类型（EQ,CONTAINS等）|AssertionType
|key|获取数据的key，比如jsonPath（$.msg）|String
|value|预期结果|Object

###### example

    assertion:
      - key: $.code
        value: 10000
      - key: $.msg
        value: 操作成功

####    SaveParam

|字段名称|描述|数据类型
|---|---|---|
|saveType|保存数据在哪里，默认class(可选step、method、class、suite)|SaveType
|type|数据源类型，默认json|DataType
|data|数据源，默认响应体|String
|name|保存值的key|String
|value|获取值的方法，比如jsonPath（$.data.id）|String

###### example

    save:
        #如果为数组，则随机取一个
      - value: $.data..customerId
        name: customerId

####    beforeSuite

    所有测试之前运行，只运行1次
    格式同testMethod

####    beforeClass

    当前yml文件下的所有测试之前运行
    格式同testMethod
    
####    AfterSuite

    有测试运行完成之后运行
    格式同testMethod

####    AfterClass

    当前yml文件下的所有测试运行完成之后运行
    格式同testMethod
    
####    dataProvider

    测试数据，List<Map>数组，list长度即为测试运行的次数
    在class下定义，会实例化多个class
    在testMethod下定义，会实例化多个testMethod
    在testStep下定义，会实例化多个testStep
    
#### 相关参考资料

    JSONPath: https://www.cnblogs.com/aoyihuashao/p/8665873.html
    
    Ymal：https://www.jianshu.com/p/97222440cd08