package fsnl.usc.api;

import fsnl.service.kingdee.SystemParamHelper;
import fsnl.service.syn.org.UscToCqByCallBack;
import fsnl.service.syn.org.WxToUscByCallBack;
import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.context.RequestContext;
import kd.bos.entity.api.ApiResult;

import java.util.Map;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：根据ChangeType同步企业微信部门到苍穹平台和用户中心
 * @ClassName：WxDeptSyn.java
 * @Date 2022/3/14 11:28
 */
public class WxDeptSyn implements IBillWebApiPlugin {
    @Override
    public ApiResult doCustomService(Map<String, Object> params) {
        long orgId = RequestContext.get().getOrgId();
        ApiResult apiResult = new ApiResult();
        if (orgId!=0){
            //是否启用企业微信
            String isenablewxqyh = SystemParamHelper.getParamValue("isenablewxqyh");
            if ("true".equals(isenablewxqyh)) {
                //微信企业id
                String corpid = SystemParamHelper.getParamValue("corpid");
                //企业微信密钥
                String corpsecret = SystemParamHelper.getParamValue("corpsecret");
                String dpetId = params.get("id").toString();//部门编码
                String changeType = params.get("changeType").toString();
                int index1 = changeType.lastIndexOf("_");
                //然后获取从最后一个\所在索引+1开始 至 字符串末尾的字符
                String type = changeType.substring(index1+1);
                if ("party".equals(type)) {
                    WxToUscByCallBack.departmentSyn(corpid,corpsecret,dpetId,changeType);
                    UscToCqByCallBack.departmentSyn(dpetId,changeType);
                }
                apiResult.setData(dpetId);
            }
        }
        return apiResult;
    }
}
