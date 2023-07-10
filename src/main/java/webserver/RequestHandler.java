package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import static utils.WebPageReader.readByPath;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        logger.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            // TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
            RequestHeader requestHeader = buildRequestHeader(in);
            String url = requestHeader.parseRequestUrl();

            DataOutputStream dos = new DataOutputStream(out);
            byte[] body = readByPath(url);

            String extension = url.substring(url.lastIndexOf("."));

            // 파일 확장자에 따라 다른 contentType 처리
            String contentType;
            switch (extension) {
                case ".html":
                    contentType = "text/html";
                    break;
                case ".css":
                    contentType = "text/css";
                    break;
                case ".js":
                    contentType = "text/javascript";
                    break;
                default:
                    contentType = "text/plain";
                    break;
            }
            response200Header(dos, body.length, contentType);
            responseBody(dos, body);
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private static RequestHeader buildRequestHeader(InputStream in) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        RequestHeader requestHeader = new RequestHeader();
        String line = br.readLine();
        requestHeader.addRequestLine(line);
        while(!"".equals((line = br.readLine()))){
            logger.debug("Request-Headers : {}", line);
            requestHeader.appendHeader(line);
        }
        return requestHeader;
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String contentType) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            dos.writeBytes("Content-Type: " + contentType + ";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            logger.error(e.getMessage());
        }
    }
}
