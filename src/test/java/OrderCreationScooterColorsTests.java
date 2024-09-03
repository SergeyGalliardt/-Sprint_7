import org.example.*;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.core.Is.isA;

@RunWith(Parameterized.class)
@DisplayName("Оформление заказа при разных вариантах выбора цвета самоката. Ручка: POST /api/v1/orders ")
public class OrderCreationScooterColorsTests {
    private final String[] scooterColour;

    public OrderCreationScooterColorsTests(String colourName, String[] scooterColour) {
        this.scooterColour = scooterColour;

    }

    @BeforeClass
    public static void startLogging() {
        RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    }

    @AfterClass
    public static void stopLogging(){
        RestAssured.reset();
    }


    @Parameterized.Parameters(name = "Test {index} Оформление заказа если цвет самоката: {0}")
    public static Collection<Object[]> getColor() {
        return Arrays.asList(new Object[][]{
                {"чёрный жемчуг и серая безысходность", new String[]{"BLACK", "GREY"}},
                {"чёрный жемчуг", new String[]{"BLACK"}},
                {"серая безысходность", new String[]{"GREY"}},
                {"цвет не выбран", new String[]{}}

        });
    }

    @Test
    public void scooterColourTests() {
        Order order = OrderGenerator.randomOrder(); // ТД для заказа
        order.setColor(scooterColour); // Изменили цвет на необходимый
        OrdersClient ordersClient = new OrdersClient();
        Response createResponse = ordersClient.createOrder(order);
        createResponse.then()
                .assertThat()
                .statusCode(201)
                .and()
                .body("track", isA(int.class));
        int track = createResponse.jsonPath().get("track"); // Получили трек номер заказа
        ordersClient.cancelOrder(track); // Отмена заказа после выполнения тестов

    }
}