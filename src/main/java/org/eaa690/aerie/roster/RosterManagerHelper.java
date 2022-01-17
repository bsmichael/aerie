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

import lombok.extern.slf4j.Slf4j;
import org.eaa690.aerie.config.CommonConstants;
import org.eaa690.aerie.config.RosterConstants;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.roster.Country;
import org.eaa690.aerie.model.roster.Gender;
import org.eaa690.aerie.model.roster.MemberType;
import org.eaa690.aerie.model.roster.OtherInfo;
import org.eaa690.aerie.model.roster.State;
import org.eaa690.aerie.model.roster.Status;
import org.eaa690.aerie.model.roster.WebAdminAccess;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Roster Manager Helper.
 */
@Slf4j
public class RosterManagerHelper {

    /**
     * Date formatter.
     */
    private final SimpleDateFormat mdySDF = new SimpleDateFormat("MM/dd/yyyy");

    /**
     * Date formatter.
     */
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * ViewStatePattern.
     */
    private final Pattern viewStatePattern =
            Pattern.compile(".*<input type=\"hidden\" name=\"__VIEWSTATE\" id=\"__VIEWSTATE\" value=\"(.*?)\" />.*",
                    Pattern.DOTALL);

    /**
     * ViewStateMap.
     */
    private final Map<String, Element> viewStateMap = new HashMap<>();

    /**
     * List of Slack usernames.
     */
    private List<String> slackUsers = new ArrayList<>();

    /**
     * Tracks login status.
     */
    private boolean loggedIn = false;

    /**
     * Constructor.
     */
    public RosterManagerHelper() {
        // Do nothing
    }

    /**
     * Sets list of Slack user names.
     *
     * @param users list of Slack user names
     */
    public void setSlackUsers(final List<String> users) {
        if (slackUsers != null) {
            this.slackUsers = users;
            log.info("slackUsers set");
        }
    }

