package com.educarea.ServerApp;

import transfers.*;

import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Date;

public interface EducareaDB {

    Savepoint setSavepoint(String name) throws Exception;

    void rollback(Savepoint savepoint) throws Exception;

    void commit() throws Exception;

    int getUserIdByLogin(String login) throws Exception;

    int getGroupIdByName(String name) throws Exception;

    Group getGroupById(int groupId) throws Exception;

    ArrayList<Integer> getGroupsIdByUserId(int userId) throws Exception;

    GroupPerson getGroupPersonById (int groupPersonId) throws Exception;

    ArrayList<GroupPerson> getGroupPersonsByUserId(int userId) throws Exception;

    ArrayList<GroupPerson> getGroupPersonsByGroupId(int groupId) throws Exception;

    void updateGroupPersonUserId(int groupPersonId, int userId) throws Exception;

    int getUserIdByLogAndPass(String login, String password) throws Exception;

    int getUserIdByAuthToken(String token) throws Exception;

    User getUserById(int userId) throws Exception;

    ArrayList<UserTokens> getUserTokensByUserId(int userId) throws Exception;

    void insertNewUser(User user) throws Exception;

    void insertNewGroup(String name) throws Exception;

    void insertAuthToken(int userId, String token) throws Exception;

    void updateTokenTime(String token) throws Exception;

    void updateTokenAddress(String token, String address) throws Exception;

    void updateCloudToken(String token, String cloudToken) throws Exception;

    void insertGroupPerson(GroupPerson groupPerson) throws Exception;

    void deleteGroupPersonById(int groupPersonId) throws Exception;

    void deleteGroupPersonByGroupId(int groupId) throws Exception;

    void deleteGroupById(int groupId) throws Exception;

    void updateGroupPerson(int groupPersonId, GroupPerson groupPerson) throws Exception;

    void deletePersonInviteByPersonId(int groupPersonId) throws Exception;

    void insertTimetable(Timetable timetable) throws Exception;

    void updateTimetable(int timetableId, Timetable timetable) throws Exception;

    void deleteTimetable(int timetableId) throws Exception;

    Timetable getTimetableById(int timetableId) throws Exception;

    ArrayList<Timetable> getTimetableByGroupId(int groupId) throws Exception;

    ArrayList<Timetable> getTimetableByPersonId(int groupPersonId) throws Exception;

    void insertChannelMessage(ChannelMessage channelMessage) throws Exception;

    ArrayList<ChannelMessage> selectChannelMessageByPersonId(int personId, int count) throws Exception;

    ArrayList<ChannelMessage> selectChannelMessageByPersonId(int personId, Date lastDate, int count) throws Exception;

    GroupPersonCode getGroupPersonCodeByPersonId(int personId) throws Exception;

    GroupPersonCode getGroupPersonCodeByCode(String code) throws Exception;

    void insertGroupPersonCode(GroupPersonCode groupPersonCode) throws Exception;

    void deleteGroupPersonCodeByPersonId(int groupPersonId) throws Exception;

    void deleteOldTokens(int userId, int liveTokenCount) throws Exception;
}
