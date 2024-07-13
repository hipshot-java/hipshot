# hipshot

The fastest Java web server in the West.

Feedback, suggestions, and contributions are most welcome!

## Requirements

For maximum performance leveraging io_uring:

* Linux >= 5.1
* Java >= 8

There is a fallback that uses `java.nio` packages for portability in development environments, but it is not tuned for optimal performance.

## Maven Usage

Hipshot will be published to Maven central once the API has stabilized.

## Hello World Example

```java
public static void main(String[] args) {
    WebServer.create((request, response) -> {
        response.header("Content-Type", "text/plain")
            .body("Hello from Hipshot!")
            .send();
    }).start();
}
```

## License

MIT. Have fun and make cool things!
