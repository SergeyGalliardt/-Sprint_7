package org.example;

import com.google.gson.Gson;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class OrdersClient extends RestClient {
    private static final String ORDER_ENDPOINT = "/api/v1/orders";
    private static final String CANCEL_ORDER_ENDPOINT = "/api/v1/orders/cancel";
    private static final String ACCEPT_ORDER_ENDPOINT = "/api/v1/orders/accept/";
    private static final String GET_ORDER_BY_NUMBER_TRACK_ENDPOINT = "/api/v1/orders/track";

    @Step("Создать заказ")
    public Response createOrder(Order order) {
        String requestBody = new Gson().toJson(order);
        Allure.addAttachment("Request Body", "application/json", requestBody);
        return given()
                .spec(getSpecContentType())
                .body(order)
                .when()
                .post(ORDER_ENDPOINT);

    }

    @Step("Отменить заказ")
    public Response cancelOrder(int trackNumber) {
        return given()
                .spec(getSpecContentType())
                .queryParam("track", trackNumber)
                .put(CANCEL_ORDER_ENDPOINT);
    }

    @Step("Получить список всех заказов")
    public Response getListOrders() {
        return given()
                .spec(getSpecWithoutContentType())
                .get(ORDER_ENDPOINT);
    }

    @Step("Получить список заказов определённого курьера")
    public Response getListOrdersSpecificCourier(int idCourier) {
        return given()
                .spec(getSpecWithoutContentType())
                .queryParam("courierId", idCourier)
                .get(ORDER_ENDPOINT);
    }

    @Step("Получить список заказов определённого курьера на определённых станциях метро")
    public Response getListOrdersSpecificCourierSpecificMetroStation(int idCourier, String[] metroIndex) {
        return given()
                .spec(getSpecWithoutContentType())
                .queryParam("courierId", idCourier)
                .queryParams("nearestStation", metroIndex)
                .get(ORDER_ENDPOINT);
    }

    @Step("Получить список из 10 заказов доступных для взятия курьером")
    public Response getListTenOrdersAvailableCourier() {
        return given()
                .spec(getSpecWithoutContentType())
                .queryParam("limit", 10)
                .queryParam("page", 0)
                .get(ORDER_ENDPOINT);
    }

    @Step("Получить список из 10 заказов доступных для взятия курьером у определённой станции метро")
    public Response getListTenOrdersAvailableCourierSpecificMetroStation(String[] metroIndex) {
        return given()
                .spec(getSpecWithoutContentType())
                .queryParam("limit", 10)
                .queryParam("page", 0)
                .queryParams("nearestStation", metroIndex)
                .get(ORDER_ENDPOINT);
    }

    @Step("Получить трек номер заказа")
    public int getTrackNumber(Response createOrderResponse) {
        return createOrderResponse.jsonPath().getInt("track");
    }

    @Step("Получить заказ по его трек номеру")
    public Response getOrderByTrackNumber(int trackNumber) {
        return given()
                .spec(getSpecWithoutContentType())
                .queryParam("t", trackNumber)
                .get(GET_ORDER_BY_NUMBER_TRACK_ENDPOINT);


    }

    @Step("Отправить запрос на получение заказа, без передачи трек номера")
    public Response getOrderWithoutTrackNumber() {
        return given()
                .spec(getSpecWithoutContentType())
                .queryParam("t")
                .get(GET_ORDER_BY_NUMBER_TRACK_ENDPOINT);
    }

    @Step("Получить id заказа")
    public int getIdOrder(Response getOrderResponse) {
        return getOrderResponse.jsonPath().getInt("order.id");
    }


    @Step("Принять заказ")
    public Response acceptOrder(int idCourier, int idOrder) {
        return given()
                .spec(getSpecWithoutContentType())
                .queryParam("courierId", idCourier)
                .put(ACCEPT_ORDER_ENDPOINT + idOrder);

    }

    @Step("Отправить запрос на принятие заказа, без передачи id курьера")
    public Response acceptOrderWithoutIdCourier(int idOrder) {
        return given()
                .spec(getSpecWithoutContentType())
                .queryParam("courierId")
                .put(ACCEPT_ORDER_ENDPOINT + idOrder);
    }

    @Step("Отправить запрос на принятие заказа, без передачи id заказа")
    public Response acceptOrderWithoutIdOrder(int idCourier) {
        String endpoint = ACCEPT_ORDER_ENDPOINT;
        endpoint = endpoint.substring(0, endpoint.length() - 1); // Убрал последний символ из URL

        return given()
                .spec(getSpecWithoutContentType())
                .queryParam("courierId", idCourier)
                .put(endpoint);
    }
}
