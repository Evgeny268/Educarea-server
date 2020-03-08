package com.educarea.ServerApp;

import transfers.GroupPerson;

import java.util.ArrayList;

public class LogicUtils {
    public static int getGroupPersonIdByUserId(int userId, int groupId) throws Exception {
        int personId = 0;
        ArrayList<GroupPerson> groupPeople = AppContext.educareaDB.getGroupPersonsByGroupId(groupId);
        for (int i = 0; i < groupPeople.size(); i++) {
            if (groupPeople.get(i).userId==userId){
                personId = groupPeople.get(i).groupPersonId;
            }
        }
        return personId;
    }

    public static boolean groupPersonIsModerator(int personId) throws Exception{
        GroupPerson person = AppContext.educareaDB.getGroupPersonById(personId);
        if (person.moderator==1){
            return true;
        }else return false;
    }
}
