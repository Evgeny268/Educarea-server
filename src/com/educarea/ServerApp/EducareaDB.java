package com.educarea.ServerApp;

import transfers.Group;
import transfers.GroupPerson;
import transfers.User;

import java.sql.Savepoint;
import java.util.ArrayList;

public interface EducareaDB {

    Savepoint setSavepoint(String name) throws Exception;

    void rollback(Savepoint savepoint) throws Exception;

    void commit() throws Exception;

    int getUserIdByLogin(String login) throws Exception;

    int getGroupIdByName(String name) throws Exception;

    Group getGroupById(int groupId) throws Exception;

    ArrayList<Integer> getGroupsIdByUserId(int userId) throws Exception;

    ArrayList<GroupPerson> getGroupPersonsByUserId(int userId) throws Exception;

    int getUserIdByLogAndPass(String login, String password) throws Exception;

    int getUserIdByAuthToken(String token) throws Exception;

    User getUserById(int userId) throws Exception;

    void insertNewUser(User user) throws Exception;

    void insertNewGroup(String name) throws Exception;

    void insertAuthToken(int userId, String token) throws Exception;

    void updateTokenTime(String token) throws Exception;

    void updateTokenAddress(String token, String address) throws Exception;

    void updateCloudToken(String token, String cloudToken) throws Exception;

    void insertGroupPerson(GroupPerson groupPerson) throws Exception;
}
