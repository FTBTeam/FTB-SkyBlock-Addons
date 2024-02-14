package dev.ftb.ftbsba.tools.content.core;

public interface ProgressProvider {
    int getProgress();
    int getMaxProgress();

    void setProgress(int progress);

    void setMaxProgress(int maxProgress);
}
