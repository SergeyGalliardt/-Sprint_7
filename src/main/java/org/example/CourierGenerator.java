package org.example;

import io.qameta.allure.Step;
import org.apache.commons.lang3.RandomStringUtils;

public class CourierGenerator {
    public static Courier randomCourier() {
        final String login = RandomStringUtils.randomAlphabetic(10); // Рандомная латиница
        final String password = RandomStringUtils.randomNumeric(10); // Рандомные цифры
        final String firstName = RandomStringUtils.random(10, 1040, 1103, true, false); // Рандомная кириллица
        return new  Courier(login, password, firstName);
    }
}
