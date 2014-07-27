'Today I Learned' for Wear
=================================

Periodically retrieves the latest top facts from the subreddit 'Today I Learned' (/r/todayilearned/hot) and creates a notification with them.

Stores the id of the first TIL retrieved, so when it makes the next request it only pulls new facts.

Requires an Android wearable - the TILs are presented 1 per page, and can be scrolled through, left to right.

|  ![wear1](https://raw.githubusercontent.com/emmaguy/til/master/images/wear_home.png) | ![wear2](https://raw.githubusercontent.com/emmaguy/til/master/images/wear_til_with_position.png) | ![wear3](https://raw.githubusercontent.com/emmaguy/til/master/images/wear_dismiss_all.png)  |
|---|---|---|

On a phone, the notification will only allow dismissing of the TILs. There's a small settings page where the sync refresh interval and number of TILs to retrieve can be configured.

![phone1](https://raw.githubusercontent.com/emmaguy/til/master/images/phone_settings.png)

License
--------

    Copyright 201$ Emma Guy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
