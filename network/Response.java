package network;

import java.io.Serializable;

public class Response implements Serializable {
    private static final long serialVersionUID = 2L;

    private final String message;
    private final Object responseBody;

    public Response(String message) {
        this(message, null);
    }

    public Response(String message, Object responseBody) {
        this.message = message;
        this.responseBody = responseBody;
    }

    public String getMessage() {
        return message;
    }

    public Object getResponseBody() {
        return responseBody;
    }

    @Override
    public String toString() {
        return message;
    }
}