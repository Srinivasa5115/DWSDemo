package com.dws.challenge.web;

import com.dws.challenge.service.FCMService;
import com.dws.challenge.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationController {

    @Autowired
    private FCMService fcmService;

    @PostMapping("/notification")
    public ResponseEntity<String> sendNotification(@RequestBody NotificationService request) throws  InterruptedException {
        fcmService.sendMessageToToken(request);
        return new ResponseEntity("Notification has been sent.",HttpStatus.OK);
    }
}
