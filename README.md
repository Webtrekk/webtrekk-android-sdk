# Webtrekk Test

I have changed RequestProcessor which handle the tracking request using rxandroid.
And there is change in RequestFactory to handle when to call RequestProcessor.

## Reason

Rxandroid make it easier to deal with Multi-threading.
During there is network the user actions are cached when there is network available all those actions are sent to the server.
So we have to do this away from MainThread to avoid heavy load on it which may lead to app crash and avoid dealing with activity lifecycle.
