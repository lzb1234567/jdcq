package fsnl.ims.acm.bills.fsnl_infolist;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.OperateOptionConst;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.AddValidatorsEventArgs;
import kd.bos.entity.plugin.PreparePropertysEventArgs;
import kd.bos.entity.plugin.args.BeforeOperationArgs;
import kd.bos.servicehelper.operation.OperationServiceHelper;

import java.util.Date;

/**
 * @author 苏俊安
 * @version 1.00
 * @description:提交前触发保存操作
 * @ClassName:BeforeSubmit.java
 * @Date 2022/4/22 11:01
 */
public class BeforeSubmit extends AbstractOperationServicePlugIn {

    @Override
    public void onPreparePropertys(PreparePropertysEventArgs e) {

        e.getFieldKeys().add("fsnl_itm_renewalentry");
        e.getFieldKeys().add("fsnl_fentbegindate");
        e.getFieldKeys().add("fsnl_fentenddate");
        e.getFieldKeys().add("fsnl_fentcalculate");
        e.getFieldKeys().add("fsnl_fentunitid");
        e.getFieldKeys().add("fsnl_fentqty");
        e.getFieldKeys().add("fsnl_fentcurrency");
        e.getFieldKeys().add("fsnl_fentcost");
        e.getFieldKeys().add("fsnl_fentcharger");
        e.getFieldKeys().add("fsnl_fentremark");

        e.getFieldKeys().add("fsnl_fbegindate");
        e.getFieldKeys().add("fsnl_fenddate");
        e.getFieldKeys().add("fsnl_fcalculate");
        e.getFieldKeys().add("fsnl_funitid");
        e.getFieldKeys().add("fsnl_fqty");
        e.getFieldKeys().add("fsnl_currency");
        e.getFieldKeys().add("fsnl_fcost");
        e.getFieldKeys().add("fsnl_fcharger");
        e.getFieldKeys().add("fsnl_fremark");


    }

    @Override
    public void beforeExecuteOperationTransaction(BeforeOperationArgs e) {
        OperateOption option = OperateOption.create();
        String opStr = "save";
        DynamicObject[] dataEntities = e.getDataEntities();//获取当前对象
        OperationResult delResult = OperationServiceHelper.executeOperate(opStr, "fsnl_infolist", dataEntities, option);

    }


}

