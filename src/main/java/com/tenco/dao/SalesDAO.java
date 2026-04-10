package com.tenco.dao;

import com.tenco.dto.Product;
import com.tenco.dto.Sales;
import com.tenco.util.DBConnectionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SalesDAO {

    // 판매 처리 (Product product, int quantity)
    // 트랜잭션 시작 -> 판매기록, 상품 재고 차감 -> 트랜잭션 종료

    public boolean processSale(Product product, int quantity) throws SQLException {
        Connection conn = null;
        try {
            conn = DBConnectionManager.getConnection();
            conn.setAutoCommit(false);

            if (product.getStock() < quantity) {
                throw new SQLException("재고가 부족합니다. 현재 재고 : " + product.getStock());
            }

            // 판매기록 insert
            String sql = """
                    INSERT INTO sales(product_id, quantity, unit_price)
                    VALUES (?, ?, ?)
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, product.getId());
                pstmt.setInt(2, quantity);
                pstmt.setBigDecimal(3, product.getPrice());
                pstmt.executeUpdate();
            }

            // 상품 재고 차감 (update)
            String updateSql = """
                    UPDATE product SET stock = stock - ? WHERE id = ?
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setInt(1, quantity);
                pstmt.setInt(2, product.getId());
                pstmt.executeUpdate();
            }
            conn.commit();
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            System.out.println("오류 발생" + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        }
    }

    // 오늘 매출 집계

    public List<Sales> findTodaySales() throws SQLException {
        List<Sales> salesList = new ArrayList<>();
        String sql = """
                SELECT p.name AS product_name,
                	   SUM(s.quantity) AS total_quantity,
                       SUM(s.quantity * s.unit_price) AS total_price
                FROM sales s
                JOIN product p ON s.product_id = p.id
                WHERE DATE(s.sold_at) = current_date()
                GROUP BY p.id, p.name
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 조인 안할때는 builder().build() 했는데 조인하니까 필드 못씀
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    salesList.add(Sales.builder()
                            .productName(rs.getString("product_name"))
                            .quantity(rs.getInt("total_quantity"))
                            .totalPrice(rs.getBigDecimal("total_price"))
                            .build());
                }
            }
        }
        return salesList;
    }

    public static void main(String[] args)  {
        SalesDAO salesDAO = new SalesDAO();
        try {
            System.out.println(salesDAO.findTodaySales());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
