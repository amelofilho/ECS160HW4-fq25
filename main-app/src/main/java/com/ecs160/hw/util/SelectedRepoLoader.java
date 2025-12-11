package com.ecs160.hw.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class SelectedRepoLoader {

    public static SelectedRepoData loadSelectedRepo() {
        try (BufferedReader br = new BufferedReader(new FileReader("selected_repo.dat"))) {

            String repoIdLine = br.readLine();
            if (repoIdLine == null) {
                throw new IllegalStateException("selected_repo.dat is missing repo ID");
            }

            int repoId = Integer.parseInt(repoIdLine.trim());

            List<String> files = new ArrayList<>();
            String line;
            while ((line = br.readLine()) != null) {
                files.add(line.trim());
            }

            return new SelectedRepoData(repoId, files);

        } catch (Exception e) {
            throw new RuntimeException("Error reading selected_repo.dat", e);
        }
    }
}