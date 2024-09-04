import WebSocket from "ws";


export const createActivities = (ws: WebSocket) => ({

  async client(msg: string): Promise<string> {

    const myPromise: Promise<string> =  new Promise((resolve) => {

      console.log("Sending message to server");
      ws.send('Hello, server!');

      console.log(`Received message from server`);
      ws.on('message', (message: string) => {
        console.log(`Received message from server: ${message}`);
        resolve(message)
      });

    });



    return await myPromise;
  },


});
