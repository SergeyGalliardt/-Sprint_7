package org.example;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public class RestClient {
    protected static final String BASE_URL = "https://qa-scooter.praktikum-services.ru";

    protected RequestSpecification getSpecContentType() {
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .build();


    }
    protected RequestSpecification getSpecWithoutContentType(){
        return new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .build();
    }
}
