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

package org.eaa690.aerie.roster;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.ssl.FakeX509TrustManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.net.http.HttpClient;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

/**
 * Roster Manager.
 */
public class RosterManager {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(RosterManager.class);

    /**
     * HttpClient.
     */
    private final HttpClient httpClient;

    /**
     * Username to be used when interacting with eaachapters.org.
     */
    private final String username;

    /**
     * Password to be used when interacting with eaachapters.org.
     */
    private final String password;

    /**
     * Helper.
     */
    private final RosterManagerHelper helper = new RosterManagerHelper();

    /**
     * Initializes a RosterManager instance.
     *
     * @param user Username to be used when interacting with eaachapters.org
     * @param pass Password to be used when interacting with eaachapters.org
     * @param client HttpClient
     */
    public RosterManager(final String user, final String pass, final HttpClient client) {
        this.username = user;
        this.password = pass;
        this.httpClient = client;
        LOGGER.info("RosterManager initialized for " + username);
    }

    /**
     * Initializes a RosterManager instance.
     *
     * @param user Username to be used when interacting with eaachapters.org
     * @param pass Password to be used when interacting with eaachapters.org
     */
    public RosterManager(final String user, final String pass) {
        this.username = user;
        this.password = pass;
        this.httpClient = HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .sslContext(getSSLContext())
                .build();
        LOGGER.info("RosterManager initialized for " + username);
    }

    /**
     * Sets list of Slack user names.
     *
     * @param slackUsers list of Slack user names
     */
    public void setSlackUsers(final List<String> slackUsers) {
        helper.setSlackUsers(slackUsers);
    }

    /**
     * Retrieves a list of all members.
     *
     * @return list of all members
     */
    public List<Member> getAllEntries() {
        final Map<String, String> headers = helper.getHttpHeaders(httpClient, username, password);
        if (!helper.isLoggedIn()) {
            helper.login(httpClient, headers);
        }
        helper.viewSearchMembersPage(httpClient, headers);
        if (helper.isLoggedIn()) {
            helper.logout(httpClient, headers);
        }
        final String data = helper.fetchData(httpClient, headers);
        return helper.parseRecords(data);
    }

    /**
     * Saves a member entry in the roster management system.
     *
     * @param person to be created or updated
     * @return current member information
     */
    public Member savePerson(final Member person) {
        final Map<String, String> headers = helper.getHttpHeaders(httpClient, username, password);
        if (!helper.isLoggedIn()) {
            helper.login(httpClient, headers);
        }
        helper.viewSearchMembersPage(httpClient, headers);
        if (!helper.existsUser(httpClient, headers, person.getFirstName(), person.getLastName())) {
            final String viewState = helper.startAddUser(httpClient, headers);
            helper.addMember(httpClient, headers, viewState, person);
        } else {
            helper.updateMember(httpClient, headers, person);
        }
        if (helper.isLoggedIn()) {
            helper.logout(httpClient, headers);
        }
        return person;
    }

    /**
     * Deletes a member from the roster management system.
     *
     * @param rosterId of the member to be deleted
     * @return success of operation
     */
    public boolean deletePerson(final Long rosterId) {
        if (rosterId == null) {
            LOGGER.info("not deleting person as no ID provided");
            return Boolean.FALSE;
        }
        LOGGER.info("deletePerson yet to be implemented");
        return Boolean.TRUE;
    }

    private SSLContext getSSLContext() {
        SSLContext sslContext = null;
        try {
            TrustManager[] trustManagerArray = new TrustManager[1];
            trustManagerArray[0] = new FakeX509TrustManager();
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerArray, new SecureRandom());
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return sslContext;
    }
}
