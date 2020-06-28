package sliming.aop;


import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import sliming.annotation.SlimColumns;

@Aspect
@Service
public class FilterResponseColumnsAdvice {

    @Pointcut("@annotation(sliming.annotation.SlimColumns)")
    private void pointcut() {
    }

    @Around(value = "pointcut()&&@annotation(slimColumns)")
    public Object afterExec(ProceedingJoinPoint joinPoint, SlimColumns slimColumns) throws Throwable {
        Object ret = joinPoint.proceed();
        try {
            String excludeJson = slimColumns.excludeData();
            Map ignoreMap = JSON.parseObject(excludeJson, Map.class);
            removeColumns(ret, ignoreMap);
        } catch (Exception e) {
            System.out.println("AfterExec error!" + e);
        }
        return ret;
    }

    private void removeColumns(Object data, Map ignoreMap) throws Exception {
        Set set = ignoreMap.keySet();
        if (set == null || set.size() == 0 || data == null) {
            return;
        }
        if (data instanceof Collection) {
            for (Object d : ((Collection) data)) {
                removeColumns(d, ignoreMap);
            }
        } else {
            for (Object key : set) {
                Field field = null;
                Class clazz = data.getClass();
                while (field == null && clazz != null) {
                    try {
                        field = clazz.getDeclaredField((String) key);
                    } catch (NoSuchFieldException ignore) {
                    }
                    clazz = clazz.getSuperclass();
                }
                if (field != null) {
                    Object value = ignoreMap.get(key);
                    if (value != null) {
                        field.setAccessible(true);
                        removeColumns(field.get(data), (Map) value);
                    } else {
                        //判断是不是基本类型
                        field.setAccessible(true);
                        if (field.getType().getName().equals("boolean")) {
                            field.set(data, false);
                        } else if (field.getType().getName().equals("char")) {
                            field.set(data, ' ');
                        } else if (field.getType().isPrimitive()) {
                            field.set(data, (byte) 0);
                        } else {
                            field.set(data, null);
                        }
                    }
                } else {
                    throw new NoSuchFieldException((String) key);
                }
            }
        }
    }
}

