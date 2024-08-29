# hipshot

The fastest Java web server in the West.

Feedback, suggestions, and contributions are most welcome!

## Requirements

* Linux >= 5.1
* Java >= 8

A fallback using `java.nio.channels` packages for portability is in progress.

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

## Benchmarks

![IMG_2303](https://github.com/user-attachments/assets/a90ef8e9-5ed9-49d3-acf6-7e73083ae355)

## License

MIT. Have fun and make cool things!
