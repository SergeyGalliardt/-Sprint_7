package org.example;

import io.qameta.allure.Step;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class OrderGenerator {
    private static final String[] COLORS = {"BLACK", "GREY"};
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    private static String generateFutureDate() {
        LocalDate currentDate = LocalDate.now();
        LocalDate futureDate = currentDate.plusDays(RandomUtils.nextInt(1, 30)); // Дата в интервале от 1 до 30 дней
        return futureDate.format(DATE_FORMATTER);
    }

    // Генерация случайного цвета самоката или обоих цветов
    private static String[] generateRandomColors() {
        if (Math.random() < 0.5) {
            return new String[]{COLORS[(int) (Math.random() * COLORS.length)]};
        } else {
            return COLORS;
        }
    }

    public static Order randomOrder() {
        final String firstName = RandomStringUtils.random(10, 1040, 1103, true, false);
        final String lastName = RandomStringUtils.random(10, 1040, 1103, true, false);
        final String address = RandomStringUtils.random(10, 1040, 1103, true, false);
        final String metroStation = RandomStringUtils.randomNumeric(2);
        final String phone = "+7" + RandomStringUtils.randomNumeric(10);
        final int rentTime = RandomUtils.nextInt(1, 8);
        final String deliveryDate = generateFutureDate();
        final String comment = RandomStringUtils.randomAlphabetic(20);

        return new Order(firstName, lastName, address, metroStation, phone, rentTime, deliveryDate, comment, generateRandomColors());
    }

}
