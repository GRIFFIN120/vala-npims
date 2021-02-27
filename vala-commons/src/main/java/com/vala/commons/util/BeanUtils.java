package com.vala.commons.util;

import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class BeanUtils {


    public static void simplify(Class simperClass, Object target) throws Exception {
        if(target==null) return;
        List<Field> simperFields = getCompleteFields(simperClass);
        List<Field> targetFields = getCompleteFields(target.getClass());

        for (Field targetField : targetFields) {
            boolean flag = false;
            for (Field simperField : simperFields) {
                if(targetField.getName().equals(simperField.getName())){
                    flag = true;
                    break;
                }
            }
            if(!flag){
                targetField.set(target,null);
            }
        }


    }


    public static List<Field> getFieldsByAnnotation(Class domain, Class annotationClass){
        List<Field> list = new ArrayList<>();
        List<Field> completeFields = BeanUtils.getCompleteFields(domain);
        for (Field f : completeFields) {
            if(f.isAnnotationPresent(annotationClass)){
                f.setAccessible(true);
                list.add(f);
            }
        }
        return list;
    }

    public static List<Field> getLazyFieldInListForm(Class domain){
        List<Field> lzFs = new ArrayList<>();
        List<Field> fs = BeanUtils.getFieldsByAnnotation(domain, ManyToMany.class);
        List<Field> fs1 = BeanUtils.getFieldsByAnnotation(domain, OneToMany.class);
        for (Field f : fs) {
            FetchType fetch = f.getAnnotation(ManyToMany.class).fetch();
            if(fetch.equals(FetchType.LAZY)){
                lzFs.add(f);
            }
        }
        for (Field f : fs1) {
            FetchType fetch = f.getAnnotation(OneToMany.class).fetch();
            if(fetch.equals(FetchType.LAZY)){
                lzFs.add(f);
            }
        }
        return lzFs;
    }




    public static Class<?> getFieldParameterizedType(Field f) {
        f.setAccessible(true);
        Class<?> fieldType = f.getType();
        if (fieldType.equals(List.class)) {
            Type genericType = f.getGenericType();
            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                Class<?> actualTypeArgument = (Class<?>)pt.getActualTypeArguments()[0];
                return actualTypeArgument;
            }
        }
        return null;
    }


    public static Class<?> getFieldParameterizedType(Class domain, String field) {
        try {
            Field f = domain.getDeclaredField(field);
            f.setAccessible(true);
            Class<?> fieldType = f.getType();
            if (fieldType.equals(List.class)) {
                Type genericType = f.getGenericType();
                if (genericType instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType) genericType;
                    Class<?> actualTypeArgument = (Class<?>)pt.getActualTypeArguments()[0];
                    return actualTypeArgument;
                }
            }
        } catch (NoSuchFieldException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }




    public static void set(Object bean, String fieldName, Object value) throws Exception {
        Class domain = bean.getClass();
        Field field = domain.getField(fieldName);
        field.set(bean,value);
    }
    public static Object get(Object bean, String fieldName) throws Exception {
        Class domain = bean.getClass();
        Field field = domain.getField(fieldName);
        return field.get(bean);
    }




    public static String bean2conditon(Object bean) throws Exception {
        if(bean==null||!BeanUtils.isNotEmptyBean(bean)) return "";
        List<String> conds = new ArrayList<>();
        List<Field> fs = getCompleteFields(bean.getClass());
        for (Field f : fs) {
            f.setAccessible(true);
            Object val = f.get(bean);
            if(val!=null){
                if(Number.class.isAssignableFrom(f.getType())){
                    conds.add(f.getName()+"="+val);
                }else if(val instanceof String){
                    conds.add(f.getName()+"='"+val+"'");
                }
            }
        }

        String and = conds.isEmpty()?"":" and ";

        String condition = String.join(" and ", conds);

        return and +  condition;
    }





    public static List<Field> getCompleteFields(Class domain){
        List<Field> list = new ArrayList<Field>();
        load_fields(domain,list);
        return list;
    }

    private static void load_fields(Class domain, List<Field> list){
        Field[] fs = domain.getDeclaredFields();
        for (Field f : fs) {
            list.add(f);
        }
        Class parent_class = domain.getSuperclass();
        if(!Object.class.equals(parent_class)){
            load_fields(parent_class,list);
        }
    }

    public static void extend(Class domain, Object target, Object source ) {
        extend(domain,target,source,null);
    }

    /**
     * 用ext的属性覆盖bean 的
     * @param domain
     * @param target
     * @param source
     */
    public static void extend(Class domain, Object target, Object source, Class annoClass ) {
        Field[] fs = domain.getDeclaredFields();

        for(Field f:fs){
            if(annoClass!=null&&f.isAnnotationPresent(annoClass)) {
                continue;
            }

            f.setAccessible(true);
            try {
                Object val = f.get(source);
                if(val!=null){
                    f.set(target,val);
                }
            } catch (Exception e) {
                continue;
            }
        }
        Class pcls = domain.getSuperclass();
        if(!Object.class.equals(pcls)){
            extend(pcls, target, source, annoClass);
        }
    }


    public static <T> T  clone(Class<T> domain, T bean) throws Exception {
        T copy = domain.newInstance();
        List<Field> completeFields = getCompleteFields(domain);
        for (Field field : completeFields) {
            field.setAccessible(true);
            Object val = field.get(bean);
            if(val!=null){
                field.set(copy,val);
            }
        }
        return copy;
    }



    public static void printBean(Object obj){
        Class c = obj.getClass();
        System.out.println("----"+c.getName()+"-----");
        List<Field> completeFields = getCompleteFields(c);
        for (Field f : completeFields) {
            f.setAccessible(true);
            String name = f.getName();
            try {
                Object value = f.get(obj);
                if(value!=null)
                    System.out.println(name+" = "+ value);
            } catch (IllegalAccessException e) {
                System.out.println(name+" !!! "+ e.getMessage());
            }
        }
        System.out.println("---------");

    }

    public static boolean isNotEmptyBean(Object bean)  {
        boolean flag = false;
        if(bean!=null){
            Class domain = bean.getClass();
            List<Field> fields  = getCompleteFields(domain);
            for (Field field : fields) {
                field.setAccessible(true);
                Object val = null;
                try {
                    val = field.get(bean);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                if(val!=null){
                    flag = true;
                    break;
                }
            }
        }
        return flag;
    }


}
