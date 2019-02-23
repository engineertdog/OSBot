# OSBot Scripts
The following repo contains scripts from ~ 3 years ago that were built with a OSBot's Java API to perform automated tasks within a game. Most scripits share a common service to interact with the PHP Server API which has also been included in this repo. All access keys and passwords have been removed, although the server no longer exists.

# PHP Server API
The API Server, built in PHP, listens for requests from Java clients that run the scripts. Only requests with valid keys and auth tokens may push data to the server. The keys are created using openssl random pseudo bytes to create a random string which is then encoded in base 64, twice. The server also keeps track of online users, which is updated by the Java clients every 2 minutes. The API Server also has a landing page that shows statistics for all of the script data uploaded to the server.

# Info
The scripts are at least 3 years old and most likely use out dated API methods. These scripts are to be considered non-functioning, but they serve as a means to showcase the usage of Java and APIs within Java and well as some PHP knowledge.