package com.tenco.service;

import com.tenco.dao.AdminDAO;
import com.tenco.dao.ProductDAO;
import com.tenco.dao.SalesDAO;
import com.tenco.dto.Admin;
import com.tenco.dto.Product;
import com.tenco.dto.Sales;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service 계층의 역할
 * <p>
 * 1. 서비스는 비즈니스 로직(업무 규칙)을 담당하는 중간 관리자이다.
 * - 서비스는 뷰에서는 받은 요청을 검증하고, 필요한 DAO를 호출하는 결과를 돌려준다.
 * - 재고보다 많이 팔 수 없다는 규칙은 서비스에서 검증한다.
 */

@Getter
public class StoreService {

    AdminDAO adminDAO = new AdminDAO();
    ProductDAO productDAO = new ProductDAO();
    SalesDAO salesDAO = new SalesDAO();

    Admin admin = null;

    // 서비스에서는 단순 작업도 있음 (단순 DAO 위임)
    // 즉, 단순 조회는 DAO 메서드를 바로 호출하여 반환한다.
    // 별도 검증이 필요 없는 경우들
    public List<Product> getProductList() throws SQLException {
        return productDAO.findAll();
    }


    // 판매 처리는 서비스에 역할을 가장 잘 보여준다.
    // 검증 -> 쿼리 실행 -> 결과 반환 (최소 3단계)
    // 1. 검증 : 상품이 실제 존재하는지 확인(SELECT) -> ProductDAO.findByBarcode()에 위임
    //          - 상품 확인, 재고 확인
    // 2. 실행 : SaleDAO.processSale() --> 내부 트랜잭션 처리 완료
    // 3. 결과 반환 : 결과에 따른 메세지 가공해서 뷰로 전달

    public String processSale(String barcode, int quantity) throws SQLException {

        // 1단계 : 상품 존재 여부 확인
        // 뽑은 Product 객체로 2번 확인 가능

        Product product = productDAO.findByBarcode(barcode); // Product 타입으로 리턴
        if (product == null) {
            return "[ERROR] 해당 바코드의 상품이 없습니다.";
        }

        // 2단계 : 재고 충분 여부 확인 (비즈니스)
        if (product.getStock() < quantity) {
            return String.format("[ERROR] 재고 부족. 현재 재고 : %d 개", product.getStock());
        }

        // 3단계 : DAO 판매 실행 위임
        // 트랜잭션 여부에 따라 성공 실패 처리
        boolean success = salesDAO.processSale(product, quantity);
        if (!success) {
            return "[ERROR] 판매 처리 중 오류 발생";
        }

        // 4단계 : 성공 메세지 생성해서 리턴
        return String.format("[OK] %s %d 개 판매 완료. 합계 : %s원",
                product.getName(), quantity, product.getPrice().multiply(BigDecimal.valueOf(quantity)));
    }

    // 비즈니스 판단 메서드 - 기준은 서비스가 정한다.
    // 재고 부족 판단에 상품이다.
    public boolean isLowStock(Product product) {
        return product.getStock() <= product.getMinStock();
    }

    public boolean login(String adminId, String password) throws SQLException {
        if (adminId == null || adminId.trim().isEmpty()) {
            throw new SQLException("관리자 ID를 입력하세요");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new SQLException("관리자 password를 입력하세요");
        }

        admin = adminDAO.login(adminId, password);
        if (admin != null) {

            return true;
        } else {
            throw new SQLException("정확한 정보 입력");
            // 여기 파트는 view 파트인가?
        }
    }

    public void logout() throws RuntimeException {
        if (admin == null) {
            throw new RuntimeException("이미 로그아웃 상태입니다");
        } else {
            admin = null;
        }

    }

    public boolean isLoggedIn() {
        if (admin != null) {
            return true;
        } else {
            return false;
        }
    }


    public boolean isNearExpiry(Product product) {
        if (product.getExpireDate() == null) return false;
        return !product.getExpireDate().isAfter(java.time.LocalDate.now().plusDays(3));
    }

    public List<Sales> getTodaySales() throws SQLException {
        return salesDAO.findTodaySales();
    }
}
