# Source code

In order to compile or manipulate code it's necessary to have openCV library 2.3.1 for android and Android SDK components API 8, 10, 12.

### How to setup development environment and compile

You can use eclipse with ADT plugin (the same I used for the development). To download and properly set environment follow this guide http://opencv.itseez.com/trunk/doc/tutorials/introduction/android_binary_package/android_binary_package.html (2018 note: I will provide an up-to-date link soon).
Once completed, import camera_assistant project to workspace and import openCV library (both must be in the workspace directory), add a reference to openCV (right-click on camera_assistant in Package Explorer, then Properties --> Android --> Add library --> select openCV-2.3.1.jar and Apply). Should eclipse signal errors, press F5 to rebuild workspace, plug your device with USB debug setting on (it's not possible to emulate camera with AVD, you need an android phone) and click on "run as Android application".

You can find a compiled binary targeting Android 2.3+in the /bin folder. It's necessary to turn on "Unknown origin applications" under Settings --> Applications on your smartphone, then double click on .apk file to install using a file manager.