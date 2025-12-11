package com.ecs160.hw.util;

import java.util.List;

public class SelectedRepoData {
    public final int repoId;
    public final List<String> files;

    public SelectedRepoData(int repoId, List<String> files) {
        this.repoId = repoId;
        this.files = files;
    }
}