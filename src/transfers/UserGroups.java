package transfers;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.ArrayList;

@JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, property ="type")
public class UserGroups implements Serializable, Transfers{
    public ArrayList<Group> groups;
    public ArrayList<GroupPerson> groupRole;

    public UserGroups() {
        groups = new ArrayList<>();
        groupRole = new ArrayList<>();
    }

    public GroupPerson getGroupPerson(Group group){
        int groupId = group.groupId;
        for (int i = 0; i < groupRole.size(); i++) {
            if (groupRole.get(i).groupId==groupId){
                return groupRole.get(i);
            }
        }
        return null;
    }

    public void add(Group group, GroupPerson groupPerson){
        groups.add(group);
        groupRole.add(groupPerson);
    }

    public void add(UserGroups userGroups){
        for (int i = 0; i < userGroups.groups.size(); i++) {
            this.groups.add(userGroups.groups.get(i));
        }
        for (int i = 0; i < userGroups.groupRole.size(); i++) {
            this.groupRole.add(userGroups.groupRole.get(i));
        }
    }

    public void clear(){
        groups.clear();
        groupRole.clear();
    }

    public Group getGroup(int index){
        return groups.get(index);
    }
}
