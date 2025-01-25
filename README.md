# Kotlin/Spring Boot backend for the Quote app
This application serves as the backend for the React frontend, which is available at:
https://github.com/edwinbulter/quote-web

When launched, the API can be tested in IntelliJ using the file quote_api_test.http, located at
https://github.com/edwinbulter/quote-kotlin/tree/main/src/test/kotlin

Implemented features:
- When a random quote is requested and only previously sent quotes for the client exist in the database, A set of quotes will be requested at ZenQuotes and written in the H2DB database.
- Only unique quotes are written to the database:
    - by looking at the quoteText and author, quotes are compared
    - if the new quoteText/author combination doesn't appear in the database, it is added
- When requesting a random quote, 'quote ids to exclude' can be sent in the body of the POST request to avoid sending the same quote again when requesting a random quote
- Liking of quotes
    - Liked quotes will be written on an event stream
    - Liked quotes will get their likes field incremented
