package fsnl.ims.acm.bills.fsnl_infolist;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.earlywarn.EarlyWarnContext;
import kd.bos.entity.earlywarn.warn.EarlyWarnMessageInfo;
import kd.bos.entity.earlywarn.warn.plugin.IEarlyWarnMessageHandler;
import kd.bos.workflow.engine.msg.handler.YunzhijiaServiceHandler;

/**
 * @author 苏俊安
 * @version 1.00
 * @description:自定义消息处理（预警平台，预警消息跳转）
 * @ClassName:MessageProcessing.java
 * @Date 2022/3/9 14:11
 */
public class MessageProcessing implements IEarlyWarnMessageHandler {
    @Override
    public EarlyWarnMessageInfo singleMessageBuilder(DynamicObject dynamicObject, EarlyWarnContext earlyWarnContext) {
        EarlyWarnMessageInfo info = new EarlyWarnMessageInfo();
        String pkid=dynamicObject.getString("id");
        String url=System.getProperty("domain.contextUrl");
        //info.setContentUrl("http://data-center-fat.jinduo.com/ierp/?formId=fsnl_infolist&pkId="+pkid);
        //info.setMobContentUrl("http://data-center-fat.jinduo.com/ierp/mobile.html?pkId="+pkid+"&form=fsnl_infolist_mob");//消息推送到企业微信
        info.setContentUrl(url+"/?formId=fsnl_infolist&pkId="+pkid);//消息中心快速处理跳转到对应单据
        //info.setMobContentUrl(url+"/mobile.html?pkId="+pkid+"&form=fsnl_infolist_mob");//预警消息推送到企业微信


        return info;
    }

    @Override
    public EarlyWarnMessageInfo mergeMessageBuilder(EarlyWarnContext earlyWarnContext) {
        return  null;
    }
}
