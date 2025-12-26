package com.enjine.enderpearlbackport.common.data;

import java.util.UUID;

public record EnderpearlRecord(
        UUID pearlId,
        String dimensionId,
        double x, double y, double z,
        double vx, double vy, double vz
) {
}
