package fsnl.crm.prj.appbaseinfo;

import fsnl.common.util.str.StringUtil;
import fsnl.service.kingdee.AdminDvisionHelper;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.ext.form.control.MapControl;
import kd.bos.ext.form.control.events.MapSelectEvent;
import kd.bos.ext.form.control.events.MapSelectListener;
import kd.bos.form.field.BasedataEdit;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：工程报备表单插件：字段必填项控制，工程定位功能
 * @ClassName：AppBaseInfoPlugIn.java
 * @Date 2022/5/19 9:56
 */
public class AppBaseInfoPlugIn extends AbstractBillPlugIn implements MapSelectListener {

    @Override
    public void registerListener(EventObject e) {
        this.addItemClickListeners("tbmain");
        MapControl mapControl = this.getControl("fsnl_mapcontrolap");
        mapControl.addSelectListener(this);
    }

    @Override
    public void afterBindData(EventObject e) {
        this.setMustInput();
    }


    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        IDataEntityProperty property = e.getProperty();
        //工程性质
        if ("fsnl_fprjproperty".equals(property.getName())) {
            this.setMustInput();
        }
        //工程定位
        if ("fsnl_flocation".equals(property.getName())) {
            //工程定位信息
            Object fsnl_flocation = this.getView().getModel().getValue("fsnl_flocation");
            if (fsnl_flocation!=null){
                DynamicObject flcnationObj = (DynamicObject)fsnl_flocation;
                //本单地址id
                long id = flcnationObj.getLong("id");
                //经度
                String longitude = flcnationObj.getString("longitude");
                //维度
                String dimensionality = flcnationObj.getString("latitude");
            }
        }
    }

    /**
     * 处理地图，完成工程定位功能。
     * @param evt
     */
    @Override
    public void select(MapSelectEvent evt) {
        MapSelectListener.super.select(evt);
        Map<String, Object> map = evt.getPoint();
        Object pointObj = map.get("point");
        String lng = null;//经度
        String lat = null;//维度
        //模仿金蝶的实现方式，此处判断是否为hashMap类型
        if (pointObj instanceof HashMap) {
            Map<String, Object> point = (HashMap)pointObj;
            lng = point.get("lng").toString();
            lat = point.get("lat").toString();
        }
        //模仿金蝶的实现方式，此处判断是否存在别的坐标KEY标识，盲猜以前存在过这两个key
        if (map.containsKey("latitude") && map.containsKey("longitude")) {
            lat = map.get("latitude").toString();
            lng = map.get("longitude").toString();
        }
        this.getView().getModel().setValue("fsnl_lng",lng);//经度
        this.getView().getModel().setValue("fsnl_lat", lat);//维度

        //国家或地区
        //this.getView().getModel().setValue("fsnl_fcountry",dvisions.get("country"));
        //省
        String province = map.get("province").toString();
        this.getView().getModel().setValue("fsnl_fprovince",getDvisionId(province));
        //市
        String city = map.get("city").toString();
        this.getView().getModel().setValue("fsnl_fcity",getDvisionId(city));
        //详细地址
        this.getView().getModel().setValue("fsnl_faddress",map.get("address"));
        //定位预警信息
        findNeighPosition(lng,lat);
        /*以下代码使用到了百度地图API，由于暂时不考虑购买商用的百度地图api，先不使用该实现方式
        Map<String, String> dvisions = AdminDvisionHelper.findDvisions(evt);
        //经度
        String lng = dvisions.get("lng");
        this.getView().getModel().setValue("fsnl_lng",lng);
        //维度
        String lat = dvisions.get("lat");
        this.getView().getModel().setValue("fsnl_lat", lat);
        //国家或地区
        this.getView().getModel().setValue("fsnl_fcountry",dvisions.get("country"));
        //省
        this.getView().getModel().setValue("fsnl_fprovince",dvisions.get("province"));
        //市
        this.getView().getModel().setValue("fsnl_fcity",dvisions.get("city"));
        //区县
        this.getView().getModel().setValue("fsnl_fdistrict",dvisions.get("district"));
        //详细地址
        this.getView().getModel().setValue("fsnl_faddress",dvisions.get("address"));
        //定位预警信息
        findNeighPosition(lng,lat);*/
    }

    /**
     * 根据地图控件返回的行政区划名称查询基础资料行政区划的id
     * @param name 行政区划名称
     * @return 行政区划基础资料id
     */
    private String getDvisionId(String name){
        QFilter filter_name = new QFilter("name", "=", name);
        QFilter filter_enable = new QFilter("enable", "=", "1");
        QFilter[] qFilters = {filter_name,filter_enable};
        DynamicObject[] dvisions_do = BusinessDataServiceHelper.loadFromCache("bd_admindivision", "id", qFilters).values().toArray(new DynamicObject[0]);
        return dvisions_do[0].getString("id");
    }

    /**
     * 1、查找方圆500米内是否有工程，如果有则填写提示信息。
     * 2、判断是否有重复的工程，如果有则提示信息。
     * @param longitude
     * @param dimensionality
     * @return
     */
    private void findNeighPosition(String longitude,String dimensionality){
        //经度
        double lng = Double.parseDouble(longitude);
        //维度
        double lat = Double.parseDouble(dimensionality);
        QFilter qFilter_lat = new QFilter("fsnl_lng","=", lng);//经度
        QFilter qFilter_lng = new QFilter("fsnl_lat","=",lat);//维度
        QFilter[] filters_same = {qFilter_lat,qFilter_lng};
        //查询插件地址对象中与所选坐标相同的数据
        DynamicObject[] address_same = BusinessDataServiceHelper.load("fsnl_prj_appbaseinfo", "id", filters_same);
        String billNos_same = getBillNo(address_same);
        //判断是否存在相同的报备项目
        if (billNos_same!=null)
            this.getView().getModel().setValue("fsnl_fwarnmessage","以下报备单的工程定位与当前的工程定位坐标相同，请确认！！！\n报备单号："+billNos_same);
        else{
            //先计算查询点的经纬度范围
            double r = 6371;//地球半径千米
            double dis = 0.5;//0.5千米距离，需实现使用系统参数来完成配置形式
            double dlng =  2 * Math.asin(Math.sin(dis / (2 * r))/Math.cos(lat * Math.PI / 180));
            dlng = dlng*180 / Math.PI;//角度转为弧度
            double dlat = dis / r;
            dlat = dlat * 180 / Math.PI;
            double minlat = lat - dlat;
            double maxlat = lat + dlat;
            double minlng = lng - dlng;
            double maxlng = lng + dlng;
            //判断方圆区间是否有工程报备
            QFilter qFilter_minlng = new QFilter("fsnl_lng",">", minlng);
            QFilter qFilter_maxlng = new QFilter("fsnl_lng","<",maxlng);
            QFilter qFilter_minlat = new QFilter("fsnl_lat",">",minlat);
            QFilter qFilter_maxlat = new QFilter("fsnl_lat","<",maxlat);
            QFilter[] filters_scope = {qFilter_minlat,qFilter_maxlat,qFilter_minlng,qFilter_maxlng};
            //查询插件地址对象中符合桌标范围的数据
            DynamicObject[] address_scope = BusinessDataServiceHelper.load("fsnl_prj_appbaseinfo", "id", filters_scope);
            String billNo_scope = getBillNo(address_scope);
            if (billNo_scope!=null)
                this.getView().getModel().setValue("fsnl_fwarnmessage","当前工程定位500米方位内存在以下工程报备单，请确认！！！\n报备单号："+billNos_same);
            else
                this.getView().getModel().setValue("fsnl_fwarnmessage","");//清空预警
        }
    }

    /**
     * 根据地址信息获取工程报备单号
     * @param address 地址信息
     * @return 工程报备单号集合，字符串，用“，”号隔开
     */
    private String getBillNo(DynamicObject[] address){
        //遍历地址数据，拼接id条件
        StringBuffer ids_sb = new StringBuffer();
        for (DynamicObject addressInfo : address)
            ids_sb.append(addressInfo.getString("id")+",");
        if (ids_sb.length()!=0){
            QFilter qFilter = new QFilter("id","in", StringUtil.removeTail(ids_sb));
            QFilter[] filters = {qFilter};
            DynamicObject[] address_scopes = BusinessDataServiceHelper.load("fsnl_prj_appbaseinfo", "billno", filters);
            StringBuffer billno_sb = new StringBuffer();
            for (DynamicObject address_scope : address_scopes) {
                billno_sb.append(address_scope.getString("billno")+",");
            }
            if (billno_sb.length()!=0)
                return StringUtil.removeTail(billno_sb);
        }
        return null;
    }


    /**
     * 根据工程性值设置相关字段的必填性。
     */
    private void setMustInput(){
        //工程性质
        String fprjproperty = null;
        Object fsnl_fprjproperty = this.getView().getModel().getValue("fsnl_fprjproperty");
        if (fsnl_fprjproperty!=null) {
            DynamicObject prjpropertyObj = (DynamicObject)fsnl_fprjproperty;
            fprjproperty = prjpropertyObj.getString("number");
        }
        //战略协议控件
        BasedataEdit fsnl_fstrcontract = this.getView().getControl("fsnl_fstrcontract");
        //甲方名称控件
        BasedataEdit fsnl_ffirstparty = this.getView().getControl("fsnl_ffirstparty");
        //乙方名称控件
        BasedataEdit fsnl_fsecondparty = this.getView().getControl("fsnl_fsecondparty");
        //设置必填性
        if ("centralized_purchasing_prj".equals(fprjproperty)) {//集采工程
            fsnl_fstrcontract.setMustInput(true);
            fsnl_ffirstparty.setMustInput(true);
            fsnl_fsecondparty.setMustInput(true);
        }else{
            fsnl_fstrcontract.setMustInput(false);
            fsnl_ffirstparty.setMustInput(false);
            fsnl_fsecondparty.setMustInput(false);
        }
    }

}
