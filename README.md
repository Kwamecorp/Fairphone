#Introduction

##Fairphone concept

>“FairPhone’s mission is to bring a fair smartphone to the market – one designed and produced with minimal harm to people and planet.”

This is a strong statement in our days and as such the development of this product must ensure that all is within the mind goals of the [Fairphone](http://fairphone.com) company.

The major selling point of this device is the fact that it is made with borderline no human sufering and without abusing the resources of the planet targeting a long arc of time in which the device owners should not need to replace the device for another. This means that the hardware and the software should be built thinking in long terms.

The device itself will hold an Android os and will compete in the mid level market.

##FairPhoneOS
###Android Distribution overview
The Android distribution shall be based on the Android 4.2.2 code provided by Google and updated by Mediatek.

It has some customizations that are explained below and can be seen on this [video of the FairPhoneOS](https://vimeo.com/75009732)

###HomeLauncher overview

The home launcher is the central part of the software project. The Fairphone project shall have dedicated homelauncher with some very specific characteristics:

- No Hotseat, the 5 icons on the bottom of the usual homelaunchers are missing.
- EdgeSwipe Menu, a menu that appears from the sides instead of the hotseat to show 4 apps and the all apps icon.
- Complete usage of the screen to add icons and widgets.
- Permits the usage of the so called full screen widgets that occupy the entire screen.
- A way to configure the edge swipe menu.

The major concept of the homelauncher is to give the user access to the entire device display for putting icons and/or widgets.

This made the design team think on how to show hotseat (launcher fixed apps) without giving up with the major concept and the edge swipe menu was born. The user can open the edge swipe menu by swiping from both left and right and have access to the hotseat without having it always on screen.

The homelauncher code is be based on the Launcher2 project available by Google in the AOSP code.

![Edge Swift](http://www.fairphone.com/wp-content/uploads/2013/09/EdgeSwipeMenu_02-168x300.jpg)

###App Launcher Widget overview
The app launcher widget is one of the full screen widgets that will allow the users of the device to have quick access to the last used apps and their most used apps. For this efect the widget shall have two columns of items, one for each list.

This widget was designed to give the user quick access to their usage patterns. Since the application items in the widget are inserted automatically the user's usage of the phone dictates the content of the app launcher.

The app launcher widget shall be able to reset the entire list and to call the all apps drawer on command.

![App Launcher](http://www.fairphone.com/wp-content/uploads/2013/09/DynamicAppLauncher_03-169x300.jpg)

###Peace of Mind Widget overview

The peace of mind widget is also a fullscreen widget that shall enable the user to select a set of time and within that time the phone shall be disconnect from the data networks (WiFi, bluetooth, Mobile) and the audio shall be turned down. On time expiration or on user cancelation the phone shall regain the lost abilities before the piece of mind was turned on.

The user shall be able to see the time he has been on Piece of Mind. This widget may be used as a lock screen widget.

![Peace of mind](http://www.fairphone.com/wp-content/uploads/2013/09/Screenshot_2013-09-20-22-49-35-168x300.png)

###Lock screen/Mood Changer overview
The lockscreen is what the user sees when he turns on the device. This is the layer of protection between the phone and unwanted usage of the device.
The Fairphone project has conceived a Fairphone lockscreen that will be based on the Android 4.2.2 one, allowing lock screen widgets and allowing the user to select a mood changer that will depict the battery level as a set of background colors that will replace the background and give the device a very clean look.


