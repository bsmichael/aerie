/*
 *  Copyright (C) 2021 Gwinnett County Experimental Aircraft Association
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.eaa690.aerie.controller;

import lombok.extern.slf4j.Slf4j;
import org.eaa690.aerie.model.Message;
import org.eaa690.aerie.service.NotamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * MessageController.
 */
@Controller
@RequestMapping("/messages")
@Slf4j
public class MessageController {

    /**
     * NotamService.
     */
    private NotamService notamService;

    /**
     * Sets NotamService.
     *
     * @param value NotamService
     */
    @Autowired
    public void setNotamService(final NotamService value) {
        notamService = value;
    }

    /**
     * Constructor.
     *
     * @param nService NotamService
     */
    public MessageController(final NotamService nService) {
        this.notamService = nService;
    }

    /**
     * Start editing a new message.
     *
     * @param model Model
     * @param messageId Message ID
     * @return page
     */
    @GetMapping({"/addEditPost"})
    public String addEditPost(final Model model, @RequestParam("messageId") final Optional<String> messageId) {
        Message message = new Message();
        model.addAttribute("message", message);
        return "addEditPost";
    }

    /**
     * Submit a message.
     *
     * @param model Model
     * @param message Message
     * @return page
     */
    @PostMapping("/addEditPost")
    public String addEditPostSubmit(final Model model, final Message message) {
        log.info("Subject is {}", message.getSubject());
        log.info("Body is {}", message.getBody());
        return "addEditPost";
    }

}
