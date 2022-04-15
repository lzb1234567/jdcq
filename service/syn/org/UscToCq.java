package fsnl.service.syn.org;

import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.property.BasedataProp;
import kd.bos.org.model.OrgParam;
import kd.bos.orm.query.QFilter;
import kd.bos.permission.model.UserParam;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.org.OrgUnitServiceHelper;
import kd.bos.servicehelper.org.OrgViewType;
import kd.bos.servicehelper.user.UserServiceHelper;

import java.util.*;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：用户中心同步数据到苍穹平台的处理类
 * @ClassName：UscToCq.java
 * @Date 2022/3/7 12:03
 */
public class UscToCq {
    private static String cqDepartmentName = "bos_org";//苍穹平台组织的标识
    private static String uscDepartmentName = "fsnl_usc_department";//用户中心组织的标识
    private static String cqUserName = "bos_user";//苍穹平台人员的标识
    private static String uscUserName = "fsnl_usc_user";//用户中心人员的标识

    /**
     * 对外开放的同步任务
     */
    public static void sysJob(){
        //同步部门
        departmentSyn();
        //同步人员
        userSyn();
    }


    /**
     * 同步组织数据
     */
    private static void departmentSyn(){
        //1.获取用户中心部门列表
        QFilter enable_filter = new QFilter("enable", "!=", "0");//非禁用
        QFilter handword_filter = new QFilter("fsnl_fhandword", "!=", "1");//非手工新增
        QFilter[] usc_qFilters = {enable_filter,handword_filter};
        Map<String, DynamicObject> uscDept_map = getObjectMap(uscDepartmentName,"id,name,number,fsnl_forgpattenid_id,parent", usc_qFilters);
        //2.获取用户中心所有部门ID
        Map<String, String> uscDepartmentIdMap = getIdList(uscDepartmentName,usc_qFilters);
        //3.获取苍穹平台所有部门ID
        QFilter[] cq_qFilters = {new QFilter("enable", "!=", "0")};
        Map<String, String> cqDepartmentIdMap = getIdList(cqDepartmentName,cq_qFilters);
        //4.保存部门列表
        saveDepartment(uscDept_map,cqDepartmentIdMap);
        //5.判断用户中心的部门id是否存于企业微信，获得需要更新禁用状态的id列表
        List<OrgParam> updateUscDeptIdList = getUpdateDeptIdList(uscDepartmentIdMap,cqDepartmentIdMap);
        //6.更新禁用状态
        OrgUnitServiceHelper.disable(updateUscDeptIdList);
    }

    /**
     * 同步人员数据
     */
    private static void userSyn(){
        //1.获取用户中心人员列表
        QFilter enable_filter = new QFilter("enable", "!=", "0");//非禁用
        QFilter handword_filter = new QFilter("fsnl_fhandword", "!=", "1");//非手工新增
        QFilter[] usc_qFilters = {enable_filter,handword_filter};
        //需要查询的字段
        String selectProperties = "id,name,number,fsnl_fusertype,fsnl_fphone,fsnl_femail,fsnl_fidcard,fsnl_fbirthday,fsnl_favatar,fsnl_fgender," +//表头
                "fsnl_userdept.fsnl_fdeptid,fsnl_userdept.fsnl_fposition,fsnl_userdept.fsnl_fisincharge,fsnl_userdept.fsnl_fispartjob,fsnl_userdept.fsnl_fsuperiorid," +//部门列表
                "fsnl_usercontact.fsnl_fcontacttypeid,fsnl_usercontact.fsnl_fcontact,fsnl_usercontact.fsnl_fisdefault";//联系方式
        Map<String, DynamicObject> uscUser_map = getObjectMap(uscUserName,selectProperties, usc_qFilters);
        //2.获取用户中心所有人员ID
        Map<String, String> uscUserIdMap = getIdList(uscUserName,usc_qFilters);
        //3.获取苍穹平台所有人员ID
        QFilter[] cq_qFilters = {new QFilter("enable", "!=", "0")};
        Map<String, String> cqUserIdMap = getIdList(cqUserName,cq_qFilters);
        //4.保存人员列表
        saveUser(uscUser_map,cqUserIdMap);
        //5.判断用户中心的人员id是否存于企业微信，获得需要更新禁用状态的id列表
        List<UserParam> updateUscUserIdList = getUpdateUserIdList(uscUserIdMap,cqUserIdMap);
        //6.更新禁用状态
        UserServiceHelper.disable(updateUscUserIdList);
    }

