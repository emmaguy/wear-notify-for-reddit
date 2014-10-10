Reddit for Wear
=================================

Periodically retrieves the latest top posts from selected subreddits, e.g. /r/todayilearned and /r/worldnews

Stores the latest created utc of the posts retrieved, so when we makes the next request we only show the user new posts. Previously attempted to use 'before' param with the fullname of a post, but when a post is deleted or removed, this caused the request to continually retrieve nothing.

Requires an Android wearable - the posts are presented in a notification, 1 post per page, and can be scrolled through, left to right. There's an action at the end of the posts to dismiss them

|  ![wear1](https://raw.githubusercontent.com/emmaguy/til/master/images/home.png) | ![wear2](https://raw.githubusercontent.com/emmaguy/til/master/images/all.png) | ![wear3](https://raw.githubusercontent.com/emmaguy/til/master/images/reply.png)  |
|---|---|---|

There's a short settings page where the subreddits, sync refresh interval, ordering and number of posts to retrieve can be configured.

![phone1](https://raw.githubusercontent.com/emmaguy/til/master/images/phone_settings.png)

Available on the Play Store: https://play.google.com/store/apps/details?id=com.emmaguy.todayilearned

Note: this used to just be a 'Today I Learned' thing (hence the package name), but decided to expand it to other subreddits!

License
--------

    Copyright 2014 Emma Guy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
