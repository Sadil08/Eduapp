package com.eduapp.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class OverrideMarkRequest {
    @NotNull(message = "Mark is required")
    private Integer mark;
    // A justification note is mandatory for a teacher mark override (audit trail).
    @NotBlank(message = "A justification note is required")
    private String note;

    public Integer getMark() { return mark; }
    public void setMark(Integer mark) { this.mark = mark; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
