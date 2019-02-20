package com.wiseach.teamlog.web.actions;

import com.wiseach.teamlog.db.CommonDBHelper;
import com.wiseach.teamlog.web.resolutions.JsonResolution;
import com.wiseach.teamlog.web.security.UserAuthProcessor;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.util.StringUtil;
import net.sourceforge.stripes.validation.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sean
 * Date: 12-10-30
 * Time: 下午11:13
 * To change this template use File | Settings | File Templates.
 */
@UrlBinding("/setting")
public class SettingActionBean extends BaseActionBean{

    @DontValidate
    @DefaultHandler
    public Resolution view() {
        if (UserAuthProcessor.isAdmin(getContext())) {
            return new ForwardResolution(ViewHelper.SETTING_PAGE);
        }

        return returnNoAdminResolution("setting.log.type.error.description");
    }

    public Resolution create() {
        if (UserAuthProcessor.isAdmin(getContext())) {
            String types[] = StringUtil.standardSplit(workType);
            String type;
            ArrayList<String> existType = new ArrayList<String>();

            for (int i = 0; i < types.length; i++) {
                type = types[i];
                String tags[]=type.split("--");
                if(tags==null || tags.length!=2){
                	addError("workType", getMessage("error.message.work.type.invalid", StringUtil.combineParts(type)));
                	break;
                }
                String tagId=tags[0];
                String tag=tags[1];
                if (CommonDBHelper.hasTag(tag)) {
                    existType.add(tag);
                } else {
                    CommonDBHelper.newTag(tagId,tag);
                }
            }

            if (existType.size()>0) {
                addError("workType", getMessage("error.message.work.type.duplicate", StringUtil.combineParts(existType)));
            }

            return view();
        } else {
            return returnNoAdminResolution("setting.log.type.error.description");
        }
    }

    @DontValidate
    public Resolution deleteWorkType() {
        boolean validateId = typeId != null && typeId.length()>0;
        if (validateId) {
            validateId = CommonDBHelper.delTag(typeId);
        }

        return new JsonResolution<Boolean>(validateId);
    }

    public String typeId;
    @Validate(required = true)
    private String workType;

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public List<Map<String, Object>> getAllWorkType() {
        if (UserAuthProcessor.isAdmin(getContext())) {
            return CommonDBHelper.getTags();
        } else {
            return null;
        }
    }
}
