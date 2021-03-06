package fsnl.usc.basedate.user;

import fsnl.service.kingdee.SystemParamHelper;
import fsnl.service.syn.org.UscToCq;
import fsnl.service.syn.org.WxToUsc;
import kd.bos.context.RequestContext;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.list.plugin.AbstractListPlugin;

/**
 * @description：用户中心人员列表，表单插件
 * @ClassName：WxUsertListPlugin.java
 * @author 陆海泳
 * @Date   2022/2/19 10:31:31
 * @version 1.00
 */
public class WxUsertListPlugin  extends AbstractListPlugin {

    @Override
    public void itemClick(ItemClickEvent evt) {
        //企业微信同步按钮
        if ("fsnl_qywxsyn".equals(evt.getItemKey())) {
            long orgId = RequestContext.get().getOrgId();
            if (orgId!=0){
                //是否启用企业微信
                String isenablewxqyh = SystemParamHelper.getParamValue("isenablewxqyh");
                if ("true".equals(isenablewxqyh)) {
                    //微信企业id
                    String corpid = SystemParamHelper.getParamValue("corpid");
                    //企业微信密钥
                    String corpsecret = SystemParamHelper.getParamValue("corpsecret");
                    //企业微信同步至用户中心
                    WxToUsc.sysJob(corpid,corpsecret);
                    //用户中心同步至苍穹平台
                    UscToCq.sysJob();
                }
            }else{
                this.getView().showMessage("需要在人员中设置组织,才能执行此操作。");
            }
        }
    }


}
