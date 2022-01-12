describe('language switcher', () => {
  it('language switching preserved via session cookie', () => {
    cy.viewport(1920, 1080)
    cy.visit('/search')
    cy.contains('Look up Lemma')

    cy.get('.language.breadcrumb').contains('de').click()
    cy.contains('Lemma nachschlagen')

    cy.get('.language.breadcrumb').contains('en').click()
    cy.contains('Look up Lemma')

    cy.get('a').contains('Home').click()
    cy.get('nav .breadcrumb').contains('Home')
  })
})
