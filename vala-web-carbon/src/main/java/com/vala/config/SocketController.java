package com.vala.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vala.base.service.BaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
@RestController
public class SocketController {

    @Autowired
    private SimpMessagingTemplate messageTemplate;


    /**
     * 广播
     * @param message
     * @return
     */
    @MessageMapping("/welcome")
    @SendTo("/topic/greetings")
    public Map say(Map message) {
        System.out.println("/welcome:"+message);
        Map res = new HashMap();
        res.put("topic", message.get("chatType").toString());
        return res;
    }


    /**
     * 请求接口推送消息-（广播）
     * @param message
     */
    @GetMapping("/sentMes")
    public void sentMes(String message) {
        System.out.println("/sentMes:"+message);
        this.messageTemplate.convertAndSend("/queue/msg", message);
    }



    /**
     * 点对点通信
     * @param message
     */
    @MessageMapping(value = "/point")
    @SendToUser("/topic/point")
    public String point(Map message) {
        System.out.println("/point:"+message);
        return "dd";
    }

//    /**
//     * 点对点通信
//     * @param msg
//     */
//    @MessageMapping(value = "/points")
//    public void point1(MessageBean msg) throws Exception {
//
//        System.out.println(msg);
//
//
//        if("group".equalsIgnoreCase(msg.getDomain())){
//            String taskTag = msg.taskId.toString()+"_";
//            ObjectMapper mapper = new ObjectMapper();
//            String s = mapper.writeValueAsString(msg);
//            messageTemplate.convertAndSendToUser(taskTag, "/queue/points", s);
//        }else{
//            String userTag = msg.toId.toString();
//            ObjectMapper mapper = new ObjectMapper();
//            String s = mapper.writeValueAsString(msg);
//            //发送消息给指定用户, 最后接受消息的路径会变成 /user/admin/queue/points
//            messageTemplate.convertAndSendToUser(userTag, "/queue/points", s);
//
//        }
//
//
//
//    }

}
