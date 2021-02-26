package kafka6271;

public class ConnectException extends KafkaException {

    public ConnectException(String s) {
        super(s);
    }

    public ConnectException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public ConnectException(Throwable throwable) {
        super(throwable);
    }
}
