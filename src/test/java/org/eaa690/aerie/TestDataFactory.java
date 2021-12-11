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

package org.eaa690.aerie;

import com.github.javafaker.Faker;
import org.eaa690.aerie.constant.CommonConstants;
import org.eaa690.aerie.model.Member;
import org.eaa690.aerie.model.Property;
import org.eaa690.aerie.model.WeatherProduct;
import org.eaa690.aerie.model.roster.Country;
import org.eaa690.aerie.model.roster.Gender;
import org.eaa690.aerie.model.roster.MemberType;
import org.eaa690.aerie.model.roster.State;
import org.eaa690.aerie.model.roster.Status;
import org.eaa690.aerie.model.roster.WebAdminAccess;

/**
 * TestDataFactory.
 */
public class TestDataFactory {

    /**
     * Test data faker.
     */
    private static final Faker faker = new Faker();

    /**
     * Initializes an instance of <code>TestDataFactory</code> with the default data.
     */
    private TestDataFactory() {
        // private constructor
    }

    /**
     * Builds a Property for testing.
     *
     * @param key Key
     * @param value Value
     * @return Property
     */
    public static Property getProperty(final String key, final String value) {
        final Property property = new Property();
        property.setId(1L);
        property.setKey(key);
        property.setValue(value);
        return property;
    }

    /**
     * Builds a WeatherProduct for testing.
     *
     * @return WeatherProduct
     */
    public static WeatherProduct getWeatherProduct() {
        final WeatherProduct weatherProduct = new WeatherProduct();
        weatherProduct.setId(1L);
        weatherProduct.setKey(CommonConstants.ID);
        weatherProduct.setValue(CommonConstants.ID);
        return weatherProduct;
    }

    /**
     * Builds a Member for testing.
     *
     * @return Member
     */
    public static Member getMember() {
        final Member member = new Member();
        member.setMemberType(MemberType.Regular);
        member.setFirstName(faker.name().firstName());
        member.setLastName(faker.name().lastName());
        member.setAddressLine1(faker.address().streetAddress());
        member.setCity(faker.address().city());
        member.setState(State.GEORGIA);
        member.setCountry(Country.USA);
        member.setEaaNumber(faker.numerify("#######"));
        member.setGender(Gender.MALE);
        member.setStatus(Status.ACTIVE);
        member.setWebAdminAccess(WebAdminAccess.NO_ACCESS);
        return member;
    }


}