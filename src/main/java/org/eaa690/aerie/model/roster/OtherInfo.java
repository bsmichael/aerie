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

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * OtherInfo.
 */
@Getter
@Setter
@NoArgsConstructor
public class OtherInfo {

    /**
     * Raw string.
     */
    private String raw;

    /**
     * RFID.
     */
    private String rfid;

    /**
     * Slack.
     */
    private String slack;

    /**
     * Description.
     */
    private String description;

    /**
     * Num of Family.
     */
    private Long numOfFamily;

    /**
     * Family.
     */
    private List<String> family = new ArrayList<>();

    /**
     * Constructor.
     *
     * @param otherInfo raw string
     */
    public OtherInfo(final String otherInfo) {
        final OtherInfoBuilder builder = new OtherInfoBuilder();
        builder.setRaw(otherInfo);
        slack = builder.getSlack();
        rfid = builder.getRfid();
        description = builder.getAdditionalInfo();
        numOfFamily = builder.getNumOfFamily();
        if (builder.getAdditionalFamily() != null) {
            family = Arrays.asList(builder.getAdditionalFamily().split(","));
        }
        raw = builder.getRaw();
    }

    /**
     * Formatted string for storage purposes.
     *
     * @return formatted string
     */
    public String toString() {
        final List<String> elements = new ArrayList<>();
        elements.add(String.format("RFID=[%s]", rfid));
        elements.add(String.format("Slack=[%s]", slack));
        elements.add(String.format("Family=[%s]", family.stream().collect(Collectors.joining(", "))));
        elements.add(String.format("Description=[%s]", description));
        return elements.stream().collect(Collectors.joining("; "));
    }
}
