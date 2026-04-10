package com.tenco.dao;

import com.tenco.dto.Admin;
import com.tenco.util.DBConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

// DAO 는 SQL을 실행하는 녀석이다 .그 이상 이하도 아니다

/**
 * 이 메서드가 화면에 어떻게 보일지? -> 내 관심사 아님(view의 역할)
 * 실패하면 어떤 메세지를 보여줄지? -> 내 관심사 아님(Service의 역할)
 * 즉, 나는 오직 쿼리 실행 -> 결과 반환 끝
 * <p>
 * 메서드 작성 순서 - 이것만 따라가면 JDBC API 규칙을 지켜서 코딩하게됨
 * 1. 쿼리 결정 -> 어떤 SQL인지 생각하는 과정(SELECT, INSERT, UPDATE, DELETE)
 * 2. 커넥션 -> DBConnectionManager.getConnection() 호출
 * 3. 쿼리 실행 -> PreparedStatement 의 ?, ? 바인딩 처리
 * 4. 결과 매핑 -> SELECT -> ResultSet 반환, Insert, Update, Delete -> int rows (affected) 반환
 * 5. 리턴 타입 -> DTO, LIST, boolean, null 중 하나 선택
 * 6. 트랜잭션 -> SQL이 2개 이상이면 묶여야하고, 1개면 불필요
 */
public class AdminDAO {

    //사고 흐름 - 관리자 로그인 처리
    // 1. 쿼리 결정 -> id, pw, 쿼리에 던져서 일치하는 행을 조회해야함 -> SELECT
    // 2. 커넥션 객체 가져오기 (외부 자원을 열어두면 메모리 누수 close(), try re...
    // 3. 쿼ㅣ 생성 및 요청 객체 만들기
    //          -Pstmt 결정, ?, ? 바인딩 처리
    //          -executeQuery()
    // 4. 결과 집합을 DTO 에 담기
    //      -rs.next() 각 true -> 일치하는 행이 존재함
    // 5. 리턴 결과 결졍 -> 성공 : rs에서 컬럼값을 꺼내서 Admin 객체에 담기
    //                      실패 : null 반환
    //  6. 트랜잭션 결정 -> 트랜잭션 필요없음
    public Admin login(String adminId, String password) {
        String sql = """
                SELECT * FROM admins WHERE admin_id = ? AND password = ?
                """;

        try (Connection conn = DBConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, adminId);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                // 다중행, 단일행이기 때문에 while  사용할 필요 x
                // 1 row 나오거나
                if (rs.next()) {

                    Admin admin = new Admin();
                    admin.setId(rs.getInt("id"));
                    admin.setAdminId(rs.getString("admin_id"));
                    admin.setName(rs.getString("name"));
                    return admin;
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // 테스트 코드 작성
    public static void main(String[] args) {
        // 예외 클래스는 두 가지 종류로도 구분할 수 있다.
        // checked exception
        // unchecked exception

        // RuntimeException 은 unchecked exception 이다.
        // 즉 사용하는 입장에선 try 구문을 필요하면 사용해도 되고 안해도됨

        // SQLException 은 checked exception 이다.
        // 강제적으로 처리해야하는 강제성이 생김

        try {
            AdminDAO adminDAO = new AdminDAO();
            Admin admin = adminDAO.login("admin", "admin123");
            System.out.println(admin.toString());
        } catch (Exception e) {
            // throw new RuntimeException(e);
            System.out.println("오류 발생!");
        }
        System.out.println("프로그램 정상 종료!");
    }
}
