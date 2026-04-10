package com.tenco.dao;

import com.tenco.dto.Product;
import com.tenco.util.DBConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    // 테스트 코드
    public static void main(String[] args) throws SQLException {
        ProductDAO productDAO = new ProductDAO();
        System.out.println(productDAO.findAll());
    }

    // ### 1단계 - 상품 전체 목록 조회 (findAll)
    public List<Product> findAll() throws SQLException {
        List<Product> productList = new ArrayList<>();
        String sql = """
                SELECT * FROM product WHERE is_active = TRUE
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                productList.add(mapToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return productList;
    }


    // ### 2단계 - 바코드로 상품 조회 (findByBarcode)
    public Product findByBarcode(String barcode) throws SQLException {
        String sql = """
                SELECT * FROM product WHERE barcode = ?
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, barcode);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapToProduct(rs);
                }
            }

        }
        return null;

    }


    // ### 3단계 - 상품 등록 (insert)
    public boolean insert(Product product) throws SQLException {
        String sql = """
                INSERT INTO product(barcode, name, category, price, cost, stock, min_stock)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, product.getBarcode());
            pstmt.setString(2, product.getName());
            pstmt.setString(3, product.getCategory());
            pstmt.setBigDecimal(4, product.getPrice());
            pstmt.setBigDecimal(5, product.getCost());
            pstmt.setInt(6, product.getStock());
            pstmt.setInt(7, product.getMinStock());

            int countRow = pstmt.executeUpdate();

            return countRow > 0;

        } catch (Exception e) {
            throw new RuntimeException("오류");
        }
    }


    // ## 4단계 - 상품 수정 (update)
    public boolean update(Product product) throws SQLException {
        String sql = """
                UPDATE product SET price = ?, stock = ?, expire_date = ? WHERE id = ?
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBigDecimal(1, product.getPrice());
            pstmt.setInt(2, product.getStock());
            pstmt.setDate(3, Date.valueOf(product.getExpireDate()));
            pstmt.setInt(4, product.getId());

            int countRow = pstmt.executeUpdate();
            return countRow > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    // ## 5단계 - 소프트 삭제 (delete)
    //
    public boolean softDelete(int id) {
        String sql = """
                UPDATE product SET is_active = FALSE WHERE id = ?
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            int countRow = pstmt.executeUpdate();

            return countRow > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    // ## 6단계 - 재고 부족 상품 조회 (findLowStock)
    public List<Product> findLowStock() throws SQLException {
        List<Product> productList = new ArrayList<>();
        String sql = """
                SELECT * FROM product WHERE stock < min_stock
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                productList.add(mapToProduct(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return productList;
    }


    private Product mapToProduct(ResultSet rs) throws SQLException {
        return Product.builder()
                .id(rs.getInt("id"))
                .barcode(rs.getString("barcode"))
                .name(rs.getString("name"))
                .category(rs.getString("category"))
                .price(rs.getBigDecimal("price"))
                .cost(rs.getBigDecimal("cost"))
                .stock(rs.getInt("stock"))
                .minStock(rs.getInt("min_stock"))
                .expireDate(rs.getDate("expire_date").toLocalDate())
                .isActive(rs.getBoolean("is_active"))
                .build();
    }

}
