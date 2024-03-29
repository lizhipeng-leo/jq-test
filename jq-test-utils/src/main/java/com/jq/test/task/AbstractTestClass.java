package com.jq.test.task;

import com.jq.test.utils.TestUtils;
import lombok.Data;
import org.apache.jmeter.engine.util.CompoundVariable;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.threads.JMeterVariables;

import java.util.*;

@Data
public abstract class AbstractTestClass implements ITestClass {
    private String name;
    private List<ITestMethod> testMethods = new ArrayList<>();
    public Map<String, String> params = new LinkedHashMap<>();
    private ITestSuite testSuite;
    private List<ITestMethod> beforeClass = new ArrayList<>();
    private List<ITestMethod> afterClass = new ArrayList<>();
    private List<ITestMethod> before = new ArrayList<>();
    private List<ITestMethod> after = new ArrayList<>();

    @Override
    public void setUp() {
        for (ITestMethod testMethod : before) {
            testMethod.doing();
        }
    }

    @Override
    public synchronized void setUpBeforeClass() {
        //执行测试前，先生成参数
        LinkedHashMap<String, String> map = new LinkedHashMap<>(params);
        for (Map.Entry<String, String> entry : map.entrySet()) {
            JMeterContextService.getContext().setVariables(new JMeterVariables());
            params.put(entry.getKey(), new CompoundVariable(replace(entry.getValue())).execute());
            addParams(JMeterContextService.getContext().getVariables());
        }
        for (ITestMethod testMethod : beforeClass) {
            testMethod.doing();
        }
    }

    @Override
    public void tearDown() {
        for (ITestMethod testMethod : after) {
            testMethod.doing();
        }
    }

    @Override
    public void tearDownAfterClass() {
        for (ITestMethod testMethod : afterClass) {
            testMethod.doing();
        }
    }

    @Override
    public void addParams(JMeterVariables jMeterVariables) {
        if (jMeterVariables != null) {
            Iterator<Map.Entry<String, Object>> iterator = jMeterVariables.getIterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> map = iterator.next();
                getParams().put(map.getKey(), map.getValue().toString());
            }
        }
    }

    @Override
    public void save(String key, String value) {
        params.put(key, value);
    }

    @Override
    public String replace(String content) {
        return TestUtils.replace(content, getTestSuite(), params, 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractTestClass that = (AbstractTestClass) o;
        return this.hashCode() == that.hashCode();
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, testMethods, params, testSuite.getParams(), beforeClass, afterClass);
    }

    @Override
    public String toString() {
        return name;
    }
}
