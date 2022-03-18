package fsnl.usc.basedate.user;

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
    public MessageHandler getMessageHandle() {
        return null;
    }

    @Override
    public void execute(RequestContext requestContext, Map<String, Object> map) throws KDException {

    }
}
