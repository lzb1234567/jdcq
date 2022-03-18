package fsnl.service.syn.org;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import fsnl.service.kingdee.DynamicObjectHelper;
import fsnl.service.wx.WxDepartmentServiceHelper;
import fsnl.service.wx.WxUserServiceHelper;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.property.BasedataProp;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DBServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：根据回调信息将企业微信数据同步至用户中心
 * @ClassName：WxToUscByCallBack.java
 * @Date 2022/3/15 14:32
 */
public class WxToUscByCallBack {
    private static String departmentName = "fsnl_usc_department";//组织的标识
    private static String userName = "fsnl_usc_user";//人员的标识
    private static String userid = RequestContext.get().getUid();//当前用户

    /**
     * 同步部门
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @param id 部门id
     * @param changeType 类型
     */
    public static void departmentSyn(String corpid,String corpsecret,String id ,String changeType){
        //获取企业微信该部门数据
        String dpet_str = WxDepartmentServiceHelper.getDepartment(corpid,corpsecret,id);
        JSONObject dept_json = JSONObject.parseObject(dpet_str);
        if ("create_party".equals(changeType)) {//新增部门事件
            insertDepartment(dept_json);
        }else if ("update_party".equals(changeType)){//更新部门事件
            updateDepartment(dept_json);
        }else if ("delete_party".equals(changeType)){//删除部门事件
            DynamicObject[] departments = new DynamicObject[1];
            //主键
            String pkId = DynamicObjectHelper.getPkIdByNumber(departmentName,dept_json.getString("number"));
            //根据主键获取用户中心人员数据
            DynamicObject updateObject = BusinessDataServiceHelper.loadSingle(pkId, departmentName);
            updateObject.set("enable","0");
            updateObject.set("fsnl_Fenabler",userid);
            updateObject.set("fsnl_Fenabletime",new Date());
            OperationServiceHelper.executeOperate("save", departmentName, departments, OperateOption.create());
        }
    }

    /**
     * 同步人员
     * @param corpid 企业id
     * @param corpsecret 密钥
     * @param id 用户id
     * @param changeType 类型
     */
    public static void userSyn(String corpid,String corpsecret,String id ,String changeType){
        //获取企业微信该成员数据
        String user_str = WxUserServiceHelper.getUserById(corpid,corpsecret,id);
        JSONObject user_json = JSONObject.parseObject(user_str);
        if ("create_user".equals(changeType)) {//新增成员事件
            insertUser(user_json);
        }else if ("update_user".equals(changeType)){//更新成员事件
            updateUser(user_json);
        }else if ("delete_user".equals(changeType)){//删除成员事件
            //禁用人员，工号数据不为空则禁用
            JSONObject wxExtattr = user_json.getJSONObject("extattr");
            JSONArray wxAttrs = wxExtattr.getJSONArray("attrs");
            if (wxAttrs.size() != 0) {
                //工号
                String jobNumber = wxAttrs.getJSONObject(0).getString("value");
                //主键
                String pkId = DynamicObjectHelper.getPkIdByNumber(userName, jobNumber);
                //根据主键获取人员数据
                DynamicObject userObject = BusinessDataServiceHelper.loadSingle(pkId, userName);
                userObject.set("enable","0");
                userObject.set("fsnl_Fenabler",userid);
                userObject.set("fsnl_Fenabletime",new Date());
                OperationServiceHelper.executeOperate("save", departmentName, new DynamicObject[]{userObject}, OperateOption.create());
            }
        }
    }

