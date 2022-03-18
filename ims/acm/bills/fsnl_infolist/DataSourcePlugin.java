package fsnl.ims.acm.bills.fsnl_infolist;

import kd.bos.entity.earlywarn.EarlyWarnContext;
import kd.bos.entity.filter.FilterCondition;
import kd.bos.orm.query.QFilter;
import kd.bos.service.earlywarn.impl.DefaultEarlyWarnBillDataSource;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 苏俊安
 * @version 1.00
 * @description:信息清单自定义数据源
 * @ClassName:DataSourcePlugin.java
 * @Date 2022/2/25 15:09
 */
public class DataSourcePlugin extends DefaultEarlyWarnBillDataSource {

    /**
     * 构建过滤数据源
     *
     * @param dataSource      数据源
     * @param filterCondition 过滤条件
     * @param context         引擎执行上下文
     * @return 过滤条件
     */
    public List<QFilter> buildFilter(String dataSource, FilterCondition filterCondition, EarlyWarnContext context) {
        List<QFilter> filters = super.buildFilter(dataSource, filterCondition, context);
        Map<String, Object> customConditionMap = context.getCustomConditionDataMap();//得到自定义条件参数

        String alertdays = (String) customConditionMap.get("alertdays");

        String tian = alertdays.replaceAll(" ", "");//去空格
        String[] str = tian.split(",");//用,进行分割符
        List<Date> timeList = new ArrayList<>();//时间集合
        for (String st : str) {//遍历得到的天数
            //获取今天之前的日期
            int day = Integer.parseInt(st);
            //得到几天前的时间
            Calendar now = Calendar.getInstance();//获取了当前日期和时间
            now.set(Calendar.DATE, now.get(Calendar.DATE) - day);//得到当前日期的前几天
            Date yujingtime = now.getTime();//得到预警时间

            //修改日期格式
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
            try {
                timeList.add(simpleDateFormat.parse(simpleDateFormat.format(yujingtime)));
            } catch (ParseException e) {
                e.printStackTrace();

            }
        }

        QFilter filter = new QFilter("fsnl_fenddate", "in", timeList);

        filters.add(filter);
        return filters;
    }
}
