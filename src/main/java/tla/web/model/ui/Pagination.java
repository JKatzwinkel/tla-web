package tla.web.model.ui;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import lombok.Getter;
import tla.domain.dto.extern.PageInfo;

/**
 * For display of pagination widget in UI.
 */
@Getter
public class Pagination {

    public static final int OMISSION_THRESHOLD = 2;

    private long fromResult;
    private long toResult;
    private long totalResults;

    private List<Integer> pages;
    private int currentPage;
    private int lastPage;

    /**
     * Takes the pagination info from DTO and turns it into something we can
     * render more easily.
     */
    public Pagination(PageInfo dtoPage) {
        if (dtoPage != null) {
            this.currentPage = dtoPage.getNumber() + 1;
            this.totalResults = dtoPage.getTotalElements();
            this.fromResult = (this.currentPage - 1l) * dtoPage.getSize() + 1;
            this.toResult = (this.currentPage - 1l) * dtoPage.getSize() + dtoPage.getNumberOfElements();
            this.lastPage = Math.max(dtoPage.getTotalPages(), 1);
            this.pages = this.makePageLinks();
        } else {
            this.currentPage = 1;
            this.pages = List.of(this.currentPage);
        }
    }

    /**
     * Returns distance to first, last, or current page, depending on what is
     * closest.
     * @param page
     * @return
     */
    protected int distanceToRequiredPage(int page) {
        return List.of(
            1,
            this.currentPage,
            this.lastPage
        ).stream().map(
            requiredPage -> Math.abs(requiredPage - page)
        ).reduce(
            Math::min
        ).orElseThrow();
    }

    /**
     * Creates a sequence of <code>Integer</code> values representing the pages
     * covered by this pagination object. <code>null</code> values represent
     * ellipsis placeholders (n pages omitted).
     */
    protected List<Integer> makePageLinks() {
        List<Integer> included = new LinkedList<>();
        List<Integer> omitted = new LinkedList<>();
        IntStream.rangeClosed(1, this.lastPage).forEach(
            page-> {
                if (this.distanceToRequiredPage(page) > OMISSION_THRESHOLD) {
                    omitted.add(page);
                } else {
                    if (omitted.size() > 1) {
                        included.add(null);
                    } else {
                        included.addAll(omitted);
                    }
                    omitted.clear();
                    included.add(page);
                }
            }
        );
        return included;
    }

}