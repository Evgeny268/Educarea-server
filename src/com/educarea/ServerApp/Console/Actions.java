package com.educarea.ServerApp.Console;

import com.educarea.ServerApp.AppContext;
import com.educarea.ServerApp.LogicUtils;


public class Actions {
    public static void deleteGroupById(int groupId) throws Exception{
        LogicUtils utils = new LogicUtils();
        if (utils.deleteGroup(groupId)){
            System.out.println("Group was delete");
        }else {
            System.out.println("group was NOT delete");
        }
    }

    public static void deleteGroupByName(String groupName) throws Exception{
        int groupId = AppContext.educareaDB.getGroupIdByName(groupName);
        deleteGroupById(groupId);
    }
}
