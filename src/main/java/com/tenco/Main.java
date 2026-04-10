package com.tenco;

import com.tenco.dao.SalesDAO;
import com.tenco.view.StoreView;

import java.sql.SQLException;

//TIP 코드를 <b>실행</b>하려면 <shortcut actionId="Run"/>을(를) 누르거나
// 에디터 여백에 있는 <icon src="AllIcons.Actions.Execute"/> 아이콘을 클릭하세요.
public class Main {
    public static void main(String[] args) {

        SalesDAO salesDAO = new SalesDAO();
        try {
            System.out.println(salesDAO.findTodaySales().toString());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        // StoreView 실행 → 프로그램 시작
        System.out.println("111111111111");
        StoreView view = new StoreView();

        // try-finally 패턴:
        // try 블록에서 정상 종료되든, 예외가 발생하든
        // finally 블록은 반드시 실행된다.
//        try {
//            view.run();
//        } finally {
//            // 프로그램 종료 시 커넥션 풀 자원 해제
//            // 호출하지 않으면 JVM 종료 후에도 커넥션이 남아 있을 수 있음
//            DBConnectionManager.close();
//        }
//    }
// }
    }
}