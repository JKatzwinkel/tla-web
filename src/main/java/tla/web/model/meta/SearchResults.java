package tla.web.model.meta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import tla.domain.command.SearchCommand;
import tla.domain.dto.extern.PageInfo;
import tla.domain.dto.extern.SearchResultsWrapper;
import tla.domain.dto.meta.AbstractDto;
import tla.web.model.mappings.MappingConfig;

@Getter
@NoArgsConstructor
public class SearchResults extends ObjectsContainer {

    private List<TLAObject> objects;

    private SearchCommand<? extends AbstractDto> query;

    private PageInfo page;

    private Map<String, Map<String, Long>> facets;

    public SearchResults(SearchResultsWrapper<?> dto) {
        super(dto);
        this.objects = this.importDTOSearchResults(dto.getResults());
        this.query = dto.getQuery();
        this.page = dto.getPage();
        this.facets = dto.getFacets();
    }

    /**
     * Convert list of DTO instances to their respective UI model counterparts.
     */
    protected List<TLAObject> importDTOSearchResults(Collection<? extends AbstractDto> dtos) {
        if (dtos != null) {
            return dtos.stream().map(
                d -> MappingConfig.convertDTO(d)
            ).toList();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Converts search results in a paged DTO to their respective frontend model
     * types.
     *
     * @param dto Search result page DTO.
     * @return Search result page
     */
    public static SearchResults from(SearchResultsWrapper<? extends AbstractDto> dto) {
        return new SearchResults(dto);
    }

}