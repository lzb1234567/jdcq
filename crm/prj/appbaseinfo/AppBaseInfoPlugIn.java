package fsnl.crm.prj.appbaseinfo;

import fsnl.common.util.number.DoubleUtil;
import fsnl.common.util.str.StringUtil;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.control.Control;
import kd.bos.form.field.BasedataEdit;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.fi.bcm.formplugin.database.BaseDataEditExt;
import org.apache.poi.hpsf.Decimal;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.EventObject;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：工程报备表单插件：字段必填项控制，工程定位功能
 * @ClassName：AppBaseInfoPlugIn.java
 * @Date 2022/5/19 9:56
 */
public class AppBaseInfoPlugIn extends AbstractBillPlugIn {

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
                this.findNeighPosition(longitude,dimensionality,id);
            }
        }
    }

    /**
     * 1、查找方圆500米内是否有工程，如果有则填写提示信息。
     * 2、判断是否有重复的工程，如果有则提示信息。
     * @param longitude
     * @param dimensionality
     * @param billAddresId
     * @return
     */
    private void findNeighPosition(String longitude,String dimensionality,long billAddresId){
        //经度
        double lng = Double.parseDouble(longitude);
        //维度
        double lat = Double.parseDouble(dimensionality);
        QFilter qFilter_source = new QFilter("source","=","fsnl_prj_appbaseinfo");//单据标识，即工程报备单标识
        QFilter qFilter_id = new QFilter("id","!=",billAddresId);//将本单关联的地址信息排除
        QFilter qFilter_lat = new QFilter("longitude","=", lng);//经度
        QFilter qFilter_lng = new QFilter("latitude","=",lat);//维度
        QFilter[] filters_same = {qFilter_lat,qFilter_lng,qFilter_source,qFilter_id};
        //查询插件地址对象中与所选坐标相同的数据
        DynamicObject[] address_same = BusinessDataServiceHelper.load("cts_address", "id", filters_same);
        //判断是否存在相同的报备项目
        if (isInBaseInfo(address_same))
            this.getView().getModel().setValue("fsnl_fwarnmessage","已有工程项目，请检查是否重复报备！");
        else{
            //先计算查询点的经纬度范围
            double r = 6371;//地球半径千米
            double dis = 0.5;//0.5千米距离
            double dlng =  2*Math.asin(Math.sin(dis/(2*r))/Math.cos(lat*Math.PI/180));
            dlng = dlng*180/Math.PI;//角度转为弧度
            double dlat = dis/r;
            dlat = dlat*180/Math.PI;
            double minlat =lat-dlat;
            double maxlat = lat+dlat;
            double minlng = lng -dlng;
            double maxlng = lng + dlng;
            //判断方圆区间是否有工程报备
            QFilter qFilter_minlng = new QFilter("longitude",">", minlng);
            QFilter qFilter_maxlng = new QFilter("longitude","<",maxlng);
            QFilter qFilter_minlat = new QFilter("latitude",">",minlat);
            QFilter qFilter_maxlat = new QFilter("latitude","<",maxlat);
            QFilter[] filters_scope = {qFilter_minlat,qFilter_maxlat,qFilter_minlng,qFilter_maxlng,qFilter_source,qFilter_id};
            //查询插件地址对象中符合桌标范围的数据
            DynamicObject[] address_scope = BusinessDataServiceHelper.load("cts_address", "id", filters_scope);
            if (isInBaseInfo(address_scope))
                this.getView().getModel().setValue("fsnl_fwarnmessage","附近有工程项目，请检查是否重复报备！");
        }
    }

    /**
     * 按地址信息查询是否存在报备申请单数据
     * @param address 地址信息
     * @return 是否存在数据
     */
    private boolean isInBaseInfo(DynamicObject[] address){
        //遍历地址数据，拼接id条件
        StringBuffer ids_sb = new StringBuffer();
        for (DynamicObject addressInfo : address)
            ids_sb.append(addressInfo.getString("id")+",");
        QFilter qFilter_ids = new QFilter("id","in", StringUtil.removeTail(ids_sb));
        QFilter[] filters_exists = {qFilter_ids};
        return QueryServiceHelper.exists("fsnl_prj_appbaseinfo", filters_exists);
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
