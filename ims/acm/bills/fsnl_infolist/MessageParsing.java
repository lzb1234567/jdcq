package fsnl.ims.acm.bills.fsnl_infolist;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.earlywarn.EarlyWarnContext;
import kd.bos.entity.earlywarn.kit.StringTemplateParser;
import kd.bos.entity.earlywarn.warn.plugin.IEarlyWarnMessageCompiler;
import kd.bos.ksql.util.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 苏俊安
 * @version 1.00
 * @description:自定义消息解析
 * @ClassName:MessageParsing.java
 * @Date 2022/3/18 8:58
 */
public class MessageParsing implements IEarlyWarnMessageCompiler {

    /**
     * 构建单个消息
     * @param expression 表达式
     * @param fields 参数列表
     * @param data 数据
     * @param context 预警引擎执行上下文
     * @return 转换后的消息
     */
    @Override
    public String getSingleMessage(String expression, List<String> fields, DynamicObject data, EarlyWarnContext context) {
        if(null == expression || null == data){
            return "";
        }

        Map<String,String> macroMap = new HashMap<>();
        for (String field: fields) {//遍历自定义解析
            String value = "";
            if("fsnl_fremark".equals(field)){
                String pkid=data.getString("id");
                String url=System.getProperty("domain.contextUrl");
                int i = url.length();
                String lastStr=url.substring(i-1);
               /* if("/".equals(lastStr)){//不确定domain.contextUrl是不是已/结尾
                    value = "\t\n"+"链接地址："+url+"mobile.html?pkId="+pkid+"&form=fsnl_infolist_mob"+"\t\n"+"url:"+url+"\t\n"+"i:"+i+"\t\n"+"lastStr:"+lastStr;//自定义字段内容
                }else {
                    value = "\t\n" + "链接地址：" + url + "/mobile.html?pkId=" + pkid + "&form=fsnl_infolist_mob"+"\t\n"+"url:"+url+"\t\n"+"i:"+i+"\t\n"+"lastStr:"+lastStr;//自定义字段内容
                }*/
               value="\t\n" + "链接地址：" +"data-center-fat.jinduo.com/ierp/mobile.html?pkId="+ pkid + "&form=fsnl_infolist_mob";
            }else{
                String[] arr = StringUtil.split(field,".");
                Object objValue = getValue(data, arr);
                value = objValue == null ? "" : objValue.toString();
            }
            macroMap.put(field, value);//参数，参数内容放到macroMap集合
        }


        StringTemplateParser parser = new StringTemplateParser();
        return parser.parse(expression,macroName->macroMap.get(macroName));//返回字段名→字段内容
    }


    @Override
    public String getMergeMessage(String s, List<String> list, EarlyWarnContext earlyWarnContext) {
        return null;
    }

    /**
     *
     * @param data 动态对象
     * @param arr 数组
     * @return 字段内容
     */
    private String getValue(DynamicObject data, String[] arr){
        if(null == arr || arr.length == 0){
            return "";
        }

        Object obj = data.get(arr[0]);
        if(obj instanceof DynamicObject){//判断数组里的0项是不是对象实例
            return getValue((DynamicObject)obj, Arrays.copyOfRange(arr, 1, arr.length));//对象，对象内容
        }

        return kd.bos.entity.earlywarn.kit.StringUtil.toSafeString(obj);//字段内容
    }
}
