var http = require('http');

http.createServer(function (req, res) {
    setTimeout(() => {
        res.write('Hello World!');
        res.end();

        //
    }, 5000);

}).listen(8081);