    /**
     * 将企业微信的部门数据插入到用户中心
     * @param dept_json
     */
    private static void insertDepartment(JSONObject dept_json){
        DynamicObject[] departments = new DynamicObject[1];
        DynamicObject usc_department = BusinessDataServiceHelper.newDynamicObject(departmentName);
        String dept_id = dept_json.getString("id");
        usc_department.set("number",dept_id);//编码
        usc_department.set("id",DBServiceHelper.genGlobalLongId());//内码
        usc_department.set("name",dept_json.getString("name"));//名称
        //形态
        if ("1".equals(dept_id)) {//根部门
            usc_department.set("fsnl_forgpattenid_id",1);//集团
        }else if ("7".equals(dept_id)||"43".equals(dept_id)){//集团公司或者星星易装
            usc_department.set("fsnl_forgpattenid_id",2);//公司
        }else {
            usc_department.set("fsnl_forgpattenid_id",6);//部门
        }
        usc_department.set("enable","1");//使用状态，默认为可用，1
        //usc_department.set("level","0");//级次
        usc_department.set("fsnl_fhandword","0");//是否手工新增，默认否
        usc_department.set("creator",userid);//创建人
        usc_department.set("createtime",new Date());//创建时间
        usc_department.set("status","C");//数据状态，默认为已审核
        //父部门
        Map<String, DynamicObject> uscDeptMap = getDeptMap();
        usc_department.set("parent",uscDeptMap.get(dept_id));
        departments[0] = usc_department;
        OperationServiceHelper.executeOperate("save", departmentName, departments, OperateOption.create());
    }

    /**
     * 将企业微信修改的部门数据更新到用户中心
     * @param dept_json
     */
    private static void updateDepartment(JSONObject dept_json){
        DynamicObject[] departments = new DynamicObject[1];
        //主键
        String pkId = DynamicObjectHelper.getPkIdByNumber(departmentName,dept_json.getString("number"));
        //根据主键获取用户中心人员数据
        DynamicObject updateObject = BusinessDataServiceHelper.loadSingle(pkId, departmentName);
        updateObject.set("name",dept_json.getString("name"));//名称
        updateObject.set("modifier",userid);//修改人
        updateObject.set("modifytime",new Date());//修改时间
        departments[0] = updateObject;
        OperationServiceHelper.executeOperate("save", departmentName, departments, OperateOption.create());
    }

