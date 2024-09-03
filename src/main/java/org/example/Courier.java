package org.example;

import io.qameta.allure.Step;

public class Courier {

    private String login;
    private String password;
    private String firstName;


    public Courier(String login, String password, String firstName) {
        this.login = login;
        this.password = password;
        this.firstName = firstName;
    }

    public Courier() {
    }

    public String getLogin() {
        return login;
    }

    @Step("Изменить login")
    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    @Step("Изменить password")
    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    @Step("Изменить firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

}
