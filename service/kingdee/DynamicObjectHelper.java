package fsnl.service.kingdee;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：苍穹数据对象帮助类
 * @ClassName：DynamicObjectHelper.java
 * @Date 2022/3/15 15:03
 */
public class DynamicObjectHelper {
    /**
     * 获取对象
     * @param numbers 对象编码
     * @return
     */
    public static DynamicObject getDynamicObject(String entityName, String numbers){
        List<String> ids = new ArrayList<>();
        ids.add(numbers);
        List<Object> pkIds = getPkIdByNumbers(entityName, ids);
        if (!"0".equals(numbers))
            return BusinessDataServiceHelper.loadSingle(pkIds.get(0).toString(), entityName);
        return null;
    }

    /**
     * 根据编码集合获取主键id集合
     * @param entityName 表单标识
     * @param numbers 编码集合
     * @return
     */
    public static List<Object> getPkIdByNumbers(String entityName,List<String> numbers){
        QFilter[] qFilters = {new QFilter("number", "in", numbers)};
        List<Object> ids = QueryServiceHelper.queryPrimaryKeys(entityName, qFilters, null, numbers.size());
        return ids;
    }

    /**
     * 根据编码获取主键id
     * @param entityName 表单标识
     * @param number 编码
     * @return
     */
    public static String getPkIdByNumber(String entityName,String number){
        QFilter[] qFilters = {new QFilter("number", "=", number)};
        List<Object> ids = QueryServiceHelper.queryPrimaryKeys(entityName, qFilters, null, 1);
        if (ids.size()!=0)
            return ids.get(0).toString();
        return null;
    }
}
