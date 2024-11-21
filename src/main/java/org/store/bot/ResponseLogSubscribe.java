package org.store.bot;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ResponseLogSubscribe {
    private String signature;
    private boolean isSuccess;
    private List<String> logs;

    public ResponseLogSubscribe(String signature, boolean isSuccess, List<String> logs) {
        this.signature = signature;
        this.isSuccess = isSuccess;
        this.logs = logs;
    }
}
