package io.jettra.pd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SequenceService {
    private static final Logger LOG = Logger.getLogger(SequenceService.class);

    private final Map<String, SequenceMetadata> sequences = new ConcurrentHashMap<>();

    public void createSequence(String name, String database, long startValue, long increment) {
        LOG.infof("Creating sequence: %s in database: %s (Start: %d, Increment: %d)", name, database, startValue,
                increment);
        sequences.put(name, new SequenceMetadata(name, database, startValue, increment));
    }

    public synchronized Long nextValue(String name) {
        SequenceMetadata seq = sequences.get(name);
        if (seq == null) {
            return null;
        }
        long nextVal = seq.currentValue() + seq.increment();
        sequences.put(name, new SequenceMetadata(name, seq.database(), nextVal, seq.increment()));
        return nextVal;
    }

    public Long currentValue(String name) {
        SequenceMetadata seq = sequences.get(name);
        return (seq != null) ? seq.currentValue() : null;
    }

    public boolean resetSequence(String name, long newValue) {
        SequenceMetadata seq = sequences.get(name);
        if (seq != null) {
            sequences.put(name, new SequenceMetadata(name, seq.database(), newValue, seq.increment()));
            return true;
        }
        return false;
    }

    public boolean deleteSequence(String name) {
        return sequences.remove(name) != null;
    }

    public List<SequenceMetadata> listSequences(String database) {
        if (database == null || database.isEmpty()) {
            return new ArrayList<>(sequences.values());
        }
        return sequences.values().stream()
                .filter(s -> database.equals(s.database()))
                .toList();
    }
}
