package fsnl.service.wx;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fsnl.common.http.HttpURLConnectionUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：企业微信人员相关接口帮助类
 * @ClassName：WxUserServiceHelper.java
 * @Date 2022/2/19 10:48
 */
public class WxUserServiceHelper extends WxParent {


    /**
     * 获取部门成员详情
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @param depId 获取的部门id
     * @param fetchChild 1/0：是否递归获取子部门下面的成员
     * @return
     */
    public static String getUserDetailsListByDeptId(String corpid,String corpsecret,String depId,String fetchChild){
        String token = getToken(corpid,corpsecret);
        String url = "https://qyapi.weixin.qq.com/cgi-bin/user/list?access_token="+token+"&department_id="+depId+"&fetch_child="+fetchChild+"";
        return getValue(HttpURLConnectionUtil.doGet(url),"userlist");
    }

    /**
     * 获取所有成员详情
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @return
     */
    public static String getAllUserDetailsList(String corpid,String corpsecret){
        return getUserDetailsListByDeptId(corpid,corpsecret,"1","1");
    }


    /**
     * 获取部门成员
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @param depId 获取的部门id
     * @param fetchChild 1/0：是否递归获取子部门下面的成员
     * @return
     */
    public static String getUserListByDeptId(String corpid,String corpsecret,String depId,String fetchChild){
        String token = getToken(corpid,corpsecret);
        String url = "https://qyapi.weixin.qq.com/cgi-bin/user/simplelist?access_token="+token+"&department_id="+depId+"&fetch_child="+fetchChild+"";
        return getValue(HttpURLConnectionUtil.doGet(url),"userlist");
    }

    /**
     * 获取所有部门成员
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @return
     */
    public static String getAllUserList(String corpid,String corpsecret){
        return getUserListByDeptId(corpid,corpsecret,"1","1");
    }

    /**
     * 获取所有人员的userId
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @return
     */
    public static List<String> getAllUserIdList(String corpid,String corpsecret){
        List<String> id_list = new ArrayList<>();
        String ids = getAllUserList(corpid,corpsecret);
        JSONArray jsonArray = JSONArray.parseArray(ids);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            id_list.add(jsonObject.getString("userid"));
        }
        return id_list;
    }

    /**
     * 根据部门id获取需要的用户信息
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @param deptIdLsit 部门id集合
     * @return
     */
    public static JSONArray filterUsers(String corpid,String corpsecret,List<String> deptIdLsit){
        JSONArray dept_jsonArray = new JSONArray();

        //除根部门之外，即金舵陶瓷有限公司,id为1
        for (String id : deptIdLsit) {
            if (!id.equals("1")) {
                String userList = getUserDetailsListByDeptId(corpid, corpsecret, id, "1");
                JSONArray jsonArray = JSONArray.parseArray(userList);
                dept_jsonArray.addAll(jsonArray);
            }
        }
        return dept_jsonArray;
    }

    /**
     * 通过userId获取成员信息
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @param id 企业微信的userId
     * @return
     */
    public static String getUserById(String corpid,String corpsecret,String id){
        String token = getToken(corpid,corpsecret);
        String url = "https://qyapi.weixin.qq.com/cgi-bin/user/get?access_token="+token+"&userid="+id;
        return HttpURLConnectionUtil.doGet(url);
    }
}
