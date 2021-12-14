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

package org.eaa690.aerie.model;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eaa690.aerie.config.CommonConstants;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JotForm API - Java Client.
 */
@Getter
@Setter
public class JotForm {

    /**
     * Base URL.
     */
    private static String baseUrl = "https://api.jotform.com/";

    /**
     * API Version.
     */
    private static String version = "v1";

    /**
     * API Key.
     */
    private String apiKey;

    /**
     * Debug mode.
     */
    private boolean debugMode;

    /**
     * Default Constructor.
     */
    public JotForm() {
        this.apiKey = null;
        this.debugMode = false;
    }

    /**
     * Contructor.
     *
     * @param key API Key
     */
    public JotForm(final String key) {
        this(key, false);
    }

    /**
     * Constructor.
     *
     * @param key API Key
     * @param mode debug mode
     */
    public JotForm(final String key, final boolean mode) {
        this.apiKey = key;
        this.debugMode = mode;
    }

    /**
     * Get a list of submissions for this account.
     *
     * @param offset Start of each result set for form list.
     * @param limit Number of results in each result set for form list.
     * @param filter Filters the query results to fetch a specific form range.
     * @param orderBy Order results by a form field name.
     * @return Returns basic details such as title of the form, when it was created,
     * number of new and total submissions.
     */
    public JSONObject getSubmissions(final String offset,
                                     final String limit,
                                     final HashMap<String, String> filter,
                                     final String orderBy) {
        HashMap<String, String> params = createConditions(offset, limit, filter, orderBy);

        return executeGetRequest("/user/submissions", params);
    }

    private void log(final String message) {
        if (this.debugMode) {
            System.out.println(message);
        }
    }

    private JSONObject executeHttpRequest(final String path,
                                          final HashMap<String, String> params,
                                          final String method)
            throws UnsupportedEncodingException {
        HttpClient client = HttpClientBuilder.create().build();

        HttpUriRequest req;
        HttpResponse resp;

        if (method.equals("GET")) {
            req = new HttpGet(JotForm.baseUrl + JotForm.version + path);
            req.addHeader("apiKey", this.apiKey);

            if (params != null) {
                URI uri = null;
                URIBuilder ub = new URIBuilder(req.getURI());

                for (final Map.Entry<String, String> entry : params.entrySet()) {
                    try {
                        uri = ub.addParameter(entry.getKey(), entry.getValue()).build();
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                }
                ((HttpRequestBase) req).setURI(uri);
            }
        } else if (method.equals("POST")) {
            req = new HttpPost(JotForm.baseUrl + JotForm.version + path);
            req.addHeader("apiKey", this.apiKey);

            if (params != null) {
                List<NameValuePair> parameters = new ArrayList<>(params.size());

                for (final Map.Entry<String, String> entry : params.entrySet()) {
                    parameters.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }

                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters, "UTF-8");
                ((HttpPost) req).setEntity(entity);
            }
        } else if (method.equals("DELETE")) {
            req = new HttpDelete(JotForm.baseUrl + JotForm.version + path);
            req.addHeader("apiKey", this.apiKey);
        } else {
            req = null;
        }

        try {
            resp = client.execute(req);

            int statusCode = resp.getStatusLine().getStatusCode();

            if (statusCode != HttpStatus.SC_OK) {
                this.log(resp.getStatusLine().getReasonPhrase());
            }
            return new JSONObject(readInput(resp.getEntity().getContent()));
        } catch (IOException | IllegalStateException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String readInput(final InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] bytes = new byte[CommonConstants.ONE_THOUSAND_TWENTY_FOUR];

        int n = in.read(bytes);

        while (n != -1) {
            out.write(bytes, 0, n);
            n = in.read(bytes);
        }
        return new String(out.toString());
    }

    private JSONObject executeGetRequest(final String path, final HashMap<String, String> params) {
        try {
            return executeHttpRequest(path, params, "GET");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private HashMap<String, String> createConditions(final String offset,
                                                     final String limit,
                                                     final HashMap<String, String> filter,
                                                     final String orderBy) {
        HashMap<String, String> params = new HashMap<>();

        HashMap<String, String> args = new HashMap<>();
        args.put("offset", offset);
        args.put("limit", limit);
        args.put("orderby", orderBy);

        for (final Map.Entry<String, String> entry : args.entrySet()) {
            final String key = entry.getKey();
            if (StringUtils.isNotBlank(args.get(key))) {
                params.put(key, entry.getValue());
            }
        }

        if (filter != null) {
            JSONObject filterObject = new JSONObject((Map) filter);
            params.put("filter", filterObject.toString());
        }

        return params;
    }

}
