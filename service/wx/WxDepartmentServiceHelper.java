package fsnl.service.wx;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fsnl.common.http.HttpURLConnectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：企业微信部门相关接口帮助类
 * @ClassName：WxDepartmentServiceHelper.java
 * @Date 2022/2/19 11:32
 */
public class WxDepartmentServiceHelper extends WxParent {

    /**
     * 获取部门列表
     * @param corpid  企业id
     * @param corpsecret 密钥
     * @param depId 部门id。获取指定部门及其下的子部门（以及子部门的子部门等等，递归）。 如果不填，默认获取全量组织架构
     * @return
     */
    public static String getDepartmentList(String corpid, String corpsecret, String depId){
        String token = getToken(corpid,corpsecret);
        String url = "https://qyapi.weixin.qq.com/cgi-bin/department/list?access_token="+token+"&id="+depId+"";
        return getValue(HttpURLConnectionUtil.doGet(url),"department");
    }

    /**
     * 获取所有部门列表
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @return
     */
    public static String getAllDepartmentList(String corpid,String corpsecret){
        return getDepartmentList(corpid,corpsecret,"");
    }

    /**
     * 根据部门Id获取部门id列表
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @param id 部门id。获取指定部门及其下的子部门（以及子部门的子部门等等，递归）。 如果不填，默认获取全量组织架构
     * @return
     */
    public static List<String> getDepartmentIdList(String corpid,String corpsecret,String id){
        String token = getToken(corpid,corpsecret);
        String url = "https://qyapi.weixin.qq.com/cgi-bin/department/simplelist?access_token="+token+"&id="+id;
        String depIds = getValue(HttpURLConnectionUtil.doGet(url),"department_id");
        JSONArray depId_jsonArray = JSONArray.parseArray(depIds);
        List<String> idList = new ArrayList<>();
        for (int i = 0; i < depId_jsonArray.size(); i++) {
            JSONObject json = depId_jsonArray.getJSONObject(i);
            idList.add(json.getString("id"));
        }
        return idList;
    }

    /**
     * 获取所有部门Id列表
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @return
     */
    public static List<String> getAllDepartmentIdList(String corpid,String corpsecret){
        return getDepartmentIdList(corpid,corpsecret,null);
    }

    /**
     * 获取单个部门详情
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @param id 部门id。
     * @return
     */
    public static String getDepartment(String corpid,String corpsecret,String id){
        String token = getToken(corpid,corpsecret);
        String url = "https://qyapi.weixin.qq.com/cgi-bin/department/get?access_token="+token+"&id="+id;
        return getValue(HttpURLConnectionUtil.doGet(url),"department");
    }

    /**
     * 获取需要的部门列表，默认带上跟部门
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @param idLsit 部门id集合，1为跟部门，可填可不填，默认都会加上跟部门的数据。
     * @return
     */
    public static JSONArray filterDepartment(String corpid,String corpsecret,List<String> idLsit){
        JSONArray dept_jsonArray = new JSONArray();

        //获取根部门，金舵陶瓷有限公司,id为1
        JSONObject jsonObject_1 = JSONObject.parseObject(getDepartment(corpid, corpsecret, "1"));
        dept_jsonArray.add(jsonObject_1);
        for (String id : idLsit) {
            if (!id.equals("1")) {
                JSONArray jsonArray = JSONArray.parseArray(getDepartmentList(corpid, corpsecret, id));
                dept_jsonArray.addAll(jsonArray);
            }
        }
        return dept_jsonArray;
    }
}
