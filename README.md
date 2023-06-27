# Interview Planning Application

## Overview
* [Project information](#project-information)
  * [Used technologies](#used-technologies)
* [API](#api)
  * [Authentication & authorization](#authentication--authorization)
  * [Implemented API](#implemented-api)
  * [API to implement in future](#api-to-implement-in-future)
* [Setting-up the project](#setting-up-the-project)
  * [Getting the project](#getting-the-project) 
  * [Configuring docker](#configuring-docker)

## Project information
Interview planning application is a RESTful service designed for better communication between interviewers and candidates through coordinators.

Application supports next basic functionality:
  - Creating slots as Interviewer
  - Creating slots as Candidate
  - Creating bookings for already created Interviewer and Candidate slots as Coordinator

### Used technologies
- Java
- Spring
  - Boot
  - Data
    - Hibernate
  - Security
    - OAuth2
  - Web
- Facebook API

## API
This section describes all implemented and planned to implement endpoints.

### Authentication & authorization

#### Getting the JWT

To perform any authenticated or authorized request you should provide your Facebook Token to the next endpoint:

`POST /authenticate`

With requiered data parameter `{"facebookToken": "EAAHC..."}`.

As the response you will get JSON Web Token as `{"token": "eyJhb..."}`.

The possible exceptions are:
- 401 - bad_facebook_token_exception
- 401 - bad_credentials

Gained JWT should be put in request header as a parameter `Authorization` with value `Bearer eyJhb...`.

#### Edge cases

You can face next exceptions while using JWT within yout request:
- 401 - not_authenticated_exception 
- 401 - bad_token_exception
- 401 - expired_token_exception
- 401 - bad_token_signature_exception
- 401 - malformed_token_exception
- 401 - unsupported_token_exception
- 403 - access_denied_exception

#### Users
There are four groups of users:
- Guests - users without authentication
- Candidates - users that have passed authentication but have not Interviewer or Coordinator role
- Interviewers - users that have passed authentication and have Interviewer role
- Сoordinator - users that have passed authentication and have Coordinator role

#### User obtention endpoint
Interviewers and Coordinators can perform next endoint to obtain their user information:

Request: `GET /me`

Response: `{"email": "example@google.com", "role": "INTERVIEWER", "id": 123}`

### Implemented API
This section describes all already implemented business logic endoints. Section is devided by users.

#### Guest
Guests can only get current and next week of num or [authenticate](#authentication--authorization)

##### Get current number of week:
Request: `GET /weeks/current`

Response: `{"weekNum": 44}`

##### Get next number of week:
Request: `GET /weeks/next`

Response: `{"weekNum": 45}`

#### Candidate
Candidate can create or update own slots following next requirements:
- Slots must be in future
- Slot has to be 1.5 hours or more and rounded to 30 minutes
- Slot is defined as exact date and time diapason
- Updating is enabled only if there is no bookings for this slot

##### Creating Slot
Request: `POST /candidates/current/slots`

Data parametres:
- `date` - date of candidate slot
- `from` - start time of slot in format HH:mm
- `to` - end time of slot in format HH:mm

Response: {"date": "22.01.2022", "from": "9:00", "to": "17:00""}

Exceptions:
- slot_is_overlaping_exception
- invalid_boundaries_exception

##### Updating Slot
Request: `POST /candidates/current/slots/{slotId}`

URL parametres:
- `slotId` - id of slot to edit (can be obtained by [getting slots](#getting-slots) endpoint)

Data parametres:
- `date` - date of candidate slot
- `from` - start time of slot in format HH:mm
- `to` - end time of slot in format HH:mm

Response: {"date": "22.01.2022", "from": "9:00", "to": "17:00""}

Exceptions:
- slot_is_overlaping_exception
- invalid_boundaries_exception
- slot_not_found_exception
- slot_is_booked_exception
- slot_is_overlaping_exception
- invalid_boundaries_exception

##### Getting Slots
Request: `GET /candidates/current/slots`

Response: `{"candidateSlotDtoList": [{"date": "22.01.2022", "from": "9:00", "to": "17:00""}, {"date": "23.01.2022", "from": "13:00", "to": "20:00""}]}`

### API to implement in future
This section describes all business logic endoints that will be implemented soon. Section is devided by users.

#### Interviewer
Interviewer can create or update own slots following next requirements:
- Creation until end of Friday (00:00) of current week
- Slot means day of week + time diapason
- Slot boundaries are rounded to 30 minutes
- Slot duration must be greather or equal 1.5 hours
- Slot start time cannot be less than 8:00
- Slot end time cannot be greater than 22:00

Also Interviewers can set booking limit for next week (maximum amount of booking that Coordinators will able to create for that certain Interviewer and it's certain week):
- If maximum number of bookings is not set for certain week, the previous week limit is actual
- If limit was never set, any number of bookings can be assigned to interviewer

Getting of own slots is available only for current and next week.

##### Creating Slot
Request: `POST /interviewers/{interviewerId}/slots`

URL parametres:
- `interviewerId` - id of current Interviewer (can be obtained by [/me](#user-obtention-endpoint) endpoint).

Data parametres:
- `week` - number of week (can be obtained by [/weeks/\*\*](#guest) endpoints)
- `dayOfWeek` - day of week (MON, TUE, WED, THU, FRI)
- `from` - start time of slot in format HH:mm
- `to` - end time of slot in format HH:mm

Response: `{"week": 49, "dayOfWeek": "TUE", "from":"18:00", "to":"21:30"}`

Exceptions:
- interviewer_not_found_exception
- slot_is_overlaping_exception
- invalid_boundaries_exception
- invalid_day_of_week_exception
- slot_not_found_exception
- cannot_edit_this_week_exception

##### Updating Slot
Request: `POST /interviewers/{interviewerId}/slots/{slotId}`

URL parametres:
- `interviewerId` - id of current Interviewer (can be obtained by [/me](#user-obtention-endpoint) endpoint).
- `slotId` - id of slot to edit (can be obtained by [getting slots](#getting-slots) endpoint)

Data parametres:
- `week` - number of week (can be obtained by [/weeks/\*\*](#guest) endpoints)
- `dayOfWeek` - day of week (MON, TUE, WED, THU, FRI)
- `from` - start time of slot in format HH:mm
- `to` - end time of slot in format HH:mm

Response: `{"id": 123, "week": 49, "dayOfWeek": "TUE", "from":"18:00", "to":"21:30"}`

Exceptions:
- interviewer_not_found_exception
- slot_is_overlaping_exception
- invalid_boundaries_exception
- invalid_day_of_week_exception
- slot_not_found_exception
- cannot_edit_this_week_exception

##### Getting Slots
Requests:
- `GET /interviewers/{interviewerId}/slots/current` (for getting current week slots) 
- `GET /interviewers/{interviewerId}/slots/next` (for getting next week slots)

URL parametres:
- `interviewerId` - id of current Interviewer (can be obtained by [/me](#user-obtention-endpoint) endpoint).

Response - {"interviewerSlots": [{"id": 123, "week": 49, "dayOfWeek": "TUE", "from":"18:00", "to":"21:30"}, {"id": 123, "week": 50, "dayOfWeek": "MON", "from":"10:00", "to":"21:30"}]}

##### Setting Booking Limit
Request: `POST /interviewers/{interviewerId}/bookingLimit`

URL parametres:
- `interviewerId` - id of current Interviewer (can be obtained by [/me](#user-obtention-endpoint) endpoint).

Data perametres:
- `bookingLimit` - limit of bookings per next week

Response: `{"userId": 10, "weekNum": 45, "bookingLimit": 123}`

Exceptions:
- incorrect_id_exception
- incorrect_booking_limit_exception

#### Coordinator
DashBoard
Booking create
Booking update
Booking delete

## Setting-up the project
This section describes all needed steps to launch the application.

### Getting the project
First of all, you need to get the project. You can do this by two ways:
- [Getting zip project file](#getting-zip-project-file)
- [Cloning the repository](#cloning-the-repository)

#### Getting zip project file
To download project in zip follow the [link](https://github.com/sonnka/Interview-Planning-Application.git).

After downloading, unzip the archive and go to __Interview-Planning-Application-main__ directory.

#### Cloning the repository
To clone the repository run the console and type:

`git clone https://github.com/GrEFeRFeeD/intellistart-java-2022-oneweek.git`

After cloning is done change the directory to __Interview-Planning-Application-main__ by the following command:

`cd intellistart-java-2022-oneweek-main`

### Configuring docker
This section describes all needed steps to launch application via docker.

#### Configuring environmental variables
Before launching the application via docker, you need to created `api.env` file with next environment variables.
The example of such file represented in `example.env` file. The needed variables are:
- `APPLICATION_PORT` - port on which application will be run
- `JWT_SECRET` - defines secret work to assign the JWT
- `JWT_VALIDITY` - validity of JWT in seconds
- `FIRST_COORDINATOR_EMAIL` - facebook account attached email of first coordinator that will be automatically added to DB  
- `FACEBOOK_CLIENT_ID` - application client id provided by facebook
- `FACEBOOK_SECRET` - application secret provided by facebook
- `FACEBOOK_REDIRECT_URI` - URI to which you will be redirected after oauth2. Configures by facebook application
- `HIBERNATE_DDL_AUTO` - hibernate DDL launch mode:
  - `validate`: validates the schema, makes no changes to the database.
  - `update`: updates the schema.
  - `create`: creates the schema, destroying previous data.
  - `create-drop`: drop the schema when the SessionFactory is closed explicitly, typically when the application is stopped.
  - none: does nothing with the schema, makes no changes to the database.
- `DATABASE_PORT` - port on which database will be run
- `POSTGRES_USER` - name of default postgresql user
- `POSTGRES_PASSWORD` - password of default postgresql user
- `POSTGRES_DB` - postgresql database name

#### Launching the application
Once the `api.env` is created with proper variables you can launch docker with application through running the following command:

`docker-compose --env-file api.env up`

# Interview-Planning-Application
