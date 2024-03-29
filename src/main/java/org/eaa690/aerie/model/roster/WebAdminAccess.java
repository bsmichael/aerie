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

package org.eaa690.aerie.model.roster;

/**
 * WebAdminAccess.
 */
public enum WebAdminAccess {
    /**
     * Chapter Admin.
     */
    CHAPTER_ADMIN,
    /**
     * Chapter Read Only.
     */
    CHAPTER_READONLY,
    /**
     * No Access.
     */
    NO_ACCESS;

    /**
     * Display string.
     *
     * @param admin WebAdminAccess
     * @return display string
     */
    public static String getDisplayString(final WebAdminAccess admin) {
        if (CHAPTER_ADMIN.equals(admin)) {
            return "2";
        } else if (CHAPTER_READONLY.equals(admin)) {
            return "3";
        }
        return "4";
    }

    /**
     * WebAdminAccess from display string.
     *
     * @param displayString display string
     * @return WebAdminAccess
     */
    public static WebAdminAccess fromDisplayString(final String displayString) {
        if ("Chapter Admin".equalsIgnoreCase(displayString)) {
            return CHAPTER_ADMIN;
        } else if ("Chapter Read Only".equalsIgnoreCase(displayString)) {
            return CHAPTER_READONLY;
        }
        return NO_ACCESS;
    }
}
