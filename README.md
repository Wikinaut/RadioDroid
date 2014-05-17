#### RadioDroid

RadioDroid is a small Android internet radio app that uses the http://www.radio-browser.info radio community station list.

The present version is a **renewed and improved fork** of the original version. 


#### How to build

It can be cross-compiled *out of the box* on all platforms with the free Android Development Tools (ADT and SDK) http://developer.android.com/sdk . 
* Install the ADT and SDK as described on their download page
* Clone the repository to a local working directory

```
git clone https://github.com/Wikinaut/RadioDroid.git
```

* Start ADT
* Import the code to the ADT

```
In your ADT, select
File > Import > Select an import source > Android > Existing Android Code into workspace
```

#### Original version
If you wish to install and run the **original app** as published on https://github.com/segler-alex/RadioDroid, you can download that from the play store
* https://play.google.com/store/apps/details?id=net.programmierecke.radiodroid

#### TODO

Status: 20140517

- [ ] add a search function
- [x] check WiFi/WLAN connection, show a warning, beep and close if WiFi is unavailable on start
- [ ] add user option "WiFi/WLAN only"
- [x] add "Exit program" to the options menu
- [ ] add a "low bitrate" flag, or "bitrate" field to the station data
- [ ] add user option "Low bitrate streams only"
- [ ] auto extend lists if scrolling to the end
- [ ] allow to sort the selected lists alphabetically
- [ ] add lists for tags, countries
- [ ] make "Tags" clickable, list all stations with "Tag"
- [ ] add station detail activity and scroll function
- [ ] report broken stream urls or station definition errors
- [ ] concatenate country and language information in one field
- [x] add program version info
- [ ] add an "About application" page (links to the source code, feedback, donations etc.)
- [ ] add "Favorites" function (add to "Favorites", list "Favorites")
- [ ] local cache of all station data
- [ ] add "History" function (list my last recently selected stations on top of the others)
- [ ] add asx playlist file decoding support
