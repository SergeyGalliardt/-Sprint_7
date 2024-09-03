import org.example.*;
import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.junit.*;

import static org.hamcrest.core.Is.isA;
import static org.hamcrest.core.IsEqual.equalTo;

@DisplayName("Авторизация курьера. Ручка: POST /api/v1/courier/login")
public class CourierAuthorizationTests {

    CourierClient courierClient = new CourierClient();
    private Courier courier;
    private Response authorizationResponse;
    private int idCourier;
    private boolean skipDeleteCourier = false; // Флаг для пропуска удаления курьера

    @BeforeClass
    public static void startLogging() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    } // Включили логирование

    @Before
    public void createTestData() {
        courier = CourierGenerator.randomCourier(); // Создали ТД для курьера
    }

    @After
    public void cleanUp() {
        if (!skipDeleteCourier) {
            courierClient.deleteCourier(idCourier); // Удаление курьера после выполнения тестов
        }
    }

    @AfterClass
    public static void stopLogging() {
        RestAssured.reset();
    } // Выключили логирование

    @Test
    @DisplayName("Курьер может авторизоваться")
    @Description("Для авторизации, нужно передать все обязательные поля. Запрос возвращает правильный код ответа 200 и в теле - id: NNNN")
    public void courierCanLogInTest() {
        courierClient.createCourier(courier); // Создали курьера
        authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier)); // Авторизовались
        authorizationResponse
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("id", isA(int.class));
        // Получили id курьера
        idCourier = courierClient.getIdCourier(authorizationResponse);

    }


    @Test
    @DisplayName("Система вернёт ошибку, если неправильно указать пароль при авторизации")
    @Description("Запрос возвращает правильный код ответа 404 и в теле - message: Учётная запись не найдена")
    public void courierCannotLogInWithAnIncorrectPasswordTest() {
        courierClient.createCourier(courier); // Создали курьера
        authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier));
        idCourier = courierClient.getIdCourier(authorizationResponse); // Заранее авторизовались и сохранили id курьера для последующего удаления
        courier.setPassword("abracadabra"); // Изменили пароль на неправильный
        authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier)); // Попытка авторизации с некорректным паролем
        authorizationResponse.then()
                .assertThat()
                .statusCode(404)
                .and()
                .body("message", equalTo("Учетная запись не найдена"));

    }

    @Test
    @DisplayName("Система вернёт ошибку, если неправильно указать логин при авторизации")
    @Description("Запрос возвращает правильный код ответа 404 и в теле - message: Учётная запись не найдена")
    public void courierCannotLogInWithAnIncorrectLoginTest() {
        courierClient.createCourier(courier); // СОздали курьера
        authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier));
        idCourier = courierClient.getIdCourier(authorizationResponse); // Заранее авторизовались и сохранили id курьера для последующего удаления
        courier.setLogin("abracadabra"); // Изменили логин на неправильный
        authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier)); // Попытка авторизации с некорректным логином
        authorizationResponse.then()
                .assertThat()
                .statusCode(404)
                .and()
                .body("message", equalTo("Учетная запись не найдена"));
    }

    @Test
    @DisplayName("Если нет поля login при авторизации, запрос вернёт ошибку")
    @Description("Запрос возвращает правильный код ответа 400 и в теле - message: Недостаточно данных для входа")
    public void courierCannotLogInWithoutLoginTest() {
        courierClient.createCourier(courier); // СОздали курьера
        authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier));
        idCourier = courierClient.getIdCourier(authorizationResponse); // Заранее авторизовались и сохранили id курьера для последующего удаления
        courier.setLogin(null); // Не передаём поле login в запросе на авторизацию
        authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier)); // Попытка авторизации без поля login
        authorizationResponse.then()
                .assertThat()
                .statusCode(400)
                .and()
                .body("message", equalTo("Недостаточно данных для входа"));

    }

    @Test
    @DisplayName("Если нет поля password при авторизации, запрос вернёт ошибку")
    @Description("Запрос возвращает правильный код ответа 400 и в теле - message: Недостаточно данных для входа")
    @Issue("Фактический результат: ответ от сервера 504 Gateway time out, Service unavailable")
    public void courierCannotLogInWithoutPasswordTest() {
        courierClient.createCourier(courier); // Создали курьера
        authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier));
        idCourier = courierClient.getIdCourier(authorizationResponse); // Заранее авторизовались и сохранили id курьера для последующего удаления
        courier.setPassword(null); // Не передаём поле password в запросе на авторизацию
        authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier)); // Попытка авторизации без поля password
        authorizationResponse.then()
                .assertThat()
                .statusCode(400)
                .and()
                .body("message", equalTo("Недостаточно данных для входа"));


    }

    @Test
    @DisplayName("Если авторизоваться под несуществующим пользователем, запрос вернёт ошибку")
    @Description("Запрос возвращает правильный код ответа 404 и в теле - message: Учётная запись не найдена")
    public void cannotLogInNonExistentCourierTest() {
        // Не создаём курьера
        skipDeleteCourier = true; // Пропустили удаление курьера
        authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier)); // Попытка авторизации несуществующим курьером
        authorizationResponse.then()
                .assertThat()
                .statusCode(404)
                .and()
                .body("message", equalTo("Учетная запись не найдена"));
    }

}