    /**
     * 插入企业微信成员数据至用户中心
     * @param user_json 企业微信该成员数据
     */
    private static void insertUser(JSONObject user_json) {
        DynamicObject[] insertObject = new DynamicObject[1];
        DynamicObject usc_user = BusinessDataServiceHelper.newDynamicObject(userName);
        DynamicObject userType = DynamicObjectHelper.getDynamicObject("bos_usertype", "PT01");//人员类型-->职员
        String wxUserid = user_json.getString("userid");
        //编码,对应企业微信的用户名的value，获取不到工号的，不同步
        JSONObject wxExtattr = user_json.getJSONObject("extattr");
        if (wxExtattr!=null) {
            JSONArray wxAttrs = wxExtattr.getJSONArray("attrs");
            if (wxAttrs!=null&&wxAttrs.size() != 0) {
                String number = wxAttrs.getJSONObject(0).getString("value");
                usc_user.set("number", number);
                String mobile = user_json.getString("mobile");//企业微信手机
                usc_user.set("id", DBServiceHelper.genGlobalLongId());//内码
                usc_user.set("name", user_json.getString("name"));//名称

                //类型
                DynamicObjectCollection userType_doc = usc_user.getDynamicObjectCollection("fsnl_fusertype");
                DynamicObjectType userTypeObjectType = userType_doc.getDynamicObjectType();
                DynamicObject newUserTypeRow = new DynamicObject(userTypeObjectType);
                userType_doc.add(newUserTypeRow);
                // 基础资料：存储每条基础资料的数据包，属性名是个常量fbasedataid
                BasedataProp userType_basedataProp = (BasedataProp) userTypeObjectType.getProperties().get("fbasedataid");
                userType_basedataProp.setValue(newUserTypeRow, userType);
                //usc_department.set("fsnl_fidcard","");//身份证
                //usc_user.set("fsnl_fbirthday", "");//生日
                usc_user.set("fsnl_fgender", user_json.getString("gender"));//性别
                //usc_department.set("fsnl_country","");//国家地区
                usc_user.set("fsnl_fphone", mobile);//手机
                //邮箱，对应企业微信的企业邮箱
                String biz_mail = user_json.getString("biz_mail");
                usc_user.set("fsnl_femail", biz_mail);
                usc_user.set("fsnl_favatar", user_json.getString("avatar"));//头像
                usc_user.set("enable", "1");//使用状态，默认为可用，1
                usc_user.set("fsnl_fhandword", "0");//是否手工新增，默认否
                usc_user.set("creator", userid);//创建人
                usc_user.set("createtime", new Date());//创建时间
                usc_user.set("status", "C");//数据状态，默认为已审核

                //获取部门信息
                Map<String, DynamicObject> deptMap = getDeptMap();

                //人员组织信息
                DynamicObjectCollection userDept_doc = usc_user.getDynamicObjectCollection("fsnl_userdept");//获取单据体数据集
                JSONArray departmentIds = user_json.getJSONArray("department");
                JSONArray isLeaderInDepts = user_json.getJSONArray("is_leader_in_dept");
                for (int i = 0; i < departmentIds.size(); i++) {
                    String deptId = departmentIds.getString(i);
                    String leaderInDept = isLeaderInDepts.getString(i);
                    DynamicObject userdept_do = new DynamicObject(userDept_doc.getDynamicObjectType());//获取单据体数据类型
                    userdept_do.set("fsnl_fdeptid", deptMap.get(deptId));//用户中心组织
                    userdept_do.set("fsnl_fposition", user_json.getString("position"));//职位
                    userdept_do.set("fsnl_fisincharge", leaderInDept);//负责人
                    //userdept_do.set("fsnl_fispartjob","");//兼职
                    userDept_doc.add(userdept_do);
                }

                //人员联系方式
                //人员联系方式-->手机
                DynamicObjectCollection userContact_doc = usc_user.getDynamicObjectCollection("fsnl_usercontact");//获取单据体数据集
                if (StringUtils.isNotEmpty(mobile)) {
                    DynamicObject userContact_phone = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                    userContact_phone.set("fsnl_fcontacttypeid_id", 3);//类型-->手机
                    userContact_phone.set("fsnl_fcontact", mobile);//联系方式
                    userContact_phone.set("fsnl_fisdefault", "1");//是否默认
                    userContact_doc.add(userContact_phone);
                }
                //人员联系方式-->地址
                String address = user_json.getString("address");
                if (StringUtils.isNotEmpty(address)) {
                    DynamicObject userContact_address = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                    userContact_address.set("fsnl_fcontacttypeid_id", 1);//类型-->地址
                    userContact_address.set("fsnl_fcontact", address);//联系方式
                    userContact_address.set("fsnl_fisdefault", "0");//是否默认
                    userContact_doc.add(userContact_address);
                }
                //人员联系方式-->邮箱
                if (StringUtils.isNotEmpty(biz_mail)) {
                    DynamicObject userContact_address = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                    userContact_address.set("fsnl_fcontacttypeid_id", 2);//类型-->邮箱
                    userContact_address.set("fsnl_fcontact", biz_mail);//联系方式
                    userContact_address.set("fsnl_fisdefault", "0");//是否默认
                    userContact_doc.add(userContact_address);
                }

                //应用使用情况
                DynamicObjectCollection userAppCase_doc = usc_user.getDynamicObjectCollection("fsnl_userappcase");//获取单据体数据集
                DynamicObject userAppCase_do = new DynamicObject(userAppCase_doc.getDynamicObjectType());//获取单据体数据类型
                userAppCase_do.set("fsnl_fappname", "1");//系统名称,默认企业微信
                userAppCase_do.set("fsnl_fuseage", "0");//使用情况,默认启用
                userAppCase_do.set("fsnl_fappusername", wxUserid);//账号
                userAppCase_do.set("fsnl_userid", wxUserid);//用户id
                userAppCase_doc.add(userAppCase_do);
                insertObject[0] = usc_user;

                //保存数据并处理直接上级
                saveUserDate(insertObject,user_json.getJSONArray("direct_leader"));
            }
        }

    }

