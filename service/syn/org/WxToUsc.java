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
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.DBServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.OperationServiceHelper;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：企业微信同步数据至用户中心的处理类
 * @ClassName：WxToSky.java
 * @Date 2022/2/19 14:33
 */
public class WxToUsc {
    private final static Log logger = LogFactory.getLog(WxToUsc.class);
    private static String departmentName = "fsnl_usc_department";//组织的标识
    private static String userName = "fsnl_usc_user";//人员的标识
    private static String userid = RequestContext.get().getUid();//当前用户

    /**
     * 对外开放的同步任务
     * @param corpid
     * @param corpsecret
     */
    public static void sysJob(String corpid, String corpsecret){
        //同步部门
        departmentSyn(corpid,corpsecret);
        //同步人员
        userSyn(corpid,corpsecret);
    }


    /**
     * 同步组织数据
     * @param corpid 企业id
     * @param corpsecret 密钥
     */
    private static void departmentSyn(String corpid, String corpsecret){
        List<String> depIdLsit = new ArrayList<>();//初始化需要加载的部门
        depIdLsit.add("7");//获取集团公司及以下部门,id为7
        depIdLsit.add("43");//获取星星易装及以下部门，id为43
        //1.获取企业微信部门列表
        JSONArray department_jsonArray = WxDepartmentServiceHelper.filterDepartment(corpid, corpsecret,depIdLsit);
        //转换数据类型，方便做判断处理，提升性能
        Map<String, JSONObject> wx_map = getWxDeptMap(department_jsonArray,"id");
        //2.获取企业微信所有部门ID
        List<String> wxDepartmentIdList = WxDepartmentServiceHelper.getAllDepartmentIdList(corpid, corpsecret);
        //3.获取用户中心所有部门ID
        List<String> uscDepartmentIdList = getUscDepartmentIdList();
        //4.保存部门列表
        saveDepartment(wx_map,uscDepartmentIdList);
        //5.判断用户中心的部门id是否存于企业微信，获得需要更新禁用状态的id列表
        List<String> updateUscDeptIdList = getUpdateIdList(wxDepartmentIdList,uscDepartmentIdList);
        //6.更新禁用状态
        updateDeptDisable(updateUscDeptIdList);
    }

    /**
     * 同步人员数据
     * @param corpid 企业id
     * @param corpsecret 密钥
     */
    private static void userSyn(String corpid, String corpsecret){
        List<String> depIdLsit = new ArrayList<>();//初始化需要加载的部门
        depIdLsit.add("7");//获取集团公司及以下部门,id为7
        depIdLsit.add("43");//获取星星易装及以下部门，id为43
        //1.获取企业微信人员信息
        JSONArray wxUser_jsonArray = WxUserServiceHelper.filterUsers(corpid, corpsecret,depIdLsit);
        //2.转换数据类型，方便做判断处理，提升性能
        Map<String, JSONObject> wx_map = getWxDeptMap(wxUser_jsonArray,"userid");
        //3.获取企业微信人员userId列表
        List<String> wxUserIdList = WxUserServiceHelper.getAllUserIdList(corpid, corpsecret);
        //4.获取用户中心人员Id列表
        List<String> uscUserIdList = getUscUserIdList();
        //5.保存企业微信人员到用户中心
        saveUser(wx_map,uscUserIdList);
        //判断用户中心的用户中心id是否存于企业微信，获得需要更新禁用状态的id列表
        List<String> updateIdList = getUpdateIdList(wxUserIdList, uscUserIdList);
        //更新禁用状态
        updateUserDisable(updateIdList);
    }

