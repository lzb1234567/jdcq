package fsnl.usc.basedate.user;

import fsnl.service.kingdee.SystemParamHelper;
import fsnl.service.syn.org.UscToCq;
import fsnl.service.syn.org.WxToUsc;
import kd.bos.context.RequestContext;
import kd.bos.exception.KDException;
import kd.bos.schedule.api.MessageHandler;
import kd.bos.schedule.executor.AbstractTask;

import java.util.Map;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：组织架构同步的定时任务插件
 * @ClassName：OrgFrameAbstractTask.java
 * @Date 2022/3/2 15:53
 */
public class OrgFrameAbstractTask extends AbstractTask {

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {
        Long orgId = 100000L;//组织Id
        //是否启用企业微信
        String isenablewxqyh = SystemParamHelper.getParamValue("isenablewxqyh",orgId);
        if ("true".equals(isenablewxqyh)) {
            //微信企业id
            String corpid = SystemParamHelper.getParamValue("corpid",orgId);
            //企业微信密钥
            String corpsecret = SystemParamHelper.getParamValue("corpsecret",orgId);
            //企业微信同步至用户中心
            WxToUsc.sysJob(corpid,corpsecret);
            //用户中心同步至苍穹平台
            UscToCq.sysJob();
        }
    }
}
