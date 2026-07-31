package library.management.system;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/books")
public class BookServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        String search = request.getParameter("search");
        List<Book> books = new ArrayList<>();

        StringBuilder sql = new StringBuilder("SELECT id, title, author, category, total_copies, available_copies FROM books");
        
        if (search != null && !search.trim().isEmpty()) {
            search = search.replaceAll("\\s+", "").toLowerCase();
            sql.append(" WHERE LOWER(REPLACE(title, ' ', '')) LIKE ? ")
               .append("    OR LOWER(REPLACE(author, ' ', '')) LIKE ? ")
               .append("    OR LOWER(REPLACE(category, ' ', '')) LIKE ? ");
        }
        
        sql.append(" ORDER BY id DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            if (search != null && !search.trim().isEmpty()) {
                String searchPattern = "%" + search + "%";
                ps.setString(1, searchPattern);
                ps.setString(2, searchPattern);
                ps.setString(3, searchPattern);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getString("category"),
                        rs.getInt("total_copies"),
                        rs.getInt("available_copies")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("books", books);
        request.getRequestDispatcher("books.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        
        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect("login.jsp");
            return;
        }

        String action = request.getParameter("action");

        if ("add".equalsIgnoreCase(action)) {
            // --- ADD NEW BOOK ---
            String title = request.getParameter("title");
            String author = request.getParameter("author");
            String category = request.getParameter("category");
            int copies = Integer.parseInt(request.getParameter("copies"));

            String sql = "INSERT INTO books (title, author, category, total_copies, available_copies) VALUES (?, ?, ?, ?, ?)";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, title);
                ps.setString(2, author);
                ps.setString(3, category);
                ps.setInt(4, copies);
                ps.setInt(5, copies);

                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else if ("edit".equalsIgnoreCase(action)) {
            // --- EDIT EXISTING BOOK ---
            int bookId = Integer.parseInt(request.getParameter("bookId"));
            String title = request.getParameter("title");
            String author = request.getParameter("author");
            String category = request.getParameter("category");
            int newTotalCopies = Integer.parseInt(request.getParameter("totalCopies"));

            try (Connection conn = DBConnection.getConnection()) {
                if (conn != null) {
                    conn.setAutoCommit(false);

                    try {
                        // 1. Fetch old total_copies & available_copies to safely adjust stock
                        int oldTotal = 0;
                        int oldAvailable = 0;
                        String fetchSql = "SELECT total_copies, available_copies FROM books WHERE id = ?";
                        try (PreparedStatement psFetch = conn.prepareStatement(fetchSql)) {
                            psFetch.setInt(1, bookId);
                            try (ResultSet rs = psFetch.executeQuery()) {
                                if (rs.next()) {
                                    oldTotal = rs.getInt("total_copies");
                                    oldAvailable = rs.getInt("available_copies");
                                }
                            }
                        }

                        // Calculate difference in copies
                        int diff = newTotalCopies - oldTotal;
                        int newAvailable = oldAvailable + diff;

                        if (newAvailable < 0) {
                            newAvailable = 0; // Prevent negative inventory
                        }

                        // 2. Update book details
                        String updateSql = "UPDATE books SET title = ?, author = ?, category = ?, total_copies = ?, available_copies = ? WHERE id = ?";
                        try (PreparedStatement psUp = conn.prepareStatement(updateSql)) {
                            psUp.setString(1, title);
                            psUp.setString(2, author);
                            psUp.setString(3, category);
                            psUp.setInt(4, newTotalCopies);
                            psUp.setInt(5, newAvailable);
                            psUp.setInt(6, bookId);
                            psUp.executeUpdate();
                        }

                        conn.commit();
                    } catch (SQLException ex) {
                        conn.rollback();
                        throw ex;
                    } finally {
                        conn.setAutoCommit(true);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        response.sendRedirect("books");
    }
}