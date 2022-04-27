package fsnl.ims.acm.bills.fsnl_infolist;

import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.earlywarn.warn.EarlyWarnConditionCheckResult;
import kd.bos.entity.earlywarn.warn.plugin.IEarlyWarnConditionForm;
import kd.bos.form.plugin.AbstractFormPlugin;
import org.apache.commons.lang.StringUtils;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 苏俊安
 * @version 1.00
 * @description:信息清单自定义预警条件
 * @ClassName:AlertDaysPlugin.java
 * @Date 2022/2/25 14:48
 */
public class AlertDaysPlugin extends AbstractFormPlugin implements IEarlyWarnConditionForm {

    /**
     * 获取自定义表单参数
     *
     * @return 自定义表单参数
     */
    @Override
    public Map<String, Object> getCustomParams() {
        IDataModel model = getModel();
        Boolean enable = (Boolean) model.getValue("enable");//是否启用该预警天数
        String alertdays = (String) model.getValue("alertdays");//预警天数

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("enable", enable);
        dataMap.put("alertdays", alertdays);
        return dataMap;
    }

    /**
     * 判断预警天数输入是否正确
     *
     * @return
     */
    @Override
    public EarlyWarnConditionCheckResult checkCustomParams() {
        IDataModel model = getModel();
        Boolean enable = (Boolean) model.getValue("enable");//是否启用该预警天数，得到的对象类型还不知道，先看看
        String alertdays = (String) model.getValue("alertdays");//预警天数

        if (enable) {
            if (StringUtils.isBlank(alertdays)) {
                return EarlyWarnConditionCheckResult.failure("请填写预警天数");
            } else {
                //用正则表达式进行测试
                //分割，判断输入的字符是否为数字
                String day = alertdays.replaceAll(" ", "");//去空格
                String[] numofdays = day.split(",");//用,进行分割符
                boolean result = true;
                for (String st : numofdays) {//遍历得到的天数
                    if (isNumericzidai(st)) {
                        result = true;
                    } else {
                        result = false;
                        break;

                    }
                }
                if (result) {
                    return EarlyWarnConditionCheckResult.success("");
                } else {
                    return EarlyWarnConditionCheckResult.failure("输入预警天数有误,请重新填写");
                }
            }

        } else {
            return null;
        }
    }

    @Override
    public void beforeBindData(EventObject e) {
        super.beforeBindData(e);
        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        for (String key : customParams.keySet()) {
            this.getView().getModel().setValue(key, customParams.get(key));
        }
    }


    /**
     * 正则表达式判断输入的是否为数字
     *
     * @param str
     * @return
     */

    public static boolean isNumericzidai(String str) {
        Pattern pattern = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }
}
