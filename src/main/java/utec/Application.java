package utec;

import com.sun.net.httpserver.HttpServer;
import utec.cli.parser.CommandLineParser;
import picocli.CommandLine;
import utec.controllers.*;

import java.net.InetSocketAddress;

public class Application {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // Rutas
        server.createContext("/users/register", new UserController.RegisterHandler());
        server.createContext("/users", new UserController.GetUserHandler());

        server.createContext("/auth/login", new AuthController.LoginHandler());

        server.createContext("/flights/create", new FlightController.CreateFlightHandler());
        server.createContext("/flights/search", new FlightController.SearchFlightsHandler());
        server.createContext("/flights/book", new BookingController.BookFlightHandler());
        server.createContext("/flights/book/", new BookingController.GetBookingHandler()); // con id

        server.createContext("/cleanup", new CleanupController());

        server.setExecutor(null);
        System.out.println("Servidor escuchando en http://localhost:8080");
        server.start();
    }
}
