package com.tenco.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Sales {

    private int id;
    private int productId;
    private int quantity;
    private BigDecimal unitPrice;
    private LocalTime soldAt;

}
