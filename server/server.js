// Just a basic server setup for this site
var Stack = require('stack'),
    Creationix = require('creationix'),
    Http = require('http'),
    Cluster = require('cluster');


//var numCPUs = require('os').cpus().length;
var numCPUs = 4;
var appName = "blog.tomsheep.net";

if (Cluster.isMaster) {
    process.title = appName + ' master';
    console.log(process.title, 'started');

    for (var i = 0; i < numCPUs; i++) {
        Cluster.fork();
    }

    process.on('SIGHUP', function() {
        // master ignores  SIGHUP
    });

    Cluster.on('death', function(worker) {
        console.log(appName, 'worker', '#' + worker.pid, 'died');
        Cluster.fork();
    });

} else {
    process.title = appName + ' worker ' + process.env.NODE_WORKER_ID;
    console.log(process.title, '#' + process.pid, 'started');

    process.on('SIGHUP', function() {
        // terminate worker when receiving SIGHUP
        process.exit(0);
    });
    Http.createServer(Stack(
        Creationix.log(),
        require('wheat')(process.env.JOYENT ? process.env.HOME + "/tomsheep.net" : __dirname +"/..")
    )).listen(process.env.JOYENT ? 80 : 8080);

}


