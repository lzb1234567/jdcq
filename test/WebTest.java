package fsnl.test;

import kd.bos.ext.form.control.CustomControl;
import kd.bos.form.control.CodeEdit;
import kd.bos.form.control.Control;
import kd.bos.form.plugin.AbstractFormPlugin;

import java.util.Date;
import java.util.EventObject;
import java.util.HashMap;

/**
 * @author 陆海泳
 * @version 1.00
 * @description：自定义控件测试
 * @ClassName：WebTest.java
 * @Date 2022/4/26 16:01
 */
public class WebTest extends AbstractFormPlugin {
    @Override
    public void initialize() {
        this.addClickListeners("fsnl_buttonap");
    }

    @Override
    public void click(EventObject evt) {
        Control control = (Control) evt.getSource();
        String key = control.getKey();
        if ("fsnl_buttonap".equals(key)){
            CodeEdit codeEdit = (CodeEdit) this.getControl("fsnl_codeeditap");
            this.getView().setVisible(true,"fsnl_customcontrolap");
            String codeData = codeEdit.getText();
            CustomControl customControl = this.getControl("fsnl_customcontrolap");
            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("action","JS_STR_TO_RUN");
            hashMap.put("datas",codeData);
            hashMap.put("runtime",new Date());
            customControl.setData(hashMap);
        }
    }
}
