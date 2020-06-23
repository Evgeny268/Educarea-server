package DBUtils;

import com.educarea.ServerApp.EducareaDB;
import transfers.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EducareaDBWorker extends DBWorker implements EducareaDB {

    private static EducareaDBWorker instance;

    private EducareaDBWorker(){}

    public static synchronized EducareaDBWorker getInstance(){
        if (instance == null){
            instance = new EducareaDBWorker();
        }
        return instance;
    }

    @Override
    public Savepoint setSavepoint(String name) throws Exception {
        return connection.setSavepoint(name);
    }

    @Override
    public void rollback(Savepoint savepoint) throws Exception {
        connection.rollback(savepoint);
    }

    @Override
    public void commit() throws Exception {
        connection.commit();
    }

    @Override
    public int getUserIdByLogin(String login) throws Exception {
        int userId = 0;
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT user_id FROM user WHERE BINARY login = ?")
        .setParameters(login)
        .setTypes("String")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                userId = resultSet.getInt(1);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return userId;
    }

    @Override
    public int getGroupIdByName(String name) throws Exception {
        int groupId = 0;
        try(DBWorker.Builder builder = new Builder(true)
                .setSql("SELECT group_id FROM educarea.group WHERE BINARY name = ?")
                .setParameters(name)
                .setTypes("String")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                groupId = resultSet.getInt(1);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return groupId;
    }

    @Override
    public ArrayList<Integer> getGroupsIdByUserId(int userId) throws Exception {
        ArrayList<Integer> groupsId = new ArrayList<>();
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT group_id FROM group_person WHERE user_id = ?")
        .setParameters(String.valueOf(userId))
        .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                groupsId.add(resultSet.getInt(1));
            }
        }catch (Exception e){
            throw e;
        }
        return groupsId;
    }

    @Override
    public Group getGroupById(int groupId) throws Exception {
        Group group = null;
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT * from educarea.group WHERE group_id = ?")
        .setParameters(String.valueOf(groupId))
        .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                int id = resultSet.getInt(1);
                String name = resultSet.getString(2);
                Date date = resultSet.getDate(3);
                group = new Group(id,name,date);
            }
        }catch (Exception e){
            throw e;
        }
        return group;
    }

    @Override
    public ArrayList<GroupPerson> getGroupPersonsByUserId(int userId) throws Exception {
        ArrayList<GroupPerson> people = new ArrayList<>();
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT * FROM group_person WHERE user_id = ?")
        .setParameters(String.valueOf(userId))
        .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                int groupPersonId = resultSet.getInt(1);
                int groupId = resultSet.getInt(2);
                userId = resultSet.getInt(3);
                int personType = resultSet.getInt(4);
                int moderator = resultSet.getInt(5);
                String surname = resultSet.getString(6);
                String name = resultSet.getString(7);
                String patronymic = resultSet.getString(8);
                GroupPerson groupPerson = new GroupPerson(groupPersonId, groupId,userId,personType,moderator);
                groupPerson.surname = surname;
                groupPerson.name = name;
                groupPerson.patronymic = patronymic;
                people.add(groupPerson);
            }
        }
        return people;
    }

    @Override
    public ArrayList<GroupPerson> getGroupPersonsByGroupId(int groupId) throws Exception {
        ArrayList<GroupPerson> people = new ArrayList<>();
        try(DBWorker.Builder builder = new Builder(true)
                .setSql("SELECT * FROM group_person WHERE group_id = ?")
                .setParameters(String.valueOf(groupId))
                .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                int groupPersonId = resultSet.getInt(1);
                groupId = resultSet.getInt(2);
                int userId = resultSet.getInt(3);
                int personType = resultSet.getInt(4);
                int moderator = resultSet.getInt(5);
                String surname = resultSet.getString(6);
                String name = resultSet.getString(7);
                String patronymic = resultSet.getString(8);
                GroupPerson groupPerson = new GroupPerson(groupPersonId, groupId,userId,personType,moderator);
                groupPerson.surname = surname;
                groupPerson.name = name;
                groupPerson.patronymic = patronymic;
                people.add(groupPerson);
            }
        }
        return people;
    }

    @Override
    public int getUserIdByLogAndPass(String login, String password) throws Exception {
        int userId = 0;
        try(DBWorker.Builder builder = new Builder(true)
                .setSql("SELECT user_id FROM user WHERE BINARY login = ? and BINARY password = ?")
                .setParameters(login, password)
                .setTypes("String", "String")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                userId = resultSet.getInt(1);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return userId;
    }

    @Override
    public int getUserIdByAuthToken(String token) throws Exception {
        int userId = 0;
        try(DBWorker.Builder builder = new Builder(true)
                .setSql("SELECT user_id FROM user_tokens WHERE BINARY auth_token = ?")
                .setParameters(token)
                .setTypes("String")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                userId = resultSet.getInt(1);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return userId;
    }

    @Override
    public User getUserById(int userId) throws Exception {
        User user = null;
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT login, password FROM user WHERE user_id = ?")
        .setParameters(String.valueOf(userId))
        .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                String login = resultSet.getString(1);
                String password = resultSet.getString(2);
                user = new User(userId,login,password);
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
        return user;
    }

    @Override
    public void insertNewUser(User user) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("INSERT INTO user (login, password) VALUES (?,?)")
        .setParameters(user.login, user.password)
        .setTypes("String","String")){
            builder.build();
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void insertNewGroup(String name) throws Exception {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentDateTime = format.format(date);
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("INSERT INTO educarea.group (name, create_date) VALUES (?,?)")
        .setParameters(name,currentDateTime)
        .setTypes("String","String")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void insertGroupPerson(GroupPerson groupPerson) throws Exception {
        if ((groupPerson.personType<0 || groupPerson.personType>1) || (groupPerson.moderator<0 || groupPerson.moderator>1)){
            throw new SQLException("personType or moderator incorrect value!");
        }
        String []parametrs = new String[7];
        parametrs[0] = String.valueOf(groupPerson.groupId);
        if (groupPerson.userId==0){
            parametrs[1] = null;
        }else {
            parametrs[1] = String.valueOf(groupPerson.userId);
        }
        parametrs[2] = String.valueOf(groupPerson.personType);
        parametrs[3] = String.valueOf(groupPerson.moderator);
        parametrs[4] = groupPerson.surname;
        parametrs[5] = groupPerson.name;
        parametrs[6] = groupPerson.patronymic;
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("INSERT INTO group_person (group_id, user_id, person_type, is_moderator, surname, name, patronymic) VALUES (?,?,?,?,?,?,?)")
        .setParameters(parametrs)
        .setTypes("int","int","int","int","String","String","String")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void updateGroupPerson(int groupPersonId, GroupPerson groupPerson) throws Exception {
        if ((groupPerson.personType<0 || groupPerson.personType>1) || (groupPerson.moderator<0 || groupPerson.moderator>1)){
            throw new SQLException("personType or moderator incorrect value!");
        }
        String []parametrs = new String[8];
        parametrs[0] = String.valueOf(groupPerson.groupId);
        if (groupPerson.userId==0){
            parametrs[1] = null;
        }else {
            parametrs[1] = String.valueOf(groupPerson.userId);
        }
        parametrs[2] = String.valueOf(groupPerson.personType);
        parametrs[3] = String.valueOf(groupPerson.moderator);
        parametrs[4] = groupPerson.surname;
        parametrs[5] = groupPerson.name;
        parametrs[6] = groupPerson.patronymic;
        parametrs[7] = String.valueOf(groupPersonId);
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("UPDATE educarea.group_person SET group_id = ?, " +
                "user_id = ?, " +
                "person_type = ?, " +
                "is_moderator = ?, " +
                "surname = ?, " +
                "name = ?, " +
                "patronymic = ? " +
                "WHERE group_person_id = ?")
        .setParameters(parametrs)
        .setTypes("int","int","int","int","String","String","String","int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }


    @Override
    public void insertAuthToken(int userId, String token) throws Exception {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentDateTime = format.format(date);
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("INSERT INTO user_tokens (user_id, auth_token, last_date) VALUES (?,?,?)")
        .setParameters(String.valueOf(userId),token,currentDateTime)
        .setTypes("int","String","String")){
            builder.build();
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void updateTokenTime(String token) throws Exception {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentDateTime = format.format(date);
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("UPDATE user_tokens SET last_date = ? WHERE BINARY auth_token = ?")
        .setParameters(currentDateTime, token)
        .setTypes("String","String")){
            builder.build();
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void updateTokenAddress(String token, String address) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
                .setSql("UPDATE user_tokens SET ip_address = ? WHERE BINARY auth_token = ?")
                .setParameters(address, token)
                .setTypes("String","String")){
            builder.build();
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void updateCloudToken(String token, String cloudToken) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
                .setSql("UPDATE user_tokens SET cloud_token = ? WHERE BINARY auth_token = ?")
                .setParameters(cloudToken, token)
                .setTypes("String","String")){
            builder.build();
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void updateGroupPersonUserId(int groupPersonId, int userId) throws Exception {
        String []params = new String[2];
        if (userId == 0){
            params[0] = null;
        }else {
            params[0] = String.valueOf(userId);
        }
        params[1] = String.valueOf(groupPersonId);
        try(DBWorker.Builder builder = new Builder(false)
                .setSql("UPDATE group_person SET user_id = ? WHERE group_person_id = ?")
                .setParameters(params)
                .setTypes("int","int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public GroupPerson getGroupPersonById(int groupPersonId) throws Exception {
        GroupPerson groupPerson = new GroupPerson();
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT * FROM educarea.group_person WHERE group_person_id = ?")
        .setParameters(String.valueOf(groupPersonId))
        .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                groupPerson = new GroupPerson();
                groupPerson.groupPersonId = resultSet.getInt(1);
                groupPerson.groupId = resultSet.getInt(2);
                groupPerson.userId = resultSet.getInt(3);
                groupPerson.personType = resultSet.getInt(4);
                groupPerson.moderator = resultSet.getInt(5);
                groupPerson.surname = resultSet.getString(6);
                groupPerson.name = resultSet.getString(7);
                groupPerson.patronymic = resultSet.getString(8);
            }
        }catch (Exception e){
            throw e;
        }
        return groupPerson;
    }

    @Override
    public void insertTimetable(Timetable timetable) throws Exception {
        String []params = new String[7];
        params[0] = String.valueOf(timetable.groupId);
        if (timetable.groupPersonId==0){
            params[1] = null;
        }else {
            params[1] = String.valueOf(timetable.groupPersonId);
        }
        params[2] = timetable.objectName;
        params[3] = timetable.cabinet;
        params[4] = String.valueOf(timetable.parityweek);
        params[5] = String.valueOf(timetable.day);
        params[6] = timetable.time;
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("INSERT INTO educarea.timetable (group_id, group_person_id, object_name, cabinet, parityweek, day, time) VALUES (?,?,?,?,?,?,?)")
        .setParameters(params)
        .setTypes("int","int","String","String","int","int","String")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void updateTimetable(int timetableId, Timetable timetable) throws Exception {
        String []params = new String[8];
        params[0] = String.valueOf(timetable.groupId);
        if (timetable.groupPersonId==0){
            params[1] = null;
        }else {
            params[1] = String.valueOf(timetable.groupPersonId);
        }
        params[2] = timetable.objectName;
        params[3] = timetable.cabinet;
        params[4] = String.valueOf(timetable.parityweek);
        params[5] = String.valueOf(timetable.day);
        params[6] = timetable.time;
        params[7] = String.valueOf(timetable.timetableId);
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("UPDATE educarea.timetable SET " +
                "group_id = ?, " +
                "group_person_id = ?, " +
                "object_name = ?, " +
                "cabinet = ?, " +
                "parityweek = ?, " +
                "day = ?, " +
                "time = ? " +
                "WHERE timetable_id = ?")
        .setParameters(params)
        .setTypes("int","int","String","String","int","int","String","int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void deleteTimetable(int timetableId) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("DELETE FROM educarea.timetable WHERE timetable_id = ?")
        .setParameters(String.valueOf(timetableId))
        .setTypes("int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public Timetable getTimetableById(int timetableId) throws Exception {
        Timetable timetable = new Timetable();
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT * FROM educarea.timetable WHERE timetable_id = ?")
        .setParameters(String.valueOf(timetableId))
        .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                timetable.timetableId = resultSet.getInt(1);
                timetable.groupId = resultSet.getInt(2);
                timetable.groupPersonId = resultSet.getInt(3);
                timetable.objectName = resultSet.getString(4);
                timetable.cabinet = resultSet.getString(5);
                timetable.parityweek = resultSet.getInt(6);
                timetable.day = resultSet.getInt(7);
                timetable.time = resultSet.getString(8);
            }
        }catch (Exception e){
            throw e;
        }
        return timetable;
    }

    @Override
    public ArrayList<Timetable> getTimetableByGroupId(int groupId) throws Exception {
        ArrayList<Timetable> list = new ArrayList<>();
        try(DBWorker.Builder builder = new Builder(true)
                .setSql("SELECT * FROM educarea.timetable WHERE group_id = ?")
                .setParameters(String.valueOf(groupId))
                .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                Timetable timetable = new Timetable();
                timetable.timetableId = resultSet.getInt(1);
                timetable.groupId = resultSet.getInt(2);
                timetable.groupPersonId = resultSet.getInt(3);
                timetable.objectName = resultSet.getString(4);
                timetable.cabinet = resultSet.getString(5);
                timetable.parityweek = resultSet.getInt(6);
                timetable.day = resultSet.getInt(7);
                timetable.time = resultSet.getString(8);
                list.add(timetable);
            }
        }catch (Exception e){
            throw e;
        }
        return list;
    }

    @Override
    public ArrayList<Timetable> getTimetableByPersonId(int groupPersonId) throws Exception {
        ArrayList<Timetable> list = new ArrayList<>();
        try(DBWorker.Builder builder = new Builder(true)
                .setSql("SELECT * FROM educarea.timetable WHERE group_person_id = ?")
                .setParameters(String.valueOf(groupPersonId))
                .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                Timetable timetable = new Timetable();
                timetable.timetableId = resultSet.getInt(1);
                timetable.groupId = resultSet.getInt(2);
                timetable.groupPersonId = resultSet.getInt(3);
                timetable.objectName = resultSet.getString(4);
                timetable.cabinet = resultSet.getString(5);
                timetable.parityweek = resultSet.getInt(6);
                timetable.day = resultSet.getInt(7);
                timetable.time = resultSet.getString(8);
                list.add(timetable);
            }
        }catch (Exception e){
            throw e;
        }
        return list;
    }

    @Override
    public void deleteGroupPersonById(int groupPersonId) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("DELETE FROM educarea.group_person WHERE group_person_id = ?")
        .setParameters(String.valueOf(groupPersonId))
        .setTypes("int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void deleteGroupPersonByGroupId(int groupId) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
                .setSql("DELETE FROM educarea.group_person WHERE group_id = ?")
                .setParameters(String.valueOf(groupId))
                .setTypes("int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void deleteGroupById(int groupId) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
                .setSql("DELETE FROM educarea.group WHERE group_id = ?")
                .setParameters(String.valueOf(groupId))
                .setTypes("int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void insertChannelMessage(ChannelMessage channelMessage) throws Exception {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentDateTime = format.format(date);
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("INSERT INTO educarea.channel_message (person_from, text, date) VALUES (?,?,?)")
        .setParameters(String.valueOf(channelMessage.personFrom),channelMessage.text, currentDateTime)
        .setTypes("int","String","String")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void insertStudentChatMessage(StudentsChatMessage chatMessage) throws Exception {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentDateTime = format.format(date);
        try(DBWorker.Builder builder = new Builder(false)
                .setSql("INSERT INTO educarea.students_chat (group_id, group_person_id, text, date) VALUES (?,?,?,?)")
                .setParameters(String.valueOf(chatMessage.groupId), String.valueOf(chatMessage.groupPersonId), chatMessage.text, currentDateTime)
                .setTypes("int","int","String","String")){
            builder.build();
        }
    }

    @Override
    public ArrayList<ChannelMessage> selectChannelMessageByPersonId(int personId, int count) throws Exception {
        ArrayList<ChannelMessage> messages = new ArrayList<>();
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT * FROM educarea.channel_message WHERE (person_from = ?) ORDER BY channel_message.date DESC LIMIT ?;")
        .setParameters(String.valueOf(personId),String.valueOf(count))
        .setTypes("int","int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                ChannelMessage message = new ChannelMessage();
                message.channelMessageId = resultSet.getInt(1);
                message.personFrom = resultSet.getInt(2);
                message.text = resultSet.getString(3);
                String sDate = resultSet.getString(4);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                message.date = format.parse(sDate);
                message.readIn = resultSet.getDate(5);
                messages.add(message);
            }
        }catch (Exception e){
            throw e;
        }
        return messages;
    }

    @Override
    public ArrayList<ChannelMessage> selectChannelMessageByPersonId(int personId, Date lastDate, int count) throws Exception {
        ArrayList<ChannelMessage> messages = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String strDate = format.format(lastDate);
        try(DBWorker.Builder builder = new Builder(true)
                .setSql("SELECT * FROM educarea.channel_message WHERE (person_from = ? AND date < ?) ORDER BY channel_message.date DESC LIMIT ?;")
                .setParameters(String.valueOf(personId),strDate,String.valueOf(count))
                .setTypes("int","String","int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                ChannelMessage message = new ChannelMessage();
                message.channelMessageId = resultSet.getInt(1);
                message.personFrom = resultSet.getInt(2);
                message.text = resultSet.getString(3);
                String sDate = resultSet.getString(4);
                Date date = format.parse(sDate);
                message.date = date;
                message.readIn = resultSet.getDate(5);
                messages.add(message);
            }
        }catch (Exception e){
            throw e;
        }
        return messages;
    }

    @Override
    public ArrayList<StudentsChatMessage> selectStudentsChatMessage(int groupId, int count) throws Exception {
        ArrayList<StudentsChatMessage> messages = new ArrayList<>();
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT * FROM educarea.students_chat WHERE (group_id = ?) ORDER BY students_chat_id DESC LIMIT ?;")
        .setParameters(String.valueOf(groupId), String.valueOf(count))
        .setTypes("int","int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                StudentsChatMessage message = new StudentsChatMessage();
                message.studentsChatId = resultSet.getInt(1);
                message.groupId = resultSet.getInt(2);
                message.groupPersonId = resultSet.getInt(3);
                message.text = resultSet.getString(4);
                String sDate = resultSet.getString(5);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                message.date = format.parse(sDate);
                message.readIn = resultSet.getDate(6);
                messages.add(message);
            }
        }catch (Exception e){
            throw e;
        }
        return messages;
    }

    @Override
    public ArrayList<StudentsChatMessage> selectStudentsChatMessage(int groupId, int count, Integer lastId) throws Exception {
        ArrayList<StudentsChatMessage> messages = new ArrayList<>();
        try(DBWorker.Builder builder = new Builder(true)
                .setSql("SELECT * FROM educarea.students_chat WHERE (group_id = ? AND students_chat_id < ?) ORDER BY students_chat_id DESC LIMIT ?;")
                .setParameters(String.valueOf(groupId), String.valueOf(lastId), String.valueOf(count))
                .setTypes("int","int","int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                StudentsChatMessage message = new StudentsChatMessage();
                message.studentsChatId = resultSet.getInt(1);
                message.groupId = resultSet.getInt(2);
                message.groupPersonId = resultSet.getInt(3);
                message.text = resultSet.getString(4);
                String sDate = resultSet.getString(5);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                message.date = format.parse(sDate);
                message.readIn = resultSet.getDate(6);
                messages.add(message);
            }
        }catch (Exception e){
            throw e;
        }
        return messages;
    }

    @Override
    public ArrayList<UserTokens> getUserTokensByUserId(int userId) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        ArrayList<UserTokens> list = new ArrayList<>();
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT * FROM educarea.user_tokens WHERE user_id = ?")
        .setParameters(String.valueOf(userId))
        .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                UserTokens userTokens = new UserTokens();
                userTokens.userTokensId = resultSet.getInt(1);
                userTokens.userId = resultSet.getInt(2);
                userTokens.authToken = resultSet.getString(3);
                userTokens.cloudToken = resultSet.getString(4);
                String sDate = resultSet.getString(5);
                Date date = format.parse(sDate);
                userTokens.lastDate = date;
                userTokens.ipAddress = resultSet.getString(6);
                list.add(userTokens);
            }
        }catch (Exception e){
            throw e;
        }
        return list;
    }

    @Override
    public void deleteOldTokens(int userId, int liveTokenCount) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("delete from educarea.user_tokens where user_id = ? and last_date < (select a.last_date from (select last_date from educarea.user_tokens where user_id = ? order by last_date desc limit 1 offset ?) as a)")
        .setParameters(String.valueOf(userId), String.valueOf(userId), String.valueOf(liveTokenCount))
        .setTypes("int","int","int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void insertGroupPersonCode(GroupPersonCode groupPersonCode) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("INSERT INTO educarea.group_person_code (group_person_id, code) VALUES (?,?)")
        .setParameters(String.valueOf(groupPersonCode.groupPersonId), groupPersonCode.code)
        .setTypes("int","String")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void deleteGroupPersonCodeByPersonId(int groupPersonId) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("DELETE FROM educarea.group_person_code WHERE group_person_id = ?")
        .setParameters(String.valueOf(groupPersonId))
        .setTypes("int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public GroupPersonCode getGroupPersonCodeByCode(String code) throws Exception {
        GroupPersonCode groupPersonCode = null;
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT * FROM educarea.group_person_code WHERE BINARY code = ?")
        .setParameters(code)
        .setTypes("String")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                groupPersonCode = new GroupPersonCode();
                groupPersonCode.groupPersonCodeId = resultSet.getInt(1);
                groupPersonCode.groupPersonId = resultSet.getInt(2);
                groupPersonCode.code = resultSet.getString(3);
            }
        }catch (Exception e){
            throw e;
        }
        return groupPersonCode;
    }

    @Override
    public GroupPersonCode getGroupPersonCodeByPersonId(int personId) throws Exception {
        GroupPersonCode groupPersonCode = null;
        try(DBWorker.Builder builder = new Builder(true)
                .setSql("SELECT * FROM educarea.group_person_code WHERE group_person_id = ?")
                .setParameters(String.valueOf(personId))
                .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                groupPersonCode = new GroupPersonCode();
                groupPersonCode.groupPersonCodeId = resultSet.getInt(1);
                groupPersonCode.groupPersonId = resultSet.getInt(2);
                groupPersonCode.code = resultSet.getString(3);
            }
        }catch (Exception e){
            throw e;
        }
        return groupPersonCode;
    }

    @Override
    public void deleteTimetableByGroupId(int groupId) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("DELETE FROM educarea.timetable WHERE group_id = ?")
        .setParameters(String.valueOf(groupId))
        .setTypes("int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void deleteChannelMessageByPersonId(int personId) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("DELETE FROM educarea.channel_message WHERE person_from = ?")
        .setParameters(String.valueOf(personId))
        .setTypes("int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void deleteStudentsChatMessageByGroupId(int groupId) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("DELETE FROM educarea.students_chat WHERE group_id = ?")
        .setParameters(String.valueOf(groupId))
        .setTypes("int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void deleteStudentsChatMessageByPersonId(int groupPersonId) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
                .setSql("DELETE FROM educarea.students_chat WHERE group_person_id = ?")
                .setParameters(String.valueOf(groupPersonId))
                .setTypes("int")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void deleteTokenByCloudToken(String cloudToken) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
                .setSql("DELETE FROM educarea.user_tokens WHERE cloud_token = ?")
                .setParameters(cloudToken)
                .setTypes("String")){
            builder.build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public void deleteTokenByToken(String token) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("DELETE FROM educarea.user_tokens WHERE auth_token = ?")
        .setParameters(token)
        .setTypes("String")){
            builder.build();
        }
    }

    @Override
    public void deleteTokenExceptOne(int userId, String token) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("DELETE FROM educarea.user_tokens WHERE user_id = ? AND auth_token != ?")
        .setParameters(String.valueOf(userId), token)
        .setTypes("int","String")){
            builder.build();
        }
    }

    @Override
    public void insertEvent(Event event) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentDateTime = format.format(event.date);
        if (event.groupPersonId!=null) {
            try (DBWorker.Builder builder = new Builder(false)
                    .setSql("INSERT INTO educarea.event (group_id, group_person_id, title, date, text) VALUES (?,?,?,?,?);")
                    .setParameters(String.valueOf(event.groupId), String.valueOf(event.groupPersonId), event.title, currentDateTime, event.text)
                    .setTypes("int", "int", "String", "String", "String")) {
                builder.build();
            }
        }else {
            try (DBWorker.Builder builder = new Builder(false)
                    .setSql("INSERT INTO educarea.event (group_id, group_person_id, title, date, text) VALUES (?,?,?,?,?);")
                    .setParameters(String.valueOf(event.groupId), null, event.title, currentDateTime, event.text)
                    .setTypes("int", "int", "String", "String", "String")) {
                builder.build();
            }
        }
    }

    @Override
    public void updateEvent(Event event) throws Exception {
        Integer groupPersonId = event.groupPersonId;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        String currentDateTime = format.format(event.date);
        if (groupPersonId!=null) {
            try (DBWorker.Builder builder = new Builder(false)
                    .setSql("UPDATE educarea.event SET group_person_id = ?, title = ?, date = ?, text = ?, WHERE event_id = ?")
                    .setParameters(String.valueOf(groupPersonId), event.title, currentDateTime, event.text, String.valueOf(event.eventId))
                    .setTypes("int", "String", "String", "String", "int")) {
                builder.build();
            }
        }else {
            try (DBWorker.Builder builder = new Builder(false)
                    .setSql("UPDATE educarea.event SET group_person_id = ?, title = ?, date = ?, text = ?, WHERE event_id = ?")
                    .setParameters(null, event.title, currentDateTime, event.text, String.valueOf(event.eventId))
                    .setTypes("int", "String", "String", "String", "int")) {
                builder.build();
            }
        }
    }

    @Override
    public Event getEventById(int event_id) throws Exception {
        Event event = null;
        try(DBWorker.Builder builder = new Builder(true)
        .setSql("SELECT * FROM educarea.event WHERE event_id = ?")
        .setParameters(String.valueOf(event_id))
        .setTypes("int")){
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()){
                event = new Event();
                event.eventId = resultSet.getInt(1);
                event.groupId = resultSet.getInt(2);
                event.groupPersonId = resultSet.getInt(3);
                event.title = resultSet.getString(4);
                String sDate = resultSet.getString(5);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                event.date = format.parse(sDate);
                event.text = resultSet.getString(6);
            }
        }
        return event;
    }

    @Override
    public List<Event> getEvents(int groupId) throws Exception {
        List<Event> events = new ArrayList<>();
        try(DBWorker.Builder builder = new Builder(true)
                .setSql("SELECT * FROM educarea.event WHERE group_id = ?")
                .setParameters(String.valueOf(groupId))
                .setTypes("int")) {
            builder.build();
            ResultSet resultSet = builder.getResultSet();
            while (resultSet.next()) {
                Event event = new Event();
                event.eventId = resultSet.getInt(1);
                event.groupId = resultSet.getInt(2);
                event.groupPersonId = resultSet.getInt(3);
                event.title = resultSet.getString(4);
                String sDate = resultSet.getString(5);
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                event.date = format.parse(sDate);
                event.text = resultSet.getString(6);
                events.add(event);
            }
        }
        return events;
    }

    @Override
    public void deleteEventById(int eventId) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
                .setSql("DELETE FROM educarea.event WHERE event_id = ?")
                .setParameters(String.valueOf(eventId))
                .setTypes("int")){
            builder.build();
        }
    }

    @Override
    public void deleteEventByGroupId(int groupId) throws Exception {
        try(DBWorker.Builder builder = new Builder(false)
                .setSql("DELETE FROM educarea.event WHERE group_id = ?")
                .setParameters(String.valueOf(groupId))
                .setTypes("int")){
            builder.build();
        }
    }
}
