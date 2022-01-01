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

public class MemberTypeTest {

    @Test
    public void testToDisplayString_Regular() {
        Assert.assertEquals("Regular", MemberType.toDisplayString(MemberType.Regular));
    }

    @Test
    public void testToDisplayString_Family() {
        Assert.assertEquals("Family", MemberType.toDisplayString(MemberType.Family));
    }

    @Test
    public void testToDisplayString_Lifetime() {
        Assert.assertEquals("Lifetime", MemberType.toDisplayString(MemberType.Lifetime));
    }

    @Test
    public void testToDisplayString_Honorary() {
        Assert.assertEquals("Honorary", MemberType.toDisplayString(MemberType.Honorary));
    }

    @Test
    public void testToDisplayString_Student() {
        Assert.assertEquals("Student", MemberType.toDisplayString(MemberType.Student));
    }

    @Test
    public void testToDisplayString_Prospect() {
        Assert.assertEquals("Prospect", MemberType.toDisplayString(MemberType.Prospect));
    }

    @Test
    public void testToDisplayString_NonMember() {
        Assert.assertEquals("Non-Member", MemberType.toDisplayString(MemberType.NonMember));
    }

    @Test
    public void testGetValue() {
        Assert.assertEquals("Non-Member", MemberType.NonMember.getValue());
    }

}
