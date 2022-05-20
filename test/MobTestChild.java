package fsnl.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import kd.bos.bill.AbstractMobBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.entity.datamodel.events.BizDataEventArgs;
import kd.bos.form.control.Control;
import kd.bos.form.events.ClosedCallBackEvent;

import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：测试移动端子页面插件
 * @ClassName：MobTestChild.java
 * @Date 2022/4/22 11:44
 */
public class MobTestChild extends AbstractMobBillPlugIn {
    @Override
    public void initialize() {
        this.addClickListeners("btnsubmit");
    }

    @Override
    public void beforeBindData(EventObject e) {
        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        String entryentity_str = customParams.get("entryentity").toString();
        JSONObject jsonObject = JSONObject.parseObject(entryentity_str);
        //获取页面所有字段
        Map<String, IDataEntityProperty> fields = this.getModel().getDataEntityType().getFields();
        for (String key : fields.keySet()) {
            this.getModel().setValue(key,jsonObject.get(key));
        }
    }

    @Override
    public void click(EventObject evt) {
        Control cot = (Control)evt.getSource();
        if (cot.getKey().equals("btnsubmit")) {
            HashMap<String, Object> hashMap = new HashMap<>();
            Map<String, IDataEntityProperty> fields = this.getModel().getDataEntityType().getFields();
            for (String key : fields.keySet()) {
                hashMap.put(key, this.getView().getModel().getValue(key));
            }
            this.getView().returnDataToParent(hashMap);
            this.getView().close();
        }
    }
}
