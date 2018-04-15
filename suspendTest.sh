adb shell input keyevent 82 # unlock
TEST="com.Webtrekk.SDKTest.SuspendTest#testBeforeGoBackgroundHome com.Webtrekk.SDKTest.test/android.test.InstrumentationTestRunner"
adb shell am instrument -w -e external yes -e class ${TEST} | tee adb.log  && grep -q OK adb.log || exit 1 &
cmd_pid=$!
sleep 12
adb shell input keyevent 3
sleep 3
adb shell am start -n com.Webtrekk.SDKTest/.SuspendActivity --activity-reorder-to-front
wait $cmd_pid