package fsnl.service.syn.org;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fsnl.service.kingdee.DynamicObjectHelper;
import fsnl.service.wx.WxUserServiceHelper;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.entity.property.BasedataProp;
import kd.bos.org.model.OrgParam;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.model.UserParam;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.org.OrgViewType;
import kd.bos.servicehelper.user.UserServiceHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：根据回调信息将用户中心数据同步至苍穹平台
 * @ClassName：UscToCqByCallBack.java
 * @Date 2022/3/16 9:37
 */
public class UscToCqByCallBack {

    private static String cqDepartmentName = "bos_org";//苍穹平台组织的标识
    private static String uscDepartmentName = "fsnl_usc_department";//用户中心组织的标识
    private static String cqUserName = "bos_user";//苍穹平台人员的标识
    private static String uscUserName = "fsnl_usc_user";//用户中心人员的标识

    /**
     * 将用户中心的数据同步至苍穹平台
     * @param number 部门编码
     * @param changeType 类型
     */
    public static void departmentSyn(String number ,String changeType){

        //获取用户中心部门数据
        DynamicObject usc_dept = DynamicObjectHelper.getDynamicObject(uscDepartmentName, number);

        if ("create_party".equals(changeType)) {//新增部门事件
            insertDepartment(usc_dept);
        }else if ("update_party".equals(changeType)){//更新部门事件
            updateDepartment(usc_dept);
        }else if ("delete_party".equals(changeType)) {//删除部门事件
            //获取部门主键
            String cqDeptPkId = DynamicObjectHelper.getPkIdByNumber(cqDepartmentName, usc_dept.getString("number"));
            List<OrgParam> disableList = new ArrayList<>();
            OrgParam param = new OrgParam();
            param.setId(Long.parseLong(cqDeptPkId));
            disableList.add(param);
            OrgUnitServiceHelper.disable(disableList);
        }
    }

    /**
     * 将用户中心人员数据同步至苍穹平台
     * @param id 企业微信userid
     * @param changeType 类型
     */
    public static void userSyn(String corpid,String corpsecret,String id ,String changeType){
        //获取企业微信该成员数据
        String user_str = WxUserServiceHelper.getUserById(corpid,corpsecret,id);
        JSONObject user_json = JSONObject.parseObject(user_str);
        JSONObject wxExtattr = user_json.getJSONObject("extattr");
        if (wxExtattr!=null) {
            JSONArray wxAttrs = wxExtattr.getJSONArray("attrs");
            if (wxAttrs != null && wxAttrs.size() != 0) {
                String jobNumber = wxAttrs.getJSONObject(0).getString("value");
                //获取用户中心部门数据
                DynamicObject usc_user = DynamicObjectHelper.getDynamicObject(uscUserName, jobNumber);
                if ("create_user".equals(changeType)) {//新增成员事件
                    insertUser(usc_user);
                }else if ("update_user".equals(changeType)){//更新成员事件
                    updateUser(usc_user);
                }else if ("delete_user".equals(changeType)) {//删除成员事件
                    //获取部门主键
                    String cqUserPkId = DynamicObjectHelper.getPkIdByNumber(cqUserName, jobNumber);
                    List<UserParam> disableList = new ArrayList<>();
                    UserParam param = new UserParam();
                    param.setId(Long.parseLong(cqUserPkId));
                    disableList.add(param);
                    UserServiceHelper.disable(disableList);
                }
            }
        }
    }

    /**
     * 将用户中心部门数据插入到苍穹平台
     * @param usc_dept 用户中心数据
     */
    private static void insertDepartment(DynamicObject usc_dept) {
        //需要进insert的数据
        List<OrgParam> insertList = new ArrayList<>();
        //构建数据
        OrgParam param = buildDepartment(usc_dept);
        insertList.add(param);
        OrgUnitServiceHelper.add(insertList);
    }

    /**
     * 将用户中心部门数据同步至苍穹平台
     * @param usc_dept 用户中心数据
     */
    private static void updateDepartment(DynamicObject usc_dept) {
        //需要进行update的数据
        List<OrgParam> updateList = new ArrayList<>();
        //苍穹平台部门主键
        String cqDeptPkId = DynamicObjectHelper.getPkIdByNumber(cqDepartmentName, usc_dept.getString("number"));
        //构建数据
        OrgParam param = buildDepartment(usc_dept);
        param.setId(Long.parseLong(cqDeptPkId));//主键id
        updateList.add(param);
        OrgUnitServiceHelper.update(updateList);
    }

    /**
     * 将用户中心人员数据插入至苍穹平台
     * @param usc_user 用户中心人员数据
     */
    private static void insertUser(DynamicObject usc_user){
        //需要进行insert的数据
        List<UserParam> insertList = new ArrayList<>();
        //构建数据
        UserParam param = buildUser(usc_user);
        insertList.add(param);
        //调用保存新增人员接口
        UserServiceHelper.add(insertList);
    }

    /**
     * 将用户中心人员数据更新至苍穹平台
     * @param usc_user 用户中心人员数据
     */
    private static void updateUser(DynamicObject usc_user){
        //需要进行update的数据
        List<UserParam> updateList = new ArrayList<>();
        //构建数据
        UserParam param = buildUser(usc_user);
        //苍穹平台部门主键
        String cqUserPkId = DynamicObjectHelper.getPkIdByNumber(cqUserName, usc_user.getString("number"));
        param.setId(Long.parseLong(cqUserPkId));//主键id
        updateList.add(param);
        //调用保存新增人员接口
        UserServiceHelper.update(updateList);
    }

