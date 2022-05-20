package fsnl.global.area;

import kd.bd.sbd.business.helper.BillTreeBuildParameter;
import kd.bd.sbd.business.helper.EntityParseHelper;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.MainEntityType;
import kd.bos.entity.property.DecimalProp;
import kd.bos.entity.property.MaterielProp;
import kd.bos.entity.property.QtyProp;
import kd.bos.entity.tree.TreeNode;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.plugin.AbstractFormPlugin;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashSet;
import java.util.List;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：总面积计算配置表的基础资料界面插件
 * @ClassName：AreaCalculateConfig.java
 * @Date 2022/5/11 15:34
 */
public class AreaCalculateConfig extends AbstractFormPlugin {
    /*总数量字段*/
    private  String qtyNameKey = "fsnl_qtyname";
    /*总数量字段标识*/
    private String qtyTagKey = "fsnl_qtytag";
    /*物料字段*/
    private String materielNameKey = "fsnl_materielname";
    /*物料字段标识*/
    private String materielTagKey = "fsnl_materieltag";
    /*汇总字段*/
    private String totalNameKey = "fsnl_totalname";
    /*汇总字段标识*/
    private String totalTagKey = "fsnl_totaltag";

    /**
     * 用户与界面上的控件交互时，触发此事件
     * @param e
     */
    @Override
    public void registerListener(EventObject e) {
        //给文本字段注册监听事件，用来选择单据的字段
        List<String> listenerKeyList = new ArrayList<>();
        listenerKeyList.add(qtyNameKey);//总数量字段
        listenerKeyList.add(materielNameKey);//物料字段
        listenerKeyList.add(totalNameKey);//汇总字段
        this.addClickListeners(listenerKeyList.toArray(new String[0]));
    }

    /**
     * 点击后触发操作事件
     * @param evt
     */
    @Override
    public void click(EventObject evt) {
        Control control = (Control) evt.getSource();
        //点击总数量字段后弹窗
        if (control.getKey().equals(qtyNameKey)) {
            this.showSelectFieldForm(qtyNameKey);
        }
        //点击总数量字段后弹窗
        if (control.getKey().equals(materielNameKey)) {
            this.showSelectFieldForm(materielNameKey);
        }
        //点击总数量字段后弹窗
        if (control.getKey().equals(totalNameKey)) {
            this.showSelectFieldForm(totalNameKey);
        }
    }

    /**
     * 子界面关闭时，如果回调函数由父界面处理，则会触发父界面的此事件
     * @param closedCallBackEvent
     */
    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent){
        //触发的字段标识
        String sourceFieldKey = closedCallBackEvent.getActionId();
        //回调数据
        Object returnData = closedCallBackEvent.getReturnData();
        //动态表单实体模型
        MainEntityType mainType = this.getsrcEntityType();
        //字段数据模型
        IDataEntityProperty property = null;
        String propertyStr = null;
        if(!StringUtils.isBlank(returnData)){
            propertyStr = (String)returnData;
            property = mainType.findProperty(propertyStr);

        }
        //总数量字段相关信息处理
        if(qtyNameKey.equals(sourceFieldKey)) {
            if (property instanceof QtyProp){
                //根据动态表单实体模型和回调数据解析回调结果。
                String fieldCaption = EntityParseHelper.buildPropFullCaption(mainType, propertyStr);
                this.getView().getModel().setValue(qtyNameKey, fieldCaption);
                this.getView().getModel().setValue(qtyTagKey, propertyStr);
            }else {
                this.getView().showMessage("不是数量类型。");
            }
        }
        //物料字段相关信息处理
        if(materielNameKey.equals(sourceFieldKey)) {
            if (property instanceof MaterielProp){
                //根据动态表单实体模型和回调数据解析回调结果。
                String fieldCaption = EntityParseHelper.buildPropFullCaption(mainType, propertyStr);
                this.getView().getModel().setValue(materielNameKey, fieldCaption);
                this.getView().getModel().setValue(materielTagKey, propertyStr);
            }else {
                this.getView().showMessage("不是物料字段。");
            }
        }
        //汇总字段相关信息处理
        if(totalNameKey.equals(sourceFieldKey)) {
            if (property instanceof DecimalProp){
                //根据动态表单实体模型和回调数据解析回调结果。
                String fieldCaption = EntityParseHelper.buildPropFullCaption(mainType, propertyStr);
                this.getView().getModel().setValue(totalNameKey, fieldCaption);
                this.getView().getModel().setValue(totalTagKey, propertyStr);
            }else {
                this.getView().showMessage("不是数值类型。");
            }
        }

    }

    /**
     * 展示要选择的字段数据
     * @param sourceCtrlKey
     */
    private void showSelectFieldForm( String sourceCtrlKey){
        //获取选择的单据的 动态表单主实体模型
        MainEntityType srcEntityType = getsrcEntityType();
        if(srcEntityType == null ){
            this.getView().showTipNotification("请先选择单据！");
            return;
        }
        //创建树形
        BillTreeBuildParameter parameter = new BillTreeBuildParameter(srcEntityType,(HashSet)null,(IDataEntityProperty)null);
        TreeNode srcBillNode = EntityParseHelper.buildBillTreeNodes(parameter);
        //弹窗
        FormShowParameter showParameter = new FormShowParameter();
        showParameter.setFormId("botp_selectfield");//此处使用BOTP中选择字段的页面标识
        String nodesJson = SerializationUtils.toJsonString(srcBillNode);
        showParameter.getCustomParams().put("treenodes", nodesJson);
        showParameter.setCloseCallBack(new CloseCallBack(this, sourceCtrlKey));
        showParameter.getOpenStyle().setShowType(ShowType.Modal);
        this.getView().showForm(showParameter);
    }

    /**
     * 获取动态表单主实体模型
     * @return 动态表单主实体模型
     */
    private MainEntityType getsrcEntityType(){
        //需要查询的基础资料字段的值 实体标识
        DynamicObject sourceBill = this.getModel().getDataEntity().getDynamicObject("fsnl_bill");
        if(sourceBill == null){
            return null;
        }else{
            //需要查询的实体标识
            String srcEntityTypeNumber = sourceBill.getString("number");
            //动态表单主实体模型
            MainEntityType srcEntityType = EntityMetadataCache.getDataEntityType(srcEntityTypeNumber);
            return srcEntityType;
        }
    }
}
