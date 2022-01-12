describe('search page', () => {

  it('renders', () => {
    cy.viewport(1920, 1080)
    cy.visit('/search')
    cy.scrollTo('top')

    // test language switcher
    cy.get('.language.breadcrumb > .lang-de').click()
    cy.get('#toggle-lemma-search-form-button').should('contain', 'Wörterbuch')

    cy.get('.language.breadcrumb > .lang-en').click()
    cy.get('#toggle-lemma-search-form-button').should('contain', 'Dictionary')

    // test search form expansion/collapse
    cy.get('#toggle-lemma-quick-search-form-button').should('be.visible')
    cy.contains('Look up Lemma').click()
    cy.contains('Dictionary entries').click()
  })

  it('forms collapse && expand', () => {
    cy.viewport(1920, 1080)
    cy.visit('/search')

    // dictionary search form expanded by default
    cy.contains('Sub-dictionary').should('be.visible')
    cy.contains('Lemma ID').should('not.be.visible')
    cy.contains('Hieroglyphs').should('not.be.visible')

    // expand lemma ID search form
    cy.contains('Look up Lemma').click()
    cy.contains('Sub-dictionary').should('not.be.visible')
    cy.contains('Lemma ID').should('be.visible')

    // collapse lemma ID search form
    cy.contains('Look up Lemma').click()
    cy.contains('Sub-dictionary').should('not.be.visible')
    cy.contains('Lemma ID').should('not.be.visible')
  })

})
