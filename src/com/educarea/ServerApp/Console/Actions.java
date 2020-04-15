package com.educarea.ServerApp.Console;

import com.educarea.ServerApp.AppContext;
import com.educarea.ServerApp.EducLogger;
import transfers.Group;
import transfers.GroupPerson;

import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Actions {
    public static void deleteGroupById(int groupId) throws Exception{
        Logger log = Logger.getLogger(EducLogger.class.getName());
        ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
        Savepoint savepoint = null;
        try {
            savepoint = AppContext.educareaDB.setSavepoint("deleteGroup");
        } catch (Exception e) {
            log.log(Level.WARNING, "error",e);
            return;
        }
        try {
            for (int i = 0; i < groupPeople.size(); i++) {
                AppContext.educareaDB.deleteGroupPersonCodeByPersonId(groupPeople.get(i).groupPersonId);
                AppContext.educareaDB.deleteChannelMessageByPersonId(groupPeople.get(i).groupPersonId);
            }
            AppContext.educareaDB.deleteTimetableByGroupId(groupId);
            AppContext.educareaDB.deleteGroupPersonByGroupId(groupId);
            AppContext.educareaDB.deleteGroupById(groupId);
            AppContext.educareaDB.commit();
        } catch (Exception e) {
            log.log(Level.WARNING, "error",e);
            try {
                AppContext.educareaDB.rollback(savepoint);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void deleteGroupByName(String groupName) throws Exception{
        int groupId = AppContext.educareaDB.getGroupIdByName(groupName);
        deleteGroupById(groupId);
    }
}
