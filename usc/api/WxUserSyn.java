package fsnl.usc.api;

import fsnl.common.util.wx.AesException;
import fsnl.common.util.wx.WXBizMsgCrypt;
import fsnl.service.kingdee.SystemParamHelper;
import fsnl.service.syn.org.UscToCq;
import fsnl.service.syn.org.UscToCqByCallBack;
import fsnl.service.syn.org.WxToUscByCallBack;
import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.context.RequestContext;
import kd.bos.entity.api.ApiResult;
import kd.bos.orm.query.QFilter;

import java.util.Map;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：根据ChangeType同步企业微信人员到苍穹平台和用户中心
 * @ClassName：WxUserSyn.java
 * @Date 2022/2/24 10:52
 */
public class WxUserSyn implements IBillWebApiPlugin {

    @Override
    public ApiResult doCustomService(Map<String, Object> params) {
        Long orgId = 100000L;//组织Id
        ApiResult apiResult = new ApiResult();
        //是否启用企业微信
        String isenablewxqyh = SystemParamHelper.getParamValue("isenablewxqyh",orgId);
        if ("true".equals(isenablewxqyh)) {
            //微信企业id
            String corpid = SystemParamHelper.getParamValue("corpid",orgId);
            //企业微信密钥
            String corpsecret = SystemParamHelper.getParamValue("corpsecret",orgId);
            String userid = params.get("id").toString();//企业微信userid
            //企业微信回调事件的类型,将其截取,用来判断是部分的修改回调还是用户的修改回调
            String changeType = params.get("changeType").toString();
            int index = changeType.lastIndexOf("_");
            //然后获取从最后一个_所在索引+1开始 至 字符串末尾的字符
            String type = changeType.substring(index + 1);
            if ("user".equals(type)) {
                //企业微信同步到用户中心
                WxToUscByCallBack.userSyn(corpid, corpsecret, userid, changeType);
                //用户中心同步到苍穹平台
                UscToCqByCallBack.userSyn(corpid, corpsecret, userid, changeType);
                apiResult.setData(userid);
            }
        }
        return apiResult;
    }
}