    /**
     *
     * @param wx_map
     * @param uscDepartmentIdList
     */
    private static void saveDepartment(Map<String, JSONObject> wx_map,List<String> uscDepartmentIdList){
        List<DynamicObject> insertList = new ArrayList<>();//需要进insert的数据
        List<String> update_id_list = new ArrayList<>();//需要进行update的数据
        //形态
        //DynamicObject orgform01 = getDynamicObject(patternName, "Orgform01");//集团
        //DynamicObject orgform02 = getDynamicObject(patternName, "Orgform02");//公司
        //DynamicObject orgform06 = getDynamicObject(patternName, "Orgform06");//部门
        //如果id存在于用户中心，则为需要更新的数据，反之为需要insert的数据
        for (String id : wx_map.keySet()) {
            if (uscDepartmentIdList.contains(id)) {
                //记录需要update的id
                update_id_list.add(id);
            }else {
                //构建insert数据
                DynamicObject usc_department = BusinessDataServiceHelper.newDynamicObject(departmentName);
                JSONObject dept_jsonObject = wx_map.get(id);
                String dept_id = dept_jsonObject.getString("id");
                usc_department.set("number",dept_id);//编码
                usc_department.set("id",DBServiceHelper.genGlobalLongId());//内码
                usc_department.set("name",dept_jsonObject.getString("name"));//名称
                //形态
                if ("1".equals(dept_id)) {//根部门
                    //usc_department.set("fsnl_forgpattenid",orgform01);//集团
                    usc_department.set("fsnl_forgpattenid_id",1);//集团
                }else if ("7".equals(dept_id)||"43".equals(dept_id)){//集团公司或者星星易装
                    //usc_department.set("fsnl_forgpattenid",orgform02);//公司
                    usc_department.set("fsnl_forgpattenid_id",2);//公司
                }else {
                    //usc_department.set("fsnl_forgpattenid",orgform06);//部门
                    usc_department.set("fsnl_forgpattenid_id",4);//部门
                }
                usc_department.set("enable","1");//使用状态，默认为可用，1
                //usc_department.set("level","0");//级次
                usc_department.set("fsnl_fhandword","0");//是否手工新增，默认否
                usc_department.set("creator",userid);//创建人
                usc_department.set("createtime",new Date());//创建时间
                usc_department.set("status","C");//数据状态，默认为已审核
                insertList.add(usc_department);
            }
        }
        //如果有需要update的数据，则构建需要update的数据，合并至insertList
        if (update_id_list.size()!=0){
            DynamicObject[] updateObjects = getDeptObject(update_id_list);
            for (DynamicObject updateObject : updateObjects) {
                JSONObject dept_jsonObject = wx_map.get(updateObject.getString("number"));
                String dept_id = dept_jsonObject.getString("id");
                //形态
                if ("1".equals(dept_id)) {//根部门
                    //usc_department.set("fsnl_forgpattenid",orgform01);//集团
                    updateObject.set("fsnl_forgpattenid_id",1);//集团
                }else if ("7".equals(dept_id)||"43".equals(dept_id)){//集团公司或者星星易装
                    //usc_department.set("fsnl_forgpattenid",orgform02);//公司
                    updateObject.set("fsnl_forgpattenid_id",2);//公司
                }else {
                    //usc_department.set("fsnl_forgpattenid",orgform06);//部门
                    updateObject.set("fsnl_forgpattenid_id",4);//部门
                }
                updateObject.set("name",dept_jsonObject.getString("name"));//名称
                updateObject.set("modifier",userid);//修改人
                updateObject.set("modifytime",new Date());//修改时间
                insertList.add(updateObject);
            }
        }
        //保存数据
        DynamicObject[] newInsertObjects = insertList.toArray(new DynamicObject[insertList.size()]);
        OperationResult operationResult = OperationServiceHelper.executeOperate("save", departmentName, newInsertObjects, OperateOption.create());
        //重新获取保存后的数据
        List<Object> successPkIds = operationResult.getSuccessPkIds();
        DynamicObject[] resultObjects = BusinessDataServiceHelper.load(successPkIds.toArray(), EntityMetadataCache.getDataEntityType(departmentName));
        //更新父部门
        setParentObject(resultObjects,wx_map);
        OperationServiceHelper.executeOperate("save", departmentName, resultObjects, OperateOption.create());
    }

