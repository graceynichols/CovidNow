# CovidNow

## Table of Contents
1. [Overview](#Overview)
1. [Product Spec](#Product-Spec)
1. [Wireframes](#Wireframes)
2. [Schema](#Schema)

## Overview
### Description
A coronavirus news app. Users can get current information regarding case counts and news for their are and see a map view and mark areas as "hot spots" that have too many people or where you can't social distance properly, and view places other people have marked as "hot spots" (if the user is in a hotspot without the app open they will get a push notification). The app also periodically tracks the user's location and if the user self designates as having covid the app alerts any users who were at the same place on the same day as the infected user for the past 14 days. 

### App Evaluation
[Evaluation of your app across the following attributes]
- **Category:**
    - Health, News
- **Mobile:**
    - The news feed is meant to be a quick daily update on the situation, easily digestible on the go and made for scrolling
    - The map is also good for mobile so you can check places on your way to them, and mark places as hot spots right then and there 
- **Story:**
    - Keep people updated on the latest  COVID-19 news from trusted sources, and crowd source a "hot spot" map to let people know when to avoid certain places.
- **Market:**
    - Anybody in an area affected by COVID-19 (so everywhere) could use this app.
- **Habit:**
    - This app would let people have an easy daily checkin for coronavirus news, and they would also check it anytime they are going to a public place to make sure it's not currently a "hot spot".
- **Scope:**
    - This app would start out with a few news sources and just with people marking and reviewing places, but eventually it could grow to include more verified sources and the map view has a lot of potential where people could also rate, review, and compare different places.

## Product Spec

### 1. User Stories (Required and Optional)

**Required Must-have Stories**

* User can sign up with email for an account
* User can login (and stay logged in)
* User can view the number of cases in their state or country
* User can view a stream of news items (either headlines or tweets) from a few trusted sources (WHO, CDC, John Hopkins medical, etc)
* User can view a map screen and search for locations
* Each location on the map is either marked as a "hot spot" or not
* User can mark locations as a "hot spot" for other users to see and optionally upload photos

**Optional Nice-to-have Stories**

* User can rate and review places on map view
* User can connect to their facebook account and share articles through messenger
* Favorite/save places you go frequently
* User can click on another user's screenname and be taken to a profile page 

### 2. Screen Archetypes

* Log in screen with signup button
   * User can either login or opt to sign up

* Signup screen
    * User can sign up for an account by putting their username, password, and current location (possibly limited to U.S. states only)
* Main Stream of news
   * number of cases in the user's state displayed at the top
   * Stream of news headlines with the source's name, the headline, and the time posted

* News detail view
    * Click on an article in the stream to see a full screen view with a summary, author and link to the article on the web

* Map view
    * Search for places using google maps sdk

* Location detail view
    * See if a location has been marked as a hotspot and see the most recent photo uploaded by other users

* Content (review page
    * Screen to mark a location as a hotspot and upload a photo

### 3. Navigation

**Tab Navigation** (Tab to Screen)

* Home / news stream
* Map
* Profile

**Flow Navigation** (Screen to Screen)

* Login ->
   * Signup
* Home stream ->
   * Article detail view
* Map ->
    * Location detail view ->
    * Compose review screen

* Profile

## Wireframes
![](https://i.imgur.com/MIC3tOz.jpg)

### [BONUS] Digital Wireframes & Mockups

### [BONUS] Interactive Prototype

## Schema 
Data Models
- User
    Username (String)
    Password (String)
    Email (String)
    Location
        Denotes users current location, used to determine their article feed
    (Optional) Profile Pic (ParseFile)
- Article
    Headline (String)
    Source (String)
    Date/ Time (Date)
        Date/time article was posted
    Summary (String)
        Only visible in article details view
    Link (String)
        Link to article’s webpage
- Location
    Coordinates
    Location ID (int or long depending on what’s needed)
        Location ID for google maps, this is how I will identify and look up locations on the backend
    Address (String)
        For the location details view
    Name (String)
        For the location details view
    Hotspot status (boolean)
        Whether this location has been designated a “hot spot” in the last two hours
    Last hotspot status (Date/time)
        The date and time the last time this place has been designated as a hotspot
    Photo (ParseFile)
        Most recent user uploaded photo of the location
Outlined Network Requests
    Profile Tab
        Login Screen
            (Read/GET) Find user in parse database using username and password
        Signup
            Create/POST new user object in parse database recording username, password, email, and (optional feature) profile picture
        Profile Screen
            Read/GET Current user’s name, location, and (optional) profile picture
    Home tab
        Main Article Screen
            Read/GET Current user’s location
            Coronavirus News API: Read/Get list of relevant articles, only store locally
            Article Detail View
                Display stored article, no Parse request
Map tab
    Map Search Screen
        Read/Get user’s current location
        Use Google Maps + Google Places API for search function
        Location Detail View
            Read/Get Location details (Hotspot status, name, address, photo) from Parse
            Compose Review Screen
                Update/PUT “Hot spot” status for location
                Update/PUT Update image for location
                Camera View
                Create/POST Create new image for location

### Models
[Add table of models]
### Networking
- [Add list of network requests by screen ]
- [Create basic snippets for each Parse network request]
- [OPTIONAL: List endpoints if using existing API such as Yelp]
