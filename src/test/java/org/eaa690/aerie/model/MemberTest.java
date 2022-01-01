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

import org.junit.Assert;
import org.junit.Test;

public class MemberTest {

    @Test
    public void testCompareToSame() {
        final Member m1 = new Member();
        m1.setId(1L);
        final Member m2 = new Member();
        m2.setId(1L);
        Assert.assertEquals(0, m1.compareTo(m2));
    }

    @Test
    public void testCompareToDifferent() {
        final Member m1 = new Member();
        m1.setId(1L);
        final Member m2 = new Member();
        m2.setId(2L);
        Assert.assertEquals(-1, m1.compareTo(m2));
    }

    @Test
    public void testHashCode() {
        final Member m1 = new Member();
        m1.setId(1L);
        m1.setRosterId(1L);
        Assert.assertNotNull(m1.hashCode());
    }

    @Test
    public void testEquals() {
        final Member m1 = new Member();
        m1.setId(1L);
        Assert.assertNotNull(m1.equals(null));
    }
}
