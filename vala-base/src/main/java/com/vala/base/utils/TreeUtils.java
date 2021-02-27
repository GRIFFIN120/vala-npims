package com.vala.base.utils;



import com.vala.base.entity.BaseEntity;
import com.vala.base.entity.TreeEntity;

import java.util.*;

public class TreeUtils {
    public static  <O extends TreeEntity> List<O> treeBean(List<O> list){

        List<O> results = new ArrayList();

        Map<Integer, O> map = toMap(list);
        Iterator<Integer> it1 = map.keySet().iterator();
        while (it1.hasNext()){
            Integer id = it1.next();
            O bean = map.get(id);
            Integer pid = bean.getPid();
            if(pid!=null&&pid==0){
                results.add(bean);
            }else if(map.containsKey(pid)){
                O parent = map.get(pid);
                if(parent.getChildren()==null){
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(bean);
            }
        }

        return results;
    }

    public static  <T extends BaseEntity> Map<Integer, T> toMap(List<T> list){
        Map<Integer, T> map = new LinkedHashMap<Integer,T>();
        Iterator<T> it = list.iterator();
        while (it.hasNext()){
            T next = it.next();
            map.put(next.getId(),next);
        }
        return map;
    }
}
