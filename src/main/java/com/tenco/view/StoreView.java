package com.tenco.view;

import com.tenco.service.StoreService;

public class StoreView {
    StoreService storeService = new StoreService();




    public boolean isLogin() {
        if(storeService.getAdmin() == null) {
            System.out.println("로그인 아닌 상태입니다.");
            return true;
        }
        return false;
    }
}