    /**
     *
     * @param usc_map
     * @param cqDepartmentIdMap
     */
    private static void saveDepartment(Map<String, DynamicObject> usc_map,Map<String, String> cqDepartmentIdMap){
        List<OrgParam> insertList = new ArrayList<>();//需要进insert的数据
        List<OrgParam> updateList = new ArrayList<>();//需要进行update的数据
        long orgId = RequestContext.get().getOrgId();//根组织id
        QFilter viewid = new QFilter("View", "=", 1);
        QFilter[] qFilters = {viewid};
        DynamicObject[] usc_hrViews = BusinessDataServiceHelper.load("bos_org_structure", "org_id", qFilters);
        List<String> hrViewLsit = new ArrayList<>();
        for (DynamicObject usc_hrView : usc_hrViews) {
            hrViewLsit.add(usc_hrView.getString("org_id"));
        }
         //如果id存在于用户中心，则为需要更新的数据，反之为需要insert的数据
        for (String id : usc_map.keySet()) {
            DynamicObject uscDept_do = usc_map.get(id);
            OrgParam param = new OrgParam();
            param.setName(uscDept_do.getString("name"));
            param.setNumber(uscDept_do.getString("number"));
            //组织形态
            param.setOrgPatternId(uscDept_do.getLong("fsnl_forgpattenid_id"));
            param.setDuty(OrgViewType.Admin);
            if (hrViewLsit.contains(id)) {
                //构建update数据
                param.setId(Long.parseLong(cqDepartmentIdMap.get(id)));
                updateList.add(param);
            }else {
                //构建insert数据
                param.setParentId(orgId);//上级组织
                insertList.add(param);
            }
        }
        //记录操作过的id
        List<Object> ids = new ArrayList<>();
        //保存数据
        OrgUnitServiceHelper.add(insertList);
        getResultOrgIds(insertList,ids);
        //修改数据
        OrgUnitServiceHelper.update(updateList);
        getResultOrgIds(updateList,ids);
        //清空update数据,并重新获取需要操作的数据
        updateList = new ArrayList<>();
        DynamicObject[] cqDepartmentObjects = BusinessDataServiceHelper.load(ids.toArray(), EntityMetadataCache.getDataEntityType(cqDepartmentName));
        HashMap<String, DynamicObject> cq_Map = new HashMap<>();
        for (DynamicObject cqDepartmentObject : cqDepartmentObjects) {
            cq_Map.put(cqDepartmentObject.getString("number"),cqDepartmentObject);
        }
        for (DynamicObject cqDepartmentObject : cqDepartmentObjects) {
            OrgParam param = new OrgParam();
            String number = cqDepartmentObject.getString("number");
            param.setId(cqDepartmentObject.getLong("id"));
            param.setName(cqDepartmentObject.getString("name"));
            param.setNumber(number);
            //组织形态
            param.setOrgPatternId(cqDepartmentObject.getLong("orgpattern_id"));
            if (!("7".equals(number)||"43".equals(number))) {
                String parent_number= usc_map.get(number).getString("parent.number");//用户中心对应的父部门编码
                long parentId = cq_Map.get(parent_number).getLong("id");//通过父部门编码得到对应苍穹的组织id，即为上级组织id
                param.setParentId(parentId);//上级组织
            }
            param.setDuty(OrgViewType.Admin);
            updateList.add(param);
        }
        //更新部门信息
        OrgUnitServiceHelper.update(updateList);
    }

