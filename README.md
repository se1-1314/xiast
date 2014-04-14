xiast
=====

Xiast is a scheduling tool

# Configuration
Copy `xiast.conf.example` to `xiast.conf` and fill in all information.
When using sqlite as database back-end, only `:database` is required
and needs to contain the location of the sqlite file.

# Testing
In order to perform testing you first need to create a new schema in
your database. This can be done by simply duplicating your current
schema and appending `-test` as a suffix, e.g. duplicating `xiast` to
`xiast-test`.


Tests can be performed by running `lein midje` in the project directory.