    /**
     * 构建苍穹部门数据
     * @param usc_dept 用户中心部门数据
     * @return
     */
    private static OrgParam buildDepartment(DynamicObject usc_dept){
        OrgParam param = new OrgParam();
        param.setName(usc_dept.getString("name"));
        param.setNumber(usc_dept.getString("number"));
        //组织形态
        param.setOrgPatternId(usc_dept.getLong("fsnl_forgpattenid_id"));
        param.setDuty(OrgViewType.Admin);
        //获取上级组织的信息
        DynamicObject cqParentDeptObject = DynamicObjectHelper.getDynamicObject(uscDepartmentName, usc_dept.getString("parent.number"));
        param.setParentId(cqParentDeptObject.getLong("id"));//上级组织
        return param;
    }

    /**
     * 构建苍穹人员数据
     * @param usc_user 用户中心人员数据
     * @return
     */
    private static UserParam buildUser(DynamicObject usc_user){
        UserParam param = new UserParam();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("number", usc_user.getString("number"));//编码，即工号
        dataMap.put("name", usc_user.getString("name"));//名称
        //获取用户中心人员类型
        DynamicObjectCollection userType_doc = usc_user.getDynamicObjectCollection("fsnl_fusertype");
        //获取苍穹平台人员数据实体，并从实体中获取人员类型
        DynamicObject bos_user = BusinessDataServiceHelper.newDynamicObject("bos_user");
        DynamicObjectCollection newUserType_doc = bos_user.getDynamicObjectCollection("usertypes");
        DynamicObjectType newUserTypeObjectType = newUserType_doc.getDynamicObjectType();
        //遍历用户中心人员类型，填充到苍穹平台的人员类型集合
        for (DynamicObject userType : userType_doc) {
            DynamicObject newUserTypeDataRow = new DynamicObject(newUserTypeObjectType);
            newUserType_doc.add(newUserTypeDataRow);
            // 基础资料：存储每条基础资料的数据包，属性名是个常量fbasedataid
            BasedataProp newUserType_basedataProp = (BasedataProp)newUserTypeObjectType.getProperties().get("fbasedataid");
            DynamicObject newUserType_basedataid = userType.getDynamicObject("fbasedataid");
            newUserType_basedataProp.setValue(newUserTypeDataRow, newUserType_basedataid);
        }
        dataMap.put("usertypes", newUserType_doc);//类型
        dataMap.put("phone", usc_user.getString("fsnl_fphone"));//电话
        dataMap.put("email", usc_user.getString("fsnl_femail"));//邮箱
        dataMap.put("idcard", usc_user.getString("fsnl_fidcard"));//身份证号
        dataMap.put("birthday", usc_user.getString("fsnl_fbirthday"));//生日
        dataMap.put("gender", usc_user.getString("fsnl_fgender"));//性别
        dataMap.put("picturefield", usc_user.getString("fsnl_favatar"));//头像

        // 职位分录
        List<Map<String, Object>> posList = new ArrayList<>();
        DynamicObjectCollection uscUserdept = usc_user.getDynamicObjectCollection("fsnl_userdept");
        for (int i = 0; i < uscUserdept.size(); i++) {
            DynamicObject userDept = uscUserdept.get(i);
            Map<String, Object> entryentity = new HashMap<>();
            DynamicObject uscUserDept = userDept.getDynamicObject("fsnl_fdeptid");
            Map<String, Object> dptNumMap = new HashMap<>();
            dptNumMap.put("number", uscUserDept.getString("number"));//部门编码
            entryentity.put("dpt", dptNumMap);//部门id
            entryentity.put("position", userDept.getString("fsnl_fposition"));//职位
            entryentity.put("isincharge", userDept.getString("fsnl_fisincharge"));//负责人
            entryentity.put("ispartjob", userDept.getString("fsnl_fispartjob"));//兼职
            entryentity.put("seq", i+1);//行号
            posList.add(entryentity);
        }
        dataMap.put("entryentity", posList);

        // 联系方式分录
        DynamicObjectCollection uscUserContacts = usc_user.getDynamicObjectCollection("fsnl_usercontact");
        DynamicObjectCollection cqUserContacts = bos_user.getDynamicObjectCollection("contactentity");
        for (int i = 0; i < uscUserContacts.size(); i++) {
            DynamicObject userContact = uscUserContacts.get(i);
            DynamicObject cqCserContact = new DynamicObject(cqUserContacts.getDynamicObjectType());
            cqCserContact.set("contacttype", userContact.getDynamicObject("fsnl_fcontacttypeid"));//类型
            cqCserContact.set("contact", userContact.getString("fsnl_fcontact"));//联系方式
            cqCserContact.set("isdefault", userContact.getString("fsnl_fisdefault"));//是否默认
            cqUserContacts.add(cqCserContact);
        }
        dataMap.put("contactentity", cqUserContacts);

        param.setDataMap(dataMap);
        return param;
    }

}
