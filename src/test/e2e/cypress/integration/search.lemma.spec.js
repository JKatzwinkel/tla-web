describe('lemma search', () => {
  it('search by translation', () => {
    cy.viewport(1920, 1080)
    cy.visit('/search')

    // click on input field's label should set focus on input field
    cy.contains('Hieratic').click()
    cy.contains('Sub-dictionary').siblings().get(
      'input[value=hieratic]'
    ).invoke('prop', 'checked').should('eq', false)
    cy.contains('Transliteration').click()
    cy.focused().should('have.attr', 'name', 'transcription')
    cy.focused().type('nfr')

    // submit search form
    cy.contains('Search in Dictionary').click()

    // expand collapsed search result properties
    cy.get('button').contains('Hieroglyphs').click()
    cy.get('.hieroglyphs').get('svg').should('be.visible')
    cy.get('button').contains('Word class').click()
    cy.contains('adverb').should('be.visible')
    cy.get('button').contains('Word class').click()
    cy.contains('adverb').should('not.be.visible')
    cy.get('button').contains('Translations').click()
    cy.get('button').contains('German').click()
    cy.contains('Vollkommenheit').should('be.visible')

    // go to results page 4
    cy.get('ul.pagination').contains('4').click()

    // click on search result based on translation
    cy.get('button').contains('Translations').click()
    cy.get('button').contains('German').click()
    cy.contains('die Schöne').parents('.result-list-item').contains(
      'nfr.t'
    ).click()

    // should arrive on lemma details page
    cy.contains('Dictionary Entry')
    cy.contains('Digitalisiertes Zettelarchiv')
  })
})
