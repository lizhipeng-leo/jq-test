package com.jq.test.entity;

import com.jq.test.task.YmlTestMethod;
import com.jq.test.task.ITestClass;
import com.jq.test.task.ITestMethod;
import com.jq.test.utils.FieldCheckFactory;
import com.jq.test.utils.TestUtils;
import lombok.Data;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class YmlTestMethodEntity {
    /**
     * 参数化
     */
    private List<LinkedHashMap<String, String>> dataProvider = new ArrayList<>();
    /**
     * 测试名称
     */
    private String name;
    /**
     * 需求地址
     */
    private String stories;
    /**
     * 测试对应bug地址
     */
    private String bug;
    /**
     * 测试步骤
     */
    private List<YmlHttpStepEntity> step = new ArrayList<>();
    /**
     * 执行次数
     */
    private int invocationCount = 1;
    /**
     * 字段检查构造工厂
     */
    private List<FieldCheckFactory> fieldCheck = new ArrayList<>();

    /**
     * @param testClass 测试类
     * @return 测试方法
     */
    public List<ITestMethod> build(ITestClass testClass) {
        if (dataProvider.isEmpty()) {
            dataProvider = Collections.singletonList(new LinkedHashMap<>());
        }
        if (fieldCheck.isEmpty()) {
            fieldCheck = Collections.singletonList(new FieldCheckFactory());
        }
        List<ITestMethod> result = new ArrayList<>();
        for (int i = 0; i < invocationCount; i++) {
            for (Map<String, String> data : dataProvider) {
                for (FieldCheckFactory factory : fieldCheck) {
                    YmlTestMethod testMethod = buildTestMethod(testClass, data, i, factory);
                    result.add(testMethod);
                }
            }
        }
        return result;
    }

    /**
     * @param testClass 测试类
     * @param data      参数
     * @param i         第几次执行
     * @param factory   字段检查构造工厂
     * @return 测试方法
     */
    private YmlTestMethod buildTestMethod(ITestClass testClass, Map<String, String> data, Integer i, FieldCheckFactory factory) {
        Map<String, String> map = new LinkedHashMap<>(data);
        map.put("methodNum", i.toString());
        YmlTestMethod testMethod = new YmlTestMethod();
        testMethod.setTestClass(testClass);
        testMethod.setParams(map);
        testMethod.setBug(this.bug);
        testMethod.setStories(this.stories);
        String name = TestUtils.firstNonEmpty(factory.getName(), factory.getMsg()).orElse("");
        if (name.isEmpty()) {
            testMethod.setName(this.name);
        } else {
            testMethod.setName(this.name + "-" + name);
        }
        testMethod.setTestSteps(step.parallelStream().flatMap(entity
                -> entity.build(testMethod, factory).stream()).collect(Collectors.toList()));
        return testMethod;
    }
}
