package tla.web.model.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Simple single-purpose POJO for initializing search form collapsible
 * expansion states (which search form currently has the focus)
 */
@Getter
@AllArgsConstructor
public class SearchFormExpansionState {
    private String key;
    private boolean expanded;
}