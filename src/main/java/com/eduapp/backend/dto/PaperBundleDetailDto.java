package com.eduapp.backend.dto;

import java.util.List;

public class PaperBundleDetailDto extends PaperBundleSummaryDto {
    private List<PaperSummaryDto> papers;

    public PaperBundleDetailDto() {
        super();
    }

    public List<PaperSummaryDto> getPapers() { return papers; }
    public void setPapers(List<PaperSummaryDto> papers) { this.papers = papers; }
}
