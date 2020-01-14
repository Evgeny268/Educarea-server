package com.educarea.ServerApp;

import transfers.User;

import java.sql.Savepoint;

public interface EducareaDB {

    Savepoint setSavepoint(String name) throws Exception;

    void rollback(Savepoint savepoint) throws Exception;

    void commit() throws Exception;

    int getUserIdByLogin(String login) throws Exception;

    void insertNewUser(User user) throws Exception;
}
