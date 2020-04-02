package transfers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;


import java.io.IOException;

public class TransfersFactory {

    public static Transfers createFromJSON(String jsonString){
        ObjectNode node = null;
        try{
            node = new ObjectMapper().readValue(jsonString,ObjectNode.class);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
        if (node.has("type")){
            ObjectMapper objectMapper = new ObjectMapper();
            if (node.get("type").asText().equals("."+Registration.class.getSimpleName())){
                try{
                    Registration registration = (Registration)objectMapper.readValue(jsonString,Registration.class);
                    return registration;
                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
            }else if (node.get("type").asText().equals("."+User.class.getSimpleName())){
                try {
                    User user = (User) objectMapper.readValue(jsonString, User.class);
                    return user;
                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
            }else if (node.get("type").asText().equals("."+TransferRequestAnswer.class.getSimpleName())){
                try {
                    TransferRequestAnswer transferRequestAnswer = (TransferRequestAnswer) objectMapper.readValue(jsonString, TransferRequestAnswer.class);
                    return transferRequestAnswer;
                }catch (IOException e){
                    e.printStackTrace();
                    return null;
                }
            }else if (node.get("type").asText().equals("."+Authentication.class.getSimpleName())) {
                try {
                    Authentication out = objectMapper.readValue(jsonString, Authentication.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+Authorization.class.getSimpleName())) {
                try {
                    Authorization out = objectMapper.readValue(jsonString, Authorization.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+GroupPerson.class.getSimpleName())) {
                try {
                    GroupPerson out = objectMapper.readValue(jsonString, GroupPerson.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+Group.class.getSimpleName())) {
                try {
                    Group out = objectMapper.readValue(jsonString, Group.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+UserGroups.class.getSimpleName())) {
                try {
                    UserGroups out = objectMapper.readValue(jsonString, UserGroups.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+GroupPersons.class.getSimpleName())) {
                try {
                    GroupPersons out = objectMapper.readValue(jsonString, GroupPersons.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+GroupPersonInvite.class.getSimpleName())) {
                try {
                    GroupPersonInvite out = objectMapper.readValue(jsonString, GroupPersonInvite.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+MyInvite.class.getSimpleName())) {
                try {
                    MyInvite out = objectMapper.readValue(jsonString, MyInvite.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+MyInvites.class.getSimpleName())) {
                try {
                    MyInvites out = objectMapper.readValue(jsonString, MyInvites.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+Timetable.class.getSimpleName())) {
                try {
                    Timetable out = objectMapper.readValue(jsonString, Timetable.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+Timetables.class.getSimpleName())) {
                try {
                    Timetables out = objectMapper.readValue(jsonString, Timetables.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+ChannelMessage.class.getSimpleName())) {
                try {
                    ChannelMessage out = objectMapper.readValue(jsonString, ChannelMessage.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+ChannelMessages.class.getSimpleName())) {
                try {
                    ChannelMessages out = objectMapper.readValue(jsonString, ChannelMessages.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+VersionInfo.class.getSimpleName())) {
                try {
                    VersionInfo out = objectMapper.readValue(jsonString, VersionInfo.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+VersionList.class.getSimpleName())) {
                try {
                    VersionList out = objectMapper.readValue(jsonString, VersionList.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+GroupPersonCode.class.getSimpleName())) {
                try {
                    GroupPersonCode out = objectMapper.readValue(jsonString, GroupPersonCode.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else if (node.get("type").asText().equals("."+UserTokens.class.getSimpleName())) {
                try {
                    UserTokens out = objectMapper.readValue(jsonString, UserTokens.class);
                    return out;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            else {
                return null;
            }
        }else {
            return null;
        }
    }
}