    /**
     * 将企业微信人员信息保存至用户中心
     * @param wx_map
     * @param uscUserIdList
     */
    private static void saveUser(Map<String, JSONObject> wx_map,List<String> uscUserIdList){
        List<DynamicObject> insertList = new ArrayList<>();//需要进insert的数据
        List<String> update_id_list = new ArrayList<>();//需要进行update的数据

        DynamicObject userType = DynamicObjectHelper.getDynamicObject("bos_usertype", "PT01");//人员类型-->职员
        //获取部门信息
        List<String> uscDepartmentIdList = getUscDepartmentIdList();
        DynamicObject[] uscDept_dos = getDeptObject(uscDepartmentIdList);
        Map<String,DynamicObject> deptMap = new HashMap<>();
        if (uscDept_dos!=null) {
            for (DynamicObject uscDept_do : uscDept_dos) {
                //使用状态
                String enable = uscDept_do.getString("enable");
                //是否手工新增
                boolean handword = uscDept_do.getBoolean("fsnl_fhandword");
                //非禁用和非手工新增的才插入map
                if ("1".equals(enable)&&!handword)
                    deptMap.put(uscDept_do.getString("number"),uscDept_do);
            }
        }

        //如果id存在于用户中心，则为需要更新的数据，反之为需要insert的数据
        for (String id : wx_map.keySet()) {
            if (uscUserIdList.contains(id)) {
                //记录需要update的id
                update_id_list.add(id);
            }else {
                //构建insert数据
                DynamicObject usc_user = BusinessDataServiceHelper.newDynamicObject(userName);
                JSONObject user_jsonObject = wx_map.get(id);
                String wxUserid = user_jsonObject.getString("userid");
                //编码,对应企业微信的用户名的value，获取不到工号的，不同步
                JSONObject wxExtattr = user_jsonObject.getJSONObject("extattr");
                JSONArray wxAttrs = wxExtattr.getJSONArray("attrs");
                if (wxAttrs.size()!=0){
                    String number = wxAttrs.getJSONObject(0).getString("value");
                    usc_user.set("number",number);
                    String mobile = user_jsonObject.getString("mobile");//企业微信手机
                    usc_user.set("id",DBServiceHelper.genGlobalLongId());//内码
                    usc_user.set("name",user_jsonObject.getString("name"));//名称

                    //类型
                    DynamicObjectCollection userType_doc = usc_user.getDynamicObjectCollection("fsnl_fusertype");
                    DynamicObjectType userTypeObjectType = userType_doc.getDynamicObjectType();
                    DynamicObject newUserTypeRow = new DynamicObject(userTypeObjectType);
                    userType_doc.add(newUserTypeRow);
                    // 基础资料：存储每条基础资料的数据包，属性名是个常量fbasedataid
                    BasedataProp userType_basedataProp = (BasedataProp)userTypeObjectType.getProperties().get("fbasedataid");
                    userType_basedataProp.setValue(newUserTypeRow, userType);
                    //usc_department.set("fsnl_fidcard","");//身份证
                    usc_user.set("fsnl_fbirthday","");//生日
                    usc_user.set("fsnl_fgender",user_jsonObject.getString("gender"));//性别
                    //usc_department.set("fsnl_country","");//国家地区
                    usc_user.set("fsnl_fphone",mobile);//手机
                    //邮箱，对应企业微信的企业邮箱
                    String biz_mail = user_jsonObject.getString("biz_mail");
                    usc_user.set("fsnl_femail",biz_mail);
                    usc_user.set("fsnl_favatar",user_jsonObject.getString("avatar"));//头像
                    usc_user.set("enable","1");//使用状态，默认为可用，1
                    usc_user.set("fsnl_fhandword","0");//是否手工新增，默认否
                    usc_user.set("creator",userid);//创建人
                    usc_user.set("createtime",new Date());//创建时间
                    usc_user.set("status","C");//数据状态，默认为已审核

                    //人员组织信息
                    DynamicObjectCollection userDept_doc = usc_user.getDynamicObjectCollection("fsnl_userdept");//获取单据体数据集
                    JSONArray departmentIds = user_jsonObject.getJSONArray("department");
                    JSONArray isLeaderInDepts = user_jsonObject.getJSONArray("is_leader_in_dept");
                    for (int i = 0; i < departmentIds.size(); i++) {
                        String deptId = departmentIds.getString(i);
                        String leaderInDept = isLeaderInDepts.getString(i);
                        DynamicObject userdept_do = new DynamicObject(userDept_doc.getDynamicObjectType());//获取单据体数据类型
                        userdept_do.set("fsnl_fdeptid",deptMap.get(deptId));//用户中心组织
                        userdept_do.set("fsnl_fposition",user_jsonObject.getString("position"));//职位
                        userdept_do.set("fsnl_fisincharge",leaderInDept);//负责人
                        //userdept_do.set("fsnl_fispartjob","");//兼职
                        userDept_doc.add(userdept_do);
                    }

                    //人员联系方式
                    //人员联系方式-->手机
                    DynamicObjectCollection userContact_doc = usc_user.getDynamicObjectCollection("fsnl_usercontact");//获取单据体数据集
                    if (StringUtils.isNotEmpty(mobile)) {
                        DynamicObject userContact_phone = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                        userContact_phone.set("fsnl_fcontacttypeid_id",3);//类型-->手机
                        userContact_phone.set("fsnl_fcontact",mobile);//联系方式
                        userContact_phone.set("fsnl_fisdefault","1");//是否默认
                        userContact_doc.add(userContact_phone);
                    }
                    //人员联系方式-->地址
                    String address = user_jsonObject.getString("address");
                    if (StringUtils.isNotEmpty(address)) {
                        DynamicObject userContact_address = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                        userContact_address.set("fsnl_fcontacttypeid_id",1);//类型-->地址
                        userContact_address.set("fsnl_fcontact",address);//联系方式
                        userContact_address.set("fsnl_fisdefault","0");//是否默认
                        userContact_doc.add(userContact_address);
                    }
                    //人员联系方式-->邮箱
                    if (StringUtils.isNotEmpty(biz_mail)) {
                        DynamicObject userContact_address = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                        userContact_address.set("fsnl_fcontacttypeid_id",2);//类型-->邮箱
                        userContact_address.set("fsnl_fcontact",biz_mail);//联系方式
                        userContact_address.set("fsnl_fisdefault","0");//是否默认
                        userContact_doc.add(userContact_address);
                    }

                    //应用使用情况
                    DynamicObjectCollection userAppCase_doc = usc_user.getDynamicObjectCollection("fsnl_userappcase");//获取单据体数据集
                    DynamicObject userAppCase_do = new DynamicObject(userAppCase_doc.getDynamicObjectType());//获取单据体数据类型
                    userAppCase_do.set("fsnl_fappname","1");//系统名称,默认企业微信
                    userAppCase_do.set("fsnl_fuseage","0");//使用情况,默认启用
                    userAppCase_do.set("fsnl_fappusername",wxUserid);//账号
                    userAppCase_do.set("fsnl_userid",wxUserid);//用户id
                    userAppCase_doc.add(userAppCase_do);

                    insertList.add(usc_user);
                }
            }
        }
        //如果有需要update的数据
        if (update_id_list.size()!=0){
            List<Object> pkIds = getPkIdByUserId(userName, update_id_list);
            DynamicObject[] updateObjects = BusinessDataServiceHelper.load(pkIds.toArray(), EntityMetadataCache.getDataEntityType(userName));
            for (DynamicObject updateObject : updateObjects) {
                DynamicObjectCollection userAppCase = updateObject.getDynamicObjectCollection("fsnl_userappcase");
                if (userAppCase.size()!=0){
                    JSONObject user_jsonObject = wx_map.get(userAppCase.get(0).getString("fsnl_userid"));
                    updateObject.set("modifier",userid);//修改人
                    updateObject.set("modifytime",new Date());//修改时间
                    String mobile = user_jsonObject.getString("mobile");//企业微信手机
                    updateObject.set("name",user_jsonObject.getString("name"));//名称
                    //邮箱，对应企业微信的企业邮箱
                    String biz_mail = user_jsonObject.getString("biz_mail");
                    updateObject.set("fsnl_femail",biz_mail);
                    updateObject.set("fsnl_favatar",user_jsonObject.getString("avatar"));//头像
                    updateObject.set("fsnl_fgender",user_jsonObject.getString("gender"));//性别
                    updateObject.set("fsnl_fphone",mobile);//手机

                    //人员组织信息
                    DynamicObjectCollection userDept_doc = updateObject.getDynamicObjectCollection("fsnl_userdept");//获取单据体数据集
                    userDept_doc.removeAll(userDept_doc);
                    JSONArray departmentIds = user_jsonObject.getJSONArray("department");
                    JSONArray isLeaderInDepts = user_jsonObject.getJSONArray("is_leader_in_dept");
                    for (int i = 0; i < departmentIds.size(); i++) {
                        String deptId = departmentIds.getString(i);//部门编码
                        String leaderInDept = isLeaderInDepts.getString(i);
                        DynamicObject userdept_do = new DynamicObject(userDept_doc.getDynamicObjectType());//获取单据体数据类型
                        userdept_do.set("fsnl_fdeptid",deptMap.get(deptId));//用户中心组织
                        userdept_do.set("fsnl_fposition",user_jsonObject.getString("position"));//职位
                        userdept_do.set("fsnl_fisincharge",leaderInDept);//负责人
                        //userdept_do.set("fsnl_fispartjob","");//兼职
                        userDept_doc.add(userdept_do);
                    }

                    //人员联系方式
                    //人员联系方式-->手机
                    DynamicObjectCollection userContact_doc = updateObject.getDynamicObjectCollection("fsnl_usercontact");//获取单据体数据集
                    userContact_doc.removeAll(userContact_doc);
                    if (StringUtils.isNotEmpty(mobile)) {
                        DynamicObject userContact_phone = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                        userContact_phone.set("fsnl_fcontacttypeid_id",3);//类型-->手机
                        userContact_phone.set("fsnl_fcontact",mobile);//联系方式
                        userContact_phone.set("fsnl_fisdefault","1");//是否默认
                        userContact_doc.add(userContact_phone);
                    }
                    //人员联系方式-->地址
                    String address = user_jsonObject.getString("address");
                    if (StringUtils.isNotEmpty(address)) {
                        DynamicObject userContact_address = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                        userContact_address.set("fsnl_fcontacttypeid_id",1);//类型-->地址
                        userContact_address.set("fsnl_fcontact",address);//联系方式
                        userContact_address.set("fsnl_fisdefault","0");//是否默认
                        userContact_doc.add(userContact_address);
                    }
                    //人员联系方式-->邮箱
                    if (StringUtils.isNotEmpty(biz_mail)) {
                        DynamicObject userContact_address = new DynamicObject(userContact_doc.getDynamicObjectType());//获取单据体数据类型
                        userContact_address.set("fsnl_fcontacttypeid_id",2);//类型-->邮箱
                        userContact_address.set("fsnl_fcontact",biz_mail);//联系方式
                        userContact_address.set("fsnl_fisdefault","0");//是否默认
                        userContact_doc.add(userContact_address);
                    }
                    insertList.add(updateObject);
                }
            }
        }
        //保存数据
        DynamicObject[] newInsertObjects = insertList.toArray(new DynamicObject[insertList.size()]);
        //补充直接上级数据
        OperationResult operationResult = OperationServiceHelper.executeOperate("save", userName, newInsertObjects, OperateOption.create());
        List<Object> successPkIds = operationResult.getSuccessPkIds();
        DynamicObject[] newUserObjects = BusinessDataServiceHelper.load(successPkIds.toArray(), EntityMetadataCache.getDataEntityType(userName));
        setSuperiorObject(newUserObjects,wx_map);
        OperationServiceHelper.executeOperate("save", userName, newUserObjects, OperateOption.create());
    }



