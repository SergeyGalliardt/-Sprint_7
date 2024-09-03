package org.example;

import com.google.gson.Gson;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class CourierClient extends RestClient {
    private static final String CREATE_COURIER_ENDPOINT = "/api/v1/courier";
    private static final String AUTHORIZATION_COURIER_ENDPOINT = "/api/v1/courier/login";
    private static final String DELETE_COURIER_ENDPOINT = "/api/v1/courier/";

    @Step("Создать курьера")
    public Response createCourier(Courier courier) {
        String requestBody = new Gson().toJson(courier);
        Allure.addAttachment("Request body", "application/json", requestBody);
        return given()
                .spec(getSpecContentType())
                .body(courier)
                .when()
                .post(CREATE_COURIER_ENDPOINT);

    }

    @Step("Авторизоваться курьером")
    public Response loginCourier(CourierCredentials courierCredentials) {
        String requestBody = new Gson().toJson(courierCredentials);
        Allure.addAttachment("Request body", "application/json", requestBody);
        return given()
                .spec(getSpecContentType())
                .body(courierCredentials)
                .post(AUTHORIZATION_COURIER_ENDPOINT);


    }

    @Step("Получить id курьера")
    public int getIdCourier(Response authorizationResponse) {
        return authorizationResponse.jsonPath().getInt("id");
    }


    @Step("Удалить созданного курьера")
    public Response deleteCourier(int id) {
        return given()
                .spec(getSpecWithoutContentType())
                .delete(DELETE_COURIER_ENDPOINT + id);

    }

    @Step("Отправить запрос на удаление курьера, без передачи id")
    public Response deleteCourierWithoutId() {
        return given()
                .spec(getSpecWithoutContentType())
                .delete(DELETE_COURIER_ENDPOINT);
    }


}
