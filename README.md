本项目用于解决将接口返回的字段设置为null，且不影响原有代码逻辑

## 接口字段过滤/瘦身

### 一、接入方案

##### 1、初始化bean

```
@Bean
public SlimingColumnsAop slimingColumnsAop() {
    return new SlimingColumnsAop();
}
```

##### 2、初始化bean
接口添加注解@SlimColumns
```
@PostMapping("test")
@SlimColumns(excludeData = "{\"students\":{\"age\":null},\"classNo\":null}")
public List<Class> getAllClass(){
    //业务逻辑
}
```

### 二、举例

class 类
```
@Data
public class Class {
 private List<Student> students;
 private int grade;
 private Long classNo;
}
```

student 类
```
@Data
public class Student {
 private int age;
 private String gender;
 private <Course> courses;
}
```

course 类
```
@Data
public class Course {
 private Long courseNo;
 private String name;
}
```

配置

`{\"students\":{\"age\":null},\"classNo\":null}`

响应

```
[{
     "students":[
     {
         "age":0,
         "gender":"male",
         "courses":[{"courseNo":999,"name":"math"},{"courseNo":998,"name":"English"}]
     }]
 },
 {
      "students":[
      {
          "age":0,
          "gender":"female",
          "courses":[{"courseNo":999,"name":"math"},{"courseNo":998,"name":"English"}]
      },
      {
        "age":0,
        "gender":"female",
        "courses":[{"courseNo":999,"name":"math"},{"courseNo":998,"name":"English"}]
    }]
  }]
```

P.S. 基本类型设置为null后，会设置为默认值（char为' '）
