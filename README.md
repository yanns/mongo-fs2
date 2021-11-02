# query from Mongo with FS2

The mongo driver exposes query cursors as reactive publisher.

When using FS2 interoperability with reactive streams, I can observe that we query elements one by one, leading to a lot of DB queries, leading to bad performances.


## with FS2:
Mongo log:
```
command":{"getMore":3736877399621840227,"collection":"test","batchSize":2
```

## with akka-streams:
Mongo log:
```
"command":{"getMore":5259084578442247860,"collection":"test","batchSize":8
```

# Try it yourself

## start mongo

```
docker-compose up -d
```

## Inject some data

in sbt:
```
runMain mongo_fs2.Populate
```

## Read data
in sbt:
```
run
```
and select the main class to run
