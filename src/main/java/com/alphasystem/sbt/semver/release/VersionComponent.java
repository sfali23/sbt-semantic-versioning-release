package com.alphasystem.sbt.semver.release;

import java.util.Map;

public enum VersionComponent {
    NONE(0),
    MAJOR(2),
    MINOR(4),
    PATCH(8),
    HOT_FIX(16),
    NEW_PRE_RELEASE(32),
    PRE_RELEASE(64),
    PROMOTE_TO_RELEASE(128),
    SNAPSHOT(256);

    private static final Map<Integer, VersionComponent> indexToComponent = Map.of(
            NONE.index, VersionComponent.NONE,
            MAJOR.index, VersionComponent.MAJOR,
            MINOR.index, VersionComponent.MINOR,
            PATCH.index, VersionComponent.PATCH,
            HOT_FIX.index, VersionComponent.HOT_FIX,
            NEW_PRE_RELEASE.index, VersionComponent.NEW_PRE_RELEASE,
            PRE_RELEASE.index, VersionComponent.PRE_RELEASE,
            PROMOTE_TO_RELEASE.index, VersionComponent.PROMOTE_TO_RELEASE,
            SNAPSHOT.index, VersionComponent.SNAPSHOT);

    private final int index;

    VersionComponent(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public static VersionComponent fromIndex(int index) {
        return indexToComponent.get(index);
    }
}
