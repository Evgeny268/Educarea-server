package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.Objects;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class User implements Serializable, Transfers {
    public int iduser = 0;
    public String login = null;
    public String password = null;

    public User() {
    }

    public User(User user){
        this.iduser = user.iduser;
        this.login = user.login;
        this.password = user.password;
    }

    public User(int iduser, String login) {
        this.iduser = iduser;
        this.login = login;
    }

    public User(String login) {
        this.login = login;
    }

    public User(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public User(int iduser, String login, String password) {
        this.iduser = iduser;
        this.login = login;
        this.password = password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(login, user.login) &&
                Objects.equals(password, user.password);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, password);
    }
}
