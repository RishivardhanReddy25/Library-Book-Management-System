package library.management.system;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/requests")
public class RequestServlet extends HttpServlet {
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
        List<BookRequest> requestList = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null) {
                String sql;
                if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                    sql = "SELECT r.id, r.user_id, u.username, r.book_id, b.title AS book_title, r.request_date, r.status " +
                          "FROM book_requests r " +
                          "JOIN users u ON r.user_id = u.id " +
                          "JOIN books b ON r.book_id = b.id " +
                          "ORDER BY r.id DESC";
                } else {
                    sql = "SELECT r.id, r.user_id, u.username, r.book_id, b.title AS book_title, r.request_date, r.status " +
                          "FROM book_requests r " +
                          "JOIN users u ON r.user_id = u.id " +
                          "JOIN books b ON r.book_id = b.id " +
                          "WHERE r.user_id = ? " +
                          "ORDER BY r.id DESC";
                }

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    if (!"ADMIN".equalsIgnoreCase(user.getRole())) {
                        stmt.setInt(1, user.getId());
                    }
                    try (ResultSet rs = stmt.executeQuery()) {
                        while (rs.next()) {
                            BookRequest req = new BookRequest();
                            req.setId(rs.getInt("id"));
                            req.setUserId(rs.getInt("user_id"));
                            req.setUsername(rs.getString("username"));
                            req.setBookId(rs.getInt("book_id"));
                            req.setBookTitle(rs.getString("book_title"));
                            req.setRequestDate(rs.getDate("request_date"));
                            req.setStatus(rs.getString("status"));
                            requestList.add(req);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.setAttribute("requestList", requestList);
        request.getRequestDispatcher("/requests.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login.jsp");
            return;
        }

        User user = (User) session.getAttribute("user");
        String action = request.getParameter("action");

        // =====================================================
        // STUDENT ACTION: CREATE NEW BOOK REQUEST
        // =====================================================
        if ("create".equalsIgnoreCase(action)) {
            String bookIdParam = request.getParameter("bookId");
            if (bookIdParam != null && !bookIdParam.trim().isEmpty()) {
                int bookId = Integer.parseInt(bookIdParam);

                try (Connection conn = DBConnection.getConnection()) {
                    if (conn != null) {
                        String insertSql = "INSERT INTO book_requests (user_id, book_id, request_date, status) VALUES (?, ?, ?, 'PENDING')";
                        try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                            stmt.setInt(1, user.getId());
                            stmt.setInt(2, bookId);
                            stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
                            stmt.executeUpdate();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } 
        // =====================================================
        // ADMIN ACTIONS: APPROVE / REJECT REQUESTS
        // =====================================================
        else if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            String reqIdParam = request.getParameter("requestId");
            if (reqIdParam != null && !reqIdParam.trim().isEmpty()) {
                int requestId = Integer.parseInt(reqIdParam);

                try (Connection conn = DBConnection.getConnection()) {
                    if (conn != null) {
                        conn.setAutoCommit(false);
                        try {
                            if ("approve".equalsIgnoreCase(action)) {
                                int userId = 0;
                                int bookId = 0;

                                String fetchSql = "SELECT user_id, book_id FROM book_requests WHERE id = ?";
                                try (PreparedStatement stmt = conn.prepareStatement(fetchSql)) {
                                    stmt.setInt(1, requestId);
                                    try (ResultSet rs = stmt.executeQuery()) {
                                        if (rs.next()) {
                                            userId = rs.getInt("user_id");
                                            bookId = rs.getInt("book_id");
                                        }
                                    }
                                }
                                if (userId > 0 && bookId > 0) {
                                    // Update request status to APPROVED
                                    String updateReqSql = "UPDATE book_requests SET status = 'APPROVED' WHERE id = ?";
                                    try (PreparedStatement stmt = conn.prepareStatement(updateReqSql)) {
                                        stmt.setInt(1, requestId);
                                        stmt.executeUpdate();
                                    }

                                    // Create a new transaction (Shows directly in Admin Portal Transactions)
                                    LocalDate today = LocalDate.now();
                                    LocalDate dueDate = today.plusDays(14); // 14-day borrowing limit

                                    String insertTxSql = "INSERT INTO transactions (student_id, book_id, issue_date, due_date, fine_amount, status) VALUES (?, ?, ?, ?, 0.0, 'ISSUED')";
                                    try (PreparedStatement stmt = conn.prepareStatement(insertTxSql)) {
                                        stmt.setInt(1, userId);
                                        stmt.setInt(2, bookId);
                                        stmt.setDate(3, java.sql.Date.valueOf(today));
                                        stmt.setDate(4, java.sql.Date.valueOf(dueDate));
                                        stmt.executeUpdate();
                                    }

                                    // Decrease available copies
                                    String updateBookSql = "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0";
                                    try (PreparedStatement stmt = conn.prepareStatement(updateBookSql)) {
                                        stmt.setInt(1, bookId);
                                        stmt.executeUpdate();
                                    }
                                }
                            } else if ("reject".equalsIgnoreCase(action)) {
                                // Mark request as REJECTED so it shows in student requests/history
                                String updateReqSql = "UPDATE book_requests SET status = 'REJECTED' WHERE id = ?";
                                try (PreparedStatement stmt = conn.prepareStatement(updateReqSql)) {
                                    stmt.setInt(1, requestId);
                                    stmt.executeUpdate();
                                }
                            }

                            conn.commit();
                        } catch (SQLException ex) {
                            conn.rollback();
                            throw ex;
                        } finally {
                            conn.setAutoCommit(true);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        response.sendRedirect(request.getContextPath() + "/requests");
    }
}