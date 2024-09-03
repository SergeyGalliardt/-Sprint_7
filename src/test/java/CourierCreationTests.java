import org.example.*;
import io.qameta.allure.Description;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.junit.*;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.isA;

@DisplayName("Создание курьера. Ручка: POST /api/v1/courier")
public class CourierCreationTests {
    private Courier courier;
    CourierClient courierClient = new CourierClient();
    private int idCourier;
    private boolean skipDeleteCourier = false; // Флаг для пропуска удаления курьера

    @BeforeClass
    public static void setUp() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    } // Включили логирование

    @Before
    public void createTestData() {
        courier = CourierGenerator.randomCourier(); // ТД для создания курьера
    }

    @After
    public void cleanUp() {
        if (!skipDeleteCourier) {
            courierClient.deleteCourier(idCourier);
        }  // Удалить курьера
    }

    @AfterClass
    public static void stopLogging() {
        RestAssured.reset();
    } // Выключили логирование


    @Test
    @DisplayName("Курьера можно создать")
    @Description("Чтобы создать курьера, нужно передать в ручку все обязательные поля. Запрос возвращает правильный код ответа 201 и в теле - ok: true")
    public void courierCanBeCreatedTest() {
        // Создали курьера
        courierClient.createCourier(courier)
                .then()
                .assertThat()
                .body("ok", equalTo(true))
                .and()
                .statusCode(201);
        // Убедиться что курьер создан, при помощи авторизации
        Response authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier));
        authorizationResponse
                .then()
                .assertThat()
                .body("id", isA(int.class)) // Проверили что после авторизации получили id курьера
                .and()
                .statusCode(200);
        // Получение id созданного курьера для последующего удаления
        idCourier = courierClient.getIdCourier(authorizationResponse);
    }

    @Test
    @DisplayName("Нельзя создать двух одинаковых курьеров")
    @Description("Запрос возвращает правильный код ответа 409 и в теле - message: Этот логин уже используется. Попробуйте другой")
    public void identicalCouriersCannotBeCreatedTest() {
        courierClient.createCourier(courier); // Создали курьера
        Response authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier));
        idCourier = courierClient.getIdCourier(authorizationResponse); // Заранее авторизовался и сохранил id курьера для последующего удаления
        courierClient.createCourier(courier)
                .then()
                .assertThat()
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой.")) // Текст ошибки серая зона, в баг выносить не стал, ибо апидок дышит ошибками :D
                .and()
                .statusCode(409);
    }

    @Test
    @DisplayName("Нельзя создать курьера с логином, который уже есть")
    @Description("Запрос возвращает правильный код ответа 409 и в теле - message: Этот логин уже используется. Попробуйте другой")
    public void sameLoginCouriersCannotBeCreatedTest() {
        courierClient.createCourier(courier); // Создали курьера
        Response authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier));
        idCourier = courierClient.getIdCourier(authorizationResponse); // Заранее авторизовался и сохранил id курьера для последующего удаления
        courier.setFirstName("fxs"); // Меняю имя и пароль для создания другого курьера с таким же логином
        courier.setPassword("1234567890");
        // Запрос на создание курьера с таким же логином
        courierClient.createCourier(courier)
                .then()
                .assertThat()
                .body("message", equalTo("Этот логин уже используется. Попробуйте другой."))
                .and()
                .statusCode(409);
    }

    @Test
    @DisplayName("Нельзя создать курьера, без передачи поля login")
    @Description("Запрос возвращает правильный код ответа 400 и в теле - message: Недостаточно данных для создания учётной записи")
    public void cannotCreateCourierWithoutLoginTest() {
        skipDeleteCourier = true; // Пропустили удаление курьера
        courier.setLogin(null);
        courierClient.createCourier(courier)
                .then()
                .assertThat()
                .body("message", equalTo("Недостаточно данных для создания учетной записи"))
                .and()
                .statusCode(400);
    }

    @Test
    @DisplayName("Нельзя создать курьера, без передачи поля password")
    @Description("Запрос возвращает правильный код ответа 400 и в теле - message: Недостаточно данных для создания учётной записи")
    public void cannotCreateCourierWithoutPasswordTest() {
        skipDeleteCourier = true; // Пропустили удаление курьера
        courier.setPassword(null);
        courierClient.createCourier(courier)
                .then()
                .assertThat()
                .body("message", equalTo("Недостаточно данных для создания учетной записи"))
                .and()
                .statusCode(400);
    }

    // Так как нет явных требований, а в апидоке повсюду ошибки, считаю это поле необязательным
    // Такие требования у нас были в части курса по ручному тестированию
    @Test
    @DisplayName("Можно создать курьера, без передачи поля firstName")
    @Description("Запрос возвращает правильный код ответа 201 и в теле - ok: true")
    public void courierCanBeCreatedWithoutFirstNameTest() {
        courier.setFirstName(null);
        courierClient.createCourier(courier)
                .then()
                .assertThat()
                .body("ok", equalTo(true))
                .and()
                .statusCode(201);
        // Авторизация и получение id созданного курьера для последующего удаления
        Response authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier));
        idCourier = courierClient.getIdCourier(authorizationResponse);
    }
}