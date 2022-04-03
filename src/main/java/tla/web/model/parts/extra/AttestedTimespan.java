package tla.web.model.parts.extra;

public class AttestedTimespan extends tla.domain.model.extern.AttestedTimespan {

    public static AttestedTimespan of(tla.domain.model.extern.AttestedTimespan dto) {
        AttestedTimespan o = new AttestedTimespan();
        o.setAttestations(dto.getAttestations());
        o.setPeriod(dto.getPeriod());
        o.setContains(dto.getContains());
        return o;
    }

    /**
     * compute sum of attestation counts of this attested timespan and all its decendants.
     */
    public AttestationStats getTotal() {
        AttestationStats stats = this.getContains().stream().collect(
            AttestationStats::new,
            (result, timespan) -> result.add(
                AttestedTimespan.of(timespan).getTotal()
            ),
            AttestationStats::add
        );
        return stats.add(this.getAttestations());
    }

}
