package fsnl.service.kingdee;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.ext.form.control.events.MapSelectEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.HttpUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：行政区划获取帮助类
 * @ClassName：AdminDvisionHelper.java
 * @Date 2022-05-30 10:38
 */
public class AdminDvisionHelper {
    //日志
    private final static Log logger = LogFactory.getLog(AdminDvisionHelper.class);

    /**
     * 该方法相关代码复制kd.bos.address.plugin.AddressInfoDynamicFieldPagePlugin.setSelectData();
     * 地图控件中的地址信息没有区\县数据，需要根据地址信息中的坐标从百度题图API中获取完整的地址信息，并根据参考码获取金蝶行政区划数据。
     *
     * @param evt 地图控件返回的地址信息
     * @return 行政区划数据,国家（country）、省（province）、市（city）、区县（district）
     */
    public static Map<String,String> findDvisions(MapSelectEvent evt){
        Map<String,String> dvisions = new HashMap<>();
        String location = null;
        Map<String, Object> map = evt.getPoint();
        Object pointObj = map.get("point");
        if (pointObj instanceof HashMap) {
            Map<String, Object> point = (HashMap)pointObj;
            location = point.get("lat") + "," + point.get("lng");
        }

        if (map.containsKey("latitude") && map.containsKey("longitude")) {
            location = map.get("latitude") + "," + map.get("longitude");
        }

        if (StringUtils.isNotBlank(location)) {
            String selectProperties = "id,mapconfigentry.id,mapconfigentry.mapkey,mapconfigentry.mapurl,mapconfigentry.mapconfigtype,mapfieldmappingentry.id,mapfieldmappingentry.addressconfigfield,mapfieldmappingentry.mapinterfacefield,mapfieldmappingentry.admindivisionnumber";
            QFilter advanceAddressQFilter = new QFilter("number", "=", "SYSTEM_PRESET");
            DynamicObject advanceAddress = BusinessDataServiceHelper.loadSingleFromCache("cts_advance_address", selectProperties, advanceAddressQFilter.toArray());
            DynamicObject mapConfig = null;
            Iterator var = advanceAddress.getDynamicObjectCollection("mapconfigentry").iterator();

            while (true) {
                if (!var.hasNext()) {
                    assert mapConfig != null;

                    String mapKey = mapConfig.getString("mapkey");
                    String mapUrl = mapConfig.getString("mapurl");
                    if (StringUtils.isBlank(mapKey) || StringUtils.isBlank(mapUrl)) {
                        return null;
                    }

                    Map<String, String> param = new HashMap();
                    param.put("location", location);
                    param.put("ak", mapKey);
                    param.put("output", "json");
                    String url = getUrlParam(mapUrl, param);
                    String result = HttpUtils.request(url);
                    ObjectMapper jackson = new ObjectMapper();
                    JsonNode node = null;
                    try {
                        node = jackson.readTree(result).get("result");
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    Map<String, String> resultMap = new HashMap();
                    jsonLeaf(resultMap, null, node);

                    //经度
                    dvisions.put("lng",resultMap.get("location.lng"));
                    //维度
                    dvisions.put("lat",resultMap.get("location.lat"));

                    //国家
                    QFilter countryQFilter = new QFilter("name", "=", resultMap.get("addressComponent.country"));
                    DynamicObject countryObject = BusinessDataServiceHelper.loadSingleFromCache("bd_country", "id", countryQFilter.toArray());
                    dvisions.put("country",countryObject.getString("id"));
                    //详细地址
                    dvisions.put("address",resultMap.get("formatted_address"));

                    QFilter admindivisionQFilter = new QFilter("areacode", "=", resultMap.get("addressComponent.adcode"));
                    DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingleFromCache("bd_admindivision", "id,longnumber", admindivisionQFilter.toArray());
                    if (dynamicObject != null) {
                        String longnumber = dynamicObject.getString("longnumber");
                        String[] split1 = longnumber.split("\\.");
                        QFilter longnumberQFilter = new QFilter("number", "in", split1);
                        DynamicObject[] dvisions_do = BusinessDataServiceHelper.loadFromCache("bd_admindivision", "id,longnumber", longnumberQFilter.toArray(), "level asc").values().toArray(new DynamicObject[0]);
                        for (DynamicObject dvision_do : dvisions_do) {
                            //行政区划名称
                            String name = dvision_do.getString("name");
                            //行政区划id
                            String id = dvision_do.getString("id");
                            //省
                            String province = resultMap.get("addressComponent.province");
                            if (name.equals(province))
                                dvisions.put("province",id);
                            //市
                            String city = resultMap.get("addressComponent.city");
                            if (name.equals(city))
                                dvisions.put("city",id);
                            //区
                            String district = resultMap.get("addressComponent.district");
                            if (name.equals(district))
                                dvisions.put("district",id);
                        }
                    }else{
                        logger.info("该参考码获取不到苍穹的行政区划，具体数据如下："+resultMap.toString()+"");
                    }
                    return dvisions;
                }

                DynamicObject dynamicObject = (DynamicObject)var.next();
                if ("1".equals(dynamicObject.getString("mapconfigtype"))) {
                    mapConfig = dynamicObject;
                }
            }
        }
        return null;
    }

    /**
     * 拼接百度地图API的请求地址
     * 该方法相关代码复制kd.bos.address.plugin.AddressInfoDynamicFieldPagePlugin.getUrlParam();
     * @param url 百度地图API请求地址
     * @param map 参数集合
     * @return 完整的请求路径
     */
    private static String getUrlParam(String url, Map<String, String> map) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        if (map != null && !map.isEmpty()) {
            Iterator var4 = map.entrySet().iterator();

            while(var4.hasNext()) {
                Map.Entry<String, String> param = (Map.Entry)var4.next();
                builder.queryParam(param.getKey(), new Object[]{param.getValue()});
            }
        }

        return builder.build().toString();
    }

    /**
     * 将百度地图API返回的数据转为map集合
     * 该方法相关代码复制kd.bos.address.plugin.AddressInfoDynamicFieldPagePlugin.jsonLeaf();
     * @param map
     * @param key
     * @param node
     */
    private static void jsonLeaf(Map<String, String> map, String key, JsonNode node) {
        if (node.isValueNode()) {
            map.put(key, node.asText());
        }

        Map.Entry entry;
        String newKey;
        if (node.isObject()) {
            for(Iterator it = node.fields(); it.hasNext(); jsonLeaf(map, newKey, (JsonNode)entry.getValue())) {
                entry = (Map.Entry)it.next();
                newKey = (String)entry.getKey();
                if (StringUtils.isNotBlank(key)) {
                    newKey = key + "." + newKey;
                }
            }
        }

        if (node.isArray()) {
            for(int i = 0; i < node.size(); ++i) {
                jsonLeaf(map, key + "[" + i + "]", node.get(i));
            }
        }

    }
}