    /**
     * 将企业微信人员信息保存至苍穹
     * @param usc_map
     * @param cqUserIdMap
     */
    private static void saveUser(Map<String, DynamicObject> usc_map,Map<String, String> cqUserIdMap){
        List<UserParam> insertList = new ArrayList<>();//需要进行insert的数据
        List<UserParam> updateList = new ArrayList<>();//需要进行update的数据
        for (String jobNumber : usc_map.keySet()) {
            DynamicObject uscUser_do = usc_map.get(jobNumber);
            UserParam param = new UserParam();
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("number", uscUser_do.getString("number"));//编码，即工号
            dataMap.put("name", uscUser_do.getString("name"));//名称
            //dataMap.put("username", "用户名");
            //获取用户中心人员类型
            DynamicObjectCollection userType_doc = uscUser_do.getDynamicObjectCollection("fsnl_fusertype");
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
            dataMap.put("phone", uscUser_do.getString("fsnl_fphone"));//电话
            dataMap.put("email", uscUser_do.getString("fsnl_femail"));//邮箱
            dataMap.put("idcard", uscUser_do.getString("fsnl_fidcard"));//身份证号
            dataMap.put("birthday", uscUser_do.getString("fsnl_fbirthday"));//生日
            dataMap.put("gender", uscUser_do.getString("fsnl_fgender"));//性别
            dataMap.put("picturefield", uscUser_do.getString("fsnl_favatar"));//头像

            // 职位分录
            List<Map<String, Object>> posList = new ArrayList<>();
            DynamicObjectCollection uscUserDepts = uscUser_do.getDynamicObjectCollection("fsnl_userdept");
            for (int i = 0; i < uscUserDepts.size(); i++) {
                DynamicObject userDept = uscUserDepts.get(i);
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
            DynamicObjectCollection uscUserContacts = uscUser_do.getDynamicObjectCollection("fsnl_usercontact");
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

            if (cqUserIdMap.keySet().contains(jobNumber)) {
                //构建update数据
                param.setId(Long.parseLong(cqUserIdMap.get(jobNumber)));
                param.setDataMap(dataMap);
                updateList.add(param);
            }else {
                //构建insert数据
                param.setDataMap(dataMap);
                insertList.add(param);
            }
        }
        //保存数据

        //修改数据
        UserServiceHelper.update(updateList);
        //更新上级部门
        updateList.addAll(insertList);//合并所有数据，一起修改
        for (UserParam userParam : updateList) {
            Map<String, Object> dataMap = userParam.getDataMap();
            //通过工号获取用户中心的人员部门信息，并构建部门编码和直接上级的映射关系，使用map进行存储
            String jobNumber = dataMap.get("number").toString();
            DynamicObject uscUser_do = usc_map.get(jobNumber);
            DynamicObjectCollection uscUserDepts = uscUser_do.getDynamicObjectCollection("fsnl_userdept");
            Map<String,String> uscUserDeptmap = new HashMap<>();
            for (DynamicObject uscUserDept : uscUserDepts) {
                //部门
                DynamicObject  dept_do = uscUserDept.getDynamicObject("fsnl_fdeptid");
                //直接上级
                DynamicObject superior_do = uscUserDept.getDynamicObject("fsnl_fsuperiorid");
                //key为部门的编码，value为直接上级的人员编码
                if (superior_do!=null)
                    uscUserDeptmap.put(dept_do.getString("number"),superior_do.getString("number"));
            }
            //遍历苍穹人员部门信息，根据部门信息的部门编码匹配直属上级
            List<Map<String, Object>> cqEntryEntityList = (List<Map<String, Object>>)dataMap.get("entryentity");//苍穹部门单据体
            for (Map<String, Object> cqDeptMap : cqEntryEntityList) {
                Map<String,Object> cqDept = (Map<String,Object>)cqDeptMap.get("dpt");
                String cqDeptNumber = cqDept.get("number").toString();
                //设置部门编码
                Map<String, Object> cqSuperiorNumMap = new HashMap<>();
                cqSuperiorNumMap.put("number", uscUserDeptmap.get(cqDeptNumber));
                cqDeptMap.put("superior", cqSuperiorNumMap);//直接上级
            }
        }
        UserServiceHelper.update(updateList);
    }

    /**
     * 获取对象id集合
     * @param entityName
     * @param qFilters
     * @return
     */
    private static Map<String,String> getIdList(String entityName,QFilter[] qFilters){
        DynamicObject[] usc_departments = BusinessDataServiceHelper.load(entityName, "id,number", qFilters);
        Map<String,String> map = new HashMap<>();
        for (DynamicObject usc_department : usc_departments) {
            map.put(usc_department.getString("number"),usc_department.getString("id"));
        }
        return map;
    }

    /**
     * 判断用户中心的id是否存在于企业微信，构建不存在部门的id集合
     * @param uscIdMap 用户中心部门的id集合
     * @param cqIdMap 苍穹平台部门的id集合
     * @return
     */
    private static List<OrgParam> getUpdateDeptIdList(Map<String,String> uscIdMap,Map<String,String> cqIdMap){
        List<OrgParam> paramList = new ArrayList<>();
        for (String id : uscIdMap.keySet()) {
            OrgParam param = new OrgParam();
            if (!cqIdMap.keySet().contains(id))
                param.setId(Long.parseLong(uscIdMap.get(id)));
        }
        return paramList;
    }

    /**
     * 判断用户中心的id是否存在于企业微信，构建不存在部门的id集合
     * @param uscIdMap 用户中心部门的id集合
     * @param cqIdMap 苍穹平台部门的id集合
     * @return
     */
    private static List<UserParam> getUpdateUserIdList(Map<String,String> uscIdMap,Map<String,String> cqIdMap){
        List<UserParam> paramList = new ArrayList<>();
        for (String id : uscIdMap.keySet()) {
            UserParam param = new UserParam();
            if (!cqIdMap.keySet().contains(id))
                param.setId(Long.parseLong(uscIdMap.get(id)));
        }
        return paramList;
    }

    /**
     * 获取对象数据
     * @param entityName 对象标识，即表单标识
     * @param qFilters 过滤条件
     * @return
     */
    private static Map<String,DynamicObject> getObjectMap(String entityName,String selectProperties,QFilter[] qFilters){
        Map<String,DynamicObject> map = new HashMap<>();
        DynamicObject[] entity_do = BusinessDataServiceHelper.load(entityName, selectProperties,qFilters);
        for (DynamicObject entity : entity_do) {
            //过来掉根部门
            if (!entity.getString("number").equals("1"))
                map.put(entity.getString("number"),entity);
        }
        return map;
    }

    /**
     * 记录操作的OrgId
     * @param orgParamList 组织数据
     * @param list 存储id的集合
     */
    private static void getResultOrgIds(List<OrgParam> orgParamList,List<Object> list){
        for (OrgParam orgParam : orgParamList) {
            if (orgParam.isSuccess())
                list.add(orgParam.getId());
        }
    }
}
