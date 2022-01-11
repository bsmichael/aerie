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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.eaa690.aerie.config.CommonConstants;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * GateCode.
 */
@Entity
@Table(name = "GATE_CODE")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GateCode extends BaseEntity implements Comparable<GateCode> {

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
                date.toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .format(DateTimeFormatter.ofPattern("EEE. MMM dd, yyyy")),
                codeAt690AirportRd, codeAt770AirportRd);
    }

    /**
     * Compares GateCode records.
     *
     * @param o other GateCode
     * @return comparison
     */
    @Override
    public int compareTo(final GateCode o) {
        if (o == null || date.before(o.getDate())) {
            return -1;
        } else if (date.after(o.getDate())) {
            return 1;
        }
        return 0;
    }

    /**
     * Required implementation.
     *
     * @param o other Object
     * @return comparison
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final GateCode gateCode = (GateCode) o;
        if (compareTo(gateCode) == 0) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * Required implementation.
     *
     * @return hashCode
     */
    @Override
    public int hashCode() {
        int result = 0;
        if (getDate() != null) {
            result = getDate().hashCode();
        }
        int at770AirportRdResult = 0;
        if (getCodeAt770AirportRd() != null) {
            at770AirportRdResult = getCodeAt770AirportRd().hashCode();
        }
        result = CommonConstants.THIRTY_ONE * result + at770AirportRdResult;
        int at690AirportRdResult = 0;
        if (getCodeAt690AirportRd() != null) {
            at690AirportRdResult = getCodeAt690AirportRd().hashCode();
        }
        result = CommonConstants.THIRTY_ONE * result + at690AirportRdResult;
        return result;
    }
}
