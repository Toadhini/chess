# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```
[![Sequence Diagram]](https://sequencediagram.org/index.html?presentationMode=readOnly#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUexE7+4wp6l7FovFqXtYJ+cLtn6pavIaSpLPU+wgheertBAdZoFByyXAmlDtimGD1OEThOFmEwQZ8MDQcCyxwfECFISh+xXOgHCmF4vgBNA7CMjEIpwBG0hwAoMAADIQFkhRYcwTrUP6zRtF0vQGOo+RoFmipzGsvz-BwVygYKQH+uB5afCCak7A2Onwr6zpdjACBCeKGKCcJBJEmApJvoYu40vuDJMlOylcjeHl3kuwowGKEpujKcplu8SqmCazBujAaAQMwABmvhNjAmUyLeDpJuZnbdr2-ZZR2VCDuJNSutMl7QEgABeKAcFGMZxmgGJjOKVAmkg66Hhw5hICaqhjKSJVaXl8DIKmMDpgAjAROaqHm8yLEWJb1D41V6rVDW7HRCAGBusBpZtDElcOuUVUiR0emNFmCq59RGQCACycggPEagYlAwCbE0mwcGs4XaC4z0cGwqjxMSAD8mAlXFMBNPofw7DAMzZMcYAfXDJVZa5IHVP6zJbfEO2Nc1KCxgp7Wdd1vUov1GCDQdI047jpUYRNolpk482jGMi3LXsa3QBtJNk3tbMwCgB2GEjWyoy0ADSUt3Z2goXfSMCHnIKDPvE56Xte7OZZrApBfUj4BkbW4m9lAWXflZUutbL62yb+MTaZ9T2eKGSqAB076SgawUVR6CjSbpkExJYGEcHXywZe4fIZCVx2+NyZTdhMC4fh-O+Yn5HJ4h9Zp1L9GNgxleMd4fj+F4KDoDEcSJI3zf2b4WCiQ9hOlg00gRvxEbtBG3Q9HJqgKcMFE7XkBT1AAPGHpdoOU6FmQ8sK6bPUD1fPaBLyvSHlNCjzAVdLvWfYXeG-Bq+ktublUjlWuMmA+t35RD-+bygWVMuEK4onyXllDWVemAEYpxgLTZAHAehWmwEgBmUszYx2dpZHsfYpaewqkTcWe9drtU2rveqjVWZq0TFnEoYAeZ82zPyIWq1iyiwVAQshksZaHQAGp0xgJAJCqDX7m0vpZEYvl0543uu5P+-J6j9W4MeS8X8U6R1NsI+8wVB7D1Hq7eIODpEfnPv6Tup5-aBxGBcNRW8vzoN0pIzKmdKjc1znhLMdEmxmFlpKNAiCUDgEatLFhUADHqwmqBeoABJDA5YEAiygBifqqhxzEFatYpxk0aH1AAKxuP5oLfMzD1owESWoFJVMCgwAgClIJJZiQNhrkxeuDMYhILQBqfiaIYAAHElQaB7ng-u3SR7j3sEqGeNVCEHyPiXE+G9tLGLAqQuq0yYDL1megU+mBo6iPqMgHIvScwqJ-k-DWwivIf2UcfCOv8FzmwAcFUKIC9RgP4RAhGxNlmNSEQ7RcuzEpFVCRguxpZPmTI4cQ9hu0KFSNjphbOtCZq8wWowwpYx4liy+Zw7x+sJY-NkX8p211xFERQA40qg4X6-NHDAd+gYECHK+tYtBFtyBDwjHAdoMAABUIU0gtBej0vp+K7noOug0EYYy5iROkGsTY8RdQoDdJyAAPlghAaxkigDVMqpUaqiprClSgAAckqC4nQgXlUqN7GAkq+m1AaC4J1nRyU7MGS8MYRrhoOqdS4F1QLObUOmnnAiXqFgSs9UqGVCxZrhGCIEFYYx5WKt1Z8SEiatUgB1aSxOka5imrTbRf1NcuGGGJn4gJ65KAlkte+J2-ponominE4JpTkl7wqWgdJfd4VZJgLk-ODDcxooxSUpJ5TWpVJqdW6A9SPEMU8HXAIHAADsbgnAoCcDECMwQ4BcQAGzwAnIYRlRQEW91jvUKSHRRnjKhas9Z98T6hqVAWlatF5lGO3ks8FKzWozKfZszVEBzQeqNW+r4Fwz7fovRg+oOt0SMuOUhNYcBj2MscmoZyphPZUoJTSulVyNldtufuTR9Qnl6NeQI9AkCEDxShd886GjwkWUKtgyhVqOagsYxwSFWKYWOJ7VzBFdCUXDvfaOkhv7yH7UOriwhTGsosv+SS4O5LcMUpdl1OButGVvoxOBpU1iEYACEQwwBRISLDJT0byQcD0LV7TaWvuM6rWFwLWOXpgOZjgmGcjtT0OuKzTlBO4wyS49Mg6Baosk8E+oQXLMoGszkBsJVS0wF4XA9zQmwl4bufB9DSoMQjAQ6w3yhrXNzCsTl+2+HyMwEicasgEY0hctPXbFTRKXZ2uldIclnG602LhC8I1MqBtZTdfWuOY3pAFljfGibuWqHONE646Ls2Y1xsCOSyuJbvHlstNgfx6Iq3BNrW5d1jWYnNviW2idClu1wpE32gd4mlojvi2OspHbJ3VNqbOhpnimkBEsDLaymwW5IASGAMHfYICQ4AFIQHFEKuYMRM1qjPTQ2DkkmjMhkj0I1EztpTP-Ws65a8CLYAQMAMHUA4AQGslAPYAB1FgkTR49FM-xBQcAlarDzSgaNtQFs7c-daxZLxlnTMfd-E+wHQMjBp3TygjPmeVb61B7ZizYPXQAFYo7QEhynaxkfigw8lpyj9NNmwuZ-Snc5qUNco-raj7z6M+KxSKsjXm4MAo4x5rjESvcyb4xMXjYWItrbmu9ph6KvvSdJxwhpOKw8+8CqpiRF29fabprrc3bUVf0-V9ATXwvpAmc975pLKXmAYlgT1bWTPoBVNgHZqeDgXN9esYNy7036i+f82AQLFmQtYbC5QwNq2+1Rbj594piXx+pd294rLPVau2-ObSpkSHZu9+Uyxh5FHgFo8MJFGVtWEaMsSslGAJ0Mp20P87v3111W1b7yCwfIZh+j+C1bifA-dmaPWfPJIdD7OLRfMfAAlfWrDLEUdKM6J-dRF-VTI1JbcLaRbrSyAwOQBQLqU4XQbgaGQzJUVSWnEvFvKAIA3GBGCgOQSsM0GscMQoZA4AwxAfEKaAHQHqRmdqE0Jg-hFgyfNgkA6adMTMfJWLYWL7AQ4Mc0IQ6iDA9Lbxeg1KFEMQNgzAvLOrArY0NcJoCgygBCPfKNWVGAYvNXKgtYXyGgzrI-IUV0JoaQBQXRG-XArALQz-L2KXW1ZQqOXXP3exD-D2YTINHOENUYfwzKSuO2WIrKPbTxDLQ7Y7StAHEJbwq7RtWJO7cdX7R7ANeEcIxFN7KQiTGQ4pe7fIypf7GdagoHBdEHBuH6eUWIaHNuFoyaYMWAYAbAGnQgA+bHcwXHfubREeMeXoYwCXYbYInZbA+DbgPADEG3LA3QzybWRYqAelZY0jf+RwmAdoNIZkY1XiXRN0VQNYRlC4vRFODQOgnwfcDPR2LTMRDTDgmYsCV1QIq7SxQo3tYNMAuTMtXxI7E7QJOop4wlF4+oOoqo1JAozIzg7I27VtPI+EgoJ7FbTJaaUo8A+PUdOEztKddIudKuMkoAA)
