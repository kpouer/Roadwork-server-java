# Roadwork-server
Roadwork server (for team work synchronization)

## Introduction

This is a server intended to help team synchronize their work when using Roadwork app (https://github.com/kpouer/Roadwork)
It runs on Java 17 and could be hosted anywhere.

`
Note that this server do not act as a proxy. It doesn't connect to any opendata service.
Just store and synchronize data sent by Roadwork application.
`

# Technical information

This service only rely on json files and no database is used at all. It uses a shared library that you can get here
https://github.com/kpouer/Roadwork-lib containing the shared datamodel between the Roadwork app and this server

# User management

Users might belong to multiple teams.
Their account are stored in a file named *data/users.json*

The password is stored using BCrypt salt

You can salt password using (it returns a different value each time, it is normal)
http://127.0.0.1:8080/salt/{password}

```json
[
{
  "username": "kpouer",
  "password": "$2a$10$VL3SsxznFxGvCnr1R9xN2ep/B69uVSumuSmqGKXDEzZu4B045BSq6", 
  "teams": ["tcfrance"]}
]
```

The service must be restarted after adding users (real user management is on todo list)

# Http service

## POST setData

The setData entry point is the main service provided by this server :

http://127.0.0.1:8080/setData/{team}/{opendataService}

The team must be a team to which the user belongs.
The opendataService is a free text describing identifying the opendataService

```json
[
  "some_id": {
    "localUpdateTime": 1653331693000,
    "serverUpdateTime": 1653331693000,
    "status": "New|Later|Ignored|Finished|Treated",
    "dirty": true|false,
  },
  "some_id 2": {
    "localUpdateTime": 1653331693000,
    "serverUpdateTime": 1653331693000,
    "status": "New|Later|Ignored|Finished|Treated",
    "dirty": true|false,
  }
]
```
