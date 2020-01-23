package tests;

import DBUtils.DBWorker;
import DBUtils.EducareaDBWorker;
import com.educarea.ServerApp.EducareaDB;
import com.educarea.ServerApp.MessageWorker;
import org.junit.jupiter.api.Test;
import transfers.User;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class Tests {
    @Test
    void selectNonexistentUser() throws Exception {
        DBWorker.init("jdbc:mysql://localhost:3306/educarea","testuser","testuser1234");
        DBWorker.connect();
        EducareaDB educareaDB = EducareaDBWorker.getInstance();
        User user = educareaDB.getUserById(99999999);
        assertNull(user);
    }

    @Test
    void insertUser() throws Exception{
        DBWorker.init("jdbc:mysql://localhost:3306/educarea","testuser","testuser1234");
        DBWorker.connect();
        EducareaDB educareaDB = EducareaDBWorker.getInstance();

        class UserTester implements Runnable{

            private int id;
            EducareaDB educareaDB;

            public UserTester(int id, EducareaDB educareaDB) {
                this.id = id;
                this.educareaDB = educareaDB;
            }

            @Override
            public void run() {
                User newUser = new User("testtest"+String.valueOf(id),"testtest"+String.valueOf(id));
                try {
                    educareaDB.insertNewUser(newUser);
                    educareaDB.commit();
                    int userId = educareaDB.getUserIdByLogAndPass(newUser.login, newUser.password);
                    assertTrue(userId != 0);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }

        ArrayList<Thread> list = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            UserTester userTester = new UserTester(i,educareaDB);
            Thread thread = new Thread(userTester);
            list.add(thread);
        }
        for (int i = 0; i < 10000; i++) {
            list.get(i).start();
        }

        for (int i = 0; i < 10000; i++) {
            list.get(i).join();
        }
    }

    @Test
    void tokenSize(){
        for (int i = 0; i < 10000; i++) {
            int tokenSize = ThreadLocalRandom.current().nextInt(MessageWorker.MIN_TOKEN_LENGTH, MessageWorker.MAX_TOKEN_LENGTH + 1);
            String token = MessageWorker.generateSafeToken(tokenSize);
            assertTrue(token.length()>=MessageWorker.MIN_TOKEN_LENGTH && token.length()<=MessageWorker.MAX_TOKEN_LENGTH);
        }
    }
}
