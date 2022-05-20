package fsnl.global.area;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.entity.datamodel.events.ChangeData;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.property.*;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;


/**
 * @author 陆海泳
 * @version 1.00
 * @description：总面积计算的表单插件
 * @ClassName：TotalAreaCount.java
 * @Date 2022/5/11 16:57
 */
public class TotalAreaCount extends AbstractBillPlugIn {
    /*总数量字段标识*/
    private String qtyTagKey = null;
    /*物料字段标识*/
    private String materielTagKey = null;
    /*汇总字段标识*/
    private String totalTagKey = null;

    /**
     * 表单视图模型初始化，创建插件后，触发此事件
     * 初始化，获取参加计算的相关标识信息
     */
    @Override
    public void initialize() {
        //获取当前业务单据标识
        String entityId = this.getView().getEntityId();
        //查询配置信息
        QFilter[] qFilters = {new QFilter("fsnl_bill","=",entityId)};
        DynamicObject[] peoh_test2s = BusinessDataServiceHelper.load("fsnl_areacalculateconfig", "fsnl_qtyTag,fsnl_materielTag,fsnl_totalTag", qFilters);
        if (peoh_test2s.length == 1){
            DynamicObject peoh_test2 = peoh_test2s[0];
            /*总数量字段标识*/
            qtyTagKey = peoh_test2.getString("fsnl_qtyTag");
            /*物料字段标识*/
            materielTagKey = peoh_test2.getString("fsnl_materielTag");
            /*汇总字段标识*/
            totalTagKey = peoh_test2.getString("fsnl_totalTag");
        }
    }

    /**
     * 值更新事件
     * @param e
     */
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        //字段模型
        IDataEntityProperty property = e.getProperty();
        //当数量、单位、物料字段发生变化时，计算总面积
        if (property instanceof UnitProp || property instanceof MaterielProp || property instanceof QtyProp) {
            //判断字段在表头还是在表体
            DataEntityPropertyCollection properties = this.getModel().getDataEntity().getDataEntityType().getProperties();
            boolean contains = properties.contains(property);
            //变化的数据集合
            ChangeData[] changeSet = e.getChangeSet();
            for (ChangeData changeData : changeSet) {
                //单据体变更的索引
                int rowIndex = changeData.getRowIndex();
                //获取配置信息，计算总面积，并赋值
                totalArea(rowIndex,contains);
            }
        }

    }

    /**
     * 获取业务单据上的物料相关信息，计算总面
     * @param rowIndex 行下标
     * @param contains 判断字段在表头还是在表体
     */
    private void totalArea(int rowIndex,boolean contains){
        //数量
        double qty = 0;
        //长
        double length = 0;
        //宽
        double width = 0;
        //尺寸单位
        String unit = null;
        //数量对象
        Object qtyObj = null;
        //物料对象
        Object materielObj = null;
        if (contains){
            qtyObj = this.getView().getModel().getValue(qtyTagKey);
            materielObj = this.getModel().getValue(materielTagKey);
        }else{
            qtyObj = this.getView().getModel().getValue(qtyTagKey, rowIndex);
            materielObj = this.getModel().getValue(materielTagKey,rowIndex);
        }
        if (qtyObj!=null)
            qty = Double.parseDouble(qtyObj.toString());

        if (materielObj!=null) {
            DynamicObject materiel = (DynamicObject) materielObj;
            //获取物料字段的数据模型并强转为基础资料数据模型
            BasedataProp property = (BasedataProp) this.getModel().getProperty(materielTagKey);
            //物料上业务策略
            String baseEntityId = property.getBaseEntityId();
            //判断业务策略是否为物料，为物料则直接获取物料属性信息，否则取相关业务物料信息的物料信息
            if (baseEntityId.equals("bd_material")) {
                length = Double.parseDouble(materiel.getString("length"));
                width = Double.parseDouble(materiel.getString("width"));
                unit = materiel.getString("lengthunit");
            }else {
                //获取相关物料信息中的物料
                DynamicObject materiel2 = (DynamicObject) materiel.get("masterid");
                length = Double.parseDouble(materiel2.getString("length"));
                width = Double.parseDouble(materiel2.getString("width"));
                DynamicObject unitObj = materiel2.getDynamicObject("lengthunit");
                unit = unitObj.getString("number");
            }
        }
        //总面积
        double totalArea = 0;
        //根据单位选择不同的计算公式，最终计算为平方米
        if ("mm".equals(unit)){
            totalArea = length*width/1000/1000*qty;
        }else if ("cm".equals(unit)){
            totalArea = length*width/1000*qty;
        }else if ("m".equals(unit)){
            totalArea = length*width*qty;
        }else if ("km".equals(unit)){
            totalArea = length*width*qty*1000;
        }
        //总面积赋值
        if (contains)
            this.getView().getModel().setValue(totalTagKey,totalArea);
        else
            this.getView().getModel().setValue(totalTagKey,totalArea,rowIndex);
    }
}
