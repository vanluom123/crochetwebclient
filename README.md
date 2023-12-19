# Crochet WebClient

This Java library provides convenient integration with REST APIs using the Spring WebClient and Google Guice for dependency injection. The library is built with Java 17 and managed using Gradle.

## Usage

### 1. Add Dependency

Add the following dependency to your Gradle project:

```groovy
implementation 'com.github.crochetwebclient:crochetwebclient:1.0.3'

```
Maven project:
```
<dependency>
  <groupId>com.github.crochetwebclient</groupId>
  <artifactId>crochetwebclient</artifactId>
  <version>1.0.3</version>
</dependency> 
```

### 2. Configuration

Create a configuration class to set up the WebClientService using Google Guice for injection.

```java
@Configuration
public class WebClientConfiguration {
    @Bean
    @Scope(scopeName = "prototype")
    public WebClientService webClientService() {
        Injector injector = Guice.createInjector(new WebClientModule());
        WebClientService service = injector.getInstance(WebClientServiceImpl.class);
        return service;
    }
}

```

### 3. Example Usage

Inject `WebClientService` into your components or services and use it to make REST API calls.

```java
@Service
public class MyApiService {
    private final WebClientService webClientService;

    @Autowired
    public MyApiService(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    // Your methods that use webClientService for REST API calls
}

```

## Building the Project

To build the project, use the following Gradle command:

```bash
./gradlew build

```

## Contributing

Feel free to contribute to the project by forking it and submitting pull requests. Make sure to follow the coding standards and provide tests for new features.

## License

This project is licensed under the [MIT License](https://www.notion.so/LICENSE.md).

## Acknowledgments

- [Spring Framework](https://spring.io/)
- [Google Guice](https://github.com/google/guice)
- [Gradle](https://gradle.org/)

## Contact

For any inquiries or issues, please open an issue on the [GitHub repository](https://github.com/your-username/your-project).

---

**Note**: Replace `your.group`, `your-artifact`, and `your-version` with your actual group, artifact, and version information.

Feel free to customize this template according to your project's specific needs.
