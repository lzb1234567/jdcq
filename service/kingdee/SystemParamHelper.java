package fsnl.service.kingdee;

import kd.bos.context.RequestContext;
import kd.bos.entity.AppInfo;
import kd.bos.entity.AppMetadataCache;
import kd.bos.entity.param.AppParam;
import kd.bos.servicehelper.parameter.SystemParamServiceHelper;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：系统参数服务类
 * @ClassName：SystemParamHelper.java
 * @Date 2022/3/16 16:00
 */
public class SystemParamHelper {
    /**
     * 获取系统服务云中的基础服务的系统参数
     * @param paramKey 系统参数的字段标识，可在开发平台中查看
     * @return 系统参数的value
     */
    public static String getParamValue(String paramKey){
        //获取上下文中的当前登陆的业务单元id
        long orgId = RequestContext.get().getOrgId();
        //根据应用编码从缓存中获取应用信息
        AppInfo kdec_cgfw = AppMetadataCache.getAppInfo("base");
        //获取应用的主键
        String appId = kdec_cgfw.getId();
        //基础服务的APPID
        AppParam apm = new AppParam();
        apm.setAppId(appId);
        apm.setOrgId(orgId); // 参数的组织ID

        //获取整体应用参数
        //Map<String,Object> paramWhole = SystemParamServiceHelper.loadAppParameterFromCache(apm);

        //从整个应用参数中获取某个参数属性值
        //Object paramValue = paramWhole.get(paramKey);

        //直接获取某个应用下的某个属性参数值
        Object value = SystemParamServiceHelper.loadAppParameterFromCache(apm, paramKey);
        if (value!=null)
            return value.toString();
        return null;
    }
}
