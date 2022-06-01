package fsnl.crm.prj.appbaseinfo;

import kd.bos.bill.AbstractMobBillPlugIn;
import kd.bos.ext.form.control.events.MapSelectListener;
import kd.bos.form.control.events.*;
import kd.bos.list.MobileSearch;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：工程报备申请移动端插件：字段必填项控制，工程定位功能
 * @ClassName：AppBaseInfoMobPlugIn.java
 * @Date 2022-05-31 14:20
 */
public class AppBaseInfoMobPlugIn extends AbstractMobBillPlugIn implements MapSelectListener, MobileSearchTextChangeListener {
    @Override
    public void registerListener(EventObject e) {
        MobileSearch search = this.getControl("fsnl_mobilesearchap");
        search.addMobileSearchTextChangeListener(this);

    }


    @Override
    public void click(MobileSearchTextChangeEvent mobileSearchTextChangeEvent) {
        String text = mobileSearchTextChangeEvent.getText();
        this.getView().showMessage(text);
    }



    /*@Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        IClientViewProxy clientViewProxy = this.getView().getService(IClientViewProxy.class);
        clientViewProxy.addAction(ClientActions.getLocation, new Object[0]);
    }

    @Override

    public void locate(LocateEvent e) {

        // TODO Auto-generated method stub

        super.locate(e);

        MobLocation location = e.getMobLocation(); //MobLocation对象包含的就是定位信息

        location.getLatitude();

        location.getAddress();

    }




    public void getLocationInfo() {

        //获取具体定位信息

        MobileFormView view = (MobileFormView)this.getView();

        MobLocation location1 = view.getLocation(); //MobLocation对象包含的就是定位信息

    }*/

}
