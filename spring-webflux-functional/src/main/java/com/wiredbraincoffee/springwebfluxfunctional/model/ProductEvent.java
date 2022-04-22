package com.wiredbraincoffee.springwebfluxfunctional.model;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class ProductEvent {
    private Long eventId;
    private String eventType;
}
