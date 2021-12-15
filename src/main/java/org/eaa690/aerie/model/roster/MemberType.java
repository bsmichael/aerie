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
 * MemberType.
 */
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

    /**
     * Value.
     */
    private final String value;

    /**
     * Constructor.
     *
     * @param v
     */
    MemberType(final String v) {
        this.value = v;
    }

    /**
     * Get Value.
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     * Display string.
     *
     * @param memberType MemberType
     * @return display string
     */
    public static String toDisplayString(final MemberType memberType) {
        if (Regular.equals(memberType)) {
            return "Regular";
        } else if (Family.equals(memberType)) {
            return "Family";
        } else if (Lifetime.equals(memberType)) {
            return "Lifetime";
        } else if (Honorary.equals(memberType)) {
            return "Honorary";
        } else if (Student.equals(memberType)) {
            return "Student";
        } else if (Prospect.equals(memberType)) {
            return "Prospect";
        }
        return "Non-Member";
    }
}

