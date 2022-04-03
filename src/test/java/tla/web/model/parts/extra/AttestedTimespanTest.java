package tla.web.model.parts.extra;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tla.domain.model.extern.AttestedTimespan.AttestationStats;

/**
 * tests for {@link AttestedTimespan}.
 *
 * @author jkatzwinkel
 */
public class AttestedTimespanTest {

    @Test
    @DisplayName("attestation count total should compute recursively over nested timespans")
    void testAttestationsStatsTotal() throws Exception {
        var t13 = tla.domain.model.extern.AttestedTimespan.builder().attestations(
            AttestationStats.builder().count(2).texts(2).sentences(2).build()
        ).build();
        var t12 = tla.domain.model.extern.AttestedTimespan.builder().attestations(
            AttestationStats.builder().count(4).texts(4).sentences(4).build()
        ).contains(
            List.of(t13)
        ).build();
        var t11 = tla.domain.model.extern.AttestedTimespan.builder().attestations(
            AttestationStats.builder().count(8).texts(8).sentences(8).build()
        ).contains(
            List.of(t12)
        ).build();
        var t2 = tla.domain.model.extern.AttestedTimespan.builder().attestations(
            AttestationStats.builder().count(1).texts(1).sentences(1).build()
        ).build();
        AttestedTimespan t = AttestedTimespan.of(
            tla.domain.model.extern.AttestedTimespan.builder().contains(
                List.of(t11, t2)
            ).build()
        );
        AttestationStats total = t.getTotal();
        assertAll("nested attestation counts should be properly added up",
            () -> assertEquals(15, total.getCount()),
            () -> assertEquals(15, total.getTexts()),
            () -> assertEquals(15, total.getSentences())
        );
    }

}