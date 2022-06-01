package fsnl.test;

import fsnl.service.kingdee.AdminDvisionHelper;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.ext.form.control.MapControl;
import kd.bos.ext.form.control.events.MapSelectEvent;
import kd.bos.ext.form.control.events.MapSelectListener;
import kd.bos.form.control.events.ItemClickEvent;

import java.util.*;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：地址控件取值测试
 * @ClassName：MapTest.java
 * @Date 2022/5/17 14:19
 */
public class MapTest extends AbstractBillPlugIn implements MapSelectListener {
    @Override
    public void registerListener(EventObject e) {
        this.addItemClickListeners("tbmain");
        MapControl mapControl = this.getControl("fsnl_mapcontrolap");
        mapControl.addSelectListener(this);

    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        if (evt.getItemKey().equals("fsnl_baritemap")) {
            MapControl fsnl_addressfield = this.getView().getControl("fsnl_mapcontrolap");
            fsnl_addressfield.getAddress();
            this.getView().showMessage("阿大撒发射点发");
        }
    }

    @Override
    public void select(MapSelectEvent evt) {
        MapSelectListener.super.select(evt);
        Map<String, String> dvisions = AdminDvisionHelper.findDvisions(evt);
        this.getView().getModel().setValue("fsnl_basedatafield",dvisions.get("city"));
    }


}
