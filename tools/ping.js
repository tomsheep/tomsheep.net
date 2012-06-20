var httplib = require('http'),
    urllib = require('url');

function check_url(url) {
  console.log("url:"+url);
  var options = urllib.parse(url);
  var request = httplib.request(options, function(res) {
    console.log(res.statusCode);
    var d = new Date();
    if ( res.statusCode === 200) {
      // it's OK
      console.log("[" + d.toISOString() + "]" + " It works!");
    } else {
      console.log("[" + d.toISOString() + "]" + " Down!!!!!!!!!!!!");
    }
  });
  request.end();
}

function ping(ts, url) {
  console.log("start pinging");
  check_url(url);
  setInterval(check_url, ts, url);
}

//ping(3000, "http://localhost:8080/");
ping(1000 * 60 * 60, "http://tomsheep.net/");