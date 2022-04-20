package fsnl.ims.acm.bills.fsnl_infolist;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;

import java.util.Date;

/**
 * @author 苏俊安
 * @version 1.00
 * @description:信息清单的保存操作插件
 * @Function：续费相关信息更新至表头信息
 * @ClassName:DataUpdatePlugin.java
 * @Date 2022/2/28 9:22
 */
public class DataUpdatePlugin extends AbstractOperationServicePlugIn {

    /**
     * 操作执行前，准备加载单据数据前，触发此事件
     *
     * @param e
     */
    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {
        //要求加载购入日期，续费日期
        e.getFieldKeys().add("fsnl_fbegindate");
        e.getFieldKeys().add("fsnl_fentbegindate");
        e.getFieldKeys().add("fsnl_fcalculate");
        e.getFieldKeys().add("fsnl_fcost");
        e.getFieldKeys().add("fsnl_fentcalculate");
        e.getFieldKeys().add("fsnl_fentcost");
    }


    /**
     * 系统预置的操作校验器加载完毕，执行校验之前，触发此事件；
     * 点击保存，判断并把计费方式，续费数量更新至表头
     *
     * @param e
     */
    @Override
    public void onAddValidators(AddValidatorsEventArgs e) {

        DynamicObject[] dataEntities = e.getDataEntities();//获取当前对象
        DynamicObjectCollection entryEntity = (DynamicObjectCollection) dataEntities[0].get("fsnl_itm_renewalentry");
        //判断单据体续费信息是否存在记录
        if(entryEntity.size()==0){
            DynamicObject newObj = entryEntity.addNew();
            newObj.set("fsnl_fentbegindate",(Date) dataEntities[0].get("fsnl_fbegindate"));//续费日期
            newObj.set("fsnl_fentenddate",(Date) dataEntities[0].get("fsnl_fenddate"));//到期日期
            newObj.set("fsnl_fentcalculate",dataEntities[0].get("fsnl_fcalculate"));//计费方式
            newObj.set("fsnl_fentunitid",dataEntities[0].get("fsnl_funitid"));//计量单位
            newObj.set("fsnl_fentqty",dataEntities[0].get("fsnl_fqty"));//续费数量
            newObj.set("fsnl_fentcurrency", dataEntities[0].get("fsnl_currency"));//本位币
            newObj.set("fsnl_fentcost", dataEntities[0].get("fsnl_fcost"));//续费金额
            newObj.set("fsnl_fentcharger", dataEntities[0].get("fsnl_fcharger"));//续费人
            newObj.set("fsnl_fentremark", dataEntities[0].get("fsnl_fremark"));//备注
            entryEntity.set(0,newObj);

        }else {
            DynamicObject lastObj = entryEntity.get(entryEntity.size() - 1);


            //判断单锯体续费信息是否存在记录
            if (lastObj.get("fsnl_fentbegindate") != null) {
                //判断单锯体续费信息行号最大的记录中，续费日期是否=单据头的购入日期，是就结束，不是就继续
                Date fbeginDate = (Date) dataEntities[0].get("fsnl_fbegindate");
                Date fentbeginDate = (Date) lastObj.get("fsnl_fentbegindate");
                //先加个判断单头时间是否为空，空的话把最新一笔数据更新到表头22420
                if(fbeginDate==null){
                    dataEntities[0].set("fsnl_fbegindate", lastObj.get("fsnl_fentbegindate"));//购入日期
                    dataEntities[0].set("fsnl_fenddate", lastObj.get("fsnl_fentenddate"));//到期日期
                    dataEntities[0].set("fsnl_fcalculate", lastObj.get("fsnl_fentcalculate"));//计费方式
                    dataEntities[0].set("fsnl_funitid", lastObj.get("fsnl_fentunitid"));//计量单位
                    dataEntities[0].set("fsnl_fcost", lastObj.get("fsnl_fentcost"));//续费金额
                    dataEntities[0].set("fsnl_fqty", lastObj.get("fsnl_fentqty"));//续费数量
                }
                //end
                else if (fbeginDate.toString().equals(fentbeginDate.toString())) {
                    dataEntities[0].set("fsnl_fenddate", lastObj.get("fsnl_fentenddate"));//到期日期
                    dataEntities[0].set("fsnl_fcalculate", lastObj.get("fsnl_fentcalculate")); //计费方式
                    dataEntities[0].set("fsnl_funitid", lastObj.get("fsnl_fentunitid"));  //计量单位
                    dataEntities[0].set("fsnl_fcost", lastObj.get("fsnl_fentcost"));  //续费金额
                    dataEntities[0].set("fsnl_fqty", lastObj.get("fsnl_fentqty"));//续费数量
                } else {
                    if (fbeginDate.before(fentbeginDate)) {
                        dataEntities[0].set("fsnl_fbegindate", lastObj.get("fsnl_fentbegindate"));//购入日期
                        dataEntities[0].set("fsnl_fenddate", lastObj.get("fsnl_fentenddate"));//到期日期
                        dataEntities[0].set("fsnl_fcalculate", lastObj.get("fsnl_fentcalculate"));//计费方式
                        dataEntities[0].set("fsnl_funitid", lastObj.get("fsnl_fentunitid"));//计量单位
                        dataEntities[0].set("fsnl_fcost", lastObj.get("fsnl_fentcost"));//续费金额
                        dataEntities[0].set("fsnl_fqty", lastObj.get("fsnl_fentqty"));//续费数量
                    } else {
                        e.addValidator(new Dayvalidator());

                    }
                }
            }

            //判断单锯体续费信息的购入日期是否存在，不存在的时候也让它更新至单据头22420
            if (lastObj.get("fsnl_fentbegindate") == null) {
                dataEntities[0].set("fsnl_fbegindate", lastObj.get("fsnl_fentbegindate"));//购入日期
                dataEntities[0].set("fsnl_fenddate", lastObj.get("fsnl_fentenddate"));//到期日期
                dataEntities[0].set("fsnl_fcalculate", lastObj.get("fsnl_fentcalculate"));//计费方式
                dataEntities[0].set("fsnl_funitid", lastObj.get("fsnl_fentunitid"));//计量单位
                dataEntities[0].set("fsnl_fcost", lastObj.get("fsnl_fentcost"));//续费金额
                dataEntities[0].set("fsnl_fqty", lastObj.get("fsnl_fentqty"));//续费数量

            }

        }

    }



}
