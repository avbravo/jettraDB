package io.jettra.pd;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SequenceServiceTest {

    SequenceService sequenceService;

    @BeforeEach
    public void setup() {
        sequenceService = new SequenceService();
    }

    @Test
    public void testSequenceLifecycle() {
        String db = "test_db";
        String name = "test_seq";

        // 1. Create
        sequenceService.createSequence(name, db, 10, 2);

        // 2. Current
        Assertions.assertEquals(10L, sequenceService.currentValue(name));

        // 3. Next
        Assertions.assertEquals(12L, sequenceService.nextValue(name));
        Assertions.assertEquals(14L, sequenceService.nextValue(name));

        // 4. List
        List<SequenceMetadata> list = sequenceService.listSequences(db);
        Assertions.assertFalse(list.isEmpty());
        Assertions.assertEquals(name, list.get(0).name());

        // 5. Reset
        sequenceService.resetSequence(name, 100);
        Assertions.assertEquals(100L, sequenceService.currentValue(name));

        // 6. Delete
        sequenceService.deleteSequence(name);
        Assertions.assertNull(sequenceService.currentValue(name));
    }
}
