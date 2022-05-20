package fsnl.test;

import kd.bos.bill.AbstractMobBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.control.Control;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.form.events.ContextMenuClickEvent;
import org.apache.commons.lang.StringUtils;

import java.util.EventObject;
import java.util.HashMap;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：移动表单插件测试
 * @ClassName：MobTestPlugIn.java
 * @Date 2022/4/22 8:38
 */
public class MobTestPlugIn extends AbstractMobBillPlugIn {

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
    }

    @Override
    public void initialize() {
        //this.addClickListeners("fsnl_vectorap2");
        this.addClickListeners("fsnl_cardentryflexpanelap2");
        this.addClickListeners("fsnl_vectorap21");
    }

    @Override
    public void click(EventObject evt) {
        Control cot = (Control)evt.getSource();
        if (cot.getKey().equals("fsnl_vectorap21")) {
            showFrom("fsnl_vectorap21");
        }
        if (cot.getKey().equals("fsnl_cardentryflexpanelap2")) {
            showFrom("fsnl_cardentryflexpanelap2");
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent closedCallBackEvent) {
        if (closedCallBackEvent.getActionId().equals("fsnl_vectorap21")||closedCallBackEvent.getActionId().equals("fsnl_cardentryflexpanelap2")) {
            EntryGrid grid = this.getView().getControl("entryentity");
            int[] selectRows = grid.getSelectRows();
            int row = selectRows[0];
            HashMap<String, Object> returnData = (HashMap<String, Object>) closedCallBackEvent.getReturnData();

            for (String key : returnData.keySet()) {
                this.getModel().setValue(key, returnData.get(key),row);
            }
        }
    }

    private void showFrom(String conId){
        //创建弹出页面对象，FormShowParameter表示弹出页面为动态表单
        FormShowParameter ShowParameter = new FormShowParameter();
        //设置弹出页面的编码
        ShowParameter.setFormId("fsnl_mob02");
        //设置弹出页面标题
        ShowParameter.setCaption("分录详情");
        //设置页面关闭回调方法
        HashMap<String, Object> hashMap = new HashMap<>();
        //如果被点击控件为确认，则获取页面相关控件值，组装数据传入returnData返回给父页面，最后关闭页面
        EntryGrid grid = this.getView().getControl("entryentity");
        int[] selectRows = grid.getSelectRows();
        hashMap.put("row", selectRows[0]+"");
        DynamicObject entryentity = this.getModel().getEntryRowEntity("entryentity", selectRows[0]);
        hashMap.put("entryentity", entryentity);
        ShowParameter.setCustomParams(hashMap);
        this.getView().returnDataToParent(hashMap);
        ShowParameter.setCloseCallBack(new CloseCallBack(this,conId));
        //设置弹出页面打开方式，支持模态，新标签等
        ShowParameter.getOpenStyle().setShowType(ShowType.Modal);
        //弹出页面对象赋值给父页面
        this.getView().showForm(ShowParameter);
    }
}
