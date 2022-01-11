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

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * GateCode.
 */
@Entity
@Table(name = "GATE_CODE")
@Getter
@Setter
public class GateCode extends BaseEntity {

    /**
     * Date formatter.
     */
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("EEE. MMMMM dd, yyyy");

    /**
     * Date gate code is active.
     */
    private Date date;

    /**
     * Code at 770 Airport Rd.
     */
    private String codeAt770AirportRd;

    /**
     * Code at 690 Airport Rd.
     */
    private String codeAt690AirportRd;

    /**
     * Gets gate code as displayed text.
     *
     * @return display text
     */
    @JsonIgnore
    public String getDisplayText() {
        return String.format("Beginning %s, the gate code at 690 Airport Rd is %s and the code at 770 Airport Rd is %s",
                SIMPLE_DATE_FORMAT.format(date), codeAt690AirportRd, codeAt770AirportRd);
    }
}
