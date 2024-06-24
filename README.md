# event
![Build](https://github.com/trevorism/event/actions/workflows/deploy.yml/badge.svg)
![GitHub last commit](https://img.shields.io/github/last-commit/trevorism/event)
![GitHub language count](https://img.shields.io/github/languages/count/trevorism/event)
![GitHub top language](https://img.shields.io/github/languages/top/trevorism/event)

API enables events. Send events, register topics and subscriptions for publishing and subscribing to events.

Authentication is optional to send events. Authenticated events will be passed through to topic subscribers.

Events may be correlated by adding an `X-Correlation-ID` header on the event request.

# How to build
`gradle clean build`

