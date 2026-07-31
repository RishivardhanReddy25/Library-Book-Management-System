package library.management.system;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

@WebServlet("/issueReturn")
public class IssueReturnServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        
        if (currentUser == null || !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        String action = request.getParameter("action");
        int bookId = Integer.parseInt(request.getParameter("bookId"));

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            if ("issue".equalsIgnoreCase(action)) {
                int studentId = Integer.parseInt(request.getParameter("studentId"));
                
                try (PreparedStatement psCheck = conn.prepareStatement("SELECT available_copies FROM books WHERE id = ?")) {
                    psCheck.setInt(1, bookId);
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next() && rs.getInt("available_copies") > 0) {
                            
                            String txSql = "INSERT INTO transactions (student_id, book_id, issue_date, due_date, status) VALUES (?, ?, ?, ?, 'ISSUED')";
                            try (PreparedStatement psTx = conn.prepareStatement(txSql)) {
                                psTx.setInt(1, studentId);
                                psTx.setInt(2, bookId);
                                psTx.setDate(3, Date.valueOf(LocalDate.now()));
                                psTx.setDate(4, Date.valueOf(LocalDate.now().plusDays(14)));
                                psTx.executeUpdate();
                            }

                            try (PreparedStatement psDec = conn.prepareStatement("UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0")) {
                                psDec.setInt(1, bookId);
                                psDec.executeUpdate();
                            }
                            conn.commit();
                        } else {
                            conn.rollback();
                        }
                    }
                }

            } else if ("return".equalsIgnoreCase(action)) {
                int transactionId = Integer.parseInt(request.getParameter("transactionId"));

                String returnTxSql = "UPDATE transactions SET return_date = ?, status = 'RETURNED' WHERE id = ?";
                try (PreparedStatement psTx = conn.prepareStatement(returnTxSql)) {
                    psTx.setDate(1, Date.valueOf(LocalDate.now()));
                    psTx.setInt(2, transactionId);
                    psTx.executeUpdate();
                }

                try (PreparedStatement psInc = conn.prepareStatement("UPDATE books SET available_copies = available_copies + 1 WHERE id = ?")) {
                    psInc.setInt(1, bookId);
                    psInc.executeUpdate();
                }

                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/transactions");
    }
}