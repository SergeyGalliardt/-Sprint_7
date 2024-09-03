import org.example.*;
import io.qameta.allure.Description;
import io.qameta.allure.Issue;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.junit.*;

import static org.hamcrest.core.IsEqual.equalTo;

@DisplayName("Принять заказ. Ручка: PUT /api/v1/orders/accept/:id")
public class AcceptOrderTests {
    CourierClient courierClient = new CourierClient();
    private Courier courier;
    private int idCourier;
    OrdersClient ordersClient = new OrdersClient();
    private Order order;
    private int trackNumber;
    private int idOrder;
    private boolean skipDeleteCourier = false; // пропуск удаления курьера
    private boolean skipCancelOrder = false; // пропуск отмены заказа

    @BeforeClass
    public static void startLogging() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    } // Включили логирование

    // Создали ТД для курьера и заказа
    @Before
    public void createTestData() {
        courier = CourierGenerator.randomCourier();
        order = OrderGenerator.randomOrder();
    }

    @After
    public void cleanUp() {
        if (!skipDeleteCourier) {
            courierClient.deleteCourier(idCourier);
        } // Удалить курьера
        if (!skipCancelOrder) {
            ordersClient.cancelOrder(trackNumber);
        } // Отменить заказ

    }

    @AfterClass
    public static void stopLogging() {
        RestAssured.reset();
    } // Выключили логирование


    @Test
    @DisplayName("Курьер может принять заказ")
    @Description("Запрос возвращает правильный код ответа 200 и в теле - ok: true")
    @Issue("После принятия заказа курьером, заказ дублируется в списке заказов. Создаётся полная копия принятого заказа, но с другим id")
    public void acceptOrderTest() {
        courierClient.createCourier(courier); // Создали курьера
        Response authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier)); // Авторизовались
        idCourier = courierClient.getIdCourier(authorizationResponse); // Получили id курьера
        Response createOrderResponse = ordersClient.createOrder(order); // Создали заказ
        trackNumber = ordersClient.getTrackNumber(createOrderResponse); // Получили трек номер заказа
        Response getOrderResponse = ordersClient.getOrderByTrackNumber(trackNumber); // Получили информацию о заказе
        idOrder = ordersClient.getIdOrder(getOrderResponse); // Получили id заказа
        // Отправили запрос на принятие заказа
        ordersClient.acceptOrder(idCourier, idOrder)
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("ok", equalTo(true));
        // Проверили, что заказ принят в работу этим курьером
        ordersClient.getListOrdersSpecificCourier(idCourier)
                .then()
                .assertThat()
                .statusCode(200)
                .body("orders[0].id", equalTo(idOrder)) // Проверить, что у курьера отображается заказ с этим id
                .and()
                .body("pageInfo.total", equalTo(1)); // Проверить, что у курьера отображается только 1 принятый заказ
    }

    @Test
    @DisplayName("Если не передать id курьера, запрос вернёт ошибку")
    @Description("Запрос возвращает правильный код ответа 400 и в теле - message: Недостаточно данных для поиска")
    public void acceptOrderWithoutIdCourierTest() {
        skipDeleteCourier = true; // Пропустили удаление курьера
        Response createOrderResponse = ordersClient.createOrder(order); // Создали заказ
        trackNumber = ordersClient.getTrackNumber(createOrderResponse); // Получили трек номер заказа
        Response getOrderResponse = ordersClient.getOrderByTrackNumber(trackNumber); // Получили информацию о заказе
        idOrder = ordersClient.getIdOrder(getOrderResponse); // Получили id заказа
        // Отправили запрос без id курьера
        ordersClient.acceptOrderWithoutIdCourier(idOrder)
                .then()
                .assertThat()
                .statusCode(400)
                .and()
                .body("message", equalTo("Недостаточно данных для поиска"));


    }

    @Test
    @DisplayName("Если передать неверный id курьера, запрос вернёт ошибку")
    @Description("Запрос возвращает правильный код ответа 404 и в теле - message: Курьера с таким id не существует")
    public void acceptOrderIncorrectIdCourierTest() {
        skipDeleteCourier = true; // Пропустили удаление курьера
        Response createOrderResponse = ordersClient.createOrder(order);
        trackNumber = ordersClient.getTrackNumber(createOrderResponse);
        Response getOrderResponse = ordersClient.getOrderByTrackNumber(trackNumber);
        idOrder = ordersClient.getIdOrder(getOrderResponse); // Получили id заказа
        int incorrectIdCourier = 12345678; // id несуществующего курьера
        // Отправили запрос с некорректным id курьера
        ordersClient.acceptOrder(incorrectIdCourier, idOrder)
                .then()
                .assertThat()
                .statusCode(404)
                .and()
                .body("message", equalTo("Курьера с таким id не существует"));

    }

    @Test
    @DisplayName("Если не передать id заказа, запрос вернёт ошибку")
    @Description("Запрос возвращает правильный код ответа 400 и в теле - message: Недостаточно данных для поиска")
    @Issue("Фактический результат: {\n" + "\"code\": 404,\n" + "\"message\": \"Not Found.\"\n" + "}")
    public void acceptOrderWithoutIdOrderTest() {
        skipCancelOrder = true; // Пропустили отмену заказа
        courierClient.createCourier(courier); // Создали курьера
        Response authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier)); // Авторизовались
        idCourier = courierClient.getIdCourier(authorizationResponse); // Получили id курьера
        // Отправили запрос без id заказа
        ordersClient.acceptOrderWithoutIdOrder(idCourier)
                .then()
                .assertThat()
                .statusCode(400)
                .and()
                .body("message", equalTo("Недостаточно данных для поиска"));
    }

    @Test
    @DisplayName("Если передать неверный id заказа, запрос вернёт ошибку")
    @Description("Запрос возвращает правильный код ответа 404 и в теле - message: Заказа с таким id не существует")
    public void acceptOrderIncorrectIdOrderTest() {
        skipCancelOrder = true; // Пропустили отмену заказа
        courierClient.createCourier(courier); // Создали курьера
        Response authorizationResponse = courierClient.loginCourier(CourierCredentials.from(courier)); // Авторизовались
        idCourier = courierClient.getIdCourier(authorizationResponse); // Получили id курьера
        int incorrectIdOrder = 12345678; // id несуществующего заказа
        // Отправили запрос с некорректным id заказа
        ordersClient.acceptOrder(idCourier, incorrectIdOrder)
                .then()
                .assertThat()
                .statusCode(404)
                .and()
                .body("message", equalTo("Заказа с таким id не существует"));

    }

}
