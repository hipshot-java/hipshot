package dev.hipshot.web.server;

import dev.hipshot.web.server.codec.WebServerRequestDecoder;
import dev.hipshot.web.server.codec.WebServerResponseEncoder;
import dev.hipshot.web.util.ObjectPool;
import lombok.extern.slf4j.Slf4j;
import sh.blake.niouring.IoUring;
import sh.blake.niouring.IoUringServerSocket;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

@Slf4j
public class NioUringWebServer extends WebServer {
    private final Options options;
    private final BiConsumer<WebServerRequest, WebServerResponse> handler;
    private final ExecutorService pool;
    private final ObjectPool<ByteBuffer> inBufferPool;
    private final ObjectPool<ByteBuffer> outBufferPool;
    private final List<IoUring> rings = new ArrayList<>();

    public NioUringWebServer(Options options, BiConsumer<WebServerRequest, WebServerResponse> handler) {
        this.options = options;
        this.handler = handler;
        pool = Executors.newFixedThreadPool(options.threads());
        inBufferPool = new ObjectPool<>(() -> ByteBuffer.allocateDirect(options.requestBufferSize()));
        outBufferPool = new ObjectPool<>(() -> ByteBuffer.allocateDirect(options.responseBufferSize()));
    }

    public NioUringWebServer start() {
        var serverSocket = new IoUringServerSocket(options.host(), options.port());
        serverSocket.onAccept((ring, socket) -> {
            ring.queueAccept(serverSocket);

            var inBuffer = inBufferPool.take();
            var outBuffer = outBufferPool.take();
            var requestDecoder = new WebServerRequestDecoder();

            socket.onRead(received -> {
                received.flip();
                if (!received.hasRemaining()) {
                    ring.queueClose(socket);
                    return;
                }
                var request = requestDecoder.decode(received);
                if (request != null) {
                    request.ring(ring);
                    var response = new WebServerResponse();
                    response.onSend(res -> {
                        WebServerResponseEncoder.encode(response, outBuffer);
                        ring.queueWrite(socket, outBuffer);
                    });
                    handler.accept(request, response);
                }
                received.compact();
                ring.queueRead(socket, received);
            });

            socket.onWrite(ByteBuffer::compact);

            socket.onClose(() -> {
                inBufferPool.give(inBuffer.clear());
                outBufferPool.give(outBuffer.clear());
            });

            socket.onException(Throwable::printStackTrace);

            ring.queueRead(socket, inBuffer);
        });

        serverSocket.onException(Throwable::printStackTrace);

        for (int i = 0; i < options.threads(); i++) {
            var ring = new IoUring(8192);
            ring.onException(Throwable::printStackTrace);
            ring.queueAccept(serverSocket);
            rings.add(ring);
            pool.execute(ring::loop);
        }

        return this;
    }

    @Override
    public WebServer stop() {
        pool.shutdown();
        rings.forEach(IoUring::close);
        // TODO: wake up?
        return this;
    }
}
