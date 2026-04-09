package com.tenco.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Admin {

    private int id;
    private String adminId;
    private String password;
    private String name;

}
