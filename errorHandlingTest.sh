#!/usr/bin/env bash
adb shell am start -n com.Webtrekk.SDKTest/.ThrowExceptionActivity --ez NULL true
adb shell am force-stop com.Webtrekk.SDKTest
adb shell am instrument -w -e external yes -e class com.Webtrekk.SDKTest.ErrorHandlerTest#testFatalCompeteSimple com.Webtrekk.SDKTest.test/android.test.InstrumentationTestRunner

adb shell am start -n com.Webtrekk.SDKTest/.ThrowExceptionActivity --ez NULL true
adb shell am force-stop com.Webtrekk.SDKTest
adb shell am start -n com.Webtrekk.SDKTest/.ThrowExceptionActivity
adb shell am force-stop com.Webtrekk.SDKTest
adb shell am instrument -w -e external yes -e class com.Webtrekk.SDKTest.ErrorHandlerTest#testFatalCompeteComplex com.Webtrekk.SDKTest.test/android.test.InstrumentationTestRunner
