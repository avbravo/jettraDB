package io.jettra.engine.geo;

import io.jettra.engine.core.AbstractEngine;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;

@ApplicationScoped
public class GeoEngine extends AbstractEngine {

    public record GeoPoint(String id, double lat, double lon, String desc) {}

    private final List<GeoPoint> pointBuffer = new java.util.concurrent.CopyOnWriteArrayList<>();

    public Uni<Void> register(GeoPoint p) {
        return writeData(0, "geo:" + p.id(), p.lat() + "," + p.lon())
                .onItem().invoke(() -> pointBuffer.add(p));
    }

    /**
     * Optimized Nearby Search using Haversine.
     */
    public Uni<List<GeoPoint>> search(double lat, double lon, double radiusKm) {
        return Uni.createFrom().item(() -> {
            return pointBuffer.stream()
                .filter(p -> computeDistance(lat, lon, p.lat(), p.lon()) <= radiusKm)
                .toList();
        });
    }

    private double computeDistance(double la1, double lo1, double la2, double lo2) {
        double dLat = Math.toRadians(la2 - la1);
        double dLon = Math.toRadians(lo2 - lo1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(la1)) * Math.cos(Math.toRadians(la2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * 6371 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
