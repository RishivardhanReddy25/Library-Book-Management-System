package library.management.system;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/dashboard")
public class DashboardServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");

        int totalBooks = 0;       // Unique titles count
        int totalBookCopies = 0; // Sum of all physical book copies
        int totalUsers = 0;
        int activeLoans = 0;
        int pendingRequests = 0;

        int myActiveLoans = 0;
        int myRequests = 0;      // Added: Student total book requests
        double myFine = 0.0;

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    // Count unique book titles AND total sum of copies
                    String sqlBooks = "SELECT COUNT(*) AS total_titles, COALESCE(SUM(total_copies), 0) AS total_copies FROM books";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlBooks);
                         ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            totalBooks = rs.getInt("total_titles");
                            totalBookCopies = rs.getInt("total_copies");
                        }
                    }

                    // Count registered students
                    String sqlUsers = "SELECT COUNT(*) FROM users WHERE UPPER(role) = 'STUDENT'";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlUsers);
                         ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            totalUsers = rs.getInt(1);
                        }
                    }

                    // Count active issued books
                    String sqlLoans = "SELECT COUNT(*) FROM transactions WHERE UPPER(status) IN ('ISSUED', 'OVERDUE')";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlLoans);
                         ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            activeLoans = rs.getInt(1);
                        }
                    }

                    // Count pending requests
                    String sqlReqs = "SELECT COUNT(*) FROM book_requests WHERE UPPER(status) = 'PENDING'";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlReqs);
                         ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            pendingRequests = rs.getInt(1);
                        }
                    }
                } else {
                    // Student metrics: Active loans & fines
                    String sqlMyLoans = "SELECT COUNT(*), COALESCE(SUM(fine_amount), 0) FROM transactions WHERE student_id = ? AND UPPER(status) IN ('ISSUED', 'OVERDUE')";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlMyLoans)) {
                        stmt.setInt(1, user.getId());
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                myActiveLoans = rs.getInt(1);
                                myFine = rs.getDouble(2);
                            }
                        }
                    }

                    // Student metrics: Total book requests submitted
                    String sqlMyReqs = "SELECT COUNT(*) FROM book_requests WHERE user_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sqlMyReqs)) {
                        stmt.setInt(1, user.getId());
                        try (ResultSet rs = stmt.executeQuery()) {
                            if (rs.next()) {
                                myRequests = rs.getInt(1);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.setAttribute("totalBooks", totalBooks);
        request.setAttribute("totalBookCopies", totalBookCopies);
        request.setAttribute("totalUsers", totalUsers);
        request.setAttribute("activeLoans", activeLoans);
        request.setAttribute("pendingRequests", pendingRequests);

        request.setAttribute("myActiveLoans", myActiveLoans);
        request.setAttribute("myRequests", myRequests);
        request.setAttribute("myFine", myFine);

        request.getRequestDispatcher("/dashboard.jsp").forward(request, response);
    }
}