    /**
     * 更新企业微信成员数据至用户中心
     * @param users_json 企业微信成员数据
     */
    private static void updateUser(JSONObject users_json){
        DynamicObject[] updateObject = new DynamicObject[1];
        //编码,对应企业微信的用户名的value，获取不到工号的，不同步
        JSONObject wxExtattr = users_json.getJSONObject("extattr");
        JSONArray wxAttrs = wxExtattr.getJSONArray("attrs");
        if (wxAttrs.size() != 0) {
            //工号
            String jobNumber = wxAttrs.getJSONObject(0).getString("value");
            //主键
            String pkId = DynamicObjectHelper.getPkIdByNumber(userName,jobNumber);
            //根据主键获取用户中心人员数据
            DynamicObject userObject = BusinessDataServiceHelper.loadSingle(pkId, userName);
            userObject.set("modifier", userid);//修改人
            userObject.set("modifytime", new Date());//修改时间
            String mobile = users_json.getString("mobile");//企业微信手机
            userObject.set("name", users_json.getString("name"));//名称
            //邮箱，对应企业微信的企业邮箱
            String biz_mail = users_json.getString("biz_mail");
            userObject.set("fsnl_femail", biz_mail);
            userObject.set("fsnl_favatar", users_json.getString("avatar"));//头像
            userObject.set("fsnl_fgender", users_json.getString("gender"));//性别
            userObject.set("fsnl_fphone", mobile);//手机

            //获取部门信息
            Map<String, DynamicObject> deptMap = getDeptMap();

            //人员组织信息
            DynamicObjectCollection userDept_doc = userObject.getDynamicObjectCollection("fsnl_userdept");//获取单据体数据集
            userDept_doc.removeAll(userDept_doc);
            JSONArray departmentIds = users_json.getJSONArray("department");
            JSONArray isLeaderInDepts = users_json.getJSONArray("is_leader_in_dept");
            for (int i = 0; i < departmentIds.size(); i++) {
                String deptId = departmentIds.getString(i);
                String leaderInDept = isLeaderInDepts.getString(i);
                DynamicObject userdept_do = new DynamicObject(userDept_doc.getDynamicObjectType());//获取单据体数据类型
                userdept_do.set("fsnl_fdeptid", deptMap.get(deptId));//用户中心组织
                userdept_do.set("fsnl_fposition", users_json.getString("position"));//职位
                userdept_do.set("fsnl_fisincharge", leaderInDept);//负责人
                //userdept_do.set("fsnl_fispartjob","");//兼职
                userDept_doc.add(userdept_do);
            }

            //人员联系方式
            //人员联系方式-->手机
            DynamicObjectCollection userContact_doc = userObject.getDynamicObjectCollection("fsnl_usercontact");//获取单据体数据集
            userContact_doc.removeAll(userContact_doc);
            if (StringUtils.isNotEmpty(mobile)) {
                DynamicObject userContact_phone = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                userContact_phone.set("fsnl_fcontacttypeid_id", 3);//类型-->手机
                userContact_phone.set("fsnl_fcontact", mobile);//联系方式
                userContact_phone.set("fsnl_fisdefault", "1");//是否默认
                userContact_doc.add(userContact_phone);
            }
            //人员联系方式-->地址
            String address = users_json.getString("address");
            if (StringUtils.isNotEmpty(address)) {
                DynamicObject userContact_address = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                userContact_address.set("fsnl_fcontacttypeid_id", 1);//类型-->地址
                userContact_address.set("fsnl_fcontact", address);//联系方式
                userContact_address.set("fsnl_fisdefault", "0");//是否默认
                userContact_doc.add(userContact_address);
            }
            //人员联系方式-->邮箱
            if (StringUtils.isNotEmpty(biz_mail)) {
                DynamicObject userContact_address = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                userContact_address.set("fsnl_fcontacttypeid_id", 2);//类型-->邮箱
                userContact_address.set("fsnl_fcontact", biz_mail);//联系方式
                userContact_address.set("fsnl_fisdefault", "0");//是否默认
                userContact_doc.add(userContact_address);
            }
            updateObject[0] = userObject;
        }
        //更新数据并处理直接上级
        saveUserDate(updateObject,users_json.getJSONArray("direct_leader"));
    }

