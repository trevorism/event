Feature: Context Root of this API
  In order to use the API, it must be available

  Scenario: HTTP GET on the ContextRoot
    Given the application is alive
    When I navigate to "https://event.data.trevorism.com"
    Then then a link to the help page is displayed

