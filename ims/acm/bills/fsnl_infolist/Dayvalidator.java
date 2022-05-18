package fsnl.ims.acm.bills.fsnl_infolist;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.ExtendedDataEntity;
import kd.bos.entity.validate.AbstractValidator;

import java.util.Date;

/**
 * 自定义过滤器
 *
 * @author 苏俊安
 * @version 1.00
 * @description:信息清单-续费日期小于购入日期报错提示
 * @ClassName:Dayvalidator.java
 * @Date 2022/2/28 11:58
 */
public class Dayvalidator extends AbstractValidator {
    @Override
    public void validate() {
        for (ExtendedDataEntity extendedDataEntity : this.getDataEntities()) {
            this.addErrorMessage(extendedDataEntity, "续费日期不能小于购入日期");
        }

    }

}