    /**
     * 获取用户中心所有非禁用的部门ID(编码)
     * @return
     */
    private static List<String> getUscDepartmentIdList(){
        QFilter enable_filter = new QFilter("enable", "!=", "0");//非禁用
        QFilter handword_filter = new QFilter("fsnl_fhandword", "!=", "1");//非手工新增
        QFilter[] qFilters = {enable_filter,handword_filter};
        String number = "number";
        DynamicObject[] usc_department = BusinessDataServiceHelper.load(departmentName, number, qFilters);
        List<String> list = new ArrayList<>();
        for (DynamicObject dynamicObject : usc_department) {
            list.add(dynamicObject.getString(number));
        }
        return list;
    }

    /**
     * 根据组织id集合获取组织对象
     * @param id_list
     * @return
     */
    private static DynamicObject[] getDeptObject(List<String> id_list){
        if (id_list.size()!=0){
            List<Object> pkIds = DynamicObjectHelper.getPkIdByNumbers(departmentName, id_list);
            DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load(pkIds.toArray(), EntityMetadataCache.getDataEntityType(departmentName));
            return dynamicObjects;
        }
        return null;
    }

    /**
     * 赋值直接上级组织
     * @param usc_do 组织对象数值
     * @param direct_leaders 企业微信成员直接上级
     */
    private static void setSuperiorObject(DynamicObject[] usc_do,JSONArray direct_leaders){
        //构建数据，以企业微信userId作为key值，组织对象作为value值，方便匹配数据
        HashMap<String, DynamicObject> usc_Map = new HashMap<>();
        for (DynamicObject dynamicObject : usc_do) {
            DynamicObjectCollection userAppCase = dynamicObject.getDynamicObjectCollection("fsnl_userappcase");
            for (DynamicObject appCase : userAppCase) {
                String appName = appCase.getString("fsnl_fappname");
                if ("1".equals(appName)) {//企业微信
                    String userId = appCase.get("fsnl_userid").toString();
                    usc_Map.put(userId,dynamicObject);
                    break;
                }
            }

        }
        for (DynamicObject dynamicObject : usc_do) {
            DynamicObjectCollection userAppCase = dynamicObject.getDynamicObjectCollection("fsnl_userappcase");
            for (DynamicObject appCase : userAppCase) {
                String appName = appCase.getString("fsnl_fappname");
                if ("1".equals(appName)) {//企业微信
                    String fsnl_userid = appCase.get("fsnl_userid").toString();
                    //取企业微信第一个直接上级，并赋值给用户中心人员的部门列表的第一行部门。
                    if (direct_leaders.size()!=0) {
                        DynamicObjectCollection userDet = dynamicObject.getDynamicObjectCollection("fsnl_userdept");
                        userDet.get(0).set("fsnl_fsuperiorid",usc_Map.get(direct_leaders.get(0)));
                    }
                }
            }
        }
    }

    /**
     * 获取用户中心部门数据
     * @return
     */
    private static Map<String, DynamicObject> getDeptMap(){
        //获取部门id列表
        List<String> uscDepartmentIdList = getUscDepartmentIdList();
        //根据id列表获取所有部门信息
        DynamicObject[] uscDept_dos = getDeptObject(uscDepartmentIdList);
        //组装map
        Map<String, DynamicObject> deptMap = new HashMap<>();
        for (DynamicObject uscDept_do : uscDept_dos) {
            deptMap.put(uscDept_do.getString("number"), uscDept_do);
        }
        return deptMap;
    }

    /**
     * 报错用户数据并更新直接上级
     * @param saveObject 用户数据
     * @param direct_leader 直接上级
     */
    private static void saveUserDate(DynamicObject[] saveObject,JSONArray direct_leader){
        //保存数据
        OperationResult operationResult = OperationServiceHelper.executeOperate("save", userName, saveObject, OperateOption.create());
        //补充直接上级数据
        List<Object> successPkIds = operationResult.getSuccessPkIds();
        DynamicObject[] newUserObjects = BusinessDataServiceHelper.load(successPkIds.toArray(), EntityMetadataCache.getDataEntityType(userName));
        setSuperiorObject(newUserObjects,direct_leader);
        OperationServiceHelper.executeOperate("save", userName, newUserObjects, OperateOption.create());
    }
}
