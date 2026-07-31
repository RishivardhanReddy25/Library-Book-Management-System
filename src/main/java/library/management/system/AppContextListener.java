package library.management.system;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

// =========================================================================
// JAVAX IMPORTS (Preserved for Tomcat 9 / Java EE 8)
// =========================================================================
// import javax.servlet.ServletContextEvent;
// import javax.servlet.ServletContextListener;
// import javax.servlet.annotation.WebListener;

// =========================================================================
// JAKARTA IMPORTS (Active for Tomcat 10+ / Jakarta EE)
// =========================================================================
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Triggered when application starts up
        System.out.println("Library Management System context initialized successfully.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("Cleaning up database drivers and background threads...");

        // 1. Deregister JDBC drivers to prevent memory leaks
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
                System.out.println("Deregistered JDBC driver: " + driver);
            } catch (SQLException e) {
                System.err.println("Error deregistering JDBC driver: " + driver);
                e.printStackTrace();
            }
        }

        // 2. Shut down MySQL connection cleanup thread
        try {
            AbandonedConnectionCleanupThread.checkedShutdown();
            System.out.println("MySQL AbandonedConnectionCleanupThread stopped successfully.");
        } catch (Exception e) {
            System.err.println("Error shutting down AbandonedConnectionCleanupThread.");
            e.printStackTrace();
        }
    }
}