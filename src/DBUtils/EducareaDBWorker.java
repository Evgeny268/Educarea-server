package DBUtils;

import com.educarea.ServerApp.EducareaDB;
import transfers.Group;
import transfers.GroupPerson;
import transfers.User;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        try(DBWorker.Builder builder = new Builder(false)
        .setSql("INSERT INTO group_person (group_id, user_id, person_type, is_moderator, surname, name, patronymic) VALUES (?,?,?,?,?,?,?)")
        .setParameters(String.valueOf(groupPerson.groupId),String.valueOf(groupPerson.userId),String.valueOf(groupPerson.personType),
                String.valueOf(groupPerson.moderator), groupPerson.surname,groupPerson.name,groupPerson.patronymic)
        .setTypes("int","int","int","int","String","String","String")){
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
            e.printStackTrace();
            throw e;
        }
    }
}
