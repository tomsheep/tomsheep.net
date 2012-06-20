var httplib = require('http'),
    urllib = require('url'),
    email = require('emailjs');


var mail_account = {
         user:    "", 
         password:"", 
         host:    "smtp.gmail.com", 
         ssl:     true
    };

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
      // send mail to me
      var text = "The server is dectected to be down in: " + d.toISOString();
      text += "\n\n";
      text += url;
      var title = "[tomsheep.net] Blog is not working";
      /////////
      var server  = email.server.connect( mail_account );

      // send the message and get a callback with an error or details of the message that was sent
      server.send({
         text:    text, 
         from:    "tomsheep.net", 
         to:      "tomsheep.cn@gmail.com",
         subject: title
      }, function(err, message) { console.log(err || message); });
    }
  });
  request.end();
}

function ping(ts, url) {
  console.log("start pinging");
  check_url(url);
  setInterval(check_url, ts, url);
}

//ping(3000, "http://localhost:8080/123");
ping(1000 * 60 * 60, "http://tomsheep.net/");