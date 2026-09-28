package co.edu.escuelaing.framework;

@FunctionalInterface
public interface RouteHandler {
    String handle(Request request) throws Exception;
}
