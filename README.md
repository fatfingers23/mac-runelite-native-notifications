# Native Mac Notifications

This replaces the current RuneLite Notifications with native mac notifications from the notification center. Will need 
to go into RuneLite settings and turn off Notifications. If not double will show. 

## Developer notes
During development notifications will show that they come from Java. But should say RuneLite
in production since it is bundled.

# How to turn off RuneLite Default notifications
1. Open settings and type 'Runelite'.
2. Click the gear.
3. Look under Notification Settings. Disable "Enable tray notifications"


#Special Thanks
This repo has taken the way [intellij-community](https://github.com/JetBrains/intellij-community) calls native notifications 
via Java.