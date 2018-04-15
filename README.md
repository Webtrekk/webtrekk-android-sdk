# Webtrekk Android SDK

The Webtrekk SDK allows you to track user activities, screen flow and media usage for an App. All data is send to the Webtrekk tracking system for further analysis.

## Getting Started

1. Clone source code and open project in Android Studio
1. Compile and run.

sdk_test is test application, webtrekk_sdk is library.

## Deployment

All documentation about deployment you can find at Webtrekk Support Center in [Android Tracking](https://support.webtrekk.com/hc/en-us/articles/115001508189-Android-Tracking) section


## Unit tests

For unit test you can start Instrumentation unit tests for SDKTest project.

The follow tests can help you understand in details how SDK works:

1. AdClearidTest - functionality connected with AdClearID support
1. CDBUnitTest - CDB tracking
1. ErrorHandlerTest - Exception tracking.
1. RecommendationsTest - providing recommendations.


## Library support

Minimum Android SDK version support is 14.
Webtrekk SDK has optional dependency from Google Play API that activate functionality connected with Advertised ID.

## Versioning

We use [SemVer](http://semver.org/) for versioning. After each release tag with version number is created.

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE.md) file for details