    /**
     * Performs login to EAA's roster management system.
     *
     * @param httpClient HttpClient
     * @param headers Map of Headers
     */
    public void login(final HttpClient httpClient, final Map<String, String> headers) {
        log.debug("Performing login...");
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/main.aspx";
        final String requestBodyStr = buildLoginRequestBodyString(headers);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        final HttpRequest request = builder.build();

        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            loggedIn = true;
        } catch (Exception e) {
            log.error("[Login] Error", e);
        }
    }

    /**
     * Checks if session is currently logged in.
     *
     * @return session is logged in (or not)
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Logs out.
     *
     * @param httpClient HttpClient
     * @param headers Map of Headers
     */
    public void logout(final HttpClient httpClient, final Map<String, String> headers) {
        log.debug("Performing logout...");
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/main.aspx";
        final String requestBodyStr = buildLogoutRequestBodyString(headers);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        final HttpRequest request = builder.build();
        try {
            httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            loggedIn = false;
        } catch (Exception e) {
            log.error("[Logout] Error", e);
        }
    }

    /**
     * Gets searchmembers page in EAA's roster management system.
     *
     * @param httpClient HttpClient
     * @param headers Map of Headers
     */
    public void viewSearchMembersPage(final HttpClient httpClient,
                                       final Map<String, String> headers) {
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .GET();
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        final HttpRequest request = builder.build();

        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            log.debug("Received status code=" + response.statusCode());

            final Document doc = Jsoup.parse(response.body());
            viewStateMap.put(RosterConstants.VIEW_STATE, doc.getElementById(RosterConstants.VIEW_STATE));
            viewStateMap.put(RosterConstants.VIEW_STATE_GENERATOR,
                    doc.getElementById(RosterConstants.VIEW_STATE_GENERATOR));
            headers.put(RosterConstants.VIEW_STATE, getViewStateValue(viewStateMap.get(RosterConstants.VIEW_STATE)));
        } catch (IOException | InterruptedException e) {
            log.error("[Search Page] Error", e);
        }
    }

    /**
     * Gets searchmembers page in EAA's roster management system.
     *
     * @param httpClient HttpClient
     * @param headers Map of Headers
     * @param viewState ViewState
     * @param person Member
     */
    public void addMember(final HttpClient httpClient, final Map<String, String> headers,
                          final String viewState, final Member person) {
        try {
            final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
            final HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(uriStr))
                    .POST(buildNewUserRequestBodyString(person, viewState));
            headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,"
                    + "image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            headers.put(RosterConstants.CONTENT_TYPE, "application/x-www-form-urlencoded");
            headers.put("sec-ch-ua", "\" Not;A Brand\";v=\"99\", \"Google Chrome\";v=\"91\", \"Chromium\";v=\"91\"");
            headers.put("sec-ch-ua-mobile", "?0");
            headers.put("upgrade-insecure-requests", "1");
            headers.put("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/91.0.4472.164 Safari/537.36");
            headers.put("sec-fetch-site", "same-origin");
            headers.put("sec-fetch-mode", "navigate");
            headers.put("sec-fetch-user", "?1");
            headers.put("sec-fetch-dest", "document");
            final HttpRequest request = builder.build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Received status code=" + response.statusCode());
            log.info("Received headers=" + response.headers());
            log.info("[Add member] response: " + response.body());
        } catch (IOException | InterruptedException e) {
            log.error("[Add Member] Error", e);
        }
    }

    /**
     * Updates a member.
     *
     * @param httpClient HttpClient
     * @param headers Map of Headers
     * @param person Member
     */
    public void updateMember(final HttpClient httpClient, final Map<String, String> headers, final Member person) {
        log.info("Updating existing entry");
    }

    /**
     * Checks if a user exists in EAA's roster management system.
     *
     * @param httpClient HttpClient
     * @param headers Map of Header
     * @param firstName First name
     * @param lastName Last name
     * @return if user exists of not
     */
    public boolean existsUser(final HttpClient httpClient,
                               final Map<String, String> headers,
                               final String firstName,
                               final String lastName) {
        log.debug(String.format("Checking if %s %s exists...", firstName, lastName));
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        final String requestBodyStr = buildExistsUserRequestBodyString(headers, firstName, lastName);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/"
                + "webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        final HttpRequest request = builder.build();

        final StringBuilder sb = new StringBuilder();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            log.debug("Received status code=" + response.statusCode());
            sb.append(response.body());
        } catch (IOException | InterruptedException e) {
            log.error("[existsUser] Error", e);
        }
        return sb.toString().contains("lnkViewUpdateMember");
    }

    /**
     * Checks if a user exists in EAA's roster management system.
     *
     * @param httpClient HttpClient
     * @param headers Map of Headers
     * @return add user string
     */
    public String startAddUser(final HttpClient httpClient,
                               final Map<String, String> headers) {
        log.debug("Starting add user process...");
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        final String requestBodyStr = buildAddUserRequestBodyString(headers);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/"
                + "webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }

        try {
            final HttpResponse<String> response = httpClient.send(builder.build(),
                    HttpResponse.BodyHandlers.ofString());
            log.debug("Received status code=" + response.statusCode());
            log.debug("Received headers: " + response.headers());
            final Matcher m = viewStatePattern.matcher(response.body());
            if (m.matches()) {
                return m.group(1);
            }
        } catch (IOException | InterruptedException e) {
            log.error("[startAddUser] Error", e);
        }
        return null;
    }

    /**
     * Fetch's data from EAA's roster management system.
     *
     * @param httpClient HttpClient
     * @param headers Map of headers
     * @return fetch data string
     */
    public String fetchData(final HttpClient httpClient, final Map<String, String> headers) {
        final String uriStr = RosterConstants.EAA_CHAPTERS_SITE_BASE + "/searchmembers.aspx";
        final String requestBodyStr = buildFetchDataRequestBodyString(headers);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(uriStr))
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyStr));
        headers.remove(RosterConstants.VIEW_STATE);
        headers.put(RosterConstants.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/"
                + "webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            builder.setHeader(entry.getKey(), entry.getValue());
        }
        final HttpRequest request = builder.build();

        final StringBuilder sb = new StringBuilder();
        try {
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());
            log.info("Received status code=" + response.statusCode());
            sb.append(response.body());
        } catch (IOException | InterruptedException e) {
            log.error("[fetchData] Error", e);
        }
        return sb.toString();
    }

    /**
     * Gets HttpHeaders.
     *
     * @param httpClient HttpClient
     * @param username Username
     * @param password Password
     * @return Map of Headers
     */
    public Map<String, String> getHttpHeaders(
            final HttpClient httpClient, final String username, final String password) {
        final Map<String, String> headers = new HashMap<>();
        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(URI.create(RosterConstants.EAA_CHAPTERS_SITE_BASE + "/main.aspx"))
                .GET()
                .build();
        try {
            final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.debug("Received status code=" + response.statusCode());
            final HttpHeaders responseHeaders = response.headers();
            final String cookieStr = responseHeaders.firstValue("set-cookie").orElse("");
            headers.put("cookie", cookieStr.substring(0, cookieStr.indexOf(";")));
        } catch (IOException | InterruptedException e) {
            log.error("[getHttpHeaders] Error", e);
        }
        headers.put(RosterConstants.EVENT_TARGET, "");
        headers.put(RosterConstants.EVENT_ARGUMENT, "");
        headers.put(RosterConstants.VIEW_STATE, getViewStateValue(viewStateMap.get(RosterConstants.VIEW_STATE)));
        headers.put(RosterConstants.VIEW_STATE_GENERATOR,
                getViewStateGeneratorValue(viewStateMap.get(RosterConstants.VIEW_STATE_GENERATOR)));
        headers.put(RosterConstants.EVENT_VALIDATION, "/wEdAAaUkhCi8bB8A8YPK1mx/fN+Ob9NwfdsH6h5T4oBt2E/NC/PSAvxybIG70"
                + "Gi7lMSo2Ha9mxIS56towErq28lcj7mn+o6oHBHkC8q81Z+42F7hK13DHQbwWPwDXbrtkgbgsBJaWfipkuZE5/MRRQAXrNwOiJp3Y"
                + "Glq4qKyVLK8XZVxQ==");
        headers.put(RosterConstants.USERNAME, username);
        headers.put(RosterConstants.PASSWORD, password);
        headers.put(RosterConstants.BUTTON, "Submit");
        headers.put(RosterConstants.USER_AGENT, "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_6) AppleWebKit/537.36 "
                + "(KHTML, like Gecko) Chrome/85.0.4183.121 Safari/537.36");
        headers.put(RosterConstants.CONTENT_TYPE, "application/x-www-form-urlencoded");
        headers.put(RosterConstants.EXPORT_BUTTON, "Results+To+Excel");
        headers.put(RosterConstants.STATUS, "Active");
        headers.put(RosterConstants.FIRST_NAME, "");
        headers.put(RosterConstants.LAST_NAME, "");
        headers.put(RosterConstants.SEARCH_MEMBER_TYPE, "");
        headers.put(RosterConstants.CURRENT_STATUS, "");
        headers.put(RosterConstants.ROW_COUNT, "");
        headers.put(RosterConstants.VIEW_STATE_ENCRYPTED, "");
        headers.put(RosterConstants.LAST_FOCUS, "");
        return headers;
    }

    private String buildLoginRequestBodyString(final Map<String, String> headers) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.EVENT_VALIDATION);
        data.add(RosterConstants.USERNAME);
        data.add(RosterConstants.PASSWORD);
        data.add(RosterConstants.BUTTON);
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            final String key = entry.getKey();
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                if (RosterConstants.USERNAME.equals(key)
                        || RosterConstants.PASSWORD.equals(key)
                        || RosterConstants.BUTTON.equals(key)) {
                    sb.append(key.replaceAll("\\$", "%24"));
                } else {
                    sb.append(key);
                }
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(entry.getValue()
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else {
                    sb.append(entry.getValue());
                }
            }
        }
        return sb.toString();
    }

    private String buildLogoutRequestBodyString(final Map<String, String> headers) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.EVENT_VALIDATION);
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            final String key = entry.getKey();
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                sb.append(key);
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(entry.getValue()
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else if (RosterConstants.EVENT_TARGET.equals(key)) {
                    sb.append("ctl00$lnkbtnLogoff");
                } else {
                    sb.append(entry.getValue());
                }
            }
        }
        return sb.toString();
    }

    private String buildFetchDataRequestBodyString(final Map<String, String> headers) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.LAST_FOCUS);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.VIEW_STATE_ENCRYPTED);
        data.add(RosterConstants.FIRST_NAME);
        data.add(RosterConstants.LAST_NAME);
        data.add(RosterConstants.EXPORT_BUTTON);
        data.add(RosterConstants.STATUS);
        data.add(RosterConstants.SEARCH_MEMBER_TYPE);
        data.add(RosterConstants.CURRENT_STATUS);
        data.add(RosterConstants.ROW_COUNT);
        return addDataFromHeaders(headers, sb, data);
    }

    private String buildExistsUserRequestBodyString(final Map<String, String> headers, final String firstName,
                                                    final String lastName) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.VIEW_STATE_ENCRYPTED);
        data.add(RosterConstants.SEARCH_BUTTON);
        data.add(RosterConstants.FIRST_NAME + RosterConstants.EQUALS + firstName);
        data.add(RosterConstants.LAST_NAME + RosterConstants.EQUALS + lastName);
        data.add(RosterConstants.STATUS + "=Active");
        data.add(RosterConstants.SEARCH_MEMBER_TYPE);
        data.add(RosterConstants.CURRENT_STATUS);
        return addDataFromHeaders(headers, sb, data);
    }

    private String buildAddUserRequestBodyString(final Map<String, String> headers) {
        final StringBuilder sb = new StringBuilder();
        final List<String> data = new ArrayList<>();
        data.add(RosterConstants.EVENT_TARGET);
        data.add(RosterConstants.EVENT_ARGUMENT);
        data.add(RosterConstants.VIEW_STATE);
        data.add(RosterConstants.VIEW_STATE_GENERATOR);
        data.add(RosterConstants.VIEW_STATE_ENCRYPTED);
        data.add(RosterConstants.SHOW_MEMBER_ADD_PANEL_BUTTON + RosterConstants.EQUALS + "Add a new Person!");
        data.add(RosterConstants.STATUS + "=Active");
        data.add(RosterConstants.SEARCH_MEMBER_TYPE);
        data.add(RosterConstants.CURRENT_STATUS);
        return addDataFromHeaders(headers, sb, data);
    }

    private HttpRequest.BodyPublisher buildNewUserRequestBodyString(final Member person, final String viewState) {
        final Map<Object, Object> data = new HashMap<>();
        data.put(RosterConstants.EVENT_TARGET, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.EVENT_ARGUMENT, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.VIEW_STATE, viewState);
        data.put(RosterConstants.VIEW_STATE_GENERATOR,
                getViewStateGeneratorValue(viewStateMap.get(RosterConstants.VIEW_STATE_GENERATOR)));
        data.put(RosterConstants.VIEW_STATE_ENCRYPTED, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.FIRST_NAME, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.LAST_NAME, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.STATUS, Status.getDisplayString(person.getStatus()));
        data.put(RosterConstants.SEARCH_MEMBER_TYPE, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.CURRENT_STATUS, RosterConstants.EMPTY_STRING);
        data.put(RosterConstants.ADD_NEW_MEMBER_BUTTON, "Add This Person");
        // Required Field
        data.put(RosterConstants.TEXT_FIRST_NAME, person.getFirstName());
        // Required Field
        data.put(RosterConstants.TEXT_LAST_NAME, person.getLastName());
        if (person.getNickname() != null) {
            data.put(RosterConstants.TEXT_NICK_NAME, person.getNickname());
        } else {
            data.put(RosterConstants.TEXT_NICK_NAME, RosterConstants.EMPTY_STRING);
        }
        if (person.getSpouse() != null) {
            data.put(RosterConstants.SPOUSE, person.getSpouse());
        } else {
            data.put(RosterConstants.SPOUSE, RosterConstants.EMPTY_STRING);
        }
        data.put(RosterConstants.GENDER, Gender.getDisplayString(person.getGender()));
        // Numbers only
        if (person.getEaaNumber() != null) {
            data.put(RosterConstants.MEMBER_ID, person.getEaaNumber());
        } else {
            data.put(RosterConstants.MEMBER_ID, RosterConstants.EMPTY_STRING);
        }
        // Required Field
        data.put(RosterConstants.MEMBER_TYPE, MemberType.toDisplayString(person.getMemberType()));
        // Required Field
        data.put(RosterConstants.CURRENT_STANDING, Status.getDisplayString(person.getStatus()));
        // Required Field
        data.put(RosterConstants.ADMIN_LEVEL, WebAdminAccess.getDisplayString(person.getWebAdminAccess()));
        if (person.getAddressLine1() != null) {
            data.put(RosterConstants.ADDRESS_LINE_1, person.getAddressLine1());
        } else {
            data.put(RosterConstants.ADDRESS_LINE_1, RosterConstants.EMPTY_STRING);
        }
        if (person.getAddressLine2() != null) {
            data.put(RosterConstants.ADDRESS_LINE_2, person.getAddressLine2());
        } else {
            data.put(RosterConstants.ADDRESS_LINE_2, RosterConstants.EMPTY_STRING);
        }
        if (person.getCity() != null) {
            data.put(RosterConstants.CITY, person.getCity());
        } else {
            data.put(RosterConstants.CITY, RosterConstants.EMPTY_STRING);
        }
        data.put(RosterConstants.STATE, State.getDisplayString(person.getState()));
        if (person.getZipCode() != null) {
            data.put(RosterConstants.ZIP_CODE, person.getZipCode());
        } else {
            data.put(RosterConstants.ZIP_CODE, RosterConstants.EMPTY_STRING);
        }
        data.put(RosterConstants.COUNTRY, Country.toDisplayString(person.getCountry()));
        // Must be in mm/dd/yyyy format
        if (person.getBirthDate() != null) {
            data.put(RosterConstants.BIRTH_DATE, person.getBirthDate());
        } else {
            data.put(RosterConstants.BIRTH_DATE, RosterConstants.EMPTY_STRING);
        }
        // Must be in mm/dd/yyyy format
        if (person.getJoined() != null) {
            data.put(RosterConstants.JOIN_DATE, person.getJoined());
        } else {
            data.put(RosterConstants.JOIN_DATE, RosterConstants.EMPTY_STRING);
        }
        // Must be in mm/dd/yyyy format
        // Required Field
        data.put(RosterConstants.EXPIRATION_DATE, mdySDF.format(person.getExpiration()));
        if (person.getOtherInfo() != null) {
            data.put(RosterConstants.OTHER_INFO, person.getOtherInfo());
        } else {
            data.put(RosterConstants.OTHER_INFO, RosterConstants.EMPTY_STRING);
        }
        if (person.getHomePhone() != null) {
            data.put(RosterConstants.HOME_PHONE, person.getHomePhone()
                    .replaceAll("\\.", RosterConstants.EMPTY_STRING)
                    .replaceAll("-", RosterConstants.EMPTY_STRING)
                    .replaceAll("\\(", RosterConstants.EMPTY_STRING)
                    .replaceAll("\\)", RosterConstants.EMPTY_STRING));
        } else {
            data.put(RosterConstants.HOME_PHONE, RosterConstants.EMPTY_STRING);
        }
        if (person.getCellPhone() != null) {
            data.put(RosterConstants.CELL_PHONE, person.getCellPhone()
                    .replaceAll("\\.", RosterConstants.EMPTY_STRING)
                    .replaceAll("-", RosterConstants.EMPTY_STRING)
                    .replaceAll("\\(", RosterConstants.EMPTY_STRING)
                    .replaceAll("\\)", RosterConstants.EMPTY_STRING));
        } else {
            data.put(RosterConstants.CELL_PHONE, RosterConstants.EMPTY_STRING);
        }
        if (person.getEmail() != null) {
            data.put(RosterConstants.EMAIL, person.getEmail());
        } else {
            data.put(RosterConstants.EMAIL, RosterConstants.EMPTY_STRING);
        }
        if (person.getRatings() != null) {
            data.put(RosterConstants.RATINGS, person.getRatings());
        } else {
            data.put(RosterConstants.RATINGS, RosterConstants.EMPTY_STRING);
        }
        if (person.getAircraftOwned() != null) {
            data.put(RosterConstants.AIRCRAFT_OWNED, person.getAircraftOwned());
        } else {
            data.put(RosterConstants.AIRCRAFT_OWNED, RosterConstants.EMPTY_STRING);
        }
        if (person.getAircraftProject() != null) {
            data.put(RosterConstants.AIRCRAFT_PROJECT, person.getAircraftProject());
        } else {
            data.put(RosterConstants.AIRCRAFT_PROJECT, RosterConstants.EMPTY_STRING);
        }
        if (person.getAircraftBuilt() != null) {
            data.put(RosterConstants.AIRCRAFT_BUILT, person.getAircraftBuilt());
        } else {
            data.put(RosterConstants.AIRCRAFT_BUILT, RosterConstants.EMPTY_STRING);
        }
        if (person.isImcClub()) {
            data.put(RosterConstants.IMC, "on");
        }
        if (person.isVmcClub()) {
            data.put(RosterConstants.VMC, "on");
        }
        if (person.isYePilot()) {
            data.put(RosterConstants.YOUNG_EAGLE_PILOT, "on");
        }
        if (person.isYeVolunteer()) {
            data.put(RosterConstants.YOUNG_EAGLE_VOLUNTEER, "on");
        }
        if (person.isEaglePilot()) {
            data.put(RosterConstants.EAGLE_PILOT, "on");
        }
        if (person.isEagleVolunteer()) {
            data.put(RosterConstants.EAGLE_FLIGHT_VOLUNTEER, "on");
        }
        data.put(RosterConstants.ROW_COUNT, "50");
        return ofFormData(data);
    }

    /**
     * Of form data.
     *
     * @param data map
     * @return BodyPublisher
     */
    public static HttpRequest.BodyPublisher ofFormData(final Map<Object, Object> data) {
        final StringBuilder builder = new StringBuilder();
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            final String key = URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8);
            final String value = URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8);
            //LOGGER.info("[ofFormData] appending " + key + RosterConstants.EQUALS + value);
            builder.append(key);
            builder.append(RosterConstants.EQUALS);
            builder.append(value);
        }
        final String dataStr = builder.toString();
        log.info("[ofFormData] built " + dataStr);
        return HttpRequest.BodyPublishers.ofString(dataStr);
    }

    private String addDataFromHeaders(final Map<String, String> headers,
                                      final StringBuilder sb,
                                      final List<String> data) {
        for (final Map.Entry<String, String> entry : headers.entrySet()) {
            final String key = entry.getKey();
            if (data.contains(key)) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                if (RosterConstants.FIRST_NAME.equals(key)
                        || RosterConstants.LAST_NAME.equals(key)
                        || RosterConstants.EXPORT_BUTTON.equals(key)
                        || RosterConstants.STATUS.equals(key)
                        || RosterConstants.SEARCH_MEMBER_TYPE.equals(key)
                        || RosterConstants.CURRENT_STATUS.equals(key)
                        || RosterConstants.ROW_COUNT.equals(key)) {
                    sb.append(key.replaceAll("\\$", "%24"));
                } else {
                    sb.append(key);
                }
                sb.append("=");
                if (RosterConstants.VIEW_STATE.equals(key) || RosterConstants.EVENT_VALIDATION.equals(key)) {
                    sb.append(entry.getValue()
                            .replaceAll("/", "%2F")
                            .replaceAll("=", "%3D")
                            .replaceAll("\\+", "%2B"));
                } else {
                    sb.append(entry.getValue());
                }
            }
        }
        return sb.toString();
    }

    private String getViewStateValue(final Element viewState) {
        if (viewState != null) {
            return viewState.attr("value");
        }
        return "/wEPDwUKMTY1NDU2MTA1MmRkuOlmdf9IlE5Upbw3feS5bMlNeitv2Tys6h3WSL105GQ=";
    }

    private String getViewStateGeneratorValue(final Element viewStateGenerator) {
        if (viewStateGenerator != null) {
            return viewStateGenerator.attr("value");
        }
        return "55FE2EBC";
    }

    /**
     * Parses select values from Excel spreadsheet.
     *
     * @param data data
     * @return list of parsed values
     */
    public List<Member> parseRecords(final String data) {
        log.debug("Parsing records: {}", data);
        final List<Member> records = new ArrayList<>();
        final Document doc = Jsoup.parse(data);
        final Elements tableRecords = doc.getElementsByTag("tr");
        int rowCount = 0;
        for (Element tr : tableRecords) {
            if (rowCount > 0) {
                try {
                    final Elements columns = tr.getElementsByTag("td");
                    int columnCount = 0;
                    final Member person = new Member();
                    for (Element column : columns) {
                        processColumn(columnCount, person, column);
                        columnCount++;
                    }
                    records.add(person);
                } catch (Exception e) {
                    log.error("Error", e);
                }
            }
            rowCount++;
        }
        return records;
    }

    /**
     * Processes column value.
     *
     * @param columnCount Count
     * @param person Person
     * @param column Column
     * @throws ParseException when data is invalid
     */
    private void processColumn(final int columnCount, final Member person, final Element column) {
        if (column == null) {
            return;
        }
        final String columnText = column.text().trim();
        switch (columnCount) {
            case 0:
                person.setRosterId(Long.parseLong(columnText));
                break;
            case 1:
                person.setMemberType(MemberType.valueOf(columnText.replaceAll("-", "")));
                break;
            case 2:
                person.setNickname(columnText);
                break;
            case CommonConstants.THREE:
                person.setFirstName(columnText);
                break;
            case CommonConstants.FOUR:
                person.setLastName(columnText);
                break;
            case CommonConstants.FIVE:
                person.setSpouse(columnText);
                break;
            case CommonConstants.SIX:
                person.setGender(Gender.fromDisplayString(columnText.toUpperCase()));
                break;
            case CommonConstants.SEVEN:
                person.setEmail(columnText);
                break;
            case CommonConstants.EIGHT:
                // Ignore EmailPrivate
                break;
            case CommonConstants.NINE:
                person.setUsername(columnText);
                break;
            case CommonConstants.TEN:
                person.setBirthDate(columnText);
                break;
            case CommonConstants.ELEVEN:
                person.setAddressLine1(columnText);
                break;
            case CommonConstants.TWELVE:
                person.setAddressLine2(columnText);
                break;
            case CommonConstants.THIRTEEN:
                // Ignore AddressPrivate
                break;
            case CommonConstants.FOURTEEN:
                setHomePhone(person, column);
                break;
            case CommonConstants.FIFTEEN:
                // Ignore HomePhonePrivate
                break;
            case CommonConstants.SIXTEEN:
                setCellPhone(person, column);
                break;
            case CommonConstants.SEVENTEEN:
                // Ignore CellPhonePrivate
                break;
            case CommonConstants.EIGHTEEN:
                person.setEaaNumber(columnText);
                break;
            case CommonConstants.NINETEEN:
                person.setStatus(Status.valueOf(columnText.toUpperCase()));
                break;
            case CommonConstants.TWENTY:
                person.setJoined(columnText);
                break;
            case CommonConstants.TWENTY_ONE:
                setExpiration(person, columnText);
                break;
            case CommonConstants.TWENTY_TWO:
                handleOtherInfo(person, column);
                break;
            case CommonConstants.TWENTY_THREE:
                person.setCity(columnText);
                break;
            case CommonConstants.TWENTY_FOUR:
                person.setState(State.fromDisplayString(columnText));
                break;
            case CommonConstants.TWENTY_FIVE:
                person.setCountry(Country.fromDisplayString(columnText));
                break;
            case CommonConstants.TWENTY_SIX:
                person.setZipCode(columnText);
                break;
            case CommonConstants.TWENTY_SEVEN:
                person.setRatings(columnText);
                break;
            case CommonConstants.TWENTY_EIGHT:
                person.setAircraftOwned(columnText);
                break;
            case CommonConstants.TWENTY_NINE:
                person.setAircraftProject(columnText);
                break;
            case CommonConstants.THIRTY:
                person.setAircraftBuilt(columnText);
                break;
            case CommonConstants.THIRTY_ONE:
                setImcClub(person, column);
                break;
            case CommonConstants.THIRTY_TWO:
                setVmcClub(person, column);
                break;
            case CommonConstants.THIRTY_THREE:
                setYePilot(person, column);
                break;
            case CommonConstants.THIRTY_FOUR:
                setYeVolunteer(person, column);
                break;
            case CommonConstants.THIRTY_FIVE:
                setEaglePilot(person, column);
                break;
            case CommonConstants.THIRTY_SIX:
                setEagleVolunteer(person, column);
                break;
            case CommonConstants.THIRTY_SEVEN:
                // Ignore DateAdded
                break;
            case CommonConstants.THIRTY_EIGHT:
                // Ignore DateUpdated
                break;
            case CommonConstants.THIRTY_NINE:
                setEaaExpiration(person, columnText);
                break;
            case CommonConstants.FORTY:
                setYouthProtection(person, columnText);
                break;
            case CommonConstants.FORTY_ONE:
                setBackgroundCheck(person, columnText);
                break;
            case CommonConstants.FORTY_TWO:
                // Ignore UpdatedBy
                break;
            case CommonConstants.FORTY_THREE:
                person.setWebAdminAccess(WebAdminAccess.fromDisplayString(columnText));
                break;
            default:
                // Do nothing
        }
    }

    /**
     * Sets chapter membership expiration date.
     *
     * @param person Member
     * @param columnText column text
     */
    private void setExpiration(final Member person, final String columnText) {
        try {
            person.setExpiration(simpleDateFormat.parse(columnText));
            log.debug("Chapter membership expiration set to {} for {}", person.getExpiration(), person.getRosterId());
        } catch (ParseException pe) {
            log.debug("Unable to parse expiration.  columnText={}. {}", columnText, pe.getMessage());
        }
    }

    /**
     * Sets background check date.
     *
     * @param person Member
     * @param columnText column text
     */
    private void setBackgroundCheck(final Member person, final String columnText) {
        try {
            person.setBackgroundCheck(simpleDateFormat.parse(columnText));
            log.debug("Background check set to {} for {}", person.getExpiration(), person.getRosterId());
        } catch (ParseException pe) {
            log.debug("Unable to parse background check.  columnText={}. {}", columnText, pe.getMessage());
        }
    }

    /**
     * Sets youth protection date.
     *
     * @param person Member
     * @param columnText column text
     */
    private void setYouthProtection(final Member person, final String columnText) {
        try {
            person.setYouthProtection(simpleDateFormat.parse(columnText));
            log.debug("Youth protection set to {} for {}", person.getExpiration(), person.getRosterId());
        } catch (ParseException pe) {
            log.debug("Unable to parse youth protection.  columnText={}. {}", columnText, pe.getMessage());
        }
    }

    /**
     * Sets EAA national membership expiration date.
     *
     * @param person Member
     * @param columnText column text
     */
    private void setEaaExpiration(final Member person, final String columnText) {
        try {
            person.setEaaExpiration(simpleDateFormat.parse(columnText));
            log.debug("EAA national membership expiration set to {} for {}", person.getExpiration(),
                    person.getRosterId());
        } catch (ParseException pe) {
            log.debug("Unable to parse EAA expiration.  columnText={}. {}", columnText, pe.getMessage());
        }
    }

    /**
     * Set Eagle Volunteer field.
     *
     * @param person Person
     * @param column Column
     */
    private void setEagleVolunteer(final Member person, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            person.setEagleVolunteer(Boolean.TRUE);
        } else {
            person.setEagleVolunteer(Boolean.FALSE);
        }
    }

    /**
     * Set Eagle Pilot field.
     *
     * @param person Person
     * @param column Column
     */
    private void setEaglePilot(final Member person, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            person.setEaglePilot(Boolean.TRUE);
        } else {
            person.setEaglePilot(Boolean.FALSE);
        }
    }

    /**
     * Sets YE Volunteer field.
     *
     * @param person Person
     * @param column Column
     */
    private void setYeVolunteer(final Member person, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            person.setYeVolunteer(Boolean.TRUE);
        } else {
            person.setYeVolunteer(Boolean.FALSE);
        }
    }

    /**
     * Set YE Pilot field.
     *
     * @param person Person
     * @param column Column
     */
    private void setYePilot(final Member person, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            person.setYePilot(Boolean.TRUE);
        } else {
            person.setYePilot(Boolean.FALSE);
        }
    }

    /**
     * Sets VMC Club field.
     *
     * @param person Person
     * @param column Column
     */
    private void setVmcClub(final Member person, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            person.setVmcClub(Boolean.TRUE);
        } else {
            person.setVmcClub(Boolean.FALSE);
        }
    }

    /**
     * Sets IMC Club field.
     *
     * @param person Person
     * @param column Column
     */
    private void setImcClub(final Member person, final Element column) {
        if ("yes".equalsIgnoreCase(column.text().trim())) {
            person.setImcClub(Boolean.TRUE);
        } else {
            person.setImcClub(Boolean.FALSE);
        }
    }

    /**
     * Sets Cell Phone.
     *
     * @param person Person
     * @param column Column
     */
    private void setCellPhone(final Member person, final Element column) {
        person.setCellPhone(column
                .text()
                .trim()
                .replaceAll(" ", "")
                .replaceAll("-", "")
                .replaceAll("\\(", "")
                .replaceAll("\\)", ""));
    }

    /**
     * Sets home phone value.
     *
     * @param person Person
     * @param column Column
     */
    private void setHomePhone(final Member person, final Element column) {
        person.setHomePhone(column
                .text()
                .trim()
                .replaceAll(" ", "")
                .replaceAll("-", "")
                .replaceAll("\\(", "")
                .replaceAll("\\)", ""));
    }

    /**
     * Handles OtherInfo field.
     *
     * @param person Person
     * @param column Column
     */
    private void handleOtherInfo(final Member person, final Element column) {
        final OtherInfo otherInfo = new OtherInfo(column.text().trim());
        person.setRfid(otherInfo.getRfid());
        person.setSlack(otherInfo.getSlack());
        person.setOtherInfo(otherInfo.getRaw());
        person.setAdditionalInfo(otherInfo.getDescription());
        if (otherInfo.getFamily() != null) {
            person.setFamily(String.join(", ", otherInfo.getFamily()));
        }
        if (person.getSlack() == null || "NULL".equalsIgnoreCase(person.getSlack())) {
            setSlack(slackUsers, person);
        }
        if (otherInfo.getNumOfFamily() != null) {
            person.setNumOfFamily(otherInfo.getNumOfFamily());
        }
    }

    /**
     * Assigns slack username if not already assigned and a first/last name match is found.
     *
     * @param users list of all Slack users
     * @param person Member
     */
    private void setSlack(final List<String> users, final Member person) {
        final String username = person.getFirstName() + " " + person.getLastName();
        users.forEach(str -> {
            final String[] split = str.split("\\|");
            if (!"NULL".equalsIgnoreCase(split[1]) && str.contains(username)) {
                person.setSlack(split[1]);
            }
        });
    }

}
