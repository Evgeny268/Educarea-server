package DBUtils;

import com.educarea.ServerApp.EducareaDB;
import transfers.User;

import java.sql.ResultSet;
import java.sql.Savepoint;
import java.text.SimpleDateFormat;
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
}
