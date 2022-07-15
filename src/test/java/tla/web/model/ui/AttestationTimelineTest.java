package tla.web.model.ui;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import tla.domain.model.ObjectReference;
import tla.domain.model.extern.AttestedTimespan.AttestationStats;
import tla.domain.model.extern.AttestedTimespan.Period;
import tla.web.model.parts.extra.AttestedTimespan;
import tla.web.model.ui.AttestationTimeline.QuartileFinder;

public class AttestationTimelineTest {

    @Test
    @DisplayName("place rectangles on timeline (only root nodes)")
    void timelineConstructorRectanglePlacement() {
        var att1 = AttestedTimespan.of(AttestedTimespan.builder().period(
            Period.builder().begin(-444).end(-222).ref(
                ObjectReference.builder().eclass("BTSThsEntry").id("p1").name("ancient period").type("date").build()
            ).build()
        ).attestations(
            AttestationStats.builder().count(11).build()
        ).build());
        var att2 = AttestedTimespan.of(AttestedTimespan.builder().period(
            Period.builder().begin(66).end(140).ref(
                ObjectReference.builder().eclass("BTSThsEntry").id("p2").name("recent period").type("date").build()
            ).build()
        ).attestations(
            AttestationStats.builder().count(13).build()
        ).build());
        // render timeline
        AttestationTimeline tl = AttestationTimeline.from(
            List.of(att1, att2)
        );
        assertAll("should be able to place timeline for attested timespans",
            () -> assertEquals(4, tl.getRectangles().size(), "should have placed 2x2 rectangles"),
            () -> assertTrue(tl.getRectangles().get(2).getX() > tl.getRectangles().get(0).getX(), "more recent period to the right of older one"),
            () -> assertEquals(-444, tl.getTotalTimespan().getBegin(), "total timespan covered determined by root attestation nodes (begin)"),
            () -> assertEquals(140, tl.getTotalTimespan().getEnd(), "total timespan covered determined by root attestation nodes (end)")
        );
    }

    @Test
    @DisplayName("place rectangles on timeline, including nested nodes")
    void recursiveRectanglePlacement() {
        var att3 = AttestedTimespan.of(AttestedTimespan.builder().period(
            Period.builder().begin(72).end(90).ref(
                ObjectReference.builder().eclass("BTSThsEntry").id("p2.1").name("recent sub-period").type("date").build()
            ).build()
        ).attestations(
            AttestationStats.builder().count(5).build()
        ).build());
        var att2 = AttestedTimespan.of(AttestedTimespan.builder().period(
            Period.builder().begin(66).end(140).ref(
                ObjectReference.builder().eclass("BTSThsEntry").id("p2").name("recent period").type("date").build()
            ).build()
        ).attestations(
            AttestationStats.builder().count(13).build()
        ).contains(
            List.of(att3)).build()
        );
        // render timeline
        AttestationTimeline tl = AttestationTimeline.from(
            List.of(att2)
        );
        assertAll("should be able to place nested attestation timespans",
            () -> assertEquals(4, tl.getRectangles().size(), "should have placed 2x2 rectangles")
        );
    }

    private List<AttestedTimespan> mockAttestationTimespans(List<List<Integer>> data) {
        return data.stream().map(
            years -> AttestedTimespan.of(AttestedTimespan.builder().period(
                new Period(years.get(0), years.get(1))
            ).attestations(
                AttestationStats.builder().count(years.get(2)).build()
            ).build())
        ).toList();
    }

    @Test
    @DisplayName("should find all quartiles over attestation timespans")
    void countQuartiles() {
        var periods = mockAttestationTimespans(
            List.of(
                List.of(0, 10, 10),
                List.of(10,20, 10),
                List.of(20,30, 10),
                List.of(30,40, 10)
            )
        );
        var quartiles = QuartileFinder.find(periods);
        assertEquals(3, quartiles.size(), "should have found 3 quartiles");
    }

    @Test
    @DisplayName("should be able to compute median year if between timespans")
    void attestationTimespanMedianYear() {
        var periods = List.of(
            List.of(0, 10, 10),
            List.of(10, 20, 10),
            List.of(20, 30, 10),
            List.of(30, 40, 10)
        );
        List<AttestedTimespan> timespans = mockAttestationTimespans(periods);
        var median = QuartileFinder.find(timespans).get(1);
        assertEquals(20, median, "median should be exactly in the middle");
    }

    @Test
    @DisplayName("should be able to compute median year if inside timespan")
    void attestationTimespanMedianYear_inside() {
        List<AttestedTimespan> timespans = mockAttestationTimespans(
            List.of(
                List.of(0, 10, 10),
                List.of(10,20, 10),
                List.of(20,30, 10)
            )
        );
        var median = QuartileFinder.find(timespans).get(1);
        assertEquals(20, median, "median should be at the earliest point in time guaranteed after half of attestations");
    }

    @Test
    @DisplayName("should be able to compute median from overlapping timespans")
    void attestationTimespanMedianYear_overlap() {
        List<AttestedTimespan> timespans = mockAttestationTimespans(
            List.of(
                List.of(0, 30, 1),
                List.of(0, 10, 1),
                List.of(10,20, 1),
                List.of(20,30, 1)
            )
        );
        var median = QuartileFinder.find(timespans).get(1);
        assertEquals(20, median);
    }

    @Test
    @DisplayName("should be able to compute median from nested timespans")
    void attestationTimespanMedianYear_nested() {
        List<AttestedTimespan> roots = mockAttestationTimespans(
            List.of(
                List.of(0, 30, 3),
                List.of(30, 50, 5)
            )
        );
        roots.get(0).setContains(
            mockAttestationTimespans(
                List.of(
                    List.of(0, 10, 2),
                    List.of(10, 20, 1),
                    List.of(20, 30, 4)
                )
            ).stream().map(
                a -> (tla.domain.model.extern.AttestedTimespan) a
            ).toList()
        );
        roots.get(1).setContains(
            mockAttestationTimespans(
                List.of(
                    List.of(30, 40, 2),
                    List.of(40, 50, 6)
                )
            ).stream().map(
                a -> (tla.domain.model.extern.AttestedTimespan) a
            ).toList()
        );
        var median = QuartileFinder.find(roots).get(1);
        assertEquals(40, median);
    }

    @Test
    @DisplayName("median of only 1 single period && only 1 occurrence should be at the end of that period")
    void medianYear_singlePeriod() {
        var median = QuartileFinder.find(
            mockAttestationTimespans(
                List.of(
                    List.of(-2345, -2181, 1)
                )
            )
        ).get(1);
        assertEquals(-2181, median, "median should be placed at end of timeline");
    }

}
