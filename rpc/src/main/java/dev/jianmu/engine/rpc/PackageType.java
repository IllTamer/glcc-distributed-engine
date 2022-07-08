package dev.jianmu.engine.rpc;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PackageType {

    REQUEST_PACK(0),

    RESPONSE_PACK(1);

    private final int code;

}