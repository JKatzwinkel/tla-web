package tla.web.model.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.Getter;
import tla.web.model.parts.extra.AttestedTimespan;

@Getter
public class AttestationTimeline {

    public static class QuartileFinder {
        private long counter = 0;
        private LinkedList<Double> targets = new LinkedList<>();
        @Getter
        private List<Integer> quartiles = new ArrayList<>();
        private Integer quartile = null;

        public QuartileFinder(List<AttestedTimespan> attestations) {
            var total = computeTotal(attestations);
            List.of(.25, .5, .75).stream().forEach(
                ratio -> this.targets.add(ratio * total)
            );
            var attestedTimespans = new ArrayList<AttestedTimespan>();
            attestedTimespans.addAll(attestations);
            Collections.sort(
                attestedTimespans, Comparator.comparing(AttestedTimespan::getPeriod)
            );
            attestedTimespans.forEach(this::count);
        }

        public static List<Integer> find(List<AttestedTimespan> attestations) {
            return new QuartileFinder(attestations).getQuartiles();
        }

        private void count(tla.domain.model.extern.AttestedTimespan attestedTimespan) {
            var children = new ArrayList<tla.domain.model.extern.AttestedTimespan>();
            children.addAll(attestedTimespan.getContains());
            Collections.sort(
                children,
                Comparator.comparing(tla.domain.model.extern.AttestedTimespan::getPeriod)
            );
            children.forEach(this::count);
            var end = attestedTimespan.getPeriod().getEnd();
            var count = attestedTimespan.getAttestations().getCount();
            this.counter += count;
            this.quartile = end;
            while (!this.targets.isEmpty() && this.counter >= this.targets.getFirst()) {
                this.quartiles.add(this.quartile);
                this.targets.removeFirst();
                if (this.targets.isEmpty()) {
                    return;
                }
            }
        }
    }

    public final static int FROM = -3500;
    public final static int TO = 600;
    /**
     * where the tics are to be drawn on the x-axis
     */
    public final static List<Integer> XTICS = List.of(100, 500, 1000, 2000);

    private List<Rect> rectangles;
    private long totalCount;

    private tla.domain.model.extern.AttestedTimespan.Period totalTimespan;

    /**
     * x-axis tics
     */
    private List<Tic> tics;
    private List<Tic> quartiles;

    public AttestationTimeline(List<AttestedTimespan> attestations) {
        this.totalCount = computeTotal(attestations);
        this.totalTimespan = tla.domain.model.extern.AttestedTimespan.Period.builder().begin(
            attestations.stream().mapToInt(
                attestation -> attestation.getPeriod().getBegin()
            ).min().orElse(0)
        ).end(
            attestations.stream().mapToInt(
                attestation -> attestation.getPeriod().getEnd()
            ).max().orElse(0)
        ).build();
        this.rectangles = attestations.stream().flatMap(
            attestation -> this.renderAttestedTimespan(attestation)
        ).toList();
        Collections.sort(this.rectangles);
        this.tics = this.createXTics();
        this.quartiles = this.createMarks(attestations);
    }

    public static long computeTotal(List<AttestedTimespan> attestations) {
        return attestations.stream().mapToLong(
            attestation -> attestation.getTotal().getCount()
        ).sum();
    }

    private int getHighestTicInterval(int year) {
        for (int i=XTICS.size()-1; i>=1; i--) {
            if (year % XTICS.get(i) == 0) {
                return i;
            }
        }
        return 0;
    }

    protected List<Tic> createXTics() {
        List<Tic> tics = new LinkedList<>();
        var s = XTICS.get(0);
        var start = FROM / s * s + 2 * s;
        var end = TO / s * s - 2 * s;
        for (int year=start; year <= end; year += XTICS.get(0)) {
            int i = getHighestTicInterval(year);
            tics.add(new Tic(year, i));
        }
        return tics;
    }

    protected List<Tic> createMarks(List<AttestedTimespan> attestations) {
        if (this.totalCount > 0) {
            return QuartileFinder.find(attestations).stream().map(
                quartile -> Tic.of(quartile, 6)
            ).toList();
        }
        return null;
    }

    public static AttestationTimeline from(List<AttestedTimespan> attestations) {
        return new AttestationTimeline(attestations);
    }

    protected Stream<Rect> renderAttestedTimespan(tla.domain.model.extern.AttestedTimespan attestation) {
        return Stream.concat(
            List.of(
                new Rect(attestation, 0, a -> a.getAttestations().getCount()),
                new Rect(attestation, 30, a -> AttestedTimespan.of(a).getTotal().getCount())
            ).stream(),
            attestation.getContains().stream().flatMap(
                child -> renderAttestedTimespan(child)
            )
        );
    }

    /**
     * project given year onto timeline, which is defined by {@link FROM} and {@link TO},
     * and return the result as a percentage indicating where to place that year from left
     * to right.
     */
    protected static double projectDate(int year) {
        return 100f * (year - FROM) / (TO - FROM);
    }

    @Getter
    public class Rect implements Comparable<Rect> {

        private double x;
        private double y;
        private double w;
        private double h;
        private double alpha;
        private long count;
        private tla.domain.model.extern.AttestedTimespan.Period period;

        public Rect(
            tla.domain.model.extern.AttestedTimespan attestation, double y,
            Function<tla.domain.model.extern.AttestedTimespan, Long> countFunc
        ) {
            this.period = attestation.getPeriod();
            var left = projectDate(attestation.getPeriod().getBegin());
            var right = projectDate(attestation.getPeriod().getEnd());
            this.alpha = .2f + .8f * countFunc.apply(attestation) / getTotalCount();
            this.count = attestation.getAttestations().getCount();
            this.x = left;
            this.w = right - left;
            this.y = y;
            this.h = 30f;
        }

        @Override
        public int compareTo(Rect o) {
            return Integer.compare((int) this.x, (int) o.x);
        }
    }

    @Getter
    public static class Tic {
        private double x;
        /** tic length */
        private double l;
        /** tic stroke width */
        private double w;
        private int level;
        private String label;

        public Tic(int year, int level) {
            this.level = level;
            this.x = projectDate(year);
            this.l = 6 + (int) Math.pow(1.6, level + 1) * 4;
            this.w = 0.25 + Math.pow(1.9, level) / 8;
            this.label = String.format("%d", year);
        }

        static Tic of(Integer year, int level) {
            if (year != null) {
                return new Tic(year, level);
            }
            return null;
        }

    }
}
