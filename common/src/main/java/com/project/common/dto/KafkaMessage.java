package com.project.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class KafkaMessage<T> {
    private String type;
    private T data;

    public KafkaMessage(String type, T data) {
        this.type = type;
        this.data = data;
    }
}
