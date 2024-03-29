package com.jq.test.utils;

import com.alibaba.fastjson.JSONObject;
import com.jayway.jsonpath.Option;
import com.jq.test.json.JsonPathUtils;
import io.qameta.allure.Allure;
import lombok.Getter;
import lombok.Setter;
import org.assertj.core.api.Assertions;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collection;

@Getter
@Setter
public class Assertion {
    /**
     * 数据来源
     */
    private DataType type = DataType.DEFAULT;
    /**
     * 值的类型
     */
    private ValueType valueType = ValueType.DEFAULT;
    /**
     * 判断类型
     */
    private AssertionType assertionType = AssertionType.EQ;
    /**
     * 值的key
     */
    private String key;
    /**
     * 期望值
     */
    private Object value;
    /**
     * json提取参数设置
     */
    private Option[] options = new Option[]{};

    /**
     * 判断响应是否正确
     *
     * @param entity 响应
     * @param <T>    响应体类型
     */
    public <T> void check(ResponseEntity<T> entity) {
        Object actual = buildActual(entity);
        Object value = buildExpect(actual);
        assertion(actual, value);
    }

    /**
     * 生成期望值
     *
     * @param actual 实际值
     * @return 期望值
     */
    private Object buildExpect(Object actual) {
        Object value;
        //根据值的类型转换类型
        switch (valueType) {
            case BIGDECIMAL:
                value = buildBigDecimal(this.value);
                break;
            case INTEGER:
                value = buildInteger(this.value);
                break;
            case STRING:
                value = this.value.toString();
                break;
            case DEFAULT:
            default:
                //默认状态下，类型与实际值保持一致
                if (actual instanceof Double) {
                    value = new BigDecimal(this.value.toString()).doubleValue();
                } else if (actual instanceof Integer) {
                    value = new BigDecimal(this.value.toString()).intValue();
                } else {
                    value = this.value;
                }
                break;
        }
        return value;
    }

    /**
     * 生成响应返回的值
     *
     * @param entity 响应实体
     * @param <T>    响应体类型
     * @return 实际值
     */
    private <T> Object buildActual(ResponseEntity<T> entity) {
        Object actual = null;
        //根据数据来源提取响应的实际值
        switch (type) {
            case JSON:
            case DEFAULT:
                T body = entity.getBody();
                assert body != null;
                actual = JsonPathUtils.read(body, key, options);
                break;
            case HEADER:
                String[] split = value.toString().split(":");
                actual = entity.getHeaders().get(split[0]);
                setAssertionType(AssertionType.CONTAINS);
                value = split[1];
                break;
            case STATUS:
                actual = entity.getStatusCode().value();
                break;
            case XML:
            default:
                break;
        }
        //根据值的类型转换类型
        switch (valueType) {
            case BIGDECIMAL:
                assert actual != null;
                actual = buildBigDecimal(actual);
                break;
            case INTEGER:
                assert actual != null;
                actual = buildInteger(actual);
                break;
            case STRING:
            case DEFAULT:
            default:
                if (actual instanceof BigDecimal) {
                    actual = ((BigDecimal) actual).doubleValue();
                }
                break;
        }
        return actual;
    }

    private Object buildInteger(Object actual) {
        if (actual instanceof Collection) {
            actual = ((Collection) actual).stream().map(o -> Integer.valueOf(o.toString())).toArray();
        } else {
            actual = Integer.valueOf(actual.toString());
        }
        return actual;
    }

    private Object buildBigDecimal(Object actual) {
        if (actual instanceof Collection) {
            actual = ((Collection) actual).stream().map(o -> new BigDecimal(o.toString()).doubleValue()).toArray();
        } else {
            actual = new BigDecimal(actual.toString()).doubleValue();
        }
        return actual;
    }

    /**
     * 判断实际值与期望值是否一致
     *
     * @param actual 实际值
     * @param value  期望值
     */
    private void assertion(Object actual, Object value) {
        Allure.step("判断结果:" + actual + " -> " + value, () -> {
            //根据不同的判断类型进行判断
            switch (assertionType) {
                case EQ:
                    //null转换为空字符串进行判断
                    Assertions.assertThat(actual == null ? "" : actual)
                            .as(key + "应该为：" + value)
                            .isEqualTo(value == null ? "" : value);
                    break;
                case CONTAINS:
                    if (actual instanceof Collection) {
                        Assertions.assertThat((Collection) actual).contains(value);
                    } else {
                        Assertions.assertThat(actual.toString()).contains(value.toString());
                    }
            }
        });
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }
}
