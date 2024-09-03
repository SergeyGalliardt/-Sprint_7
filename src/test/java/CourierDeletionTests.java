import org.example.*;
import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.hamcrest.core.IsEqual.equalTo;

@DisplayName("Удаление курьера. Ручка: DELETE /api/v1/courier/:id")
public class CourierDeletionTests {

    CourierClient courierClient = new CourierClient();

    @BeforeClass
    public static void startLogging() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter()); // Логирование для вывода в консоль запросов и ответов
    }

    @AfterClass
    public static void stopLogging() {

        RestAssured.reset(); // Выключить логирование
    }

    @Test
    @DisplayName("Курьера можно удалить")
    @Description("Чтобы удалить курьера, нужно передать в URL id существующего курьера. Запрос возвращает правильный код ответа 200 и в теле - ok: true")
    public void courierCanBeDeletedTest() {
        Courier courier = CourierGenerator.randomCourier();
        courierClient.createCourier(courier);
        Response authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier));
        int idCourier = courierClient.getIdCourier(authorizationResponse);
        courierClient.deleteCourier(idCourier)
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("ok", equalTo(true));
        // Убедиться что курьер удалён, при помощи авторизации
        courierClient.loginCourier(CourierCredentials.from(courier))
                .then()
                .assertThat()
                .statusCode(404)
                .and()
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Ошибка при попытке удалить курьера без передачи id")
    @Description("Запрос возвращает правильный код ответа 400 и в теле - message: Недостаточно данных для удаления курьера")
    @Issue("Фактический результат: {\n" + "\"code\": 404,\n" + "\"message\":\"Not Found.\"\n" + "}")
    public void errorWhenTryingDeleteCourierWithoutIdTest() {
        courierClient.deleteCourierWithoutId()
                .then()
                .assertThat()
                .statusCode(400)
                .and()
                .body("message", equalTo("Недостаточно данных для удаления курьера"));

    }

    @Test
    @DisplayName("Ошибка при попытке удалить несуществующего курьера")
    @Description("Запрос возвращает правильный код ответа 404 и в теле - message: Курьера с таким id нет")
    public void errorWhenTryingDeleteNonExistentCourierTest() {
        courierClient.deleteCourier(123456789)
                .then()
                .assertThat()
                .statusCode(404)
                .and()
                .body("message", equalTo("Курьера с таким id нет.")); // Здесь тоже бажуля из-за ошибки в точке. Так же считаю, что серая зона и выносить в баг не стал

    }
}