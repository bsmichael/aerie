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

/**
 * MembershipReport.
 */
@Getter
@Setter
public class MembershipReport {

    /**
     * Sum of all regular membership types.
     */
    private Long regularMemberCount = 0L;

    /**
     * Sum of all expired regular membership types.
     */
    private Long regularMemberExpiredCount = 0L;

    /**
     * Sum of all regular membership types that will expire in 30 days.
     */
    private Long regularMemberWillExpire30DaysCount = 0L;

    /**
     * Sum of "num of family" for all family membership types.
     */
    private Long familyMemberCount = 0L;

    /**
     * Sum of all expired family members.
     */
    private Long familyMemberExpiredCount = 0L;

    /**
     * Sum of all family members that will expire in 30 days.
     */
    private Long familyMemberWillExpire30DaysCount = 0L;

    /**
     * Sum of all family membership types.
     */
    private Long familyMembershipCount = 0L;

    /**
     * Sum of all expired family membership types.
     */
    private Long familyMembershipExpiredCount = 0L;

    /**
     * Sum of all family membership types that will expire in 30 days.
     */
    private Long familyMembershipWillExpire30DaysCount = 0L;

    /**
     * Sum of all lifetime membership types.
     */
    private Long lifetimeMemberCount = 0L;

    /**
     * Sum of all honorary membership types.
     */
    private Long honoraryMemberCount = 0L;

    /**
     * Sum of all student membership types.
     */
    private Long studentMemberCount = 0L;

    /**
     * Sum of all expired student membership types.
     */
    private Long studentMemberExpiredCount = 0L;

    /**
     * Sum of all student membership types that will expire in 30 days.
     */
    private Long studentMemberWillExpire30DaysCount = 0L;

    /**
     * Sum of all prospect membership types.
     */
    private Long prospectMemberCount = 0L;

    /**
     * Sum of all non-member membership types.
     */
    private Long nonMemberCount = 0L;

}
