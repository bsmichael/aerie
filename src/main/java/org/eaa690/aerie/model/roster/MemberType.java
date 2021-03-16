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

public enum MemberType {

    /**
     * Regular.
     */
    Regular("Regular"),
    /**
     * Family.
     */
    Family("Family"),
    /**
     * Lifetime.
     */
    Lifetime("Lifetime"),
    /**
     * Honorary.
     */
    Honorary("Honorary"),
    /**
     * Student.
     */
    Student("Student"),
    /**
     * Prospect.
     */
    Prospect("Prospect"),
    /**
     * Non-member.
     */
    NonMember("Non-Member");

    private final String value;

    private MemberType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MemberType fromString(String text) {
        for (MemberType b : MemberType.values()) {
            if (b.value.equalsIgnoreCase(text)) {
                return b;
            }
        }
        return null;
    }
}