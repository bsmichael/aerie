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

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

/**
 * MemberRepository.
 */
public interface MemberRepository extends Repository<Member, Long> {

    /**
     * Gets a member.
     *
     * @param rfid RFID
     * @return Member
     */
    Optional<Member> findByRfid(String rfid);

    /**
     * Gets a member.
     *
     * @param id ID
     * @return Member
     */
    Optional<Member> findById(Long id);

    /**
     * Gets a member.
     *
     * @param rosterId RosterID
     * @return Member
     */
    Optional<Member> findByRosterId(Long rosterId);

    /**
     * Gets members by first name.
     *
     * @param firstName First name
     * @return all members matching provided value
     */
    Optional<List<Member>> findByFirstName(String firstName);

    /**
     * Gets members by last name.
     *
     * @param lastName Last name
     * @return all members matching provided value
     */
    Optional<List<Member>> findByLastName(String lastName);

    /**
     * Gets all members.
     *
     * @return all members
     */
    Optional<List<Member>> findAll();

    /**
     * Saves a member.
     *
     * @param member Member
     * @return Member
     */
    Member save(Member member);

}
