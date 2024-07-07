package com.test_task.graal_wrapper.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
public class Script {
    private final String id;
    private final String scriptText;
    @Setter
    private String status;
    @Setter
    private String output;
    @Setter
    private String error;
    @Setter
    private String stdout;
    @Setter
    private String stderr;
    @Setter
    private Date startTime;
    @Setter
    private String executionTime;
    @Setter
    private String stackTrace;

    public Script(String id, String script, String status) {
        this.id = id;
        this.scriptText = script;
        this.status = status;
    }
}