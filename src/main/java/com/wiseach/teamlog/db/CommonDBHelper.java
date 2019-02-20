package com.wiseach.teamlog.db;

import org.apache.commons.dbutils.handlers.MapListHandler;

import java.util.List;
import java.util.Map;

/**
 * User: Arlen Tan
 * 12-8-28 上午11:07
 */
public class CommonDBHelper {
    public static Long newComment(String description,Long referId, Long userId) {
        return PublicDBHelper.insert("insert into comment(description,referId,userId,createTime) values(?,?,?,current_timestamp())",description,referId,userId);
    }


    public static List<Map<String, Object>> getTags() {
        return PublicDBHelper.query("select name as \"name\",id as \"id\",color as \"color\" from tag order by id", new MapListHandler(new DefaultRowProcessor()));
    }

    public static List<Map<String, Object>> getUserTags(Long userId) {
        List<Map<String, Object>> tags = PublicDBHelper.query("select name as \"name\",id as \"name\",color as \"color\"  from tag where id in (select tagId from userTag where userId=?) order by id", new MapListHandler(new DefaultRowProcessor()), userId);
        if (tags==null || tags.size()==0) {
            tags = getTags();
        }
        return tags;
    }

    public static List<Map<String, Object>> getUserTagsId(Long userId) {
        return PublicDBHelper.query("select id as \"id\",tagId as \"tagId\" from userTag where userId=?",new MapListHandler(new DefaultRowProcessor()),userId);
    }

    public static Long addUserTag(Long userId,String tagId) {
        return PublicDBHelper.insert("insert into userTag(`userId`,`tagId`) values(?,?)", userId, tagId);
    }

    public static boolean removeUserTag(Long userTagId) {
        return PublicDBHelper.exec("delete from userTag where id=?",userTagId)>0;
    }

    public static void newReferTag(String tagId, Long id, Integer referType) {
        PublicDBHelper.insert("insert into referTags(tagId,referId,referType) values(?,?,?)",tagId,id,referType);
    }

    public static boolean needCreateReferTag(String tId, Long id, Integer referType) {
        return !PublicDBHelper.exist("select count(*) from referTags where tagId=? and referId=? and referType=?",tId,id,referType);
    }

    public static void newTag(String tagId,String tag) {
        PublicDBHelper.insert("insert into tag(id,name) values(?,?)",tagId,tag);
    }

    public static boolean updateTagColor(String tagId, String color) {
        return PublicDBHelper.exec("update tag set color=? where tagId=?",color,tagId)>0;
    }

    public static void clearTags(Long id, Integer referType, Long[] currentTagIds) {
        PublicDBHelper.executeWithInParam("delete from referTags where referId=? and referType=? and tagId not in (%in0)",id,referType,currentTagIds);
    }

    public static boolean hasTag(String type) {
        return PublicDBHelper.exist("select count(*) from tag where name = ?",type);
    }

    public static boolean delTag(String typeId) {
        return PublicDBHelper.exec("delete from tag where id=?",typeId)>0;
    }
}
