import org.example.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.hamcrest.core.Is.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@DisplayName("Получение списка заказов. Ручка: GET /api/v1/orders")
public class GetListOrdersTests {
    OrdersClient ordersClient = new OrdersClient();
    CourierClient courierClient = new CourierClient();

    @BeforeClass
    public static void startLogging() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @AfterClass
    public static void stopLogging() {
        RestAssured.reset();
    }


    @Test
    @DisplayName("Получение списка всех заказов")
    public void getListOrdersTest() {
        ordersClient.getListOrders().then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("orders", isA(List.class)); // Проверили, что получили список
    }

    @Test
    @DisplayName("Получение списка заказов определённого курьера")
    public void getListOrdersSpecificCourierTest() {
        Courier courier = CourierGenerator.randomCourier();
        courierClient.createCourier(courier);
        Response response = courierClient.loginCourier(CourierCredentials.from(courier));
        int idCourier = courierClient.getIdCourier(response); // Авторизовались и получили id курьера
        ordersClient.getListOrdersSpecificCourier(idCourier)
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("orders", isA(List.class)); // Проверили, что получили список
        courierClient.deleteCourier(idCourier); // Удалили курьера

    }

    @Test
    @DisplayName("Получение списка заказов определённого курьера на определённых станциях метро")
    public void getListOrderSpecificCourierSpecificMetroStationTest() {
        Courier courier = CourierGenerator.randomCourier();
        courierClient.createCourier(courier);
        Response response = courierClient.loginCourier(CourierCredentials.from(courier));
        int idCourier = courierClient.getIdCourier(response); // Авторизовались и получили id курьера
        String[] metroIndex = {"1", "2"}; // Массив с индексами необходимого нам метро
        ordersClient.getListOrdersSpecificCourierSpecificMetroStation(idCourier, metroIndex)
                .then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("orders", isA(List.class)); // Проверили, что получили список
        courierClient.deleteCourier(idCourier); // Удалили курьера

    }

    @Test
    @DisplayName("Получение списка из 10 заказов, доступных для взятия курьером")
    public void getTenOrdersAvailableCourierTest() {
        Response response = ordersClient.getListTenOrdersAvailableCourier();
        response.then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("orders", isA(List.class));
        List<Map<String, Object>> orders = response.jsonPath().getList("orders"); //Получили список объектов содержащихся в массиве orders
        assertEquals(10, orders.size()); // Проверяем размер списка
        for (Map<String, Object> order : orders) {
            assertTrue(order.containsKey("id")); // Перебираем каждый элемент в списке orders и проверяем содержит ли он id заказа
        }
        Set<Object> idSet = new HashSet<>(); // Создаём объект HashSet, который хранит уникальные значения
        for (Map<String, Object> order : orders) {
            idSet.add(order.get("id")); // Перебираем каждый элемент в списке, извлекаем id и добавляем в HashSet
        }
        assertEquals(10, idSet.size()); // Проверяем, что получили 10 уникальных id заказов

    }

    @Test
    @DisplayName("Получение списка из 10 заказов, доступных для взятия курьером возле определённой станции метро")
    public void getTenOrdersAvailableCourierSpecificMetroStationTest() {
        String[] metroIndex = {"1"}; // Массив с индексом необходимого нам метро
        Response response = ordersClient.getListTenOrdersAvailableCourierSpecificMetroStation(metroIndex);
        response.then()
                .assertThat()
                .statusCode(200)
                .and()
                .body("orders", isA(List.class));
        List<Map<String, Object>> orders = response.jsonPath().getList("orders"); //Получили список объектов содержащихся в массиве orders
        assertEquals(10, orders.size()); // Проверяем размер списка
        for (Map<String, Object> order : orders) {
            assertTrue(order.containsKey("id")); // Перебираем каждый элемент в списке orders и проверяем содержит ли он id заказа
        }
        Set<Object> idSet = new HashSet<>(); // Создаём объект HashSet, который хранит уникальные значения
        for (Map<String, Object> order : orders) {
            idSet.add(order.get("id")); // Перебираем каждый элемент в списке, извлекаем id и добавляем в HashSet
        }
        assertEquals(10, idSet.size()); // Проверяем, что получили 10 уникальных id заказов

    }
}