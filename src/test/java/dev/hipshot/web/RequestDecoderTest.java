package dev.hipshot.web;

import dev.hipshot.web.server.codec.WebServerRequestDecoder;
import dev.hipshot.web.server.WebServerRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sh.blake.niouring.util.ByteBufferUtil;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestDecoderTest {

    @Test
    public void shouldParseSimpleRequestLine() {
        ByteBuffer buffer = ByteBufferUtil.wrapDirect("GET /test HTTP/1.1\r\n\r\n");
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        WebServerRequest request = decoder.decode(buffer);
        Assertions.assertEquals("GET", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
    }

    @Test
    public void shouldParseHeaders() {
        ByteBuffer buffer = ByteBufferUtil.wrapDirect("GET /test HTTP/1.1\r\nAccept: text/plain\r\n\r\n");
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        WebServerRequest request = decoder.decode(buffer);
        Assertions.assertEquals("GET", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
        Assertions.assertEquals("text/plain", request.headers().get("Accept").get(0));
    }

    @Test
    public void shouldParseMultipleHeaders() {
        ByteBuffer buffer = ByteBufferUtil.wrapDirect("GET /test HTTP/1.1\r\nAccept: text/plain\r\nKeep-Alive: timeout=5, max=1000\r\n\r\n");
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        WebServerRequest request = decoder.decode(buffer);
        Assertions.assertEquals("GET", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
        Assertions.assertEquals("text/plain", request.headers().get("Accept").get(0));
        Assertions.assertEquals("timeout=5, max=1000", request.headers().get("Keep-Alive").get(0));
    }

    @Test
    public void shouldParsequeryParams() {
        ByteBuffer buffer = ByteBufferUtil.wrapDirect("GET /test?foo=bar HTTP/1.1\r\n\r\n");
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        WebServerRequest request = decoder.decode(buffer);
        Assertions.assertEquals("GET", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
        Assertions.assertEquals("bar", request.queryParams().get("foo").get(0));
    }

    @Test
    public void shouldParseMultiplequeryParams() {
        ByteBuffer buffer = ByteBufferUtil.wrapDirect("GET /test?foo=bar&test=true HTTP/1.1\r\n\r\n");
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        WebServerRequest request = decoder.decode(buffer);
        Assertions.assertEquals("GET", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
        Assertions.assertEquals("bar", request.queryParams().get("foo").get(0));
        Assertions.assertEquals("true", request.queryParams().get("test").get(0));
    }

    @Test
    public void shouldParseBody() {
        ByteBuffer buffer = ByteBufferUtil.wrapDirect("POST /test?foo=bar HTTP/1.1\r\nContent-Length: 13\r\n\r\nHello, world!\r\n");
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        WebServerRequest request = decoder.decode(buffer);
        Assertions.assertEquals("POST", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
        Assertions.assertEquals("bar", request.queryParams().get("foo").get(0));
        Assertions.assertEquals("13", request.headers().get("Content-Length").get(0));
        Assertions.assertEquals("Hello, world!", StandardCharsets.UTF_8.decode(request.body()).toString());
    }

    @Test
    public void shouldParseMultipleRequests() {
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        ByteBuffer buffer = ByteBufferUtil.wrapDirect("POST /test?foo=bar HTTP/1.1\r\nContent-Length: 13\r\n\r\nHello, world!\r\n");

        WebServerRequest request = decoder.decode(buffer);
        Assertions.assertEquals("POST", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
        Assertions.assertEquals("bar", request.queryParams().get("foo").get(0));
        Assertions.assertEquals("13", request.headers().get("Content-Length").get(0));
        Assertions.assertEquals("Hello, world!", StandardCharsets.UTF_8.decode(request.body()).toString());

        buffer.position(0);

        WebServerRequest request2 = decoder.decode(buffer);
        Assertions.assertEquals("POST", request2.method());
        Assertions.assertEquals("HTTP/1.1", request2.protocol());
        Assertions.assertEquals("/test", request2.path());
        Assertions.assertEquals("bar", request2.queryParams().get("foo").get(0));
        Assertions.assertEquals("13", request2.headers().get("Content-Length").get(0));
        Assertions.assertEquals("Hello, world!", StandardCharsets.UTF_8.decode(request2.body()).toString());
    }

    @Test
    public void shouldParseFragmentedRequestLine() {
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        buffer.put("POST /test?f".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        WebServerRequest request = decoder.decode(buffer);
        Assertions.assertNull(request);
        buffer.compact();
        buffer.put("oo=bar HTTP/1.1=\r\nContent-Length: 13\r\n\r\nHello, world!\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        request = decoder.decode(buffer);

        Assertions.assertEquals("POST", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
        Assertions.assertEquals("bar", request.queryParams().get("foo").get(0));
        Assertions.assertEquals("13", request.headers().get("Content-Length").get(0));
        Assertions.assertEquals("Hello, world!", StandardCharsets.UTF_8.decode(request.body()).toString());
    }

    @Test
    public void shouldParseFragmentedHeaders() {
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        buffer.put("POST /test?foo=bar HTTP/1.1\r\nConte".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        WebServerRequest request = decoder.decode(buffer);
        Assertions.assertNull(request);
        buffer.compact();
        buffer.put("nt-Length: 13\r\n\r\nHello, world!\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        request = decoder.decode(buffer);

        Assertions.assertEquals("POST", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
        Assertions.assertEquals("bar", request.queryParams().get("foo").get(0));
        Assertions.assertEquals("13", request.headers().get("Content-Length").get(0));
        Assertions.assertEquals("Hello, world!", StandardCharsets.UTF_8.decode(request.body()).toString());
    }

    @Test
    public void shouldParseFragmentedBody() {
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        buffer.put("POST /test?foo=bar HTTP/1.1\r\nContent-Length: 13\r\n\r\nH".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        WebServerRequest request = decoder.decode(buffer);
        Assertions.assertNull(request);
        buffer.compact();
        buffer.put("ello, world!\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        request = decoder.decode(buffer);

        Assertions.assertEquals("POST", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
        Assertions.assertEquals("bar", request.queryParams().get("foo").get(0));
        Assertions.assertEquals("13", request.headers().get("Content-Length").get(0));
        Assertions.assertEquals("Hello, world!", StandardCharsets.UTF_8.decode(request.body()).toString());
    }

    @Test
    public void shouldParseChunkedBody() {
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        buffer.put("POST /test?foo=bar HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.put("7\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.put("Hello, \r\n".getBytes(StandardCharsets.UTF_8));
        buffer.flip();

        WebServerRequest request = decoder.decode(buffer);
        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
        Assertions.assertEquals("bar", request.queryParams().get("foo").get(0));
        Assertions.assertEquals("chunked", request.headers().get("Transfer-Encoding").get(0));

        AtomicInteger chunkCount = new AtomicInteger(0);
        request.chunkHandler(chunk -> {
            chunkCount.incrementAndGet();
            if (chunkCount.get() == 1) {
                Assertions.assertEquals("Hello, ", StandardCharsets.UTF_8.decode(chunk).toString());
            } else if (chunkCount.get() == 2) {
                Assertions.assertEquals("world!", StandardCharsets.UTF_8.decode(chunk).toString());
            }
        });

        buffer.compact();
        buffer.put("6\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.put("world!\r\n0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        decoder.decode(buffer);

        Assertions.assertEquals(2, chunkCount.get());
    }

    @Test
    public void shouldParseFragmentedChunkedBody() {
        WebServerRequestDecoder decoder = new WebServerRequestDecoder();
        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);

        buffer.put("POST /test?foo=bar HTTP/1.1\r\nTransfer-Encoding: chunked\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.put("7\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.put("Hel".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        WebServerRequest request = decoder.decode(buffer);
        buffer.compact();

        Assertions.assertNotNull(request);
        Assertions.assertEquals("POST", request.method());
        Assertions.assertEquals("HTTP/1.1", request.protocol());
        Assertions.assertEquals("/test", request.path());
        Assertions.assertEquals("bar", request.queryParams().get("foo").get(0));
        Assertions.assertEquals("chunked", request.headers().get("Transfer-Encoding").get(0));

        AtomicInteger chunkCount = new AtomicInteger(0);
        request.chunkHandler(chunk -> {
            chunkCount.incrementAndGet();
            if (chunkCount.get() == 1) {
                Assertions.assertEquals("Hello, ", StandardCharsets.UTF_8.decode(chunk).toString());
            } else if (chunkCount.get() == 2) {
                Assertions.assertEquals("world!", StandardCharsets.UTF_8.decode(chunk).toString());
            }
        });

        buffer.put("lo, \r\n".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        decoder.decode(buffer);
        buffer.compact();

        buffer.put("6\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.put("world!\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.put("0\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        buffer.flip();
        decoder.decode(buffer);

        Assertions.assertEquals(2, chunkCount.get());
    }
}
