一個犯懶的腐竹寫出的簡單Velocity plugin, Feel free to make a pr and make this better

不需要任何設定即可使用

Ban: `/lban <name>`

Unban: `/lunban <uuid>` <- UUID, not player name

Permission:
`bedtwL.oss.lazyban.ban` and `bedtwL.oss.lazyban.unban`

Discord&Telegram: @bedtwl


### To Build & Use:
`mvn package`

### License & Attribution
This project is licensed under the GPLv3. If you use, modify, or fork this code:

Keep it free: You cannot turn this into closed-source paidware.

Give Credit: Please include a "Special Thanks to this project" in your plugin's description or documentation.


### Web API Docs
http://server-ip:port/api/v1/

`Reqired query: player(uuid), key(config.yml), action(ban/unban)`

If action is ban, then you can pass `time` and `reason` to query
