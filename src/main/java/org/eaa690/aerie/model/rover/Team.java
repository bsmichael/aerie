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

package org.eaa690.aerie.model.rover;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eaa690.aerie.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ROVER_TEAM")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Team extends BaseEntity implements Comparable<Team> {

    /**
     * Team name.
     */
    private String name;

    /**
     * Rover IP Address.
     */
    private String roverIPAddress;

    /**
     * Compares GateCode records.
     *
     * @param o other GateCode
     * @return comparison
     */
    @Override
    public int compareTo(final Team o) {
        if (o == null) {
            return -1;
        } else {
            return name.compareTo(o.getName());
        }
    }
}
