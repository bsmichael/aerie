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

import org.junit.Assert;
import org.junit.Test;

public class GenderTest {

    @Test
    public void testGetDisplayString_Male() {
        Assert.assertEquals("Male", Gender.getDisplayString(Gender.MALE));
    }

    @Test
    public void testGetDisplayString_Female() {
        Assert.assertEquals("Female", Gender.getDisplayString(Gender.FEMALE));
    }

    @Test
    public void testGetDisplayString_Unknown() {
        Assert.assertEquals("Unknown", Gender.getDisplayString(Gender.UNKNOWN));
    }

}
