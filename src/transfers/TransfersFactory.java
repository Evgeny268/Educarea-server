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
            }
            else {
                return null;
            }
        }else {
            return null;
        }
    }
}
