package fsnl.test;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.form.control.events.ItemClickEvent;

import java.util.EventObject;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：地址控件取值测试
 * @ClassName：MapTest.java
 * @Date 2022/5/17 14:19
 */
public class MapTest extends AbstractBillPlugIn {
    @Override
    public void registerListener(EventObject e) {
        this.addItemClickListeners("tbmain");
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        if (evt.getItemKey().equals("fsnl_baritemap")) {
            Object fsnl_addressfield = this.getView().getModel().getValue("fsnl_addressfield");
            this.getView().showMessage("阿大撒发射点发");
        }
    }
}
