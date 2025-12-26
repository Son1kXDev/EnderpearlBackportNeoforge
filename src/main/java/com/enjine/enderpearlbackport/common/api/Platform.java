package com.enjine.enderpearlbackport.common.api;

public final class Platform {

    private static PlatformAdapter adapter;

    private Platform() {
    }

    public static void init(PlatformAdapter platformAdapter) {
        adapter = platformAdapter;
    }

    public static PlatformAdapter adapter() {
        if (adapter == null) {
            throw new IllegalStateException("PlatformAdapter not initialized");
        }
        return adapter;
    }
}
