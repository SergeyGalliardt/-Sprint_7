import org.example.*;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;

@DisplayName("Получить заказ по его трек номеру. Ручка: GET /api/v1/orders/track")
public class GetOrderByTrackNumberTests {
    OrdersClient ordersClient = new OrdersClient();

    @BeforeClass
    public static void startLogging(){
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter()); // Включили логирование
    }

    @AfterClass
    public static void stopLogging(){
        RestAssured.reset();
    } // Выключили логирование

    @Test
    @DisplayName("Получить заказ по его номеру")
    @Description("Запрос возвращает правильный код ответа 200 и в теле объект order с информацией о заказе")
    public void getOrderByTrackNumberTest(){
        Order order = OrderGenerator.randomOrder(); // ТД для заказа
        Response createOrderResponse = ordersClient.createOrder(order); // Создали заказ
        int trackNumber = ordersClient.getTrackNumber(createOrderResponse); // Получили трек номер
        // Отправили запрос на получение заказа
        ordersClient.getOrderByTrackNumber(trackNumber)
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("order.track", equalTo(trackNumber)); // Проверяем, что получили нужный объект с соответствующим трек номером
        ordersClient.cancelOrder(trackNumber); // Удалили заказ
    }
    @Test
    @DisplayName("Запрос без трек номера возвращает ошибку")
    @Description("Запрос возвращает правильный код ответа 400 и в теле - message: Недостаточно данных для поиска")
    public void getOrderWithoutTrackNumberTest(){
        // Отправили запрос на получение заказа без трек номера
        ordersClient.getOrderWithoutTrackNumber()
                .then()
                .assertThat()
                .statusCode(400)
                .and()
                .body("message",equalTo("Недостаточно данных для поиска"));
    }
    @Test
    @DisplayName("Запрос с несуществующим заказом возвращает ошибку")
    @Description("Запрос возвращает правильный код ответа 404 и в теле - message: Заказ не найден")
    public void getOrderIncorrectTrackNumberTest(){
        int incorrectTrackNumber = 12345678; // Несуществующий трек номер заказа
        // Отправили запрос с некорректным трек номером
        ordersClient.getOrderByTrackNumber(incorrectTrackNumber)
                .then()
                .assertThat()
                .statusCode(404)
                .and()
                .body("message",equalTo("Заказ не найден"));
    }
}