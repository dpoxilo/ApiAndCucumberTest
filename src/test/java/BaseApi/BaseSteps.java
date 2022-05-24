package BaseApi;

import io.cucumber.java.ru.Дано;
import io.cucumber.java.ru.И;
import io.cucumber.java.ru.Когда;
import io.cucumber.java.ru.Тогда;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.restassured.RestAssured.given;

public class BaseSteps {
    public String baseUriMortyApi = "https://rickandmortyapi.com/api";
    public String baseUriReqresApi = "https://reqres.in/api";

    public String charId;
    public String mortyLoc;
    public String mortyRace;
    public String lastCharRace;
    public String lastCharLoc;
    public int lastEpisode;
    public int lastChar;

    @Когда("^узнаем информацию о персонаже c id \"([^\"]*)\"")
    @Step("Узнаем информацию о персонаже c id {id}")
    public void gettingCharacter(String id) {
        Response gettingCharacter = given()
                .baseUri(baseUriMortyApi)
                .when()
                .get("/character/" + id)
                .then()
                .statusCode(200)
                .extract()
                .response();
        charId = new JSONObject(gettingCharacter.getBody().asString()).get("id").toString();
        mortyLoc = new JSONObject(gettingCharacter.getBody().asString()).getJSONObject("location").get("name").toString();
        mortyRace = new JSONObject(gettingCharacter.getBody().asString()).get("species").toString();
    }

    @И("^получаем номер последнего эпизода с участием этого персонажа")
    @Step("Получаем номер последнего эпизода с участием этого персонажа")
    public void gettingLastEpisode() {
        Response gettingLastEpisode = given()
                .baseUri(baseUriMortyApi)
                .when()
                .get("/character/" + charId)
                .then()
                .statusCode(200)
                .extract()
                .response();
        int lastEpisodeIndex = (new JSONObject(gettingLastEpisode.getBody().asString()).getJSONArray("episode").length() - 1);
        lastEpisode = Integer.parseInt(new JSONObject(gettingLastEpisode.getBody().asString()).getJSONArray("episode")
                .get(lastEpisodeIndex).toString().replaceAll("\\D", ""));
    }

    @И("^получаем последнего персонажа в этом эпизоде")
    @Step("Получаем последнего персонажа в этом эпизоде")
    public void gettingLastCharacter() {
        Response gettingLastChar = given()
                .baseUri(baseUriMortyApi)
                .when()
                .get("/episode/" + lastEpisode)
                .then()
                .statusCode(200)
                .extract()
                .response();
        int lastCharIndex = (new JSONObject(gettingLastChar.getBody().asString()).getJSONArray("characters").length() - 1);
        lastChar = Integer.parseInt(new JSONObject(gettingLastChar.getBody().asString()).getJSONArray("characters")
                .get(lastCharIndex).toString().replaceAll("\\D", ""));
    }

    @И("^узнаем информацию о последнем персонаже")
    @Step("Узнаем информацию о последнем персонаже")
    public void gettingLastCharInfo() {
        Response gettingLastCharInfo = given()
                .baseUri(baseUriMortyApi)
                .when()
                .get("/character/" + lastChar)
                .then()
                .statusCode(200)
                .extract()
                .response();
        lastCharRace = new JSONObject(gettingLastCharInfo.getBody().asString()).get("species").toString();
        lastCharLoc = new JSONObject(gettingLastCharInfo.getBody().asString()).getJSONObject("location").get("name").toString();
    }

    @Тогда("^сравниваем местонахождение обоих персонажей")
    @Step("Сравниваем местонахождение обоих персонажей")
    public void locAssert() {
        if (mortyLoc.equals(lastCharLoc)) {
            System.out.println("Персонажи находятся в одном месте");
        } else {
            System.out.println("Персонажи находятся в разных местах");
        }
    }

    @И("^сравниваем расу обоих персонажей")
    @Step("Сравниваем расу обоих персонажей")
    public void raceAssert() {
        if (mortyRace.equals(lastCharRace)) {
            System.out.println("Персонажи одной расы");
        } else {
            System.out.println("Персонажи не одной расы");
        }
    }

    @Дано("^json файл, производим post запрос с редактированием полей и проверкой тела")
    @Step("Дан json файл, производим post запрос с редактированием полей и проверкой тела")
    public void sendBody() throws IOException {
        JSONObject body = new JSONObject(new String(Files.readAllBytes(Paths.get("src/test/resources/json/1.json"))));
        body.put("name", "Tomato");
        body.put("job", "Eat market");
        Response postJson = given()
                .header("Content-type", "application/json")
                .baseUri(baseUriReqresApi)
                .body(body.toString())
                .when()
                .post("/users")
                .then()
                .statusCode(201)
                .extract()
                .response();
        Assertions.assertEquals((new JSONObject(postJson.getBody().asString()).get("name")), (body.get("name")), "Fail");
        Assertions.assertEquals((new JSONObject(postJson.getBody().asString()).get("job")), (body.get("job")), "Fail");
    }
}
