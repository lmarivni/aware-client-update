AWARE Plugin: SMOKE REGISTRATION PLUGIN
=======================================

This plugin measures smoking events when study subjects manually register they have smoked. Date
and time can be adjusted. Subjects can also delete smoking events.

# Settings
Parameters adjustable on the dashboard and client:
- **status_plugin_template**: (boolean) activate/deactivate plugin

# Providers
##  Template Data
> content://com.aware.plugin.template.provider.smokeregistration/plugin_smoke_events

Field | Type | Description
----- | ---- | -----------
_id | INTEGER | primary key auto-incremented
timestamp | REAL | unix timestamp in milliseconds of sample
device_id | TEXT | AWARE device ID
date | TEXT | date as set by user
time | TEXT| time as set by user
