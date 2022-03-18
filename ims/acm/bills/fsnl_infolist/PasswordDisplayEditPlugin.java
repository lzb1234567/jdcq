package fsnl.ims.acm.bills.fsnl_infolist;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.bill.OperationStatus;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.ClientProperties;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.events.BeforeItemClickEvent;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeDoOperationEventArgs;
import kd.bos.form.field.FieldEdit;
import kd.bos.form.operate.FormOperate;
import kd.bos.list.BillList;
import kd.bos.workflow.engine.msg.handler.WeixinqyServiceHandler;
import kd.bos.workflow.engine.msg.handler.YunzhijiaServiceHandler;
import org.apache.commons.lang.StringUtils;

import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kd.bos.bill.OperationStatus.ADDNEW;
import static kd.bos.bill.OperationStatus.EDIT;

/**
 * @author 苏俊安
 * @version 1.00
 * @description:信息清单-点击密码显示按钮，密码明文显示
 * @ClassName:PasswordDisplayEditPlugin.java
 * @Date 2022/2/25 11:48
 */
public class PasswordDisplayEditPlugin extends AbstractBillPlugIn {

    /**
     * 界面数据包构建完毕，生成指令，刷新前端字段值、控件状态后触发此事件
     * 通过判断是新增还是修改，设置密码的初始样式
     *
     * @param e
     */
    @Override
    public void afterBindData(EventObject e) {
        OperationStatus status = this.getView().getFormShowParameter().getStatus();

        EntryGrid infolistentry = this.getControl("fsnl_itm_infolistentry");//单据体的标识
        Map<String, Object> params = new HashMap<>();
        if (status == ADDNEW) {
            params.put(ClientProperties.Type, "text");
            this.getModel().setValue("fsnl_passwordfield", "false");//设置标志位为true
        }
        if (status == EDIT) {
            params.put(ClientProperties.Type, "passwordbox");
            this.getModel().setValue("fsnl_passwordfield", "true");//设置标志位为true

        }
        infolistentry.setColumnProperty("fsnl_fpassword", "editor", params);
        super.afterBindData(e);

    }




    /**
     * 点击按钮，菜单，执行绑定的操作后，触发此事件
     * 此处用来刷新页面，修改样式
     *
     * @param afterDoOperationEventArgs
     */
    @Override
    public void afterDoOperation(AfterDoOperationEventArgs afterDoOperationEventArgs) {
        //点击保存操作，刷新页面数据
        if (afterDoOperationEventArgs.getOperateKey().equals("save")) {
            this.getView().updateView();
        }

        //点击密码显示按钮，改变密码显示样式
        if(afterDoOperationEventArgs.getOperateKey().equals("donothing")){
            boolean ispassword = (boolean) this.getView().getModel().getValue("fsnl_passwordfield");
            if (ispassword) {
                EntryGrid infolistentry = this.getControl("fsnl_itm_infolistentry");
                Map<String, Object> params = new HashMap<>();
                params.put(ClientProperties.Type, "text");//显示为文本样式
                infolistentry.setColumnProperty("fsnl_fpassword", "editor", params);
                this.getModel().setValue("fsnl_passwordfield", "false");//设置标志位为false

            } else {
                EntryGrid infolistentry = this.getControl("fsnl_itm_infolistentry");//单据体的标识
                Map<String, Object> params = new HashMap<>();
                params.put(ClientProperties.Type, "passwordbox");//显示为密码显示样式
                infolistentry.setColumnProperty("fsnl_fpassword", "editor", params);
                this.getModel().setValue("fsnl_passwordfield", "true");//设置标志位为true

            }

        }
    }

    /**
     * 点击操作前，触发此事件
     * 点击提交前触发保存操作
     * @param args
     */
    @Override
    public void beforeDoOperation(BeforeDoOperationEventArgs args) {
        String key_opkey= "submit";//提交标识
        FormOperate formOperate = (FormOperate) args.getSource();
        if (StringUtils.equals(key_opkey,formOperate.getOperateKey())){
            this.getView().invokeOperation("save");

        }

    }
}
