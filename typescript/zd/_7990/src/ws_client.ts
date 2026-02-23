import WebSocket from "ws";

async function run(): Promise<void> {

  const wss = new WebSocket.Server({ port: 8085 });

  const myPromise: Promise<string> =  new Promise((resolve) => {

    wss.on('connection', (ws: WebSocket) => {

      console.log('New client connected');

      ws.on('message', (message: string) => {
        console.log(`Received message: ${message}`);
        ws.send(`Server received your message: ${message}`);
      });

      ws.on('close', () => {
        console.log('Client disconnected');
        resolve("done");
      });

    });

  });

  await myPromise;


}

run().catch((err) => {
  console.error(err);
  process.exit(1);
});
