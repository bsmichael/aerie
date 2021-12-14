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

package org.eaa690.aerie.ssl;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Password Authenticator.
 */
public class PasswordAuthenticator extends Authenticator {

    /**
     * User.
     */
    private final String user;

    /**
     * Password.
     */
    private final String password;

    /**
     * Constructor.
     *
     * @param u user
     * @param p password
     */
    public PasswordAuthenticator(final String u, final String p) {
        this.user = u;
        this.password = p;
    }

    /**
     * Get Password Authentication.
     *
     * @return PasswordAuthentication
     */
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }
}
