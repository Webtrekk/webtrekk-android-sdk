./gradlew assembleRelease
./gradlew assembleDebug

#do unit tests

 #run emulator
gnome-terminal -x sh -c "emulator -avd Nexus_S_API_21"
 #wait untill emulator started
sleep 30
 #do test
./gradlew cAT

#do test with AdvID
bash testIntallAttribution.sh yes

#do test without AdvID
bash testIntallAttribution.sh

#do errorHandlingTest
bash errorHandlingTest.sh

#do suspendTest
bash suspendTest.sh

 #extract logs
cd ..
adb logcat -d > all_log.txt
adb logcat -d WebtrekkSDK:* *:S > webtrekk_log.txt

#kill emulator
adb emu kill