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

package org.eaa690.aerie.service;

import lombok.extern.slf4j.Slf4j;
import org.eaa690.aerie.model.GateCode;
import org.eaa690.aerie.model.GateCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Gate Code Service.
 */
@Slf4j
public class GateCodeService {

    /**
     * GateCodeRepository.
     */
    @Autowired
    private GateCodeRepository gateCodeRepository;

    /**
     * Sets GateCodeRepository.
     * Note: mostly used for unit test mocks
     *
     * @param value GateCodeRepository
     */
    @Autowired
    public void setGateCodeRepository(final GateCodeRepository value) {
        gateCodeRepository = value;
    }

    /**
     * Save a gate code.
     *
     * @param gateCode GateCode
     */
    public void setGateCode(final GateCode gateCode) {
        // check if gate code is already set
        gateCodeRepository.save(gateCode);
    }

    /**
     * Gets all gate codes.
     *
     * @return list of gate codes
     */
    public List<GateCode> getAll() {
        return gateCodeRepository.findAll().orElse(new ArrayList<>());
    }

    /**
     * Gets the current gate code.
     *
     * @return GateCode
     */
    public GateCode getCurrentGateCode() {
        final List<GateCode> codes = new ArrayList<>();
        gateCodeRepository
                .findAll()
                .ifPresent(gateCodes -> gateCodes
                        .stream()
                        .filter(gc -> gc.getDate().before(new Date()))
                        .forEach(codes::add));
        if (!codes.isEmpty()) {
            return codes.get(0);
        }
        return null;
    }

    /**
     * Gets the upcoming gate code, if available.
     *
     * @return GateCode
     */
    public GateCode getUpcomingGateCode() {
        final List<GateCode> codes = new ArrayList<>();
        gateCodeRepository
                .findAll()
                .ifPresent(gateCodes -> gateCodes
                        .stream()
                        .filter(gc -> gc.getDate().before(new Date()))
                        .forEach(codes::add));
        if (!codes.isEmpty()) {
            return codes.get(0);
        }
        return null;
    }
}
