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

public enum State {
    /**
     * Alabama.
     */
    ALABAMA,
    /**
     * Georgia.
     */
    GEORGIA,
    /**
     * Florida.
     */
    FLORIDA,
    /**
     * North Carolina.
     */
    NORTH_CAROLINA,
    /**
     * South Carolina.
     */
    SOUTH_CAROLINA,
    /**
     * Tennessee.
     */
    TENNESSEE;

    /**
     * Translates state string to enum.
     *
     * @param state String
     * @return enum
     */
    public static State deriveState(final String state) {
        if ("AL".equalsIgnoreCase(state)) {
            return State.ALABAMA;
        } else if ("FL".equalsIgnoreCase(state)) {
            return State.FLORIDA;
        } else if ("NC".equalsIgnoreCase(state)) {
            return State.NORTH_CAROLINA;
        } else if ("SC".equalsIgnoreCase(state)) {
            return State.SOUTH_CAROLINA;
        } else if ("TN".equalsIgnoreCase(state)) {
            return State.TENNESSEE;
        }
        return State.GEORGIA;
    }

    /**
     * Gets displayable string value.
     *
     * @param state State
     * @return displayable value
     */
    public static String getDisplayString(final State state) {
        if (ALABAMA.equals(state)) {
            return "AL";
        } else if (FLORIDA.equals(state)) {
            return "FL";
        } else if (NORTH_CAROLINA.equals(state)) {
            return "NC";
        } else if (SOUTH_CAROLINA.equals(state)) {
            return "SC";
        } else if (TENNESSEE.equals(state)) {
            return "TN";
        }
        return "GA";
    }

    /**
     * Gets State from displayable string value.
     *
     * @param displayString displayable string
     * @return State
     */
    public static State fromDisplayString(final String displayString) {
        if ("AL".equalsIgnoreCase(displayString)) {
            return ALABAMA;
        } else if ("FL".equalsIgnoreCase(displayString)) {
            return FLORIDA;
        } else if ("NC".equalsIgnoreCase(displayString)) {
            return NORTH_CAROLINA;
        } else if ("SC".equalsIgnoreCase(displayString)) {
            return SOUTH_CAROLINA;
        } else if ("TN".equalsIgnoreCase(displayString)) {
            return TENNESSEE;
        }
        return GEORGIA;
    }
}