    /**
     * 根据企业微信的userid获取用户中心的人员的主键id
     * @param entityName 表单标识
     * @param userIds 编码
     * @return
     */
    private static List<Object> getPkIdByUserId(String entityName,List<String> userIds){
        QFilter userId_filter = new QFilter("fsnl_userappcase.fsnl_userId", "in", userIds);
        QFilter appName_filter = new QFilter("fsnl_userappcase.fsnl_fappname", "=", "1");
        QFilter[] qFilters = {userId_filter,appName_filter};
        List<Object> ids = QueryServiceHelper.queryPrimaryKeys(entityName, qFilters, null, userIds.size());
        return ids;
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
     * 获取用户中心所有非禁用的userId(企业微信的用户ID字段)
     * @return
     */
    private static List<String> getUscUserIdList(){
        QFilter enable_filter = new QFilter("enable", "!=", "0");//非禁用
        QFilter handword_filter = new QFilter("fsnl_fhandword", "!=", "1");//非手工新增
        QFilter appName_filter = new QFilter("fsnl_userappcase.fsnl_fappname", "=", "1");//企业微信
        QFilter[] qFilters = {enable_filter,handword_filter,appName_filter};
        String fsnl_userid = "fsnl_userappcase.fsnl_userid";
        DynamicObject[] usc_User = BusinessDataServiceHelper.load(userName, fsnl_userid, qFilters);
        List<String> list = new ArrayList<>();
        for (DynamicObject dynamicObject : usc_User) {
            DynamicObjectCollection fsnl_userappcase = dynamicObject.getDynamicObjectCollection("fsnl_userappcase");
            String string = fsnl_userappcase.get(0).get("fsnl_userid").toString();
            list.add(string);
        }
        return list;
    }


    /**
     * 判断用户中心的id是否存在于企业微信，构建不存在部门的id集合
     * @param wxIdList 企业微信部门/人员的id集合
     * @param uscIdList 用户用心部门/人员的id集合
     * @return
     */
    private static List<String> getUpdateIdList(List<String> wxIdList,List<String> uscIdList){
        List<String> list = new ArrayList<>();
        if (!(uscIdList.size()==0||uscIdList==null)) {
            for (String id : wxIdList) {
                if (!uscIdList.contains(id)) {
                    list.add(id);
                }
            }
        }
        return list;
    }

    /**
     * 更新禁用部门
     * @param ids 部门Id
     */
    private static void updateDeptDisable(List<String> ids){
        if (ids.size()!=0) {
            DynamicObjectType dataEntityType = EntityMetadataCache.getDataEntityType(departmentName);
            List<Object> pkIds = DynamicObjectHelper.getPkIdByNumbers(departmentName, ids);
            DynamicObject[] deptObjects = BusinessDataServiceHelper.load(pkIds.toArray(), dataEntityType);
            for (DynamicObject deptObject : deptObjects) {
                deptObject.set("enable","0");
                deptObject.set("fsnl_Fenabler",userid);
                deptObject.set("fsnl_Fenabletime",new Date());
            }
            OperationServiceHelper.executeOperate("save", departmentName, deptObjects, OperateOption.create());
        }
    }

    /**
     * 更新禁用的人员
     * @param ids 人员id
     */
    private static void updateUserDisable(List<String> ids){
        if (ids.size()!=0) {
            DynamicObjectType dataEntityType = EntityMetadataCache.getDataEntityType(userName);
            List<Object> pkIds = DynamicObjectHelper.getPkIdByNumbers(userName, ids);
            DynamicObject[] dynamicObjects = BusinessDataServiceHelper.load(pkIds.toArray(), dataEntityType);
            for (DynamicObject dynamicObject : dynamicObjects) {
                dynamicObject.set("enable","0");
                dynamicObject.set("fsnl_Fenabler",userid);
                dynamicObject.set("fsnl_Fenabletime",new Date());
                //应用使用情况
                DynamicObjectCollection userAppCase_doc = dynamicObject.getDynamicObjectCollection("fsnl_userappcase");//获取单据体数据集
                for (DynamicObject appCase : userAppCase_doc) {
                    String fsnl_fappname = appCase.getString("fsnl_fappname");
                    if ("1".equals(fsnl_fappname)) {
                        appCase.set("fsnl_fuseage","1");//使用情况,默认启用
                        break;
                    }
                }
            }
            OperationServiceHelper.executeOperate("save", userName, dynamicObjects, OperateOption.create());
        }
    }



    /**
     * 赋值上级组织对象
     * @param usc_do 组织对象数值
     * @param wx_map 企业微信数据
     */
    private static void setParentObject(DynamicObject[] usc_do,Map<String, JSONObject> wx_map){
        HashMap<String, DynamicObject> usc_Map = new HashMap<>();
        for (DynamicObject dynamicObject : usc_do) {
            usc_Map.put(dynamicObject.getString("number"),dynamicObject);
        }
        for (DynamicObject dynamicObject : usc_do) {
            JSONObject object = wx_map.get(dynamicObject.get("number"));
            dynamicObject.set("parent",usc_Map.get(object.getString("parentid")));
        }
    }

    /**
     * 将企业微信数据转换成map,方便判断处理
     * @param jsonArray 企业微信数据
     * @param key map的key
     * @return
     */
    private static Map<String,JSONObject> getWxDeptMap(JSONArray jsonArray,String key){
        Map<String,JSONObject> map = new HashMap<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject dept_jsonObject = jsonArray.getJSONObject(i);
            map.put(dept_jsonObject.getString(key),dept_jsonObject);
        }
        return map;
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
     * @param wx_map 企业微信数据
     */
    private static void setSuperiorObject(DynamicObject[] usc_do,Map<String, JSONObject> wx_map){
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
                    JSONArray direct_leaders = wx_map.get(fsnl_userid).getJSONArray("direct_leader");
                    if (direct_leaders.size()!=0) {
                        DynamicObjectCollection userDet = dynamicObject.getDynamicObjectCollection("fsnl_userdept");
                        userDet.get(0).set("fsnl_fsuperiorid",usc_Map.get(direct_leaders.get(0)));
                    }
                }
            }

        }
    }
}
