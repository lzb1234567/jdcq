package fsnl.service.wx;

import com.alibaba.fastjson.JSONObject;
import fsnl.common.http.HttpURLConnectionUtil;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：微信接口父类，主要处理token
 * @ClassName：WxParent.java
 * @Date 2022/2/21 9:11
 */
public class WxParent {
    /**
     * 获取企业微信access_token
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @return
     */
    public static String getToken(String corpid, String corpsecret){
        String url = "https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid="+corpid+"&corpsecret="+corpsecret+"";
        String data = HttpURLConnectionUtil.doGet(url);
        return getValue(data,"access_token");
    }

    /**
     * 统一处理企业微信返回的数据，根据字段名获取value值
     * @param data
     * @param value
     * @return
     */
    public static String getValue(String data,String value){
        JSONObject json = JSONObject.parseObject(data);
        if (json.getString("errcode").equals("0")) {
            return json.getString(value);
        }
        return null;
    }
}
