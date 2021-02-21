package com.alphasystem.sbt.semver.release;

public enum VersionComponent {
    NONE(-1), PRE_RELEASE(3), PATCH(2), MINOR(1), MAJOR(0);

    private final int index;

    VersionComponent(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public boolean isMajor() {
        return index == 0;
    }

    public boolean isMinor() {
        return index == 1;
    }

    public boolean isPatch() {
        return index == 2;
    }

    public boolean isPreRelease() {
        return index == 3;
    }

    public boolean isNone() {
        return index == -1;
    }
}
